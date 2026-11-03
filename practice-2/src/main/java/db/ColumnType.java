package db;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Поддерживаемые типы данных. В памяти значения хранятся как
 * {@link Long}, {@link Double}, {@link String}, {@link LocalDate} и {@link Boolean}.
 */
public enum ColumnType {
    INT, DOUBLE, STRING, DATE, BOOLEAN;

    /**
     * Находит тип по SQL-имени (регистр не важен), например {@code INT} или {@code VARCHAR}.
     */
    public static ColumnType fromSql(String name) {
        return switch (name.toUpperCase()) {
            case "INT", "INTEGER" -> INT;
            case "DOUBLE", "FLOAT" -> DOUBLE;
            case "STRING", "VARCHAR", "TEXT" -> STRING;
            case "DATE" -> DATE;
            case "BOOLEAN", "BOOL" -> BOOLEAN;
            default -> throw new DbException("Неизвестный тип данных: " + name);
        };
    }

    /**
     * Приводит литерал из запроса к типу колонки.
     * Целое число допустимо для DOUBLE, строка вида {@code 2024-05-17} — для DATE.
     *
     * @return значение нужного Java-типа; {@code null} остаётся {@code null}
     * @throws DbException если значение не подходит к типу
     */
    public Object coerce(Object value) {
        if (value == null) {
            return null;
        }

        if (this == INT && value instanceof Long) {
            return value;
        }
        if (this == DOUBLE && (value instanceof Long || value instanceof Double)) {
            return ((Number) value).doubleValue();
        }
        if (this == STRING && value instanceof String) {
            return value;
        }
        if (this == DATE && value instanceof LocalDate) {
            return value;
        }
        if (this == DATE && value instanceof String) {
            return parseDate((String) value);
        }
        if (this == BOOLEAN && value instanceof Boolean) {
            return value;
        }
        throw new DbException("Значение " + value + " не подходит для типа " + this);
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeException exception) {
            throw new DbException("Дата должна быть в формате ГГГГ-ММ-ДД: " + text);
        }
    }

    /**
     * Записывает значение (или признак {@code null}) в бинарный поток.
     */
    public void write(DataOutput out, Object value) throws IOException {
        out.writeBoolean(value == null);
        if (value == null) {
            return;
        }

        switch (this) {
            case INT -> out.writeLong((Long) value);
            case DOUBLE -> out.writeDouble((Double) value);
            case STRING -> out.writeUTF((String) value);
            case DATE -> out.writeLong(((LocalDate) value).toEpochDay());
            case BOOLEAN -> out.writeBoolean((Boolean) value);
        }
    }

    /**
     * Читает значение, записанное методом {@link #write}.
     */
    public Object read(DataInput in) throws IOException {
        if (in.readBoolean()) {
            return null;
        }

        return switch (this) {
            case INT -> in.readLong();
            case DOUBLE -> in.readDouble();
            case STRING -> in.readUTF();
            case DATE -> LocalDate.ofEpochDay(in.readLong());
            case BOOLEAN -> in.readBoolean();
        };
    }
}
