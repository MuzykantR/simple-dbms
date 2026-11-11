import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Типы данных столбцов: целое число, дробное число, строка, дата и логическое значение.
 */
public enum DataType {
    INT, DOUBLE, STRING, DATE, BOOLEAN;

    /**
     * Находит тип по имени из запроса CREATE TABLE.
     *
     * @param name имя типа, например INT или VARCHAR
     * @return тип данных
     */
    public static DataType fromName(String name) {
        switch (name.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return INT;
            case "DOUBLE":
            case "FLOAT":
            case "REAL":
                return DOUBLE;
            case "STRING":
            case "TEXT":
            case "VARCHAR":
                return STRING;
            case "DATE":
                return DATE;
            case "BOOLEAN":
            case "BOOL":
                return BOOLEAN;
            default:
                throw new DbException("Неизвестный тип данных: " + name);
        }
    }

    /**
     * Приводит значение из запроса к этому типу.
     * Парсер отдает Long, Double, String, Boolean или null (для NULL).
     *
     * @param value значение из запроса
     * @return значение нужного типа или null
     */
    public Object convert(Object value) {
        if (value == null) {
            return null;
        }
        switch (this) {
            case INT:
                if (value instanceof Long) {
                    long number = (Long) value;
                    if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
                        throw new DbException("Число " + number + " слишком большое для INT");
                    }
                    return (int) number;
                }
                break;
            case DOUBLE:
                if (value instanceof Long) {
                    return ((Long) value).doubleValue();
                }
                if (value instanceof Double) {
                    return value;
                }
                break;
            case STRING:
                if (value instanceof String) {
                    return value;
                }
                break;
            case DATE:
                if (value instanceof String) {
                    try {
                        return LocalDate.parse((String) value);
                    } catch (DateTimeParseException e) {
                        throw new DbException("Дата должна быть в формате 'ГГГГ-ММ-ДД', а не '" + value + "'");
                    }
                }
                break;
            case BOOLEAN:
                if (value instanceof Boolean) {
                    return value;
                }
                break;
        }
        throw new DbException("Значение " + value + " не подходит для типа " + this);
    }

    /**
     * Сравнивает два значения этого типа (оба не null).
     *
     * @return отрицательное число, ноль или положительное, как у compareTo
     */
    public int compare(Object a, Object b) {
        switch (this) {
            case INT:
                return ((Integer) a).compareTo((Integer) b);
            case DOUBLE:
                return ((Double) a).compareTo((Double) b);
            case STRING:
                return ((String) a).compareTo((String) b);
            case DATE:
                return ((LocalDate) a).compareTo((LocalDate) b);
            default:
                return ((Boolean) a).compareTo((Boolean) b);
        }
    }

    /**
     * Значение в виде текста для вывода на экран.
     */
    public String format(Object value) {
        if (value == null) {
            return "NULL";
        }
        return value.toString();
    }
}
