import java.util.List;

/**
 * DELETE FROM films WHERE genre = 'horror'
 * Без WHERE удаляет все строки, но саму таблицу оставляет.
 */
public class DeleteQuery implements Query {
    private final String tableName;
    private final Condition where;

    public DeleteQuery(String tableName, Condition where) {
        this.tableName = tableName;
        this.where = where;
    }

    @Override
    public QueryResult execute(Database db) {
        Table table = db.getTable(tableName);
        if (where != null) {
            where.resolve(table.getSchema());
        }
        List<Row> found = table.findRows(where);
        for (Row row : found) {
            ForeignKeys.checkNotReferenced(db, table, row);
        }

        int deleted = 0;
        try {
            for (Row row : found) {
                table.delete(row);
                deleted++;
            }
        } finally {
            db.save(table);
        }
        return new QueryResult("Удалено строк: " + deleted);
    }
}
