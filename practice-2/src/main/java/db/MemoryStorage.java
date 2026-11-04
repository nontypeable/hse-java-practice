package db;

import java.util.List;

/**
 * Заглушка без сохранения: данные живут только пока работает программа. Нужна тестам и бенчмарку.
 */
public final class MemoryStorage implements Storage {
    @Override
    public List<Table> load() {
        return List.of();
    }

    @Override
    public void save(Table table) {
    }

    @Override
    public void delete(String tableName) {
    }
}
