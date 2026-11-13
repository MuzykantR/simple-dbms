import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранит каждую таблицу в своем файле: data/имя.tbl.
 * Таблица записывается стандартной сериализацией Java.
 * Чтобы файл не испортился, если программу закроют во время записи,
 * сначала пишется временный файл .tmp, а потом он заменяет старый.
 */
public class FileTableRepository implements TableRepository {
    private static final String EXTENSION = ".tbl";
    private static final String TEMP_EXTENSION = ".tmp";

    private final Path folder;
    private final List<String> warnings = new ArrayList<>();

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
        warnings.clear();
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {
                files.add(file);
            }
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать папку " + folder);
        }

        List<Table> tables = new ArrayList<>();
        for (Path file : files) {
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(TEMP_EXTENSION)) {
                deleteQuietly(file);
                warnings.add("Файл " + fileName + " не был дописан (программа закрылась во время сохранения), "
                        + "оставлена прошлая версия таблицы");
            } else if (fileName.endsWith(EXTENSION)) {
                try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
                    tables.add((Table) in.readObject());
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    warnings.add("Файл " + fileName + " поврежден, таблица не загружена");
                }
            }
        }
        return tables;
    }

    @Override
    public void save(Table table) {
        Path file = fileOf(table.getName());
        Path temp = folder.resolve(table.getName().toLowerCase() + EXTENSION + TEMP_EXTENSION);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(table);
        } catch (IOException e) {
            throw new DbException("Не удалось сохранить таблицу " + table.getName());
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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

    /**
     * Что случилось при последней загрузке: недописанные и поврежденные файлы.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    private Path fileOf(String tableName) {
        return folder.resolve(tableName.toLowerCase() + EXTENSION);
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            warnings.add("Не удалось удалить файл " + file.getFileName());
        }
    }
}
