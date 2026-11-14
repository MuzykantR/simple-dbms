import java.io.Serializable;

/**
 * Столбец таблицы: имя, тип данных и, если это внешний ключ, ссылка на другую таблицу.
 */
public class Column implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final DataType type;
    private String referencedTable;
    private String referencedColumn;

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

    /**
     * Делает столбец внешним ключом: его значения должны быть ключами строк другой таблицы.
     */
    public void setReference(String tableName, String columnName) {
        referencedTable = tableName;
        referencedColumn = columnName;
    }

    public boolean hasReference() {
        return referencedTable != null;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }
}
