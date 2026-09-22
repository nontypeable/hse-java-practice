package matrix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComplexMatrixTest {
    // TODO: убрать этот метод, реализовав equals() и hashCode() в ComplexMatrix
    private static void assertMatrixEquals(Complex[][] expected, ComplexMatrix actual) {
        assertEquals(expected.length, actual.rows());
        assertEquals(expected[0].length, actual.columns());

        for (int row = 0; row < expected.length; row++) {
            for (int column = 0; column < expected[row].length; column++) {
                assertEquals(expected[row][column], actual.get(row, column));
            }
        }
    }

    // TODO: убрать этот метод, реализовав equals() и hashCode() в ComplexMatrix
    private static void assertMatrixEquals(ComplexMatrix expected, ComplexMatrix actual) {
        assertEquals(expected.rows(), actual.rows());
        assertEquals(expected.columns(), actual.columns());

        for (int row = 0; row < expected.rows(); row++) {
            for (int column = 0; column < expected.columns(); column++) {
                assertEquals(expected.get(row, column), actual.get(row, column));
            }
        }
    }

    @DisplayName("Матрица 1×1 создаётся")
    @Test
    void constructorAcceptsOneByOneMatrix() {
        assertDoesNotThrow(() -> new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0)}
        }));
    }

    @DisplayName("Прямоугольная матрица создаётся")
    @Test
    void constructorAcceptsRectangularMatrix() {
        assertDoesNotThrow(() -> new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0), new Complex(3, 0)},
            {new Complex(4, 0), new Complex(5, 0), new Complex(6, 0)}
        }));
    }

    @DisplayName("Конструктор отклоняет null-матрицу")
    @Test
    void constructorRejectsNullMatrix() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(null));
    }

    @DisplayName("Конструктор отклоняет матрицу без строк")
    @Test
    void constructorRejectsMatrixWithoutRows() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(new Complex[][]{}));
    }

    @DisplayName("Конструктор отклоняет null в первой строке")
    @Test
    void constructorRejectsNullFirstRow() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(new Complex[][]{
            null,
            {new Complex(3, 0), new Complex(4, 0)},
        }));
    }

    @DisplayName("Конструктор отклоняет null среди строк")
    @Test
    void constructorRejectsNullMiddleRow() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0)},
            null,
            {new Complex(5, 0), new Complex(6, 0)}
        }));
    }

    @DisplayName("Конструктор отклоняет матрицу без столбцов")
    @Test
    void constructorRejectsMatrixWithoutColumns() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(new Complex[][]{{}}));
    }

    @DisplayName("Конструктор отклоняет непрямоугольную матрицу")
    @Test
    void constructorRejectsRaggedMatrix() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0)},
            {new Complex(3, 0)},
        }));
    }

    @DisplayName("Конструктор отклоняет null-элемент")
    @Test
    void constructorRejectsNullElement() {
        assertThrows(IllegalArgumentException.class, () -> new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), null}
        }));
    }

    @DisplayName("Конструктор копирует каждую строку")
    @Test
    void constructorDefensivelyCopiesEveryRow() {
        Complex[][] data = {
            {new Complex(0, 0)}
        };
        ComplexMatrix matrix = new ComplexMatrix(data);
        data[0][0] = new Complex(1, 1);

        assertEquals(new Complex(0, 0), matrix.get(0, 0));
    }

    @DisplayName("Конструктор не сохраняет внешний массив")
    @Test
    void constructorDoesNotReuseOuterArray() {
        Complex[][] data = {{new Complex(0, 0)}};
        ComplexMatrix matrix = new ComplexMatrix(data);
        data[0] = new Complex[]{new Complex(1, 1)};

        assertEquals(new Complex(0, 0), matrix.get(0, 0));
    }

    @DisplayName("rows возвращает число строк")
    @Test
    void rowsReturnsNumberOfRows() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0), new Complex(3, 0)},
            {new Complex(4, 0), new Complex(5, 0), new Complex(6, 0)},
        });

        assertEquals(2, matrix.rows());
    }

    @DisplayName("columns возвращает число столбцов")
    @Test
    void columnsReturnsNumberOfColumns() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0), new Complex(3, 0)},
            {new Complex(4, 0), new Complex(5, 0), new Complex(6, 0)},
        });

        assertEquals(3, matrix.columns());
    }

    @DisplayName("get возвращает верхний левый элемент")
    @Test
    void getReturnsTopLeftElement() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0)},
            {new Complex(4, 0), new Complex(5, 0)},
        });

        assertEquals(new Complex(1, 0), matrix.get(0, 0));
    }

    @DisplayName("get возвращает последний элемент")
    @Test
    void getReturnsLastElement() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0)},
            {new Complex(4, 0), new Complex(5, 0)},
        });

        assertEquals(new Complex(5, 0), matrix.get(matrix.rows() - 1, matrix.columns() - 1));
    }

    @DisplayName("get отклоняет отрицательный индекс строки")
    @Test
    void getRejectsNegativeRowIndex() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0)}
        });

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(-1, 0));
    }

    @DisplayName("get отклоняет отрицательный индекс столбца")
    @Test
    void getRejectsNegativeColumnIndex() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0)}
        });

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(0, -1));
    }

    @DisplayName("get отклоняет индекс строки, равный числу строк")
    @Test
    void getRejectsRowIndexEqualToRowCount() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0)},
            {new Complex(4, 0), new Complex(5, 0)}
        });

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(matrix.rows(), 0));
    }

    @DisplayName("get отклоняет индекс столбца, равный числу столбцов")
    @Test
    void getRejectsColumnIndexEqualToColumnCount() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0)},
            {new Complex(4, 0), new Complex(5, 0)}
        });

        assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(0, matrix.columns()));
    }

    @DisplayName("Сложение матриц одинакового размера")
    @Test
    void addsMatricesOfSameDimensions() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6)},
            {new Complex(7, 7), new Complex(8, 8)}
        });

        assertMatrixEquals(new Complex[][]{
            {new Complex(6, 6), new Complex(8, 8)},
            {new Complex(10, 10), new Complex(12, 12)}
        }, matrix1.add(matrix2));
    }

    @DisplayName("Сложение матриц с действительными и мнимыми частями")
    @Test
    void addsMatricesWithMixedParts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 2), new Complex(-3, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, -6), new Complex(7, 8)}
        });

        assertMatrixEquals(new Complex[][]{
            {new Complex(6, -4), new Complex(4, 12)}
        }, matrix1.add(matrix2));
    }

    @DisplayName("Сложение отклоняет разное число строк")
    @Test
    void addRejectsMatricesWithDifferentRowCounts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6)},
            {new Complex(7, 7), new Complex(8, 8)},
            {new Complex(9, 9), new Complex(10, 10)}
        });

        assertThrows(IllegalArgumentException.class, () -> matrix1.add(matrix2));
    }

    @DisplayName("Сложение отклоняет разное число столбцов")
    @Test
    void addRejectsMatricesWithDifferentColumnCounts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6), new Complex(7, 7)},
            {new Complex(8, 8), new Complex(9, 9), new Complex(10, 10)}
        });

        assertThrows(IllegalArgumentException.class, () -> matrix1.add(matrix2));
    }

    @DisplayName("Вычитание матриц одинакового размера")
    @Test
    void subtractsMatricesOfSameDimensions() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6)},
            {new Complex(7, 7), new Complex(8, 8)}
        });

        assertMatrixEquals(new Complex[][]{
            {new Complex(4, 4), new Complex(4, 4)},
            {new Complex(4, 4), new Complex(4, 4)}
        }, matrix2.subtract(matrix1));
    }

    @DisplayName("Вычитание матриц с действительными и мнимыми частями")
    @Test
    void subtractsMatricesWithMixedParts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, -6), new Complex(7, 8)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 2), new Complex(-3, 4)}
        });

        assertMatrixEquals(new Complex[][]{
            {new Complex(4, -8), new Complex(10, 4)}
        }, matrix1.subtract(matrix2));
    }

    @DisplayName("Вычитание отклоняет разное число строк")
    @Test
    void subtractRejectsMatricesWithDifferentRowCounts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6)},
            {new Complex(7, 7), new Complex(8, 8)},
            {new Complex(9, 9), new Complex(10, 10)}
        });

        assertThrows(IllegalArgumentException.class, () -> matrix1.subtract(matrix2));
    }

    @DisplayName("Вычитание отклоняет разное число столбцов")
    @Test
    void subtractRejectsMatricesWithDifferentColumnCounts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6), new Complex(7, 7)},
            {new Complex(8, 8), new Complex(9, 9), new Complex(10, 10)}
        });

        assertThrows(IllegalArgumentException.class, () -> matrix1.subtract(matrix2));
    }

    @DisplayName("Вычитание матрицы из самой себя даёт нулевую матрицу")
    @Test
    void subtractMatrixFromItselfProducesZeroMatrix() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });

        assertMatrixEquals(new Complex[][]{
            {new Complex(0, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(0, 0)}
        }, matrix.subtract(matrix));
    }

    @DisplayName("Умножение совместимых матриц")
    @Test
    void multipliesCompatibleMatrices() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0), new Complex(3, 0)},
            {new Complex(4, 0), new Complex(5, 0), new Complex(6, 0)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(7, 0), new Complex(8, 0)},
            {new Complex(9, 0), new Complex(10, 0)},
            {new Complex(11, 0), new Complex(12, 0)}
        });

        ComplexMatrix result = matrix1.multiply(matrix2);

        assertEquals(2, result.rows());
        assertEquals(2, result.columns());
        assertMatrixEquals(new Complex[][]{
            {new Complex(58, 0), new Complex(64, 0)},
            {new Complex(139, 0), new Complex(154, 0)}
        }, result);
    }

    @DisplayName("Умножение матриц с чисто мнимыми элементами")
    @Test
    void multipliesMatricesWithPureImaginaryElements() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(0, 1), new Complex(0, 2)},
            {new Complex(0, 3), new Complex(0, 4)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(0, 5), new Complex(0, 6)},
            {new Complex(0, 7), new Complex(0, 8)},
        });

        assertMatrixEquals(new Complex[][]{
            {new Complex(-19, 0), new Complex(-22, 0)},
            {new Complex(-43, 0), new Complex(-50, 0)}
        }, matrix1.multiply(matrix2));
    }

    @DisplayName("Умножение матриц с действительными и мнимыми частями")
    @Test
    void multipliesMatricesWithMixedParts() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{{new Complex(1, 2)}});
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{{new Complex(3, -4)}});

        assertMatrixEquals(new Complex[][]{{new Complex(11, 2)}}, matrix1.multiply(matrix2));
    }

    @DisplayName("Умножение на единичную матрицу не меняет матрицу")
    @Test
    void multiplyByIdentityMatrixKeepsMatrixUnchanged() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 2), new Complex(3, 4)},
            {new Complex(5, 6), new Complex(7, 8)}
        });
        ComplexMatrix identity = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(1, 0)}
        });

        assertMatrixEquals(matrix, matrix.multiply(identity));
    }

    @DisplayName("Умножение на нулевую матрицу даёт нулевую матрицу")
    @Test
    void multiplyByZeroMatrixProducesZeroMatrix() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 2), new Complex(3, 4)},
            {new Complex(5, 6), new Complex(7, 8)}
        });
        ComplexMatrix zero = new ComplexMatrix(new Complex[][]{
            {new Complex(0, 0), new Complex(0, 0)},
            {new Complex(0, 0), new Complex(0, 0)}
        });

        assertMatrixEquals(zero, matrix.multiply(zero));
    }

    @DisplayName("Умножение отклоняет несовместимые размеры")
    @Test
    void multiplyRejectsIncompatibleDimensions() {
        ComplexMatrix matrix1 = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2), new Complex(3, 3)},
            {new Complex(4, 4), new Complex(5, 5), new Complex(6, 6)}
        });
        ComplexMatrix matrix2 = new ComplexMatrix(new Complex[][]{
            {new Complex(5, 5), new Complex(6, 6)},
            {new Complex(7, 7), new Complex(8, 8)},
        });

        assertThrows(IllegalArgumentException.class, () -> matrix1.multiply(matrix2));
    }

    @DisplayName("Транспонирование меняет строки и столбцы местами")
    @Test
    void transposeSwapsRowsAndColumns() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2), new Complex(3, 3)},
            {new Complex(4, 4), new Complex(5, 5), new Complex(6, 6)}
        });

        ComplexMatrix result = matrix.transpose();

        assertEquals(3, result.rows());
        assertEquals(2, result.columns());
        assertMatrixEquals(new Complex[][]{
            {new Complex(1, 1), new Complex(4, 4)},
            {new Complex(2, 2), new Complex(5, 5)},
            {new Complex(3, 3), new Complex(6, 6)}
        }, result);
    }

    @DisplayName("Транспонирование квадратной матрицы сохраняет размер")
    @Test
    void transposeKeepsSquareMatrixDimensions() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2)},
            {new Complex(3, 3), new Complex(4, 4)}
        });
        ComplexMatrix result = matrix.transpose();

        assertEquals(2, result.rows());
        assertEquals(2, result.columns());
        assertMatrixEquals(new Complex[][]{
            {new Complex(1, 1), new Complex(3, 3)},
            {new Complex(2, 2), new Complex(4, 4)},
        }, result);
    }

    @DisplayName("Двойное транспонирование возвращает исходную матрицу")
    @Test
    void transposingTwiceReturnsOriginalMatrix() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 2), new Complex(3, 3)},
            {new Complex(4, 4), new Complex(5, 5), new Complex(6, 6)}
        });

        assertMatrixEquals(matrix, matrix.transpose().transpose());
    }

    @DisplayName("Определитель матрицы 1×1 равен её элементу")
    @Test
    void determinantOfOneByOneMatrixEqualsItsElement() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(3, 2)}
        });

        assertEquals(new Complex(3, 2), matrix.determinant());
    }

    @DisplayName("Вычисление определителя матрицы 2×2")
    @Test
    void determinantCalculatesTwoByTwoMatrix() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, 0)},
            {new Complex(3, 0), new Complex(4, -1)}
        });

        assertEquals(new Complex(-1, 3), matrix.determinant());
    }

    @DisplayName("Определитель треугольной матрицы равен произведению диагонали")
    @Test
    void determinantOfTriangularMatrixEqualsProductOfDiagonal() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(5, 2), new Complex(7, -3)},
            {new Complex(0, 0), new Complex(2, -1), new Complex(4, 6)},
            {new Complex(0, 0), new Complex(0, 0), new Complex(3, 0)}
        });

        assertEquals(new Complex(9, 3), matrix.determinant());
    }

    @DisplayName("Определитель вырожденной матрицы равен нулю")
    @Test
    void determinantOfSingularMatrixIsZero() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 1), new Complex(2, -1)},
            {new Complex(2, 2), new Complex(4, -2)}
        });

        assertEquals(new Complex(0, 0), matrix.determinant());
    }

    @DisplayName("Определитель отклоняет неквадратную матрицу")
    @Test
    void determinantRejectsNonSquareMatrix() {
        ComplexMatrix matrix = new ComplexMatrix(new Complex[][]{
            {new Complex(1, 0), new Complex(2, 0), new Complex(3, 0)},
            {new Complex(4, 0), new Complex(5, 0), new Complex(6, 0)}
        });

        assertThrows(IllegalArgumentException.class, matrix::determinant);
    }
}
