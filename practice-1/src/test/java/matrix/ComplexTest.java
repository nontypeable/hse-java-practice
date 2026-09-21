package matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComplexTest {
    @DisplayName("Сложение двух комплексных чисел")
    @Test
    void addsTwoComplexNumbers() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(3, 4);

        assertEquals(new Complex(4, 6), x.add(y));
    }

    @DisplayName("Прибавление нуля не меняет комплексное число")
    @Test
    void addsZeroWithoutChangingNumber() {
        Complex x = new Complex(1, 1);
        Complex y = new Complex(0, 0);

        assertEquals(x, x.add(y));
    }

    @DisplayName("Вычитание двух комплексных чисел")
    @Test
    void subtractsTwoComplexNumbers() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(1, 2);

        assertEquals(new Complex(2, 2), x.subtract(y));
    }

    @DisplayName("Вычитание комплексного числа из самого себя даёт ноль")
    @Test
    void subtractingNumberFromItselfProducesZero() {
        Complex x = new Complex(1, 1);

        assertEquals(new Complex(0, 0), x.subtract(x));
    }

    @DisplayName("Умножение двух комплексных чисел")
    @Test
    void multipliesTwoComplexNumbers() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(3, 4);

        assertEquals(new Complex(-5, 10), x.multiply(y));
    }

    @DisplayName("Умножение на единицу не меняет комплексное число")
    @Test
    void multiplyingByOneKeepsNumberUnchanged() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(1, 0);

        assertEquals(x, x.multiply(y));
    }

    @DisplayName("Квадрат мнимой единицы равен -1")
    @Test
    void multiplyingIByIProducesMinusOne() {
        Complex x = new Complex(0, 1);

        assertEquals(new Complex(-1, 0), x.multiply(x));
    }

    @DisplayName("Умножение на ноль даёт ноль")
    @Test
    void multiplyingByZeroProducesZero() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(0, 0);

        assertEquals(y, x.multiply(y));
    }

    @DisplayName("Деление двух комплексных чисел")
    @Test
    void dividesTwoComplexNumbers() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(1, 1);

        assertEquals(new Complex(3.5, 0.5), x.divide(y));
    }

    @DisplayName("Деление на мнимую единицу даёт ожидаемый результат")
    @Test
    void dividingByImaginaryUnitProducesExpectedResult() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(0, 1);

        assertEquals(new Complex(2, -1), x.divide(y));
    }

    @DisplayName("Деление на ноль выбрасывает ArithmeticException")
    @Test
    void dividingByZeroThrowsArithmeticException() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(0, 0);

        assertThrows(ArithmeticException.class, () -> x.divide(y));
    }
}
