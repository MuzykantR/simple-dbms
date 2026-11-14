/**
 * CREATE INDEX idx_films_title ON films (title)
 */
public class CreateIndexQuery implements Query {
    private final String indexName;
    private final String tableName;
    private final String columnName;

    public CreateIndexQuery(String indexName, String tableName, String columnName) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    @Override
    public QueryResult execute(Database db) {
        Table table = db.getTable(tableName);
        if (table.getIndex(columnName) != null) {
            throw new DbException("По столбцу " + columnName + " индекс уже есть");
        }
        if (db.findIndex(indexName) != null) {
            throw new DbException("Индекс с именем " + indexName + " уже есть");
        }
        HashIndex index = new HashIndex(indexName, table.getSchema(), columnName);
        index.rebuild(table);
        table.addIndex(index);
        db.save(table);
        return new QueryResult("Индекс " + indexName + " создан, разных значений: " + index.getValueCount());
    }
}
