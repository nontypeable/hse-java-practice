package db;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * СУБД: набор таблиц, выполнение запросов и контроль ограничений
 * (уникальность первичного ключа, внешние ключи). После каждого изменяющего запроса
 * затронутая таблица сохраняется через {@link Storage}.
 *
 * <p>Внешний ключ работает в режиме RESTRICT: нельзя вставить строку со ссылкой на несуществующую запись,
 * удалить запись, на которую ссылаются, или удалить таблицу, на которую ссылаются.
 */
public final class Database {
    private final Storage storage;
    private final Map<String, Table> tables = new LinkedHashMap<>();

    /**
     * Создаёт базу и загружает из хранилища сохранённые таблицы.
     */
    public Database(Storage storage) throws IOException {
        this.storage = storage;
        for (Table table : storage.load()) {
            tables.put(table.name(), table);
        }
    }

    /**
     * Разбирает и выполняет один SQL-запрос.
     *
     * @throws DbException          ошибка в запросе или нарушение ограничения
     * @throws UncheckedIOException не удалось записать данные на диск
     */
    public QueryResult execute(String sql) {
        Statement statement = Parser.parse(sql);
        try {
            if (statement instanceof Statement.CreateTable) {
                return createTable((Statement.CreateTable) statement);
            }
            if (statement instanceof Statement.DropTable) {
                return dropTable((Statement.DropTable) statement);
            }
            if (statement instanceof Statement.Insert) {
                return insert((Statement.Insert) statement);
            }
            if (statement instanceof Statement.Select) {
                return select((Statement.Select) statement);
            }
            if (statement instanceof Statement.Update) {
                return update((Statement.Update) statement);
            }
            return delete((Statement.Delete) statement);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private QueryResult createTable(Statement.CreateTable statement) throws IOException {
        if (tables.containsKey(statement.name())) {
            throw new DbException("Таблица " + statement.name() + " уже существует");
        }

        Schema schema = new Schema(statement.columns());
        for (Column column : schema.columns()) {
            if (column.references() != null) {
                checkReferenceTarget(column);
            }
        }

        Table table = new Table(statement.name(), schema);
        tables.put(table.name(), table);
        storage.save(table);
        return QueryResult.message("Таблица " + table.name() + " создана");
    }

    private void checkReferenceTarget(Column column) {
        ForeignKey reference = column.references();
        Column target = table(reference.table()).schema().primaryKey();
        if (!target.name().equals(reference.column())) {
            throw new DbException("Внешний ключ должен ссылаться на первичный ключ " + reference.table()
                + "(" + target.name() + ")");
        }
        if (target.type() != column.type()) {
            throw new DbException("Тип колонки " + column.name() + " не совпадает с типом " + reference.table()
                + "(" + target.name() + ")");
        }
    }

    private QueryResult dropTable(Statement.DropTable statement) throws IOException {
        Table table = table(statement.name());
        for (Table other : tables.values()) {
            if (other != table && !referencedColumns(other, table).isEmpty()) {
                throw new DbException("Нельзя удалить таблицу " + table.name() + ": на неё ссылается " + other.name());
            }
        }

        tables.remove(table.name());
        storage.delete(table.name());
        return QueryResult.message("Таблица " + table.name() + " удалена");
    }

    private QueryResult insert(Statement.Insert statement) throws IOException {
        Table table = table(statement.table());
        Schema schema = table.schema();

        int[] positions = new int[statement.columns() == null ? schema.columns().size() : statement.columns().size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = statement.columns() == null ? i : schema.indexOf(statement.columns().get(i));
        }

        List<Object[]> prepared = new ArrayList<>();
        Set<Object> keys = new HashSet<>();
        for (List<Object> values : statement.rows()) {
            if (values.size() != positions.length) {
                throw new DbException("Ожидалось значений: " + positions.length + ", получено: " + values.size());
            }

            Object[] row = new Object[schema.columns().size()];
            for (int i = 0; i < positions.length; i++) {
                row[positions[i]] = schema.columns().get(positions[i]).type().coerce(values.get(i));
            }

            Object key = row[schema.primaryKeyIndex()];
            if (key == null) {
                throw new DbException("Первичный ключ " + schema.primaryKey().name() + " обязателен");
            }
            if (table.containsKey(key) || !keys.add(key)) {
                throw new DbException("Нарушение уникальности первичного ключа: " + key);
            }
            checkReferences(schema, row);
            prepared.add(row);
        }

        for (Object[] row : prepared) {
            table.insert(row);
        }
        storage.save(table);
        return QueryResult.message("Добавлено строк: " + prepared.size());
    }

    private QueryResult select(Statement.Select statement) {
        Table table = table(statement.table());
        Schema schema = table.schema();

        int[] positions;
        if (statement.columns() == null) {
            positions = new int[schema.columns().size()];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = i;
            }
        } else {
            positions = new int[statement.columns().size()];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = schema.indexOf(statement.columns().get(i));
            }
        }

        Table.Scan scan = table.find(bind(statement.where(), schema));

        List<String> columns = new ArrayList<>();
        for (int position : positions) {
            columns.add(schema.columns().get(position).name());
        }
        List<List<Object>> rows = new ArrayList<>();
        for (Object[] row : scan.rows()) {
            List<Object> projected = new ArrayList<>();
            for (int position : positions) {
                projected.add(row[position]);
            }
            rows.add(projected);
        }

        String message = "Строк: " + rows.size() + (scan.index() == null ? "" : " (поиск по: " + scan.index() + ")");
        return new QueryResult(columns, rows, message);
    }

    private QueryResult update(Statement.Update statement) throws IOException {
        Table table = table(statement.table());
        Schema schema = table.schema();

        Map<Integer, Object> changes = new LinkedHashMap<>();
        for (Map.Entry<String, Object> assignment : statement.assignments().entrySet()) {
            int position = schema.indexOf(assignment.getKey());
            changes.put(position, schema.columns().get(position).type().coerce(assignment.getValue()));
        }
        if (changes.containsKey(schema.primaryKeyIndex())) {
            throw new DbException("Нельзя изменять первичный ключ");
        }

        Object[] probe = new Object[schema.columns().size()];
        for (Map.Entry<Integer, Object> change : changes.entrySet()) {
            probe[change.getKey()] = change.getValue();
        }
        checkReferences(schema, probe);

        Table.Scan scan = table.find(bind(statement.where(), schema));
        table.update(scan.rows(), changes);
        if (!scan.rows().isEmpty()) {
            storage.save(table);
        }
        return QueryResult.message("Обновлено строк: " + scan.rows().size());
    }

    private QueryResult delete(Statement.Delete statement) throws IOException {
        Table table = table(statement.table());
        Schema schema = table.schema();

        Table.Scan scan = table.find(bind(statement.where(), schema));
        Set<Object> keys = new HashSet<>();
        for (Object[] row : scan.rows()) {
            keys.add(row[schema.primaryKeyIndex()]);
        }

        for (Table child : tables.values()) {
            for (int position : referencedColumns(child, table)) {
                for (Object[] row : child) {
                    if (keys.contains(row[position])) {
                        throw new DbException("Нельзя удалить строку: на неё ссылается таблица " + child.name());
                    }
                }
            }
        }

        table.delete(scan.rows());
        if (!scan.rows().isEmpty()) {
            storage.save(table);
        }
        return QueryResult.message("Удалено строк: " + scan.rows().size());
    }

    private Table table(String name) {
        Table table = tables.get(name);
        if (table == null) {
            throw new DbException("Таблица " + name + " не существует");
        }
        return table;
    }

    private static Condition bind(Condition where, Schema schema) {
        return where == null ? null : where.bind(schema);
    }

    /**
     * Позиции колонок таблицы {@code child}, которые ссылаются на {@code parent}.
     */
    private static List<Integer> referencedColumns(Table child, Table parent) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < child.schema().columns().size(); i++) {
            ForeignKey reference = child.schema().columns().get(i).references();
            if (reference != null && reference.table().equals(parent.name())) {
                positions.add(i);
            }
        }
        return positions;
    }

    private void checkReferences(Schema schema, Object[] row) {
        for (int i = 0; i < row.length; i++) {
            ForeignKey reference = schema.columns().get(i).references();
            if (reference != null && row[i] != null && !table(reference.table()).containsKey(row[i])) {
                throw new DbException("Нарушение внешнего ключа: в таблице " + reference.table()
                    + " нет записи " + reference.column() + " = " + row[i]);
            }
        }
    }
}
