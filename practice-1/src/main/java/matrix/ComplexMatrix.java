package matrix;

import java.util.Arrays;

public class ComplexMatrix {
    private final Complex[][] matrix;

    public ComplexMatrix(Complex[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("Матрица должна быть непустой");
        }

        if (matrix[0] == null) {
            throw new IllegalArgumentException("Не должно быть null-able строк");
        }

        if (matrix[0].length == 0) {
            throw new IllegalArgumentException("Матрица должны быть непустой");
        }

        for (Complex[] complexes : matrix) {
            if (complexes == null) {
                throw new IllegalArgumentException("Не должно быть null-able строк");
            }

            if (complexes.length != matrix[0].length) {
                throw new IllegalArgumentException("Матрица должна быть прямоугольной");
            }

            for (Complex complex : complexes) {
                if (complex == null) {
                    throw new IllegalArgumentException("Некорретное значение в матрице");
                }
            }
        }

        // простое копирование массива с помощью двух циклов скучное 🌚
        this.matrix = Arrays.stream(matrix).map(Complex[]::clone).toArray(Complex[][]::new);
    }

    public int rows() {
        return matrix.length;
    }

    public int columns() {
        return matrix[0].length;
    }

    public Complex get(int row, int column) {
        if (row < 0 || column < 0) {
            throw new IndexOutOfBoundsException("Индекс не может быть меньше 0");
        }

        if (row >= matrix.length || column >= matrix[0].length) {
            throw new IndexOutOfBoundsException("Индекс не может быть больше размера матрицы");
        }

        return this.matrix[row][column];
    }

    public ComplexMatrix add(ComplexMatrix other) {
        if (rows() != other.rows() || columns() != other.columns()) {
            throw new IllegalArgumentException("Матрицы разной размерности");
        }

        Complex[][] result = new Complex[rows()][columns()];

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                result[i][j] = get(i, j).add(other.get(i, j));
            }
        }

        return new ComplexMatrix(result);
    }

    public ComplexMatrix subtract(ComplexMatrix other) {
        if (rows() != other.rows() || columns() != other.columns()) {
            throw new IllegalArgumentException("Матрицы разной размерности");
        }

        Complex[][] result = new Complex[rows()][columns()];

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                result[i][j] = get(i, j).subtract(other.get(i, j));
            }
        }

        return new ComplexMatrix(result);
    }

    public ComplexMatrix multiply(ComplexMatrix other) {
        if (columns() != other.rows()) {
            throw new IllegalArgumentException("Матрицы несовместимой размерности");
        }

        Complex[][] result = new Complex[rows()][other.columns()];

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < other.columns(); j++) {
                Complex sum = new Complex(0, 0);
                for (int k = 0; k < columns(); k++) {
                    sum = sum.add(matrix[i][k].multiply(other.matrix[k][j]));
                }
                result[i][j] = sum;
            }
        }

        return new ComplexMatrix(result);
    }

    public ComplexMatrix divide(ComplexMatrix other) {
        throw new UnsupportedOperationException("Деление пока не реализовано");
    }

    public ComplexMatrix transpose() {
        Complex[][] result = new Complex[columns()][rows()];

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                result[j][i] = matrix[i][j];
            }
        }

        return new ComplexMatrix(result);
    }

    public Complex determinant() {
        if (rows() != columns()) {
            throw new IllegalArgumentException("Матрица должна быть квадратной");
        }

        Complex[][] copy = Arrays.stream(matrix)
            .map(Complex[]::clone)
            .toArray(Complex[][]::new);

        Complex determinant = new Complex(1, 0);

        for (int column = 0; column < rows(); column++) {
            int pivotRow = column;

            for (int row = column + 1; row < matrix.length; row++) {
                if (matrix[row][column].abs() > matrix[pivotRow][column].abs()) {
                    pivotRow = row;
                }
            }

            if (copy[pivotRow][column].isZero()) {
                return new Complex(0, 0);
            }

            if (pivotRow != column) {
                Complex[] temporary = copy[column];
                copy[column] = copy[pivotRow];
                copy[pivotRow] = temporary;
                determinant = determinant.negate();
            }

            Complex pivot = copy[column][column];
            determinant = determinant.multiply(pivot);

            for (int row = column + 1; row < rows(); row++) {
                Complex factor = copy[row][column].divide(pivot);

                for (int k = column + 1; k < rows(); k++) {
                    copy[row][k] = copy[row][k].subtract(factor.multiply(copy[column][k]));
                }

                copy[row][column] = new Complex(0, 0);
            }
        }

        return determinant;
    }
}
