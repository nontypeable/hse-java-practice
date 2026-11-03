package db;

/**
 * Описание колонки таблицы.
 *
 * @param name       имя в нижнем регистре
 * @param type       тип данных
 * @param primaryKey является ли колонка первичным ключом
 * @param references внешний ключ или {@code null}
 */
public record Column(String name, ColumnType type, boolean primaryKey, ForeignKey references) {
    public Column(String name, ColumnType type) {
        this(name, type, false, null);
    }
}
