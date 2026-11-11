package db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тестирование разбора SQL-запросов в объекты {@link Statement} и {@link Condition}.
 */
class ParserTest {
    /**
     * Проверка: INSERT со всеми колонками и несколькими кортежами.
     */
    @DisplayName("INSERT со всеми колонками и несколькими кортежами")
    @Test
    void parsesInsert() {
        Statement.Insert insert = (Statement.Insert) Parser.parse(
            "INSERT INTO cars VALUES (1, 'Toyota', 25000.0, TRUE), (2, 'O''Neil', -5, NULL);");

        assertEquals("cars", insert.table());
        assertNull(insert.columns());
        assertEquals(Arrays.asList(1L, "Toyota", 25000.0, true), insert.rows().get(0));
        assertEquals(Arrays.asList(2L, "O'Neil", -5L, null), insert.rows().get(1));
    }

    /**
     * Проверка: INSERT со списком колонок.
     */
    @DisplayName("INSERT со списком колонок")
    @Test
    void parsesInsertWithColumns() {
        Statement.Insert insert = (Statement.Insert) Parser.parse("insert into Cars (id, brand) values (2, 'BMW')");

        assertEquals("cars", insert.table());
        assertEquals(List.of("id", "brand"), insert.columns());
    }

    /**
     * Проверка: SELECT, UPDATE и DELETE с условием.
     */
    @DisplayName("SELECT, UPDATE и DELETE с условием")
    @Test
    void parsesQueries() {
        Statement.Select select = (Statement.Select) Parser.parse("SELECT brand, price FROM cars WHERE price > 20000;");
        assertEquals(List.of("brand", "price"), select.columns());
        assertEquals(new Condition.Comparison("price", Operator.GT, 20000L), select.where());

        Statement.Update update = (Statement.Update) Parser.parse("UPDATE cars SET price = 30000 WHERE id = 2;");
        assertEquals(30000L, update.assignments().get("price"));

        Statement.Delete delete = (Statement.Delete) Parser.parse("DELETE FROM cars WHERE brand = 'Toyota';");
        assertEquals(new Condition.Comparison("brand", Operator.EQ, "Toyota"), delete.where());
    }

    /**
     * Проверка: AND сильнее OR.
     */
    @DisplayName("AND сильнее OR")
    @Test
    void andBindsStrongerThanOr() {
        Statement.Select select = (Statement.Select) Parser.parse("SELECT * FROM t WHERE a = 1 OR b = 2 AND c = 3");

        Condition.Or or = assertInstanceOf(Condition.Or.class, select.where());
        assertInstanceOf(Condition.And.class, or.right());
    }

    /**
     * Проверка: CREATE TABLE и DROP TABLE.
     */
    @DisplayName("CREATE TABLE и DROP TABLE")
    @Test
    void parsesDdl() {
        Statement.CreateTable create = (Statement.CreateTable) Parser.parse(
            "CREATE TABLE owners (id INT PRIMARY KEY, car_id INT REFERENCES cars(id), name STRING)");
        assertEquals(new ForeignKey("cars", "id"), create.columns().get(1).references());
        assertEquals(true, create.columns().get(0).primaryKey());

        assertEquals(new Statement.DropTable("cars"), Parser.parse("DROP TABLE cars;"));
    }

    /**
     * Проверка: синтаксические ошибки.
     */
    @DisplayName("Синтаксические ошибки")
    @Test
    void rejectsInvalidSyntax() {
        assertThrows(DbException.class, () -> Parser.parse("SELEC * FROM cars"));
        assertThrows(DbException.class, () -> Parser.parse("SELECT * cars"));
        assertThrows(DbException.class, () -> Parser.parse("SELECT * FROM cars WHERE price >"));
        assertThrows(DbException.class, () -> Parser.parse("INSERT INTO cars VALUES (1, 'abc)"));
        assertThrows(DbException.class, () -> Parser.parse("SELECT * FROM cars; extra"));
        assertThrows(DbException.class, () -> Parser.parse("SELECT # FROM cars"));
    }
}
