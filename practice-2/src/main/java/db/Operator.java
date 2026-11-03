package db;

/**
 * Операторы сравнения в условии WHERE.
 */
public enum Operator {
    EQ, NE, LT, LE, GT, GE;

    /**
     * Находит оператор по записи в запросе: {@code = != <> < <= > >=}.
     */
    public static Operator fromSymbol(String symbol) {
        return switch (symbol) {
            case "=" -> EQ;
            case "!=", "<>" -> NE;
            case "<" -> LT;
            case "<=" -> LE;
            case ">" -> GT;
            case ">=" -> GE;
            default -> throw new DbException("Неизвестный оператор: " + symbol);
        };
    }

    /**
     * Проверяет результат {@link Comparable#compareTo} на соответствие оператору.
     */
    public boolean test(int comparison) {
        return switch (this) {
            case EQ -> comparison == 0;
            case NE -> comparison != 0;
            case LT -> comparison < 0;
            case LE -> comparison <= 0;
            case GT -> comparison > 0;
            case GE -> comparison >= 0;
        };
    }
}
