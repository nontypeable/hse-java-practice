package db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Индекс по одной колонке.
 *
 * <p>Алгоритм: значения колонки хранятся в отсортированном дереве {@link TreeMap}, где каждому значению
 * соответствует набор первичных ключей строк. Поиск по {@code =} берёт одну ветку дерева, поиск по
 * {@code < <= > >=} — диапазон ({@code headMap} или {@code tailMap}), поэтому время поиска растёт
 * логарифмически, а не линейно с размером таблицы. Для {@code !=} индекс бесполезен, а {@code NULL}
 * не индексируется: сравнение с ним всегда ложно.
 */
public final class Index {
    private final String name;
    private final String column;
    private final TreeMap<Object, Set<Object>> entries = new TreeMap<>();

    /**
     * Создаёт пустой индекс.
     *
     * @param name   имя индекса
     * @param column колонка, по которой он построен
     */
    public Index(String name, String column) {
        this.name = name;
        this.column = column;
    }

    /**
     * Имя индекса.
     */
    public String name() {
        return name;
    }

    /**
     * Имя индексируемой колонки.
     */
    public String column() {
        return column;
    }

    /**
     * Проверяет, можно ли искать с этим оператором через индекс.
     *
     * @return {@code false} для {@code !=}, иначе {@code true}
     */
    public static boolean supports(Operator operator) {
        return operator != Operator.NE;
    }

    /**
     * Добавляет строку в индекс; значение {@code NULL} пропускается.
     *
     * @param key        значение индексируемой колонки
     * @param primaryKey первичный ключ строки
     */
    public void add(Object key, Object primaryKey) {
        if (key == null) {
            return;
        }
        Set<Object> keys = entries.get(key);
        if (keys == null) {
            keys = new LinkedHashSet<>();
            entries.put(key, keys);
        }
        keys.add(primaryKey);
    }

    /**
     * Убирает строку из индекса; пустой набор ключей удаляется вместе со значением.
     *
     * @param key        значение индексируемой колонки
     * @param primaryKey первичный ключ строки
     */
    public void remove(Object key, Object primaryKey) {
        if (key == null) {
            return;
        }
        Set<Object> keys = entries.get(key);
        if (keys != null) {
            keys.remove(primaryKey);
            if (keys.isEmpty()) {
                entries.remove(key);
            }
        }
    }

    /**
     * Ищет первичные ключи строк, у которых значение колонки подходит под {@code колонка оператор value}.
     *
     * @throws IllegalArgumentException для оператора {@code !=}
     */
    public List<Object> find(Operator operator, Object value) {
        Collection<Set<Object>> hits;
        if (operator == Operator.EQ) {
            hits = entries.subMap(value, true, value, true).values();
        } else if (operator == Operator.LT) {
            hits = entries.headMap(value, false).values();
        } else if (operator == Operator.LE) {
            hits = entries.headMap(value, true).values();
        } else if (operator == Operator.GT) {
            hits = entries.tailMap(value, false).values();
        } else if (operator == Operator.GE) {
            hits = entries.tailMap(value, true).values();
        } else {
            throw new IllegalArgumentException("Оператор != не использует индекс");
        }

        List<Object> result = new ArrayList<>();
        for (Set<Object> keys : hits) {
            result.addAll(keys);
        }
        return result;
    }

    /**
     * Содержимое индекса только для чтения, нужно для записи на диск.
     */
    public Map<Object, Set<Object>> entries() {
        return Collections.unmodifiableMap(entries);
    }
}
