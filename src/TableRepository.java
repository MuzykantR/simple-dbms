import java.util.List;

/**
 * Хранилище таблиц (паттерн Repository).
 * Остальной программе не важно, где и в каком виде лежат таблицы, она работает только через эти методы.
 */
public interface TableRepository {

    /**
     * Загружает все сохраненные таблицы.
     */
    List<Table> loadAll();

    /**
     * Сохраняет таблицу целиком.
     */
    void save(Table table);

    /**
     * Удаляет сохраненную таблицу.
     */
    void delete(String tableName);
}
