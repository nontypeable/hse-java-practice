package db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тестирование выполнения запросов в {@link Database}: вставка, выборка, изменение, удаление и ограничения ключей.
 */
class DatabaseTest {
    private Database database;

    /**
     * Создаёт перед каждым тестом базу с таблицами {@code cars} и {@code owners} и тремя машинами.
     */
    @BeforeEach
    void setUp() throws IOException {
        database = new Database(new MemoryStorage());
        database.execute("CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE, since DATE, used BOOLEAN)");
        database.execute("CREATE TABLE owners (id INT PRIMARY KEY, car_id INT REFERENCES cars(id), name STRING)");
        database.execute("INSERT INTO cars VALUES (1, 'Toyota', 25000.0, '2020-05-17', TRUE), "
            + "(2, 'BMW', 40000, '2021-01-01', FALSE), (3, 'Toyota', 15000.5, '2018-03-02', TRUE)");
    }

    /**
     * Проверка: вставка с неполным списком колонок оставляет NULL.
     */
    @DisplayName("Вставка с неполным списком колонок оставляет NULL")
    @Test
    void partialInsert() {
        database.execute("INSERT INTO cars (id, brand) VALUES (4, 'Kia')");

        assertEquals(List.of(Arrays.asList("Kia", null)),
            database.execute("SELECT brand, price FROM cars WHERE id = 4").rows());
    }

    /**
     * Проверка: повторный первичный ключ отклоняется, в том числе внутри одного запроса.
     */
    @DisplayName("Повторный первичный ключ отклоняется, в том числе внутри одного запроса")
    @Test
    void duplicatePrimaryKey() {
        assertThrows(DbException.class, () -> database.execute("INSERT INTO cars (id) VALUES (1)"));
        assertThrows(DbException.class, () -> database.execute("INSERT INTO cars (id) VALUES (7), (7)"));
        assertEquals(3, database.execute("SELECT * FROM cars").rows().size());
    }

    /**
     * Проверка: тип значения проверяется.
     */
    @DisplayName("Тип значения проверяется")
    @Test
    void typeChecks() {
        assertThrows(DbException.class, () -> database.execute("INSERT INTO cars (id, brand) VALUES (9, 5)"));
        assertThrows(DbException.class, () -> database.execute("INSERT INTO cars (id, since) VALUES (9, 'вчера')"));
        assertThrows(DbException.class, () -> database.execute("INSERT INTO cars (id) VALUES (1.5)"));
    }

    /**
     * Проверка: SELECT с условием, AND и OR.
     */
    @DisplayName("SELECT с условием, AND и OR")
    @Test
    void selectWithConditions() {
        assertEquals(2, database.execute("SELECT * FROM cars WHERE brand = 'Toyota'").rows().size());
        assertEquals(1, database.execute("SELECT * FROM cars WHERE price > 20000 AND used = TRUE").rows().size());
        assertEquals(3, database.execute("SELECT * FROM cars WHERE price > 30000 OR brand = 'Toyota'").rows().size());
        assertEquals(1, database.execute("SELECT * FROM cars WHERE since >= '2021-01-01'").rows().size());
        assertThrows(DbException.class, () -> database.execute("SELECT * FROM cars WHERE nope = 1"));
    }

    /**
     * Проверка: UPDATE меняет подходящие строки, первичный ключ менять нельзя.
     */
    @DisplayName("UPDATE меняет подходящие строки, первичный ключ менять нельзя")
    @Test
    void update() {
        assertEquals("Обновлено строк: 1", database.execute("UPDATE cars SET price = 30000 WHERE id = 2").message());
        assertEquals(List.of(List.of(30000.0)), database.execute("SELECT price FROM cars WHERE id = 2").rows());
        assertThrows(DbException.class, () -> database.execute("UPDATE cars SET id = 10 WHERE id = 2"));
    }

    /**
     * Проверка: DELETE удаляет подходящие строки.
     */
    @DisplayName("DELETE удаляет подходящие строки")
    @Test
    void delete() {
        assertEquals("Удалено строк: 2", database.execute("DELETE FROM cars WHERE brand = 'Toyota'").message());
        assertEquals(1, database.execute("SELECT * FROM cars").rows().size());
    }

    /**
     * Проверка: внешний ключ ограничивает вставку, удаление и DROP.
     */
    @DisplayName("Внешний ключ: вставка, удаление и DROP ограничены")
    @Test
    void foreignKey() {
        assertThrows(DbException.class, () -> database.execute("INSERT INTO owners VALUES (1, 99, 'Ann')"));
        database.execute("INSERT INTO owners VALUES (1, 1, 'Ann')");

        assertThrows(DbException.class, () -> database.execute("DELETE FROM cars WHERE id = 1"));
        assertThrows(DbException.class, () -> database.execute("UPDATE owners SET car_id = 99 WHERE id = 1"));
        assertThrows(DbException.class, () -> database.execute("DROP TABLE cars"));

        database.execute("DELETE FROM owners WHERE id = 1");
        database.execute("DELETE FROM cars WHERE id = 1");
        database.execute("DROP TABLE owners");
        database.execute("DROP TABLE cars");
        assertThrows(DbException.class, () -> database.execute("SELECT * FROM cars"));
    }

    /**
     * Проверка: поиск по первичному ключу использует ключ.
     */
    @DisplayName("Поиск по первичному ключу использует ключ")
    @Test
    void primaryKeyLookup() {
        assertTrue(database.execute("SELECT * FROM cars WHERE id = 2").message().contains("PRIMARY KEY"));
    }
}
