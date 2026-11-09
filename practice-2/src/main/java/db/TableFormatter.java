package db;

import java.util.ArrayList;
import java.util.List;

/**
 * Форматирование результата запроса в текстовую таблицу.
 *
 * <p>Алгоритм: значения всех строк переводятся в текст ({@code NULL} печатается как есть), затем для каждой
 * колонки берётся максимальная длина среди заголовка и значений. Таблица рисуется символами {@code + - |},
 * каждая ячейка дополняется пробелами до ширины колонки. Под таблицей выводится сообщение результата.
 */
public final class TableFormatter {
    private TableFormatter() {
    }

    /**
     * Форматирует результат: таблица, если в нём есть колонки, и итоговое сообщение.
     *
     * @param result результат выполнения запроса
     * @return готовый к печати текст
     */
    public static String format(QueryResult result) {
        if (!result.hasRows()) {
            return result.message();
        }

        int count = result.columns().size();
        List<String[]> cells = new ArrayList<>();
        for (List<Object> row : result.rows()) {
            String[] line = new String[count];
            for (int i = 0; i < count; i++) {
                line[i] = row.get(i) == null ? "NULL" : row.get(i).toString();
            }
            cells.add(line);
        }

        int[] widths = new int[count];
        for (int i = 0; i < count; i++) {
            widths[i] = result.columns().get(i).length();
            for (String[] line : cells) {
                widths[i] = Math.max(widths[i], line[i].length());
            }
        }

        StringBuilder text = new StringBuilder();
        String separator = separator(widths);
        text.append(separator).append('\n');
        text.append(line(result.columns().toArray(new String[0]), widths)).append('\n');
        text.append(separator).append('\n');
        for (String[] line : cells) {
            text.append(line(line, widths)).append('\n');
        }
        text.append(separator).append('\n');
        text.append(result.message());
        return text.toString();
    }

    /**
     * Строит горизонтальную линию вида {@code +----+-----+}.
     */
    private static String separator(int[] widths) {
        StringBuilder text = new StringBuilder("+");
        for (int width : widths) {
            text.append("-".repeat(width + 2)).append('+');
        }
        return text.toString();
    }

    /**
     * Строит одну строку таблицы, дополняя каждое значение пробелами до ширины колонки.
     */
    private static String line(String[] values, int[] widths) {
        StringBuilder text = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) {
            text.append(' ').append(values[i]).append(" ".repeat(widths[i] - values[i].length())).append(" |");
        }
        return text.toString();
    }
}
