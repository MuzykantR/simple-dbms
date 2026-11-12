import java.util.List;

/**
 * CREATE TABLE films (id INT PRIMARY KEY, title STRING, ...)
 */
public class CreateTableQuery implements Query {
    private final String tableName;
    private final List<Column> columns;
    private final int primaryKeyIndex;

    public CreateTableQuery(String tableName, List<Column> columns, int primaryKeyIndex) {
        this.tableName = tableName;
        this.columns = columns;
        this.primaryKeyIndex = primaryKeyIndex;
    }

    @Override
    public QueryResult execute(Database db) {
        TableSchema schema = new TableSchema(tableName, columns, primaryKeyIndex);
        db.createTable(schema);
        return new QueryResult("Таблица " + tableName + " создана, первичный ключ " + schema.getPrimaryKey().getName());
    }
}
