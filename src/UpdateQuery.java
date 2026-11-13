import java.util.List;

/**
 * UPDATE films SET rating = 8.5, in_release = TRUE WHERE id = 2
 */
public class UpdateQuery implements Query {
    private final String tableName;
    private final List<String> columnNames;
    private final List<Object> values;
    private final Condition where;

    public UpdateQuery(String tableName, List<String> columnNames, List<Object> values, Condition where) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.values = values;
        this.where = where;
    }

    @Override
    public QueryResult execute(Database db) {
        Table table = db.getTable(tableName);
        TableSchema schema = table.getSchema();

        int[] targets = new int[columnNames.size()];
        Object[] newValues = new Object[columnNames.size()];
        for (int i = 0; i < targets.length; i++) {
            targets[i] = schema.requireColumn(columnNames.get(i));
            Column column = schema.getColumn(targets[i]);
            try {
                newValues[i] = column.getType().convert(values.get(i));
            } catch (DbException e) {
                throw new DbException("Столбец " + column.getName() + ": " + e.getMessage());
            }
        }

        if (where != null) {
            where.resolve(schema);
        }
        List<Row> found = table.findRows(where);

        int changed = 0;
        try {
            for (Row row : found) {
                Row newRow = row.copy();
                for (int i = 0; i < targets.length; i++) {
                    newRow.set(targets[i], newValues[i]);
                }
                table.update(row, newRow);
                changed++;
            }
        } finally {
            db.save(table);
        }
        return new QueryResult("Изменено строк: " + changed);
    }
}
