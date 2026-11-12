/**
 * Запрос к базе (паттерн Command): каждый вид запроса сам знает, как выполниться.
 */
public interface Query {

    /**
     * Выполняет запрос.
     *
     * @param db база, с которой работает запрос
     * @return результат: сообщение и, для SELECT, найденные строки
     */
    QueryResult execute(Database db);
}
