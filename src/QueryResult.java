import java.util.ArrayList;
import java.util.List;

/**
 * Результат запроса: сообщение и, если есть, таблица со значениями в виде текста.
 * Сам ничего не печатает, печатью занимается ResultPrinter.
 */
public class QueryResult {
    private final String message;
    private final List<String> columnNames;
    private final List<String[]> rows;

    /**
     * Результат без таблицы, только сообщение.
     */
    public QueryResult(String message) {
        this.message = message;
        this.columnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    /**
     * Результат с таблицей.
     */
    public QueryResult(String message, List<String> columnNames, List<String[]> rows) {
        this.message = message;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasTable() {
        return !columnNames.isEmpty();
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String[]> getRows() {
        return rows;
    }
}
