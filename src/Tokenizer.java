import java.util.ArrayList;
import java.util.List;

/**
 * Разбивает текст запроса на кусочки (токены).
 * Например, "SELECT * FROM cars WHERE price > 20000;" превращается в
 * SELECT, *, FROM, cars, WHERE, price, >, 20000, ;
 */
public class Tokenizer {

    /**
     * Разбивает запрос на токены.
     *
     * @param sql текст запроса
     * @return список токенов
     */
    public List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < sql.length() && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    i++;
                }
                tokens.add(new Token(TokenType.WORD, sql.substring(start, i)));
            } else if (Character.isDigit(c) || (c == '-' && i + 1 < sql.length() && Character.isDigit(sql.charAt(i + 1)))) {
                int start = i;
                i++;
                while (i < sql.length() && (Character.isDigit(sql.charAt(i)) || sql.charAt(i) == '.')) {
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, sql.substring(start, i)));
            } else if (c == '\'') {
                int end = sql.indexOf('\'', i + 1);
                if (end < 0) {
                    throw new DbException("Не закрыта кавычка у строки");
                }
                tokens.add(new Token(TokenType.STRING, sql.substring(i + 1, end)));
                i = end + 1;
            } else if (c == '<' || c == '>' || c == '!') {
                String next = "";
                if (i + 1 < sql.length()) {
                    next = String.valueOf(sql.charAt(i + 1));
                }
                if (next.equals("=")) {
                    tokens.add(new Token(TokenType.SYMBOL, c + "="));
                    i += 2;
                } else if (c == '<' && next.equals(">")) {
                    tokens.add(new Token(TokenType.SYMBOL, "!="));
                    i += 2;
                } else if (c == '!') {
                    throw new DbException("Непонятный символ !, может быть имелось в виду != ?");
                } else {
                    tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c)));
                    i++;
                }
            } else if ("(),;*=".indexOf(c) >= 0) {
                tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c)));
                i++;
            } else {
                throw new DbException("Непонятный символ в запросе: " + c);
            }
        }
        return tokens;
    }
}
