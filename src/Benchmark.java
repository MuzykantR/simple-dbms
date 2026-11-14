import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Замер скорости SELECT с условием "столбец = значение" без индекса и с индексом.
 * Запуск: java Benchmark
 */
public class Benchmark {
    private static final String[] GENRES = {"drama", "comedy", "action", "horror", "cartoon", "thriller", "romance", "sci-fi"};

    public static void main(String[] args) {
        int[] sizes = {1000, 10000, 100000};
        System.out.println("Время одного поиска в микросекундах");
        System.out.println();
        System.out.println("| строк | столбец | найдено | без индекса | с индексом | быстрее в |");
        System.out.println("|-|-|-|-|-|-|");
        for (int size : sizes) {
            Table table = makeTable(size);
            measure(table, "title", "Film 7");
            measure(table, "genre", "drama");
        }
    }

    private static Table makeTable(int size) {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INT));
        columns.add(new Column("title", DataType.STRING));
        columns.add(new Column("genre", DataType.STRING));
        columns.add(new Column("rating", DataType.DOUBLE));
        Table table = new Table(new TableSchema("films", columns, 0));

        Random random = new Random(42);
        for (int i = 0; i < size; i++) {
            Row row = new Row(4);
            row.set(0, i);
            row.set(1, "Film " + (i % 1000));
            row.set(2, GENRES[random.nextInt(GENRES.length)]);
            row.set(3, random.nextInt(100) / 10.0);
            table.insert(row);
        }
        return table;
    }

    private static void measure(Table table, String columnName, String value) {
        Condition condition = new Condition(columnName, "=", value);
        condition.resolve(table.getSchema());
        int repeats = 500;

        for (int i = 0; i < 300; i++) {
            table.findRows(condition);
        }
        long start = System.nanoTime();
        int found = 0;
        for (int i = 0; i < repeats; i++) {
            found = table.findRows(condition).size();
        }
        double withoutIndex = (System.nanoTime() - start) / 1000.0 / repeats;

        HashIndex index = new HashIndex("idx_bench", table.getSchema(), columnName);
        index.rebuild(table);
        table.addIndex(index);
        for (int i = 0; i < 300; i++) {
            table.findRows(condition);
        }
        start = System.nanoTime();
        for (int i = 0; i < repeats; i++) {
            table.findRows(condition);
        }
        double withIndex = (System.nanoTime() - start) / 1000.0 / repeats;
        table.removeIndex(index);

        double speedup = withoutIndex / withIndex;
        System.out.printf("| %d | %s | %d | %.1f | %.1f | %.1f |%n",
                table.size(), columnName, found, withoutIndex, withIndex, speedup);
    }
}
