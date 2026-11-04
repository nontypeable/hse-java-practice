package db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Таблица в памяти: строки лежат в карте по первичному ключу.
 * Строка — массив значений в порядке колонок схемы. Таблицу можно обойти циклом for-each.
 * Значения должны быть заранее приведены к типам колонок ({@link ColumnType#coerce}).
 */
public final class Table implements Iterable<Object[]> {
    /**
     * Результат поиска строк.
     *
     * @param rows  подходящие строки
     * @param index чем ускорен поиск: {@code PRIMARY KEY} или {@code null} при полном просмотре
     */
    public record Scan(List<Object[]> rows, String index) {
    }

    private static final String PRIMARY_KEY = "PRIMARY KEY";

    private final String name;
    private final Schema schema;
    private final Map<Object, Object[]> rows = new LinkedHashMap<>();
    private long version;

    public Table(String name, Schema schema) {
        this.name = name;
        this.schema = schema;
    }

    public String name() {
        return name;
    }

    public Schema schema() {
        return schema;
    }

    public int size() {
        return rows.size();
    }

    /**
     * Счётчик изменений таблицы.
     */
    public long version() {
        return version;
    }

    void restoreVersion(long version) {
        this.version = version;
    }

    public boolean containsKey(Object primaryKey) {
        return rows.containsKey(primaryKey);
    }

    @Override
    public Iterator<Object[]> iterator() {
        return Collections.unmodifiableCollection(rows.values()).iterator();
    }

    /**
     * Добавляет строку.
     *
     * @throws DbException если первичный ключ пуст или уже есть в таблице
     */
    public void insert(Object[] row) {
        Object key = row[schema.primaryKeyIndex()];
        if (key == null) {
            throw new DbException("Первичный ключ не может быть NULL");
        }
        if (rows.containsKey(key)) {
            throw new DbException("Нарушение уникальности первичного ключа: " + key);
        }

        rows.put(key, row);
        version++;
    }

    /**
     * Находит строки по условию. Если в условии есть сравнение {@code первичный ключ = значение},
     * строка берётся из карты напрямую, иначе просматриваются все строки.
     *
     * @param where привязанное условие или {@code null} для всех строк
     */
    public Scan find(Condition where) {
        if (where == null) {
            return new Scan(new ArrayList<>(rows.values()), null);
        }

        String primaryKeyName = schema.primaryKey().name();
        for (Condition.Comparison comparison : where.conjuncts()) {
            if (comparison.value() != null
                && comparison.column().equals(primaryKeyName)
                && comparison.operator() == Operator.EQ) {
                List<Object[]> candidates = new ArrayList<>();
                Object[] row = rows.get(comparison.value());
                if (row != null) {
                    candidates.add(row);
                }
                return new Scan(filter(candidates, where), PRIMARY_KEY);
            }
        }

        return new Scan(filter(new ArrayList<>(rows.values()), where), null);
    }

    private List<Object[]> filter(List<Object[]> candidates, Condition where) {
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : candidates) {
            if (where.matches(schema, row)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Присваивает новые значения колонкам найденных строк. Первичный ключ менять нельзя.
     *
     * @param matched строки, полученные из {@link #find}
     * @param changes позиция колонки → новое значение
     */
    public void update(List<Object[]> matched, Map<Integer, Object> changes) {
        if (changes.containsKey(schema.primaryKeyIndex())) {
            throw new DbException("Нельзя изменять первичный ключ");
        }

        for (Object[] row : matched) {
            for (Map.Entry<Integer, Object> change : changes.entrySet()) {
                row[change.getKey()] = change.getValue();
            }
        }

        if (!matched.isEmpty()) {
            version++;
        }
    }

    /**
     * Удаляет найденные строки.
     */
    public void delete(List<Object[]> matched) {
        for (Object[] row : matched) {
            rows.remove(row[schema.primaryKeyIndex()]);
        }

        if (!matched.isEmpty()) {
            version++;
        }
    }
}
