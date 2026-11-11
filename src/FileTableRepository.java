import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранит каждую таблицу в своем файле: data/имя.tbl.
 * Таблица записывается стандартной сериализацией Java.
 */
public class FileTableRepository implements TableRepository {
    private static final String EXTENSION = ".tbl";

    private final Path folder;

    public FileTableRepository(Path folder) {
        this.folder = folder;
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new DbException("Не удалось создать папку " + folder);
        }
    }

    @Override
    public List<Table> loadAll() {
        List<Table> tables = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*" + EXTENSION)) {
            for (Path file : stream) {
                try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
                    tables.add((Table) in.readObject());
                } catch (ClassNotFoundException e) {
                    throw new DbException("Не удалось прочитать файл " + file.getFileName());
                }
            }
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать таблицы из папки " + folder);
        }
        return tables;
    }

    @Override
    public void save(Table table) {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(fileOf(table.getName())))) {
            out.writeObject(table);
        } catch (IOException e) {
            throw new DbException("Не удалось сохранить таблицу " + table.getName());
        }
    }

    @Override
    public void delete(String tableName) {
        try {
            Files.deleteIfExists(fileOf(tableName));
        } catch (IOException e) {
            throw new DbException("Не удалось удалить файл таблицы " + tableName);
        }
    }

    private Path fileOf(String tableName) {
        return folder.resolve(tableName.toLowerCase() + EXTENSION);
    }
}
