package matrix;

public record Complex(double re, double im) {
    public Complex add(Complex other) {
        return new Complex(
            re + other.re,
            im + other.im
        );
    }

    public Complex subtract(Complex other) {
        return new Complex(
            re - other.re,
            im - other.im
        );
    }

    public Complex multiply(Complex other) {
        return new Complex(
            re * other.re - im * other.im,
            re * other.im + im * other.re
        );
    }

    public Complex divide(Complex other) {
        double denominator = other.re * other.re + other.im * other.im;
        if (denominator == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Complex(
            (re * other.re + im * other.im) / denominator,
            (im * other.re - re * other.im) / denominator);
    }
}
