package db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Набор колонок таблицы. Гарантирует уникальные имена и ровно один первичный ключ.
 */
public final class Schema {
    private final List<Column> columns;
    private final Map<String, Integer> positions = new HashMap<>();
    private final int primaryKey;

    public Schema(List<Column> columns) {
        if (columns.isEmpty()) {
            throw new DbException("В таблице должна быть хотя бы одна колонка");
        }

        int primaryKeyPosition = -1;
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (positions.put(column.name(), i) != null) {
                throw new DbException("Колонка " + column.name() + " объявлена дважды");
            }
            if (column.primaryKey()) {
                if (primaryKeyPosition >= 0) {
                    throw new DbException("Первичный ключ должен быть один");
                }
                primaryKeyPosition = i;
            }
        }
        if (primaryKeyPosition < 0) {
            throw new DbException("Не задан первичный ключ (PRIMARY KEY)");
        }

        this.columns = List.copyOf(columns);
        this.primaryKey = primaryKeyPosition;
    }

    public List<Column> columns() {
        return columns;
    }

    /**
     * Позиция колонки по имени.
     *
     * @throws DbException если такой колонки нет
     */
    public int indexOf(String name) {
        Integer position = positions.get(name);
        if (position == null) {
            throw new DbException("Неизвестная колонка: " + name);
        }
        return position;
    }

    public Column column(String name) {
        return columns.get(indexOf(name));
    }

    public int primaryKeyIndex() {
        return primaryKey;
    }

    public Column primaryKey() {
        return columns.get(primaryKey);
    }
}
