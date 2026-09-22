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

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("Не должно быть null-able строк");
            }

            if (matrix[i].length != matrix[0].length) {
                throw new IllegalArgumentException("Матрица должна быть прямоугольной");
            }

            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == null) {
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

        for (int i = 0; i<rows(); i++) {
            for (int j = 0; j < columns(); j++){
                result[i][j]= get(i,j).add(other.get(i, j));
            }
        }

        return new ComplexMatrix(result);
    }

    public ComplexMatrix subtract(ComplexMatrix other) {
        if (rows() != other.rows() || columns() != other.columns()) {
            throw new IllegalArgumentException("Матрицы разной размерности");
        }

        Complex[][] result = new Complex[rows()][columns()];

        for (int i = 0; i<rows(); i++) {
            for (int j = 0; j < columns(); j++){
                result[i][j]= get(i,j).subtract(other.get(i, j));
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
            for (int j = 0; j< other.columns(); j++) {
                Complex sum = new Complex(0, 0);
                for (int k = 0; k<columns(); k++) {
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
        throw new UnsupportedOperationException("Вычисление определителя пока не реализовано");
    }
}

