/**
 * DROP TABLE films
 */
public class DropTableQuery implements Query {
    private final String tableName;

    public DropTableQuery(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public QueryResult execute(Database db) {
        ForeignKeys.checkTableNotReferenced(db, tableName);
        db.dropTable(tableName);
        return new QueryResult("Таблица " + tableName + " удалена");
    }
}
