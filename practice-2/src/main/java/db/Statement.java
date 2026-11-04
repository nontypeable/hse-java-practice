package db;

import java.util.List;
import java.util.Map;

/**
 * Разобранный SQL-запрос. Литералы в запросах — {@link Long}, {@link Double}, {@link String},
 * {@link Boolean} или {@code null}; к типам колонок их приводит {@link Database}.
 */
public interface Statement {
    record CreateTable(String name, List<Column> columns) implements Statement {
    }

    record DropTable(String name) implements Statement {
    }

    /**
     * @param columns перечисленные колонки или {@code null}, если значения даны для всех по порядку
     * @param rows    кортежи значений
     */
    record Insert(String table, List<String> columns, List<List<Object>> rows) implements Statement {
    }

    /**
     * @param columns выбираемые колонки или {@code null} для {@code *}
     * @param where   условие или {@code null}
     */
    record Select(String table, List<String> columns, Condition where) implements Statement {
    }

    /**
     * @param assignments колонка → новое значение, в порядке записи в запросе
     */
    record Update(String table, Map<String, Object> assignments, Condition where) implements Statement {
    }

    record Delete(String table, Condition where) implements Statement {
    }
}
