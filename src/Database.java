import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * База данных: все таблицы и хранилище, в которое они сохраняются.
 * При создании загружает таблицы, сохраненные в прошлый раз.
 */
public class Database {
    private final Map<String, Table> tables = new LinkedHashMap<>();
    private final TableRepository repository;

    public Database(TableRepository repository) {
        this.repository = repository;
        for (Table table : repository.loadAll()) {
            tables.put(table.getName().toLowerCase(), table);
            for (HashIndex index : repository.loadIndexes(table)) {
                if (index.getRowCount() != table.size()) {
                    index.rebuild(table);
                }
                table.addIndex(index);
            }
        }
    }

    /**
     * Таблица по имени. Если ее нет, бросает DbException.
     */
    public Table getTable(String name) {
        Table table = tables.get(name.toLowerCase());
        if (table == null) {
            throw new DbException("Таблицы " + name + " нет");
        }
        return table;
    }

    public boolean hasTable(String name) {
        return tables.containsKey(name.toLowerCase());
    }

    /**
     * Создает пустую таблицу и сразу сохраняет ее.
     */
    public void createTable(TableSchema schema) {
        if (hasTable(schema.getTableName())) {
            throw new DbException("Таблица " + schema.getTableName() + " уже есть");
        }
        Table table = new Table(schema);
        tables.put(schema.getTableName().toLowerCase(), table);
        repository.save(table);
    }

    /**
     * Удаляет таблицу вместе с ее файлом.
     */
    public void dropTable(String name) {
        Table table = getTable(name);
        tables.remove(table.getName().toLowerCase());
        repository.delete(table.getName());
    }

    /**
     * Сохраняет таблицу после изменения.
     */
    public void save(Table table) {
        repository.save(table);
        for (HashIndex index : table.getIndexes()) {
            repository.saveIndex(index);
        }
    }

    /**
     * Индекс по имени или null, если такого нет.
     */
    public HashIndex findIndex(String indexName) {
        for (Table table : tables.values()) {
            for (HashIndex index : table.getIndexes()) {
                if (index.getName().equalsIgnoreCase(indexName)) {
                    return index;
                }
            }
        }
        return null;
    }

    /**
     * Удаляет индекс из таблицы и с диска.
     */
    public void dropIndex(HashIndex index) {
        getTable(index.getTableName()).removeIndex(index);
        repository.deleteIndex(index);
    }

    public List<String> getTableNames() {
        List<String> names = new ArrayList<>();
        for (Table table : tables.values()) {
            names.add(table.getName());
        }
        return names;
    }
}
