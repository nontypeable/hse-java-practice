package db;

import db.Lexer.Kind;
import db.Lexer.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Рекурсивный разбор простого SQL: CREATE TABLE, DROP TABLE, INSERT, SELECT, UPDATE, DELETE.
 * Ключевые слова не зависят от регистра, имена приводятся к нижнему регистру.
 *
 * <p>Условие WHERE: {@code колонка оператор литерал}, объединённые AND и OR (AND сильнее), без скобок.
 */
final class Parser {
    private final List<Token> tokens;
    private int position;

    private Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Разбирает один запрос; завершающая {@code ;} необязательна.
     *
     * @throws DbException при синтаксической ошибке
     */
    static Statement parse(String sql) {
        Parser parser = new Parser(Lexer.tokenize(sql));
        Statement statement = parser.statement();
        parser.acceptSymbol(";");
        if (parser.peek().kind() != Kind.END) {
            throw parser.error("конец запроса");
        }
        return statement;
    }

    private Statement statement() {
        if (acceptWord("SELECT")) {
            return select();
        }
        if (acceptWord("INSERT")) {
            return insert();
        }
        if (acceptWord("UPDATE")) {
            return update();
        }
        if (acceptWord("DELETE")) {
            return delete();
        }
        if (acceptWord("CREATE")) {
            if (acceptWord("TABLE")) {
                return createTable();
            }
            throw error("TABLE");
        }
        if (acceptWord("DROP")) {
            expectWord("TABLE");
            return new Statement.DropTable(identifier());
        }
        throw error("SELECT, INSERT, UPDATE, DELETE, CREATE или DROP");
    }

    private Statement createTable() {
        String name = identifier();
        expectSymbol("(");
        List<Column> columns = new ArrayList<>();
        do {
            columns.add(columnDefinition());
        } while (acceptSymbol(","));
        expectSymbol(")");
        return new Statement.CreateTable(name, columns);
    }

    private Column columnDefinition() {
        String name = identifier();
        Token type = next();
        if (type.kind() != Kind.WORD) {
            throw new DbException("Для колонки " + name + " не указан тип");
        }

        boolean primaryKey = false;
        ForeignKey reference = null;
        while (true) {
            if (acceptWord("PRIMARY")) {
                expectWord("KEY");
                primaryKey = true;
            } else if (acceptWord("REFERENCES")) {
                String table = identifier();
                expectSymbol("(");
                String column = identifier();
                expectSymbol(")");
                reference = new ForeignKey(table, column);
            } else {
                break;
            }
        }
        return new Column(name, ColumnType.fromSql(type.text()), primaryKey, reference);
    }

    private Statement insert() {
        expectWord("INTO");
        String table = identifier();

        List<String> columns = null;
        if (acceptSymbol("(")) {
            columns = new ArrayList<>();
            do {
                columns.add(identifier());
            } while (acceptSymbol(","));
            expectSymbol(")");
        }

        expectWord("VALUES");
        List<List<Object>> rows = new ArrayList<>();
        do {
            expectSymbol("(");
            List<Object> values = new ArrayList<>();
            do {
                values.add(literal());
            } while (acceptSymbol(","));
            expectSymbol(")");
            rows.add(values);
        } while (acceptSymbol(","));
        return new Statement.Insert(table, columns, rows);
    }

    private Statement select() {
        List<String> columns = null;
        if (!acceptSymbol("*")) {
            columns = new ArrayList<>();
            do {
                columns.add(identifier());
            } while (acceptSymbol(","));
        }
        expectWord("FROM");
        return new Statement.Select(identifier(), columns, optionalWhere());
    }

    private Statement update() {
        String table = identifier();
        expectWord("SET");
        Map<String, Object> assignments = new LinkedHashMap<>();
        do {
            String column = identifier();
            expectSymbol("=");
            if (assignments.containsKey(column)) {
                throw new DbException("Колонка " + column + " присваивается дважды");
            }
            assignments.put(column, literal());
        } while (acceptSymbol(","));
        return new Statement.Update(table, assignments, optionalWhere());
    }

    private Statement delete() {
        expectWord("FROM");
        return new Statement.Delete(identifier(), optionalWhere());
    }

    private Condition optionalWhere() {
        return acceptWord("WHERE") ? or() : null;
    }

    private Condition or() {
        Condition result = and();
        while (acceptWord("OR")) {
            result = new Condition.Or(result, and());
        }
        return result;
    }

    private Condition and() {
        Condition result = comparison();
        while (acceptWord("AND")) {
            result = new Condition.And(result, comparison());
        }
        return result;
    }

    private Condition comparison() {
        String column = identifier();
        Token operator = next();
        if (operator.kind() != Kind.SYMBOL) {
            throw new DbException("Ожидался оператор сравнения после " + column + ", найдено " + describe(operator));
        }
        return new Condition.Comparison(column, Operator.fromSymbol(operator.text()), literal());
    }

    private Object literal() {
        Token token = next();
        if (token.kind() == Kind.STRING) {
            return token.text();
        }
        if (token.kind() == Kind.NUMBER) {
            return number(token.text());
        }
        if (token.kind() == Kind.WORD) {
            if (token.text().equalsIgnoreCase("TRUE")) {
                return Boolean.TRUE;
            }
            if (token.text().equalsIgnoreCase("FALSE")) {
                return Boolean.FALSE;
            }
            if (token.text().equalsIgnoreCase("NULL")) {
                return null;
            }
        }
        throw new DbException("Ожидалось значение, найдено " + describe(token));
    }

    private static Object number(String text) {
        try {
            if (text.contains(".")) {
                return Double.valueOf(text);
            }
            return Long.valueOf(text);
        } catch (NumberFormatException exception) {
            throw new DbException("Некорректное число: " + text);
        }
    }

    private Token peek() {
        return tokens.get(position);
    }

    private Token next() {
        Token token = tokens.get(position);
        if (token.kind() != Kind.END) {
            position++;
        }
        return token;
    }

    private boolean acceptWord(String word) {
        Token token = peek();
        if (token.kind() == Kind.WORD && token.text().equalsIgnoreCase(word)) {
            position++;
            return true;
        }
        return false;
    }

    private void expectWord(String word) {
        if (!acceptWord(word)) {
            throw error(word);
        }
    }

    private boolean acceptSymbol(String symbol) {
        Token token = peek();
        if (token.kind() == Kind.SYMBOL && token.text().equals(symbol)) {
            position++;
            return true;
        }
        return false;
    }

    private void expectSymbol(String symbol) {
        if (!acceptSymbol(symbol)) {
            throw error("'" + symbol + "'");
        }
    }

    private String identifier() {
        Token token = peek();
        if (token.kind() != Kind.WORD) {
            throw error("имя");
        }
        position++;
        return token.text().toLowerCase();
    }

    private DbException error(String expected) {
        return new DbException("Синтаксическая ошибка: ожидалось " + expected + ", найдено " + describe(peek()));
    }

    private static String describe(Token token) {
        return token.kind() == Kind.END ? "конец запроса" : "'" + token.text() + "'";
    }
}
