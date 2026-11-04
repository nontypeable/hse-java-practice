package db;

import java.util.ArrayList;
import java.util.List;

/**
 * Условие WHERE: сравнения колонки с числом, строкой и т.д., объединённые AND и OR.
 * Если в строке или в запросе NULL, сравнение считается ложным.
 */
public interface Condition {
    /**
     * Проверяет, что колонки существуют, и приводит значения к типам колонок.
     */
    Condition bind(Schema schema);

    /**
     * Подходит ли строка под условие. Условие должно быть уже привязано через {@link #bind}.
     */
    boolean matches(Schema schema, Object[] row);

    /**
     * Сравнения, которые обязательно должны выполняться (для AND — оба, для OR — никакого).
     * По ним можно искать строки через индекс.
     */
    List<Comparison> conjuncts();

    /**
     * Сравнение вида {@code колонка оператор значение}.
     */
    record Comparison(String column, Operator operator, Object value) implements Condition {
        @Override
        public Condition bind(Schema schema) {
            return new Comparison(column, operator, schema.column(column).type().coerce(value));
        }

        @Override
        public boolean matches(Schema schema, Object[] row) {
            Object actual = row[schema.indexOf(column)];
            if (value == null || actual == null) {
                return false;
            }
            return operator.test(compare(actual, value));
        }

        @Override
        public List<Comparison> conjuncts() {
            List<Comparison> result = new ArrayList<>();
            result.add(this);
            return result;
        }

        @SuppressWarnings("unchecked")
        private static int compare(Object left, Object right) {
            return ((Comparable<Object>) left).compareTo(right);
        }
    }

    /**
     * Должны выполняться оба условия.
     */
    record And(Condition left, Condition right) implements Condition {
        @Override
        public Condition bind(Schema schema) {
            return new And(left.bind(schema), right.bind(schema));
        }

        @Override
        public boolean matches(Schema schema, Object[] row) {
            return left.matches(schema, row) && right.matches(schema, row);
        }

        @Override
        public List<Comparison> conjuncts() {
            List<Comparison> result = new ArrayList<>(left.conjuncts());
            result.addAll(right.conjuncts());
            return result;
        }
    }

    /**
     * Достаточно одного из условий.
     */
    record Or(Condition left, Condition right) implements Condition {
        @Override
        public Condition bind(Schema schema) {
            return new Or(left.bind(schema), right.bind(schema));
        }

        @Override
        public boolean matches(Schema schema, Object[] row) {
            return left.matches(schema, row) || right.matches(schema, row);
        }

        @Override
        public List<Comparison> conjuncts() {
            return new ArrayList<>();
        }
    }
}
