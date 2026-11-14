import java.util.ArrayList;
import java.util.List;

/**
 * Разбирает текст запроса и создает объект запроса нужного вида.
 * Поддерживается простой формат: CREATE TABLE, DROP TABLE, INSERT, SELECT, UPDATE, DELETE.
 */
public class QueryParser {
    private final List<Token> tokens;
    private int position = 0;

    public QueryParser(String sql) {
        tokens = new Tokenizer().tokenize(sql);
    }

    /**
     * Разбирает запрос целиком.
     *
     * @return запрос, готовый к выполнению
     */
    public Query parse() {
        if (tokens.isEmpty()) {
            throw new DbException("Пустой запрос");
        }
        String command = readName().toUpperCase();
        Query query;
        switch (command) {
            case "CREATE":
                query = parseCreate();
                break;
            case "DROP":
                query = parseDrop();
                break;
            case "INSERT":
                query = parseInsert();
                break;
            case "SELECT":
                query = parseSelect();
                break;
            case "UPDATE":
                query = parseUpdate();
                break;
            case "DELETE":
                query = parseDelete();
                break;
            case "SHOW":
                expectWord("TABLES");
                query = new ShowTablesQuery();
                break;
            case "DESCRIBE":
                query = new DescribeQuery(readName());
                break;
            default:
                throw new DbException("Неизвестная команда " + command + ". Список команд: help");
        }
        if (position < tokens.size() && tokens.get(position).isSymbol(";")) {
            position++;
        }
        if (position < tokens.size()) {
            throw new DbException("Лишнее в конце запроса: " + tokens.get(position).getText());
        }
        return query;
    }

    private Query parseCreate() {
        String what = readName().toUpperCase();
        switch (what) {
            case "TABLE":
                return parseCreateTable();
            case "INDEX":
                return parseCreateIndex();
            default:
                throw new DbException("После CREATE ожидалось TABLE или INDEX, а не " + what);
        }
    }

    private Query parseCreateTable() {
        String tableName = readName();
        expectSymbol("(");
        List<Column> columns = new ArrayList<>();
        int primaryKeyIndex = -1;
        while (true) {
            String columnName = readName();
            DataType type = DataType.fromName(readName());
            Column column = new Column(columnName, type);
            columns.add(column);
            while (position < tokens.size() && tokens.get(position).getType() == TokenType.WORD) {
                String word = readName().toUpperCase();
                switch (word) {
                    case "PRIMARY":
                        expectWord("KEY");
                        if (primaryKeyIndex != -1) {
                            throw new DbException("Первичный ключ может быть только один");
                        }
                        primaryKeyIndex = columns.size() - 1;
                        break;
                    case "REFERENCES":
                        String referencedTable = readName();
                        expectSymbol("(");
                        String referencedColumn = readName();
                        expectSymbol(")");
                        column.setReference(referencedTable, referencedColumn);
                        break;
                    default:
                        throw new DbException("Непонятное слово " + word + " в описании столбца " + columnName);
                }
            }
            if (nextIsSymbol(",")) {
                position++;
            } else {
                expectSymbol(")");
                break;
            }
        }
        if (primaryKeyIndex == -1) {
            primaryKeyIndex = 0;
        }
        return new CreateTableQuery(tableName, columns, primaryKeyIndex);
    }

    private Query parseCreateIndex() {
        String indexName = readName();
        expectWord("ON");
        String tableName = readName();
        expectSymbol("(");
        String columnName = readName();
        expectSymbol(")");
        return new CreateIndexQuery(indexName, tableName, columnName);
    }

    private Query parseDrop() {
        String what = readName().toUpperCase();
        switch (what) {
            case "TABLE":
                return new DropTableQuery(readName());
            case "INDEX":
                return new DropIndexQuery(readName());
            default:
                throw new DbException("После DROP ожидалось TABLE или INDEX, а не " + what);
        }
    }

    private Query parseInsert() {
        expectWord("INTO");
        String tableName = readName();
        List<String> columnNames = null;
        if (nextIsSymbol("(")) {
            position++;
            columnNames = readNameList();
            expectSymbol(")");
        }
        expectWord("VALUES");
        List<List<Object>> rows = new ArrayList<>();
        while (true) {
            expectSymbol("(");
            List<Object> values = new ArrayList<>();
            values.add(readValue());
            while (nextIsSymbol(",")) {
                position++;
                values.add(readValue());
            }
            expectSymbol(")");
            rows.add(values);
            if (nextIsSymbol(",")) {
                position++;
            } else {
                break;
            }
        }
        return new InsertQuery(tableName, columnNames, rows);
    }

    private Query parseSelect() {
        List<String> columnNames = null;
        if (nextIsSymbol("*")) {
            position++;
        } else {
            columnNames = readNameList();
        }
        expectWord("FROM");
        String tableName = readName();
        Condition where = parseWhere();
        return new SelectQuery(tableName, columnNames, where);
    }

    private Query parseUpdate() {
        String tableName = readName();
        expectWord("SET");
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        while (true) {
            columnNames.add(readName());
            expectSymbol("=");
            values.add(readValue());
            if (nextIsSymbol(",")) {
                position++;
            } else {
                break;
            }
        }
        Condition where = parseWhere();
        return new UpdateQuery(tableName, columnNames, values, where);
    }

    private Query parseDelete() {
        expectWord("FROM");
        String tableName = readName();
        Condition where = parseWhere();
        return new DeleteQuery(tableName, where);
    }

    private Condition parseWhere() {
        if (position >= tokens.size() || !tokens.get(position).isWord("WHERE")) {
            return null;
        }
        position++;
        String columnName = readName();
        Token operator = next();
        if (operator.getType() != TokenType.SYMBOL || "(),;*".contains(operator.getText())) {
            throw new DbException("После " + columnName + " ожидался оператор сравнения (= != < > <= >=)");
        }
        Object value = readValue();
        return new Condition(columnName, operator.getText(), value);
    }

    private List<String> readNameList() {
        List<String> names = new ArrayList<>();
        names.add(readName());
        while (nextIsSymbol(",")) {
            position++;
            names.add(readName());
        }
        return names;
    }

    private Object readValue() {
        Token token = next();
        switch (token.getType()) {
            case NUMBER:
                try {
                    if (token.getText().contains(".")) {
                        return Double.parseDouble(token.getText());
                    }
                    return Long.parseLong(token.getText());
                } catch (NumberFormatException e) {
                    throw new DbException("Неправильное число: " + token.getText());
                }
            case STRING:
                return token.getText();
            case WORD:
                if (token.isWord("TRUE")) {
                    return Boolean.TRUE;
                }
                if (token.isWord("FALSE")) {
                    return Boolean.FALSE;
                }
                if (token.isWord("NULL")) {
                    return null;
                }
                throw new DbException("Ожидалось значение, а не " + token.getText() + ". Строки пишутся в кавычках: 'текст'");
            default:
                throw new DbException("Ожидалось значение, а не " + token.getText());
        }
    }

    private String readName() {
        Token token = next();
        if (token.getType() != TokenType.WORD) {
            throw new DbException("Ожидалось имя, а не " + token.getText());
        }
        return token.getText();
    }

    private void expectWord(String word) {
        Token token = next();
        if (!token.isWord(word)) {
            throw new DbException("Ожидалось " + word + ", а не " + token.getText());
        }
    }

    private void expectSymbol(String symbol) {
        Token token = next();
        if (!token.isSymbol(symbol)) {
            throw new DbException("Ожидалось " + symbol + ", а не " + token.getText());
        }
    }

    private boolean nextIsSymbol(String symbol) {
        return position < tokens.size() && tokens.get(position).isSymbol(symbol);
    }

    private Token next() {
        if (position >= tokens.size()) {
            throw new DbException("Запрос оборвался, не хватает продолжения");
        }
        Token token = tokens.get(position);
        position++;
        return token;
    }
}
