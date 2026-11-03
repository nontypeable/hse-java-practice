package db;

/**
 * Ссылка колонки на первичный ключ другой таблицы.
 *
 * @param table  имя таблицы, на которую ссылаются
 * @param column имя колонки в ней (должна быть первичным ключом)
 */
public record ForeignKey(String table, String column) {}
