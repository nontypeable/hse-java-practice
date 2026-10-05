package matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @DisplayName("Нулевое комплексное число является нулевым")
    @Test
    void zeroComplexIsZero() {
        Complex x = new Complex(0, 0);

        assertTrue(x.isZero());
    }

    @DisplayName("Ненулевое комплексное число не является нулевым")
    @Test
    void nonZeroComplexIsNotZero() {
        Complex x = new Complex(1, 1);

        assertFalse(x.isZero());
    }

    @DisplayName("Число с ненулевой действительной частью не является нулевым")
    @Test
    void realNumberIsNotZero() {
        assertFalse(new Complex(-2, 0).isZero());
    }

    @DisplayName("Число с ненулевой мнимой частью не является нулевым")
    @Test
    void imaginaryNumberIsNotZero() {
        assertFalse(new Complex(0, -2).isZero());
    }

    @DisplayName("Отрицательный ноль в любой части считается нулём")
    @Test
    void signedZerosAreZero() {
        assertTrue(new Complex(-0.0, 0.0).isZero());
        assertTrue(new Complex(0.0, -0.0).isZero());
        assertTrue(new Complex(-0.0, -0.0).isZero());
    }

    @DisplayName("Очень маленькая ненулевая часть не считается нулём")
    @Test
    void smallNonZeroPartsAreNotZero() {
        assertFalse(new Complex(Double.MIN_VALUE, 0).isZero());
        assertFalse(new Complex(0, Double.MIN_VALUE).isZero());
    }

    @DisplayName("Корректный модуль комплексного числа")
    @Test
    void absCalculatesComplexMagnitude() {
        Complex x = new Complex(3, -4);

        assertEquals(5.0, x.abs(), 1e-12);
    }

    @DisplayName("Модуль нулевого комплексного числа")
    @Test
    void absOfZeroIsZero() {
        Complex x = new Complex(0, 0);

        assertEquals(0.0, x.abs(), 1e-12);
    }

    @DisplayName("Модуль действительного числа равен абсолютному значению его части")
    @Test
    void absOfRealNumberIsPositive() {
        assertEquals(7.0, new Complex(-7, 0).abs(), 1e-12);
    }

    @DisplayName("Модуль мнимого числа равен абсолютному значению его части")
    @Test
    void absOfImaginaryNumberIsPositive() {
        assertEquals(7.0, new Complex(0, -7).abs(), 1e-12);
    }

    @DisplayName("Модуль не зависит от знаков частей")
    @Test
    void absDoesNotDependOnComponentSigns() {
        assertEquals(5.0, new Complex(3, 4).abs(), 1e-12);
        assertEquals(5.0, new Complex(-3, 4).abs(), 1e-12);
        assertEquals(5.0, new Complex(-3, -4).abs(), 1e-12);
    }

    @DisplayName("Модуль может быть иррациональным числом")
    @Test
    void absCalculatesNonIntegerMagnitude() {
        assertEquals(1.4142135623730951, new Complex(1, 1).abs(), 1e-12);
    }

    @DisplayName("Модуль числа с дробными частями")
    @Test
    void absCalculatesMagnitudeOfFractionalComponents() {
        assertEquals(0.5, new Complex(0.3, -0.4).abs(), 1e-12);
    }

    @DisplayName("Отрицание меняет знак обеих частей")
    @Test
    void negateChangesBothComponentSigns() {
        assertEquals(new Complex(-3, 4), new Complex(3, -4).negate());
        assertEquals(new Complex(3, -4), new Complex(-3, 4).negate());
    }

    @DisplayName("Отрицание действительного числа")
    @Test
    void negateChangesRealNumberSign() {
        Complex result = new Complex(3, 0).negate();

        assertEquals(-3.0, result.re(), 1e-12);
        assertEquals(0.0, result.im(), 1e-12);
    }

    @DisplayName("Отрицание мнимого числа")
    @Test
    void negateChangesImaginaryNumberSign() {
        Complex result = new Complex(0, -4).negate();

        assertEquals(0.0, result.re(), 1e-12);
        assertEquals(4.0, result.im(), 1e-12);
    }

    @DisplayName("Отрицание нуля даёт ноль")
    @Test
    void negateOfZeroIsZero() {
        Complex result = new Complex(0, 0).negate();

        assertEquals(0.0, result.re(), 1e-12);
        assertEquals(0.0, result.im(), 1e-12);
    }

    @DisplayName("Двойное отрицание возвращает исходное число")
    @Test
    void negatingTwiceReturnsOriginalValue() {
        Complex x = new Complex(2.5, -7.5);

        assertEquals(x, x.negate().negate());
    }
}
