package matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleInterfaceTest {
    private String run(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ConsoleInterface(new Scanner(input), new PrintStream(output, true, StandardCharsets.UTF_8)).run();
        return output.toString();
    }

    @DisplayName("Ввод и вывод матрицы комплексных чисел")
    @Test
    void createsAndDisplaysMatrix() {
        String output = run("1\n1 2\n3 -4\n0 2\n2\n0\n");

        assertTrue(output.contains("3.0 - 4.0i"));
        assertTrue(output.contains("2.0i"));
        assertTrue(output.contains("До свидания!"));
    }

    @DisplayName("Операция требует текущую матрицу")
    @Test
    void operationWithoutMatrixShowsError() {
        assertTrue(run("3\n0\n").contains("Сначала создайте матрицу"));
    }

    @DisplayName("Ошибки меню, размеров и элементов позволяют повторить ввод")
    @Test
    void retriesInvalidInput() {
        String output = run("abc\n99\n1\n0 2\n1\n1 1\nx y\nNaN 0\n3,5 -2,5\n0\n");

        assertTrue(output.contains("Неизвестный пункт меню"));
        assertTrue(output.contains("Введите два положительных целых числа"));
        assertTrue(output.contains("Введите две конечные числовые части"));
        assertTrue(output.contains("3.5 - 2.5i"));
    }

    @DisplayName("Сложение сохраняет результат как текущую матрицу")
    @Test
    void additionUpdatesCurrentMatrix() {
        String output = run("1\n1 1\n1 2\n3\n1 1\n3 -1\n2\n0\n");

        assertTrue(output.contains("Результат:\n4.0 + 1.0i"));
        assertTrue(output.contains("Текущая матрица (1×1):\n4.0 + 1.0i"));
    }

    @DisplayName("Вычитание использует текущую матрицу как левый операнд")
    @Test
    void subtractionPreservesOperandOrder() {
        assertTrue(run("1\n1 1\n1 2\n4\n1 1\n3 -1\n0\n")
            .contains("Результат:\n-2.0 + 3.0i"));
    }

    @DisplayName("Умножение прямоугольных матриц")
    @Test
    void multipliesRectangularMatrices() {
        assertTrue(run("1\n1 2\n1 0\n2 0\n5\n2 1\n3 0\n4 0\n0\n")
            .contains("Результат:\n11.0"));
    }

    @DisplayName("Транспонирование меняет размеры текущей матрицы")
    @Test
    void transposeUpdatesDimensions() {
        assertTrue(run("1\n1 2\n1 0\n2 0\n6\n2\n0\n")
            .contains("Текущая матрица (2×1):\n1.0\n2.0"));
    }

    @DisplayName("Определитель выводится без замены текущей матрицы")
    @Test
    void determinantKeepsCurrentMatrix() {
        String output = run("1\n2 2\n1 0\n2 0\n3 0\n4 0\n7\n2\n0\n");

        assertTrue(output.contains("Определитель: -2.0"));
        assertTrue(output.contains("Текущая матрица (2×2):\n1.0  2.0\n3.0  4.0"));
    }

    @DisplayName("Ошибка размерности не меняет текущую матрицу")
    @Test
    void failedOperationKeepsCurrentMatrix() {
        String output = run("1\n1 1\n5 0\n3\n1 2\n1 0\n2 0\n2\n0\n");

        assertTrue(output.contains("Ошибка: Матрицы разной размерности"));
        assertTrue(output.contains("Текущая матрица (1×1):\n5.0"));
    }

    @DisplayName("Не квадратная матрица не завершает программу при вычислении определителя")
    @Test
    void nonSquareDeterminantShowsError() {
        assertTrue(run("1\n1 2\n1 0\n2 0\n7\n0\n")
            .contains("Ошибка: Матрица должна быть квадратной"));
    }

    @DisplayName("Конец ввода корректно завершает программу на любом шаге")
    @Test
    void endOfInputExitsCleanly() {
        for (String input : new String[]{"", "1\n", "1\n1 2\n1 0\n"}) {
            assertTrue(run(input).contains("Ввод завершён."));
        }
    }
}
