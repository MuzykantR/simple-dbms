/**
 * Проверки внешних ключей (REFERENCES).
 * Строка может ссылаться только на существующую строку другой таблицы,
 * а строку, на которую ссылаются, нельзя удалить или поменять ей ключ.
 */
public class ForeignKeys {

    private ForeignKeys() {
    }

    /**
     * При CREATE TABLE: таблица, на которую ссылаемся, существует, ссылка идет на ее первичный ключ, типы совпадают.
     */
    public static void checkSchema(Database db, TableSchema schema) {
        for (int i = 0; i < schema.getColumnCount(); i++) {
            Column column = schema.getColumn(i);
            if (column.hasReference()) {
                TableSchema target;
                if (column.getReferencedTable().equalsIgnoreCase(schema.getTableName())) {
                    target = schema;
                } else {
                    target = db.getTable(column.getReferencedTable()).getSchema();
                }
                int targetIndex = target.requireColumn(column.getReferencedColumn());
                if (targetIndex != target.getPrimaryKeyIndex()) {
                    throw new DbException("Столбец " + column.getName() + " может ссылаться только на первичный ключ таблицы "
                            + target.getTableName());
                }
                if (target.getColumn(targetIndex).getType() != column.getType()) {
                    throw new DbException("Тип столбца " + column.getName() + " не совпадает с типом "
                            + target.getTableName() + "." + column.getReferencedColumn());
                }
            }
        }
    }

    /**
     * При INSERT и UPDATE: все ссылки строки ведут на существующие строки.
     */
    public static void checkRow(Database db, TableSchema schema, Row row) {
        for (int i = 0; i < schema.getColumnCount(); i++) {
            Column column = schema.getColumn(i);
            Object value = row.get(i);
            if (column.hasReference() && value != null) {
                Table target = db.getTable(column.getReferencedTable());
                if (target.findByKey(value) == null) {
                    throw new DbException("В таблице " + target.getName() + " нет строки с "
                            + column.getReferencedColumn() + " = " + value);
                }
            }
        }
    }

    /**
     * При UPDATE: новые ссылки существуют, а если меняется ключ, на старый ключ никто не ссылается.
     */
    public static void checkUpdate(Database db, Table table, Row oldRow, Row newRow) {
        checkRow(db, table.getSchema(), newRow);
        int keyIndex = table.getSchema().getPrimaryKeyIndex();
        if (!oldRow.get(keyIndex).equals(newRow.get(keyIndex))) {
            checkNotReferenced(db, table, oldRow);
        }
    }

    /**
     * При DELETE: на строку не ссылается ни одна строка других таблиц.
     */
    public static void checkNotReferenced(Database db, Table table, Row row) {
        Object key = row.get(table.getSchema().getPrimaryKeyIndex());
        for (String name : db.getTableNames()) {
            Table other = db.getTable(name);
            TableSchema schema = other.getSchema();
            for (int i = 0; i < schema.getColumnCount(); i++) {
                Column column = schema.getColumn(i);
                if (column.hasReference() && column.getReferencedTable().equalsIgnoreCase(table.getName())) {
                    for (Row otherRow : other) {
                        if (key.equals(otherRow.get(i))) {
                            throw new DbException("На строку " + table.getName() + " с ключом " + key
                                    + " ссылается таблица " + other.getName() + ", ее нельзя удалить или поменять ей ключ");
                        }
                    }
                }
            }
        }
    }

    /**
     * При DROP TABLE: на таблицу не ссылаются другие таблицы.
     */
    public static void checkTableNotReferenced(Database db, String tableName) {
        for (String name : db.getTableNames()) {
            if (!name.equalsIgnoreCase(tableName)) {
                TableSchema schema = db.getTable(name).getSchema();
                for (int i = 0; i < schema.getColumnCount(); i++) {
                    Column column = schema.getColumn(i);
                    if (column.hasReference() && column.getReferencedTable().equalsIgnoreCase(tableName)) {
                        throw new DbException("Таблицу " + tableName + " нельзя удалить: на нее ссылается таблица " + name);
                    }
                }
            }
        }
    }
}
