package com.sudoku;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.*;

public class Main extends Application {

    private TextField[][] cells = new TextField[9][9];
    private int[][] puzzle = new int[9][9];

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #eef4ff, #fdf7ff);");

        GridPane grid = createGrid();
        grid.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0, 0, 6);");

        Button newGameButton = createButton("New Game");
        Button checkButton = createButton("Check");
        Button clearButton = createButton("Clear");
        Button solveButton = createButton("Solve");

        newGameButton.setOnAction(e -> newGame());
        checkButton.setOnAction(e -> checkBoard());
        clearButton.setOnAction(e -> clearUserInputs());
        solveButton.setOnAction(e -> solvePuzzle());

        HBox buttons = new HBox(14, newGameButton, checkButton, clearButton, solveButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setStyle("-fx-padding: 22;");

        root.setCenter(grid);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 660, 620);

        stage.setTitle("Sudoku Game");
        stage.setScene(scene);
        stage.show();

        newGame();
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextField cell = new TextField();

                cell.setPrefSize(50, 50);
                cell.setAlignment(Pos.CENTER);
                cell.setStyle(getCellStyle(row, col, false));

                cell.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue.matches("[1-9]?")) {
                        cell.setText(oldValue);
                    }
                });

                cells[row][col] = cell;
                grid.add(cell, col, row);
            }
        }

        return grid;
    }

    private void newGame() {
        int[][] fullBoard = new int[9][9];
        fillBoard(fullBoard);

        puzzle = copyBoard(fullBoard);

        Random random = new Random();
        int cellsToRemove = 45;

        while (cellsToRemove > 0) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);

            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
                cellsToRemove--;
            }
        }

        loadPuzzle();
    }

    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    List<Integer> numbers = new ArrayList<>();

                    for (int i = 1; i <= 9; i++) {
                        numbers.add(i);
                    }

                    Collections.shuffle(numbers);

                    for (int number : numbers) {
                        if (isValidNumber(board, row, col, number)) {
                            board[row][col] = number;

                            if (fillBoard(board)) {
                                return true;
                            }

                            board[row][col] = 0;
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    private void loadPuzzle() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                cells[row][col].clear();

                if (puzzle[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setEditable(false);
                    cells[row][col].setStyle(getCellStyle(row, col, true));
                } else {
                    cells[row][col].setEditable(true);
                    cells[row][col].setStyle(getCellStyle(row, col, false));
                }
            }
        }
    }

    private void checkBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                String value = cells[row][col].getText();

                if (!value.isEmpty()) {
                    int number = Integer.parseInt(value);

                    if (!isValidMove(row, col, number)) {
                        cells[row][col].setStyle(getCellStyle(row, col, false) + "-fx-background-color: #ffb3b3;");
                    } else if (puzzle[row][col] == 0) {
                        cells[row][col].setStyle(getCellStyle(row, col, false) + "-fx-background-color: #b3ffb3;");
                    }
                }
            }
        }
    }

    private boolean isValidMove(int row, int col, int number) {
        for (int c = 0; c < 9; c++) {
            if (c != col && cells[row][c].getText().equals(String.valueOf(number))) {
                return false;
            }
        }

        for (int r = 0; r < 9; r++) {
            if (r != row && cells[r][col].getText().equals(String.valueOf(number))) {
                return false;
            }
        }

        int boxRow = row - row % 3;
        int boxCol = col - col % 3;

        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if ((r != row || c != col) && cells[r][c].getText().equals(String.valueOf(number))) {
                    return false;
                }
            }
        }

        return true;
    }

    private void clearUserInputs() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (puzzle[row][col] == 0) {
                    cells[row][col].clear();
                    cells[row][col].setStyle(getCellStyle(row, col, false));
                }
            }
        }
    }

    private void solvePuzzle() {
        int[][] board = getBoardFromCells();

        if (solve(board)) {
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    cells[row][col].setText(String.valueOf(board[row][col]));

                    if (puzzle[row][col] == 0) {
                        cells[row][col].setStyle(getCellStyle(row, col, false) + "-fx-background-color: #e6ffe6;");
                    }
                }
            }
        }
    }

    private int[][] getBoardFromCells() {
        int[][] board = new int[9][9];

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                String text = cells[row][col].getText();
                board[row][col] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }

        return board;
    }

    private boolean solve(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int number = 1; number <= 9; number++) {
                        if (isValidNumber(board, row, col, number)) {
                            board[row][col] = number;

                            if (solve(board)) {
                                return true;
                            }

                            board[row][col] = 0;
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidNumber(int[][] board, int row, int col, int number) {
        for (int c = 0; c < 9; c++) {
            if (board[row][c] == number) {
                return false;
            }
        }

        for (int r = 0; r < 9; r++) {
            if (board[r][col] == number) {
                return false;
            }
        }

        int boxRow = row - row % 3;
        int boxCol = col - col % 3;

        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (board[r][c] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[9][9];

        for (int row = 0; row < 9; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, 9);
        }

        return copy;
    }

    private Button createButton(String text) {
        Button button = new Button(text);

        String normalStyle =
                "-fx-background-color: #5b7cfa;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;";

        String hoverStyle =
                "-fx-background-color: #405fd6;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;";

        button.setStyle(normalStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));

        return button;
    }

    private String getCellStyle(int row, int col, boolean fixed) {
        String bg = fixed
                ? "-fx-background-color: #dce7ff;"
                : "-fx-background-color: #ffffff;";

        int top = row % 3 == 0 ? 3 : 1;
        int right = col == 8 ? 3 : ((col + 1) % 3 == 0 ? 3 : 1);
        int bottom = row == 8 ? 3 : ((row + 1) % 3 == 0 ? 3 : 1);
        int left = col % 3 == 0 ? 3 : 1;

        return bg +
                "-fx-border-color: #2f3a5f;" +
                "-fx-border-width: " + top + " " + right + " " + bottom + " " + left + ";" +
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1f2a44;" +
                "-fx-alignment: center;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}