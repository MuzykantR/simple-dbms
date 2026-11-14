import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Консоль базы данных: читает запросы по одному на строку и печатает результат.
 */
public class Main {
    private static final String HELP =
            "Запросы (один запрос на строку, ; в конце можно не ставить):\n" +
            "  CREATE TABLE films (id INT PRIMARY KEY, title STRING, rating DOUBLE, premiere DATE, in_release BOOLEAN)\n" +
            "  DROP TABLE films\n" +
            "  INSERT INTO films VALUES (1, 'Dune', 8.0, '2021-10-21', FALSE), (2, 'Barbie', 6.8, '2023-07-20', TRUE)\n" +
            "  INSERT INTO films (id, title) VALUES (3, 'Up')\n" +
            "  SELECT * FROM films\n" +
            "  SELECT title, rating FROM films WHERE rating > 7\n" +
            "  UPDATE films SET rating = 8.2 WHERE id = 1\n" +
            "  DELETE FROM films WHERE title = 'Barbie'\n" +
            "Типы: INT, DOUBLE, STRING, DATE ('ГГГГ-ММ-ДД'), BOOLEAN (TRUE/FALSE)\n" +
            "Операторы в WHERE: = != < > <= >=\n" +
            "help - эта подсказка, exit - выход";

    public static void main(String[] args) {
        Database db;
        FileTableRepository repository;
        try {
            repository = new FileTableRepository(Paths.get("data"));
            db = new Database(repository);
        } catch (DbException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return;
        }
        for (String warning : repository.getWarnings()) {
            System.out.println("Внимание: " + warning);
        }
        System.out.println("Загружено таблиц: " + db.getTableNames().size() + ". help - список команд, exit - выход");

        Scanner in = new Scanner(System.in);
        ResultPrinter printer = new ResultPrinter();
        boolean running = true;
        while (running) {
            System.out.print("db> ");
            String line;
            try {
                line = in.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }
            if (line.equalsIgnoreCase("exit")) {
                running = false;
            } else if (line.equalsIgnoreCase("help")) {
                System.out.println(HELP);
            } else if (line.toUpperCase().startsWith("RUN ")) {
                runScript(db, printer, line.substring(4).trim());
            } else if (!line.isEmpty()) {
                execute(db, printer, line);
            }
        }
    }

    private static void execute(Database db, ResultPrinter printer, String line) {
        try {
            Query query = new QueryParser(line).parse();
            printer.print(query.execute(db));
        } catch (DbException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void runScript(Database db, ResultPrinter printer, String fileName) {
        if (fileName.length() >= 2 && fileName.startsWith("'") && fileName.endsWith("'")) {
            fileName = fileName.substring(1, fileName.length() - 1);
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException | InvalidPathException e) {
            System.out.println("Ошибка: не удалось прочитать файл " + fileName);
            return;
        }
        for (String scriptLine : lines) {
            String text = scriptLine.trim();
            if (!text.isEmpty() && !text.startsWith("--")) {
                System.out.println("db> " + text);
                execute(db, printer, text);
            }
        }
    }
}
