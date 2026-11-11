package db;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Замер скорости SELECT без индекса и с индексом.
 *
 * <p>Алгоритм: для каждого размера создаётся таблица {@code cars} в {@link MemoryStorage} и заполняется
 * строками пачками по 1000. Каждый запрос из {@link #CASES} сначала прогревается 10 раз, затем выполняется
 * {@link #QUERIES} раз, берётся среднее время одного запроса. После этого создаются индексы по {@code brand}
 * и {@code price}, и замер повторяется. Результат печатается таблицей в формате Markdown.
 *
 * <p>Запуск: {@code java -cp target/classes db.Benchmark [размеры строк через пробел]}.
 */
public final class Benchmark {
    private static final int QUERIES = 100;
    private static final int BRANDS = 1000;

    /**
     * Один замеряемый запрос.
     *
     * @param name      описание для таблицы результатов
     * @param sqlFormat шаблон запроса с одним числовым параметром
     * @param variants  сколько разных значений параметра перебирается
     */
    private record Case(String name, String sqlFormat, int variants) {
    }

    private static final List<Case> CASES = List.of(
        new Case("brand = 'brand_k' (~0.1% строк)", "SELECT * FROM cars WHERE brand = 'brand_%d'", BRANDS),
        new Case("price < a, a ≤ 150 (диапазон, ≤0.015% строк)",
            "SELECT * FROM cars WHERE price < %d.0", 100)
    );

    private Benchmark() {
    }

    /**
     * Печатает таблицу замеров для каждого размера.
     *
     * @param args необязательные размеры таблицы; по умолчанию 10 тысяч, 100 тысяч и миллион строк
     * @throws IOException если не удалось создать базу
     */
    static void main(String[] args) throws IOException {
        int[] sizes = args.length > 0
            ? Arrays.stream(args).mapToInt(Integer::parseInt).toArray()
            : new int[]{10_000, 100_000, 1_000_000};

        System.out.println("| Строк | Запрос | Без индекса, мс | С индексом, мс | Ускорение |");
        System.out.println("|---:|---|---:|---:|---:|");

        for (int size : sizes) {
            Database database = new Database(new MemoryStorage());
            database.execute("CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE)");
            fill(database, size);

            double[] without = measure(database);
            database.execute("CREATE INDEX idx_cars_brand ON cars (brand)");
            database.execute("CREATE INDEX idx_cars_price ON cars (price)");
            double[] with = measure(database);

            for (int i = 0; i < CASES.size(); i++) {
                System.out.printf(Locale.ROOT, "| %,d | `%s` | %.3f | %.3f | ×%.0f |%n",
                    size, CASES.get(i).name(), without[i], with[i], without[i] / with[i]);
            }
        }
    }

    /**
     * Заполняет таблицу {@code cars} строками пачками по 1000 штук.
     */
    private static void fill(Database database, int size) {
        for (int start = 0; start < size; start += 1000) {
            StringBuilder sql = new StringBuilder("INSERT INTO cars VALUES ");
            for (int id = start; id < Math.min(start + 1000, size); id++) {
                sql.append(id > start ? ", " : "")
                    .append('(').append(id).append(", 'brand_").append(id % BRANDS).append("', ").append(id).append(".0)");
            }
            database.execute(sql.toString());
        }
    }

    /**
     * Замеряет все случаи из {@link #CASES}.
     *
     * @return среднее время одного запроса в миллисекундах для каждого случая
     */
    private static double[] measure(Database database) {
        double[] result = new double[CASES.size()];
        for (int c = 0; c < CASES.size(); c++) {
            run(database, c, 10);
            long start = System.nanoTime();
            run(database, c, QUERIES);
            result[c] = (System.nanoTime() - start) / 1e6 / QUERIES;
        }
        return result;
    }

    /**
     * Выполняет запрос выбранного случая {@code count} раз, каждый раз с новым значением параметра.
     */
    private static void run(Database database, int caseIndex, int count) {
        Case benchmark = CASES.get(caseIndex);
        for (int i = 0; i < count; i++) {
            int variant = i % benchmark.variants();
            database.execute(benchmark.sqlFormat().formatted(caseIndex == 0 ? variant : variant + 50));
        }
    }
}
