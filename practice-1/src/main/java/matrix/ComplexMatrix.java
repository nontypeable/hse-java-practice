package matrix;

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

        this.matrix = matrix.clone();
    }

    public Complex get(int row, int column) {
        if (row < 0 || column < 0) {
            throw new IndexOutOfBoundsException("Индекс не может быть меньше 0");
        }

        if (row > matrix.length || column > matrix[0].length) {
            throw new IndexOutOfBoundsException("Индекс не может быть больше размера матрицы");
        }

        return this.matrix[row][column];
    }
}

