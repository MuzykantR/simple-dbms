import java.io.Serializable;
import java.util.ArrayList;
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
    }

    /**
     * Удаляет строку.
     */
    public void delete(Row row) {
        rows.remove(row.get(schema.getPrimaryKeyIndex()));
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
        for (Row row : rows.values()) {
            if (condition.matches(row)) {
                result.add(row);
            }
        }
        return result;
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.values().iterator();
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new DbException("Первичный ключ " + schema.getPrimaryKey().getName() + " не может быть NULL");
        }
    }
}
