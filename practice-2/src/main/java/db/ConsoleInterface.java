package db;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Консольный интерфейс СУБД.
 *
 * <p>Алгоритм: строки читаются из ввода и накапливаются в один запрос, пока очередная строка не заканчивается
 * на {@code ;}. Тогда запрос передаётся в {@link Database}, а результат печатается через {@link TableFormatter}.
 * Команды {@code help} и {@code exit} распознаются только между запросами. Ошибка запроса не завершает программу.
 */
public final class ConsoleInterface {
    private final Scanner input;
    private final PrintStream output;
    private final Database database;

    /**
     * Связывает интерфейс с вводом, выводом и базой данных.
     *
     * @param input    источник строк запросов
     * @param output   куда печатаются приглашения и результаты
     * @param database база, которая выполняет запросы
     */
    public ConsoleInterface(Scanner input, PrintStream output, Database database) {
        this.input = input;
        this.output = output;
        this.database = database;
    }

    /**
     * Читает и выполняет запросы, пока не введена команда {@code exit} или не закончится ввод.
     */
    public void run() {
        output.println("Простая СУБД. Запросы заканчиваются ';', help — справка, exit — выход.");

        StringBuilder query = new StringBuilder();
        try {
            while (true) {
                output.print(query.isEmpty() ? "sql> " : "...> ");
                output.flush();
                String line = input.nextLine().trim();

                if (query.isEmpty()) {
                    if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                        output.println("До свидания!");
                        return;
                    }
                    if (line.equalsIgnoreCase("help")) {
                        printHelp();
                        continue;
                    }
                    if (line.isEmpty()) {
                        continue;
                    }
                }

                query.append(line).append(' ');
                if (line.endsWith(";")) {
                    execute(query.toString());
                    query.setLength(0);
                }
            }
        } catch (NoSuchElementException exception) {
            output.println("Ввод завершён.");
        }
    }

    /**
     * Выполняет один запрос и печатает результат или понятное сообщение об ошибке.
     */
    private void execute(String sql) {
        try {
            output.println(TableFormatter.format(database.execute(sql)));
        } catch (DbException exception) {
            output.println("Ошибка: " + exception.getMessage());
        } catch (UncheckedIOException exception) {
            output.println("Ошибка записи на диск: " + exception.getCause().getMessage());
        }
    }

    /**
     * Печатает примеры запросов и список типов и операторов.
     */
    private void printHelp() {
        output.println("""
            CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE, since DATE, used BOOLEAN);
            CREATE TABLE owners (id INT PRIMARY KEY, car_id INT REFERENCES cars(id), name STRING);
            INSERT INTO cars VALUES (1, 'Toyota', 25000.0, '2020-05-17', TRUE), (2, 'BMW', 40000, '2021-01-01', FALSE);
            INSERT INTO cars (id, brand) VALUES (3, 'Kia');
            SELECT brand, price FROM cars WHERE price > 20000 AND used = TRUE;
            UPDATE cars SET price = 30000 WHERE id = 2;
            DELETE FROM cars WHERE brand = 'Toyota';
            DROP TABLE owners;
            Типы: INT, DOUBLE, STRING, DATE ('ГГГГ-ММ-ДД'), BOOLEAN. Операторы: = != <> < <= > >=, AND, OR.
            """);
    }
}
