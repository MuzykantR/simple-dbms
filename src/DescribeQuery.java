import java.util.ArrayList;
import java.util.List;

/**
 * DESCRIBE films - столбцы таблицы: имя, тип, первичный ключ, ссылка на другую таблицу.
 */
public class DescribeQuery implements Query {
    private final String tableName;

    public DescribeQuery(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public QueryResult execute(Database db) {
        TableSchema schema = db.getTable(tableName).getSchema();

        List<String> header = new ArrayList<>();
        header.add("column");
        header.add("type");
        header.add("key");
        header.add("references");

        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < schema.getColumnCount(); i++) {
            Column column = schema.getColumn(i);
            String[] line = new String[4];
            line[0] = column.getName();
            line[1] = column.getType().toString();
            if (i == schema.getPrimaryKeyIndex()) {
                line[2] = "PRIMARY KEY";
            } else {
                line[2] = "";
            }
            if (column.hasReference()) {
                line[3] = column.getReferencedTable() + "(" + column.getReferencedColumn() + ")";
            } else {
                line[3] = "";
            }
            lines.add(line);
        }
        return new QueryResult("Столбцов: " + lines.size(), header, lines);
    }
}
