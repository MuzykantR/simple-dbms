import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Консоль базы данных: читает запросы по одному на строку и печатает результат.
 */
public class Main {
    private static final String HELP = "help - подсказка, exit - выход";

    public static void main(String[] args) {
        System.out.println("help - список команд, exit - выход");
        Scanner in = new Scanner(System.in);
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
            } else if (!line.isEmpty()) {
                System.out.println("Неизвестная команда: " + line);
            }
        }
    }
}
