import java.io.Serializable;

/**
 * Одна строка таблицы: значения по порядку столбцов.
 */
public class Row implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object[] values;

    public Row(int size) {
        values = new Object[size];
    }

    public Object get(int index) {
        return values[index];
    }

    public void set(int index, Object value) {
        values[index] = value;
    }

    public int size() {
        return values.length;
    }

    /**
     * Копия строки, чтобы изменить ее, не трогая оригинал.
     */
    public Row copy() {
        Row result = new Row(values.length);
        for (int i = 0; i < values.length; i++) {
            result.values[i] = values[i];
        }
        return result;
    }
}
