import java.util.ArrayList;
import java.util.List;

/**
 * SHOW TABLES - список таблиц с числом строк и столбцов.
 */
public class ShowTablesQuery implements Query {

    @Override
    public QueryResult execute(Database db) {
        List<String> header = new ArrayList<>();
        header.add("table");
        header.add("rows");
        header.add("columns");

        List<String[]> lines = new ArrayList<>();
        for (String name : db.getTableNames()) {
            Table table = db.getTable(name);
            String[] line = new String[3];
            line[0] = table.getName();
            line[1] = String.valueOf(table.size());
            line[2] = String.valueOf(table.getSchema().getColumnCount());
            lines.add(line);
        }
        return new QueryResult("Таблиц: " + lines.size(), header, lines);
    }
}
