package matrix;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ConsoleInterface {
    private final Scanner input;
    private final PrintStream output;
    private ComplexMatrix current;

    public ConsoleInterface(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
    }

    public void run() {
        output.println("Калькулятор комплексных матриц");

        try {
            while (true) {
                printMenu();
                String choice = readLine("Выберите действие: ");

                if (choice.equals("0")) {
                    output.println("До свидания!");
                    return;
                }

                try {
                    execute(choice);
                } catch (IllegalArgumentException | ArithmeticException exception) {
                    output.println("Ошибка: " + exception.getMessage());
                }
            }
        } catch (NoSuchElementException exception) {
            output.println("Ввод завершён.");
        }
    }

    private void printMenu() {
        output.println("""
            1 — Создать или заменить матрицу
            2 — Показать текущую матрицу
            3 — Прибавить матрицу
            4 — Вычесть матрицу
            5 — Умножить на матрицу
            6 — Транспонировать
            7 — Вычислить определитель
            0 — Выйти
            """);
    }

    private void execute(String choice) {
        if (choice.equals("1")) {
            current = readMatrix();
            printCurrentMatrix();
            return;
        }

        if (!choice.matches("[2-7]")) {
            output.println("Неизвестный пункт меню.");
            return;
        }

        if (current == null) {
            output.println("Сначала создайте матрицу (пункт 1).");
            return;
        }

        switch (choice) {
            case "2" -> printCurrentMatrix();
            case "3", "4", "5" -> {
                output.println("Введите вторую матрицу:");
                ComplexMatrix other = readMatrix();
                current = switch (choice) {
                    case "3" -> current.add(other);
                    case "4" -> current.subtract(other);
                    default -> current.multiply(other);
                };
                printResult();
            }
            case "6" -> {
                current = current.transpose();
                printResult();
            }
            case "7" -> output.println("Определитель: " + format(current.determinant()));
            default -> throw new IllegalArgumentException("Неизвестный пункт меню");
        }
    }

    private ComplexMatrix readMatrix() {
        int[] dimensions = readDimensions();
        Complex[][] elements = new Complex[dimensions[0]][dimensions[1]];
        output.println("Вводите каждый элемент как re im, например: 3 -4. Допустимы точка и запятая.");

        for (int row = 0; row < elements.length; row++) {
            for (int column = 0; column < elements[row].length; column++) {
                elements[row][column] = readComplex(row, column);
            }
        }

        return new ComplexMatrix(elements);
    }

    private int[] readDimensions() {
        while (true) {
            String[] parts = readLine("Число строк и столбцов (например, 2 3): ").split("\\s+");

            if (parts.length == 2) {
                try {
                    int rows = Integer.parseInt(parts[0]);
                    int columns = Integer.parseInt(parts[1]);
                    if (rows > 0 && columns > 0) {
                        return new int[]{rows, columns};
                    }
                } catch (NumberFormatException exception) {
                    // повторяем ввод размеров
                }
            }

            output.println("Введите два положительных целых числа.");
        }
    }

    private Complex readComplex(int row, int column) {
        while (true) {
            String[] parts = readLine("Элемент [" + (row + 1) + ", " + (column + 1) + "]: ")
                .replace(',', '.')
                .split("\\s+");

            if (parts.length == 2) {
                try {
                    double real = Double.parseDouble(parts[0]);
                    double imaginary = Double.parseDouble(parts[1]);
                    if (Double.isFinite(real) && Double.isFinite(imaginary)) {
                        return new Complex(real, imaginary);
                    }
                } catch (NumberFormatException exception) {
                    // повторяем ввод элемента
                }
            }

            output.println("Введите две конечные числовые части через пробел, например: 3 -4.");
        }
    }

    private String readLine(String prompt) {
        output.print(prompt);
        output.flush();
        return input.nextLine().trim();
    }

    private void printCurrentMatrix() {
        output.println("Текущая матрица (" + current.rows() + "×" + current.columns() + "):");
        printMatrix(current);
    }

    private void printResult() {
        output.println("Результат:");
        printMatrix(current);
    }

    private void printMatrix(ComplexMatrix matrix) {
        for (int row = 0; row < matrix.rows(); row++) {
            for (int column = 0; column < matrix.columns(); column++) {
                if (column > 0) {
                    output.print("  ");
                }
                output.print(format(matrix.get(row, column)));
            }
            output.println();
        }
    }

    private static String format(Complex number) {
        double real = number.re() == 0 ? 0 : number.re();
        double imaginary = number.im() == 0 ? 0 : number.im();

        if (imaginary == 0) {
            return Double.toString(real);
        }
        if (real == 0) {
            return imaginary + "i";
        }
        return real + (imaginary < 0 ? " - " : " + ") + Math.abs(imaginary) + "i";
    }
}
