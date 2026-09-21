package matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComplexTest {
    @Test
    void addsTwoComplexNumbers() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(3, 4);

        assert x.add(y).equals(new Complex(4, 6));
    }

    @Test
    void addsZeroWithoutChangingNumber() {
        Complex x = new Complex(1, 1);
        Complex y = new Complex(0, 0);

        assert x.add(y).equals(x);
    }

    @Test
    void subtractsTwoComplexNumbers() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(1, 2);

        assert x.substract(y).equals(new Complex(2, 2));
    }

    @Test
    void subtractingNumberFromItselfProducesZero() {
        Complex x = new Complex(1, 1);

        assert x.substract(x).equals(new Complex(0, 0));
    }

    @Test
    void multipliesTwoComplexNumbers() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(3, 4);

        assert x.multiply(y).equals(new Complex(-5, 10));
    }

    @Test
    void multiplyingByOneKeepsNumberUnchanged() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(1, 0);

        assert x.multiply(y).equals(x);
    }

    @Test
    void multiplyingIByIProducesMinusOne() {
        Complex x = new Complex(0, 1);

        assert x.multiply(x).equals(new Complex(-1, 0));
    }

    @Test
    void multiplyingByZeroProducesZero() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(0, 0);

        assert x.multiply(y).equals(y);
    }

    @Test
    void dividesTwoComplexNumbers() {
        Complex x = new Complex(3, 4);
        Complex y = new Complex(1, 1);

        assert x.devide(y).equals(new Complex(3.5, 0.5));
    }

    @Test
    void dividingByImaginaryUnitProducesExpectedResult() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(0, 1);

        assert x.devide(y).equals(new Complex(2, -1));
    }

    @Test
    void dividingByZeroThrowsArithmeticException() {
        Complex x = new Complex(1, 2);
        Complex y = new Complex(0, 0);

        assertThrows(ArithmeticException.class, () -> x.devide(y));
    }
}
