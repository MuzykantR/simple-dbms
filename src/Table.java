import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Таблица: структура и строки.
 * Строки лежат в словаре по значению первичного ключа, поэтому
 * найти строку по ключу можно без перебора всей таблицы.
 */
public class Table implements Iterable<Row>, Serializable {
    private static final long serialVersionUID = 1L;

    private final TableSchema schema;
    private final Map<Object, Row> rows = new LinkedHashMap<>();
    private transient Map<String, HashIndex> indexes = new HashMap<>();

    public Table(TableSchema schema) {
        this.schema = schema;
    }

    public TableSchema getSchema() {
        return schema;
    }

    public String getName() {
        return schema.getTableName();
    }

    public int size() {
        return rows.size();
    }

    /**
     * Добавляет строку. Первичный ключ должен быть заполнен и не должен повторяться.
     */
    public void insert(Row row) {
        Object key = row.get(schema.getPrimaryKeyIndex());
        checkKey(key);
        if (rows.containsKey(key)) {
            throw new DbException("В таблице " + getName() + " уже есть строка с ключом " + key);
        }
        rows.put(key, row);
        for (HashIndex index : indexes.values()) {
            index.add(row);
        }
    }

    /**
     * Заменяет строку oldRow на newRow. Ключ при этом может поменяться.
     */
    public void update(Row oldRow, Row newRow) {
        Object oldKey = oldRow.get(schema.getPrimaryKeyIndex());
        Object newKey = newRow.get(schema.getPrimaryKeyIndex());
        checkKey(newKey);
        if (newKey.equals(oldKey)) {
            rows.put(newKey, newRow);
        } else {
            if (rows.containsKey(newKey)) {
                throw new DbException("В таблице " + getName() + " уже есть строка с ключом " + newKey);
            }
            rows.remove(oldKey);
            rows.put(newKey, newRow);
        }
        for (HashIndex index : indexes.values()) {
            index.update(oldRow, newRow);
        }
    }

    /**
     * Удаляет строку.
     */
    public void delete(Row row) {
        Row removed = rows.remove(row.get(schema.getPrimaryKeyIndex()));
        if (removed != null) {
            for (HashIndex index : indexes.values()) {
                index.remove(removed);
            }
        }
    }

    /**
     * Строка с таким первичным ключом или null.
     */
    public Row findByKey(Object key) {
        return rows.get(key);
    }

    /**
     * Строки, подходящие под условие. Если условия нет, возвращает все строки.
     * Условие "ключ = значение" выполняется сразу через словарь, без перебора.
     * Условие "столбец = значение" по столбцу с индексом выполняется через индекс.
     *
     * @param condition условие, уже привязанное к этой таблице, или null
     */
    public List<Row> findRows(Condition condition) {
        List<Row> result = new ArrayList<>();
        if (condition == null) {
            result.addAll(rows.values());
            return result;
        }
        if (condition.getColumnIndex() == schema.getPrimaryKeyIndex() && condition.getOperator().equals("=")) {
            Row row = rows.get(condition.getValue());
            if (row != null) {
                result.add(row);
            }
            return result;
        }
        HashIndex index = indexes.get(condition.getColumnName().toLowerCase());
        if (index != null && condition.getOperator().equals("=") && condition.getValue() != null) {
            for (Object key : index.find(condition.getValue())) {
                result.add(rows.get(key));
            }
            return result;
        }
        for (Row row : rows.values()) {
            if (condition.matches(row)) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * Добавляет индекс к таблице.
     */
    public void addIndex(HashIndex index) {
        indexes.put(index.getColumnName().toLowerCase(), index);
    }

    public void removeIndex(HashIndex index) {
        indexes.remove(index.getColumnName().toLowerCase());
    }

    /**
     * Индекс по столбцу или null, если его нет.
     */
    public HashIndex getIndex(String columnName) {
        return indexes.get(columnName.toLowerCase());
    }

    public List<HashIndex> getIndexes() {
        return new ArrayList<>(indexes.values());
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.values().iterator();
    }

    /**
     * Индексы хранятся в своих файлах, а не внутри таблицы,
     * поэтому после загрузки таблицы список индексов создается пустым.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        indexes = new HashMap<>();
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new DbException("Первичный ключ " + schema.getPrimaryKey().getName() + " не может быть NULL");
        }
    }
}
