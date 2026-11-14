/**
 * DROP INDEX idx_films_title
 */
public class DropIndexQuery implements Query {
    private final String indexName;

    public DropIndexQuery(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public QueryResult execute(Database db) {
        HashIndex index = db.findIndex(indexName);
        if (index == null) {
            throw new DbException("Индекса " + indexName + " нет");
        }
        db.dropIndex(index);
        return new QueryResult("Индекс " + indexName + " удален");
    }
}
