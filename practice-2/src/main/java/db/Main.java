package db;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Точка входа СУБД.
 *
 * <p>Алгоритм: из первого аргумента командной строки берётся каталог данных (по умолчанию {@code data}),
 * поверх него создаётся {@link FileStorage}, затем {@link Database} загружает таблицы с диска, а
 * {@link ConsoleInterface} начинает читать запросы из стандартного ввода.
 */
public final class Main {
    private Main() {
    }

    /**
     * Запускает консольную СУБД.
     *
     * @param args необязательный путь к каталогу данных
     * @throws IOException если каталог данных недоступен или файл таблицы повреждён
     */
    static void main(String[] args) throws IOException {
        Path directory = Path.of(args.length > 0 ? args[0] : "data");
        Database database = new Database(new FileStorage(directory));
        new ConsoleInterface(new Scanner(System.in), System.out, database).run();
    }
}
