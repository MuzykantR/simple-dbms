import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * INSERT INTO films VALUES (...), (...)
 * INSERT INTO films (id, title) VALUES (...)
 * Сначала проверяются все строки, и только потом они добавляются,
 * чтобы при ошибке во второй строке не осталась добавленной первая.
 */
public class InsertQuery implements Query {
    private final String tableName;
    private final List<String> columnNames;
    private final List<List<Object>> valueRows;

    /**
     * @param columnNames столбцы из запроса или null, если они не указаны (значит, все по порядку)
     */
    public InsertQuery(String tableName, List<String> columnNames, List<List<Object>> valueRows) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.valueRows = valueRows;
    }

    @Override
    public QueryResult execute(Database db) {
        Table table = db.getTable(tableName);
        TableSchema schema = table.getSchema();

        int[] targets;
        if (columnNames == null) {
            targets = new int[schema.getColumnCount()];
            for (int i = 0; i < targets.length; i++) {
                targets[i] = i;
            }
        } else {
            targets = new int[columnNames.size()];
            for (int i = 0; i < targets.length; i++) {
                targets[i] = schema.requireColumn(columnNames.get(i));
                for (int j = 0; j < i; j++) {
                    if (targets[j] == targets[i]) {
                        throw new DbException("Столбец " + columnNames.get(i) + " указан дважды");
                    }
                }
            }
        }

        List<Row> newRows = new ArrayList<>();
        for (List<Object> values : valueRows) {
            if (values.size() != targets.length) {
                throw new DbException("Нужно " + targets.length + " значений, а передано " + values.size());
            }
            Row row = new Row(schema.getColumnCount());
            for (int i = 0; i < targets.length; i++) {
                Column column = schema.getColumn(targets[i]);
                try {
                    row.set(targets[i], column.getType().convert(values.get(i)));
                } catch (DbException e) {
                    throw new DbException("Столбец " + column.getName() + ": " + e.getMessage());
                }
            }
            newRows.add(row);
        }

        Set<Object> newKeys = new HashSet<>();
        for (Row row : newRows) {
            Object key = row.get(schema.getPrimaryKeyIndex());
            if (key == null) {
                throw new DbException("Не заполнен первичный ключ " + schema.getPrimaryKey().getName());
            }
            if (table.findByKey(key) != null || !newKeys.add(key)) {
                throw new DbException("Строка с ключом " + key + " уже есть, ничего не добавлено");
            }
        }

        for (Row row : newRows) {
            table.insert(row);
        }
        db.save(table);
        return new QueryResult("Добавлено строк: " + newRows.size());
    }
}
