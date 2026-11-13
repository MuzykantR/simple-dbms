/**
 * Условие из WHERE: столбец, оператор и значение, например price > 20000.
 */
public class Condition {
    private final String columnName;
    private final String operator;
    private final Object rawValue;

    private int columnIndex = -1;
    private DataType type;
    private Object value;

    public Condition(String columnName, String operator, Object rawValue) {
        this.columnName = columnName;
        this.operator = operator;
        this.rawValue = rawValue;
    }

    /**
     * Привязывает условие к таблице: находит столбец и приводит значение к его типу.
     * Вызывается перед проверкой строк.
     */
    public void resolve(TableSchema schema) {
        columnIndex = schema.requireColumn(columnName);
        type = schema.getColumn(columnIndex).getType();
        value = type.convert(rawValue);
    }

    /**
     * Подходит ли строка под условие.
     */
    public boolean matches(Row row) {
        Object cell = row.get(columnIndex);
        if (cell == null || value == null) {
            if (operator.equals("=")) {
                return cell == null && value == null;
            }
            if (operator.equals("!=")) {
                return !(cell == null && value == null);
            }
            return false;
        }
        int result = type.compare(cell, value);
        switch (operator) {
            case "=":
                return result == 0;
            case "!=":
                return result != 0;
            case "<":
                return result < 0;
            case ">":
                return result > 0;
            case "<=":
                return result <= 0;
            case ">=":
                return result >= 0;
            default:
                throw new DbException("Неизвестный оператор " + operator);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
