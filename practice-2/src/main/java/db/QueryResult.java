package db;

import java.util.List;

/**
 * Результат выполнения запроса.
 *
 * @param columns заголовки; пусты, если запрос не возвращает строк
 * @param rows    строки результата
 * @param message итоговое сообщение, например «Строк: 2»
 */
public record QueryResult(List<String> columns, List<List<Object>> rows, String message) {
    /**
     * Результат без таблицы, только сообщение.
     */
    public static QueryResult message(String message) {
        return new QueryResult(List.of(), List.of(), message);
    }

    /**
     * Нужно ли выводить таблицу.
     */
    public boolean hasRows() {
        return !columns.isEmpty();
    }
}
