package db;

import java.io.IOException;
import java.util.List;

/**
 * Хранилище таблиц (шаблон Repository): {@link Database} не знает, где лежат данные.
 */
public interface Storage {
    /**
     * Читает все сохранённые таблицы.
     */
    List<Table> load() throws IOException;

    /**
     * Сохраняет таблицу целиком.
     */
    void save(Table table) throws IOException;

    /**
     * Удаляет таблицу.
     */
    void delete(String tableName) throws IOException;
}
