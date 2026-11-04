package db;

import java.util.ArrayList;
import java.util.List;

/**
 * Разбивает текст запроса на токены: слова, числа, строки в одинарных кавычках и знаки.
 */
final class Lexer {
    enum Kind {WORD, NUMBER, STRING, SYMBOL, END}

    record Token(Kind kind, String text) {
    }

    private static final List<String> TWO_CHAR_SYMBOLS = List.of("<=", ">=", "<>", "!=");
    private static final String ONE_CHAR_SYMBOLS = "()*,;=<>";

    private Lexer() {
    }

    /**
     * @return токены, последний всегда {@link Kind#END}
     */
    static List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < sql.length()) {
            char c = sql.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '\'') {
                StringBuilder text = new StringBuilder();
                boolean closed = false;
                i++;
                while (i < sql.length() && !closed) {
                    char d = sql.charAt(i);
                    if (d != '\'') {
                        text.append(d);
                        i++;
                    } else if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                        text.append('\'');
                        i += 2;
                    } else {
                        closed = true;
                        i++;
                    }
                }
                if (!closed) {
                    throw new DbException("Строка не закрыта кавычкой");
                }
                tokens.add(new Token(Kind.STRING, text.toString()));
            } else if (Character.isDigit(c) || c == '-' && i + 1 < sql.length() && Character.isDigit(sql.charAt(i + 1))) {
                int start = i++;
                i = skipDigits(sql, i);
                if (i + 1 < sql.length() && sql.charAt(i) == '.' && Character.isDigit(sql.charAt(i + 1))) {
                    i = skipDigits(sql, i + 1);
                }
                tokens.add(new Token(Kind.NUMBER, sql.substring(start, i)));
            } else if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < sql.length() && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    i++;
                }
                tokens.add(new Token(Kind.WORD, sql.substring(start, i)));
            } else if (i + 1 < sql.length() && TWO_CHAR_SYMBOLS.contains(sql.substring(i, i + 2))) {
                tokens.add(new Token(Kind.SYMBOL, sql.substring(i, i + 2)));
                i += 2;
            } else if (ONE_CHAR_SYMBOLS.indexOf(c) >= 0) {
                tokens.add(new Token(Kind.SYMBOL, String.valueOf(c)));
                i++;
            } else {
                throw new DbException("Неожиданный символ '" + c + "'");
            }
        }

        tokens.add(new Token(Kind.END, ""));
        return tokens;
    }

    private static int skipDigits(String sql, int from) {
        int i = from;
        while (i < sql.length() && Character.isDigit(sql.charAt(i))) {
            i++;
        }
        return i;
    }
}
