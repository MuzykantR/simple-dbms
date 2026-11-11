import java.io.Serializable;

/**
 * Столбец таблицы: имя и тип данных.
 */
public class Column implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final DataType type;

    public Column(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }
}
