/**
 * Кусочек запроса: слово, число, строка в кавычках или символ вроде скобки и запятой.
 */
public class Token {
    private final TokenType type;
    private final String text;

    public Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    /**
     * Это слово word (без учета регистра)?
     */
    public boolean isWord(String word) {
        return type == TokenType.WORD && text.equalsIgnoreCase(word);
    }

    /**
     * Это символ symbol?
     */
    public boolean isSymbol(String symbol) {
        return type == TokenType.SYMBOL && text.equals(symbol);
    }
}
