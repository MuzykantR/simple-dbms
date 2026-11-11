/**
 * Ошибка в запросе или при работе с таблицами.
 * Текст ошибки показывается пользователю, поэтому он должен быть понятным.
 */
public class DbException extends RuntimeException {

    public DbException(String message) {
        super(message);
    }
}
