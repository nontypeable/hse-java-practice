package db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Хранение в каталоге: файл {@code <таблица>.tbl} содержит схему и строки в бинарном формате
 * ({@link java.io.DataOutput}).
 *
 * <p>Файл записывается во временный {@code .tmp}, сбрасывается на диск и атомарно переименовывается,
 * поэтому после сбоя на диске остаётся либо старая, либо новая версия целиком.
 */
public final class FileStorage implements Storage {
    private static final String TABLE_MAGIC = "HSEDB-TABLE-1";
    private static final String TABLE_SUFFIX = ".tbl";

    private interface Writer {
        void write(DataOutputStream out) throws IOException;
    }

    private final Path directory;

    /**
     * Открывает каталог (создаёт, если его нет) и удаляет недописанные {@code .tmp} от прошлого сбоя.
     */
    public FileStorage(Path directory) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory);
        for (Path file : listFiles()) {
            if (file.toString().endsWith(".tmp")) {
                Files.delete(file);
            }
        }
    }

    @Override
    public List<Table> load() throws IOException {
        List<Table> tables = new ArrayList<>();
        for (Path file : listFiles()) {
            if (file.toString().endsWith(TABLE_SUFFIX)) {
                tables.add(readTable(file));
            }
        }
        return tables;
    }

    @Override
    public void save(Table table) throws IOException {
        write(tableFile(table.name()), out -> writeTable(out, table));
    }

    @Override
    public void delete(String tableName) throws IOException {
        for (Path file : listFiles()) {
            String name = file.getFileName().toString();
            if (name.equals(tableName + TABLE_SUFFIX)) {
                Files.delete(file);
            }
        }
    }

    private List<Path> listFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                files.add(file);
            }
        }
        Collections.sort(files);
        return files;
    }

    private Path tableFile(String table) {
        return directory.resolve(table + TABLE_SUFFIX);
    }

    private void write(Path target, Writer writer) throws IOException {
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try (FileOutputStream file = new FileOutputStream(temporary.toFile());
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(file))) {
            writer.write(out);
            out.flush();
            file.getFD().sync();
        }
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private void writeTable(DataOutputStream out, Table table) throws IOException {
        Schema schema = table.schema();
        out.writeUTF(TABLE_MAGIC);
        out.writeLong(table.version());

        out.writeInt(schema.columns().size());
        for (Column column : schema.columns()) {
            out.writeUTF(column.name());
            out.writeByte(column.type().ordinal());
            out.writeBoolean(column.primaryKey());
            out.writeBoolean(column.references() != null);
            if (column.references() != null) {
                out.writeUTF(column.references().table());
                out.writeUTF(column.references().column());
            }
        }

        out.writeInt(table.size());
        for (Object[] row : table) {
            for (int i = 0; i < row.length; i++) {
                schema.columns().get(i).type().write(out, row[i]);
            }
        }
    }

    private Table readTable(Path file) throws IOException {
        String tableName = file.getFileName().toString().replaceFirst("\\.tbl$", "");

        try (DataInputStream in = open(file)) {
            requireMagic(in, TABLE_MAGIC, file);
            long version = in.readLong();

            int columnCount = in.readInt();
            List<Column> columns = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                String name = in.readUTF();
                ColumnType type = ColumnType.values()[in.readByte()];
                boolean primaryKey = in.readBoolean();
                ForeignKey reference = in.readBoolean() ? new ForeignKey(in.readUTF(), in.readUTF()) : null;
                columns.add(new Column(name, type, primaryKey, reference));
            }
            Table table = new Table(tableName, new Schema(columns));

            int rowCount = in.readInt();
            for (int i = 0; i < rowCount; i++) {
                Object[] row = new Object[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    row[j] = columns.get(j).type().read(in);
                }
                table.insert(row);
            }
            table.restoreVersion(version);
            return table;
        } catch (DbException | ArrayIndexOutOfBoundsException exception) {
            throw new IOException("Повреждён файл " + file, exception);
        }
    }

    private static DataInputStream open(Path file) throws IOException {
        return new DataInputStream(new BufferedInputStream(Files.newInputStream(file)));
    }

    private static void requireMagic(DataInputStream in, String expected, Path file) throws IOException {
        if (!in.readUTF().equals(expected)) {
            throw new IOException("Неизвестный формат файла " + file);
        }
    }
}
