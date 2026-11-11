import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Структура таблицы: имя, столбцы и номер столбца с первичным ключом.
 */
public class TableSchema implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tableName;
    private final List<Column> columns;
    private final int primaryKeyIndex;

    public TableSchema(String tableName, List<Column> columns, int primaryKeyIndex) {
        if (columns.isEmpty()) {
            throw new DbException("В таблице должен быть хотя бы один столбец");
        }
        for (int i = 0; i < columns.size(); i++) {
            for (int j = i + 1; j < columns.size(); j++) {
                if (columns.get(i).getName().equalsIgnoreCase(columns.get(j).getName())) {
                    throw new DbException("Столбец " + columns.get(i).getName() + " указан дважды");
                }
            }
        }
        this.tableName = tableName;
        this.columns = new ArrayList<>(columns);
        this.primaryKeyIndex = primaryKeyIndex;
    }

    public String getTableName() {
        return tableName;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public int getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    public Column getPrimaryKey() {
        return columns.get(primaryKeyIndex);
    }

    /**
     * Номер столбца по имени (без учета регистра) или -1, если такого нет.
     */
    public int indexOf(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Номер столбца по имени. Если столбца нет, бросает DbException.
     */
    public int requireColumn(String columnName) {
        int index = indexOf(columnName);
        if (index < 0) {
            throw new DbException("В таблице " + tableName + " нет столбца " + columnName);
        }
        return index;
    }
}
