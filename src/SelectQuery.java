import java.util.ArrayList;
import java.util.List;

/**
 * SELECT * FROM films
 * SELECT title, rating FROM films WHERE rating > 7.5
 */
public class SelectQuery implements Query {
    private final String tableName;
    private final List<String> columnNames;
    private final Condition where;

    /**
     * @param columnNames нужные столбцы или null для *
     * @param where условие или null
     */
    public SelectQuery(String tableName, List<String> columnNames, Condition where) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.where = where;
    }

    @Override
    public QueryResult execute(Database db) {
        Table table = db.getTable(tableName);
        TableSchema schema = table.getSchema();

        int[] selected;
        if (columnNames == null) {
            selected = new int[schema.getColumnCount()];
            for (int i = 0; i < selected.length; i++) {
                selected[i] = i;
            }
        } else {
            selected = new int[columnNames.size()];
            for (int i = 0; i < selected.length; i++) {
                selected[i] = schema.requireColumn(columnNames.get(i));
            }
        }

        if (where != null) {
            where.resolve(schema);
        }
        List<Row> found = table.findRows(where);

        List<String> header = new ArrayList<>();
        for (int index : selected) {
            header.add(schema.getColumn(index).getName());
        }
        List<String[]> lines = new ArrayList<>();
        for (Row row : found) {
            String[] cells = new String[selected.length];
            for (int i = 0; i < selected.length; i++) {
                Column column = schema.getColumn(selected[i]);
                cells[i] = column.getType().format(row.get(selected[i]));
            }
            lines.add(cells);
        }
        return new QueryResult("Найдено строк: " + found.size(), header, lines);
    }
}
