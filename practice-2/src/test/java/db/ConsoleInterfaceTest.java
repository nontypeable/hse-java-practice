package db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тестирование консольного интерфейса: ввод запросов, вывод таблицы и реакция на ошибки.
 */
class ConsoleInterfaceTest {
    /**
     * Запускает интерфейс на пустой базе в памяти и возвращает всё, что он напечатал.
     */
    private String run(String input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ConsoleInterface(new Scanner(input), new PrintStream(output, true, StandardCharsets.UTF_8),
            new Database(new MemoryStorage())).run();
        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * Проверка: запрос на нескольких строках выводит таблицу.
     */
    @DisplayName("Запрос на нескольких строках выводит таблицу")
    @Test
    void printsTable() throws IOException {
        String output = run("""
            CREATE TABLE cars (id INT PRIMARY KEY, brand STRING);
            INSERT INTO cars VALUES
              (1, 'Toyota'), (2, 'BMW');
            SELECT * FROM cars WHERE id > 1;
            exit
            """);

        assertTrue(output.contains("| id | brand |"));
        assertTrue(output.contains("|  2 | BMW") || output.contains("| 2  | BMW"));
        assertTrue(output.contains("Строк: 1"));
        assertTrue(output.contains("До свидания!"));
    }

    /**
     * Проверка: ошибка запроса не завершает программу.
     */
    @DisplayName("Ошибка запроса не завершает программу")
    @Test
    void continuesAfterError() throws IOException {
        String output = run("SELECT * FROM nope;\nhelp\nSELEC;\n");

        assertTrue(output.contains("Ошибка: Таблица nope не существует"));
        assertTrue(output.contains("CREATE TABLE"));
        assertTrue(output.contains("Синтаксическая ошибка"));
        assertTrue(output.contains("Ввод завершён."));
    }
}
