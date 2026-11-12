package db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тестирование хранения в файлах: восстановление данных после перезапуска и защита от сбоев записи.
 */
class FileStorageTest {
    @TempDir
    Path directory;

    /**
     * Открывает базу поверх каталога, где лежат файлы таблиц.
     */
    private Database open() throws IOException {
        return new Database(new FileStorage(directory));
    }

    /**
     * Создаёт таблицы {@code cars} и {@code owners} и заполняет их тестовыми строками.
     */
    private Database prepare() throws IOException {
        Database database = open();
        database.execute("CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE, since DATE, used BOOLEAN)");
        database.execute("CREATE TABLE owners (id INT PRIMARY KEY, car_id INT REFERENCES cars(id))");
        database.execute("INSERT INTO cars VALUES (1, 'Toyota', 25000.5, '2020-05-17', TRUE), (2, 'BMW', NULL, NULL, FALSE)");
        database.execute("INSERT INTO owners VALUES (1, 1)");
        return database;
    }

    /**
     * Проверка: данные, схема и внешние ключи восстанавливаются после перезапуска.
     */
    @DisplayName("Данные, схема и внешние ключи восстанавливаются после перезапуска")
    @Test
    void restoresEverything() throws IOException {
        prepare();
        Database restored = open();

        QueryResult cars = restored.execute("SELECT * FROM cars WHERE brand = 'Toyota'");
        assertEquals(1, cars.rows().size());
        assertEquals("2020-05-17", cars.rows().get(0).get(3).toString());
        assertEquals(2, restored.execute("SELECT * FROM cars").rows().size());
        assertThrows(DbException.class, () -> restored.execute("DELETE FROM cars WHERE id = 1"));
        assertThrows(DbException.class, () -> restored.execute("INSERT INTO cars (id) VALUES (1)"));
    }

    /**
     * Проверка: изменения сохраняются после каждого запроса.
     */
    @DisplayName("Изменения сохраняются после каждого запроса")
    @Test
    void persistsChanges() throws IOException {
        Database database = prepare();
        database.execute("UPDATE cars SET brand = 'Kia' WHERE id = 1");
        database.execute("DELETE FROM cars WHERE id = 2");

        QueryResult result = open().execute("SELECT id FROM cars WHERE brand = 'Kia'");
        assertEquals(1, result.rows().size());
        assertEquals(1, open().execute("SELECT * FROM cars").rows().size());
    }

    /**
     * Проверка: недописанные .tmp после сбоя удаляются, данные остаются прежними.
     */
    @DisplayName("Недописанные .tmp после сбоя удаляются, данные остаются прежними")
    @Test
    void removesTemporaryFiles() throws IOException {
        prepare();
        Path temporary = directory.resolve("cars.tbl.tmp");
        Files.write(temporary, new byte[]{9, 9});

        assertEquals(2, open().execute("SELECT * FROM cars").rows().size());
        assertFalse(Files.exists(temporary));
    }

    /**
     * Проверка: DROP TABLE удаляет файл таблицы.
     */
    @DisplayName("DROP TABLE удаляет файл таблицы")
    @Test
    void dropRemovesFiles() throws IOException {
        Database database = prepare();
        database.execute("DROP TABLE owners");
        database.execute("DROP TABLE cars");

        try (var files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }

    /**
     * Проверка: повреждённый файл таблицы вызывает понятную ошибку.
     */
    @DisplayName("Повреждённый файл таблицы вызывает понятную ошибку")
    @Test
    void corruptTableFails() throws IOException {
        prepare();
        Files.write(directory.resolve("cars.tbl"), new byte[]{1, 2});

        assertThrows(IOException.class, this::open);
    }
}
