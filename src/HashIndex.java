import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Хеш-индекс по одному столбцу.
 * Для каждого значения столбца хранит первичные ключи строк, в которых оно встречается,
 * поэтому условие "столбец = значение" находит строки без перебора таблицы.
 */
public class HashIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String tableName;
    private final String columnName;
    private final int columnIndex;
    private final int keyIndex;
    private final Map<Object, Set<Object>> keysByValue = new HashMap<>();
    private int rowCount;

    public HashIndex(String name, TableSchema schema, String columnName) {
        this.name = name;
        this.tableName = schema.getTableName();
        this.columnIndex = schema.requireColumn(columnName);
        this.columnName = schema.getColumn(columnIndex).getName();
        this.keyIndex = schema.getPrimaryKeyIndex();
    }

    /**
     * Добавляет строку в индекс.
     */
    public void add(Row row) {
        rowCount++;
        Object value = row.get(columnIndex);
        if (value == null) {
            return;
        }
        Set<Object> keys = keysByValue.get(value);
        if (keys == null) {
            keys = new LinkedHashSet<>();
            keysByValue.put(value, keys);
        }
        keys.add(row.get(keyIndex));
    }

    /**
     * Убирает строку из индекса.
     */
    public void remove(Row row) {
        rowCount--;
        Object value = row.get(columnIndex);
        if (value == null) {
            return;
        }
        Set<Object> keys = keysByValue.get(value);
        if (keys != null) {
            keys.remove(row.get(keyIndex));
            if (keys.isEmpty()) {
                keysByValue.remove(value);
            }
        }
    }

    /**
     * Обновляет строку после UPDATE. Если ни значение, ни ключ не поменялись, индекс не трогается.
     */
    public void update(Row oldRow, Row newRow) {
        Object oldValue = oldRow.get(columnIndex);
        Object newValue = newRow.get(columnIndex);
        boolean sameValue;
        if (oldValue == null) {
            sameValue = newValue == null;
        } else {
            sameValue = oldValue.equals(newValue);
        }
        boolean sameKey = oldRow.get(keyIndex).equals(newRow.get(keyIndex));
        if (sameValue && sameKey) {
            return;
        }
        remove(oldRow);
        add(newRow);
    }

    /**
     * Ключи строк, в которых столбец равен value.
     */
    public List<Object> find(Object value) {
        Set<Object> keys = keysByValue.get(value);
        if (keys == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(keys);
    }

    /**
     * Строит индекс заново по всем строкам таблицы.
     */
    public void rebuild(Table table) {
        keysByValue.clear();
        rowCount = 0;
        for (Row row : table) {
            add(row);
        }
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    /**
     * Сколько строк в индексе. Если не совпадает с размером таблицы, индекс устарел.
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Сколько разных значений в индексе.
     */
    public int getValueCount() {
        return keysByValue.size();
    }
}
