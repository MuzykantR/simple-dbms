import java.util.List;

/**
 * Печатает результат запроса в консоль, таблицу рисует рамкой:
 * <pre>
 * +----+-------+
 * | id | title |
 * +----+-------+
 * | 1  | Dune  |
 * +----+-------+
 * </pre>
 */
public class ResultPrinter {

    /**
     * Печатает таблицу результата (если она есть) и сообщение.
     */
    public void print(QueryResult result) {
        if (result.hasTable()) {
            printTable(result.getColumnNames(), result.getRows());
        }
        System.out.println(result.getMessage());
    }

    private void printTable(List<String> header, List<String[]> rows) {
        int[] widths = new int[header.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = header.get(i).length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < widths.length; i++) {
                if (row[i].length() > widths[i]) {
                    widths[i] = row[i].length();
                }
            }
        }

        StringBuilder border = new StringBuilder("+");
        for (int width : widths) {
            for (int k = 0; k < width + 2; k++) {
                border.append('-');
            }
            border.append('+');
        }

        System.out.println(border);
        System.out.println(makeLine(header.toArray(new String[0]), widths));
        System.out.println(border);
        for (String[] row : rows) {
            System.out.println(makeLine(row, widths));
        }
        System.out.println(border);
    }

    private String makeLine(String[] cells, int[] widths) {
        StringBuilder line = new StringBuilder("|");
        for (int i = 0; i < cells.length; i++) {
            line.append(' ').append(cells[i]);
            for (int k = cells[i].length(); k < widths[i]; k++) {
                line.append(' ');
            }
            line.append(" |");
        }
        return line.toString();
    }
}
