package com.example.wordle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class WordleController {

    @FXML
    private VBox gridRoot;
    private List<HBox> rowHBoxes;
    private int currentRow = 0;
    private int currentCol = 0;
    private boolean gameActive = true;
    private String targetWord = "fluff";
    private final List<String> currentGuess = new ArrayList<>();
    private List<String> wordList = new ArrayList<>();
    private List<Button> keyboardButtons = new ArrayList<>();
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;
    private int[] guessWins = new int[6];


    private void loadWordList() {
        try (InputStream is = getClass().getResourceAsStream("/com/example/wordle/wordleWords.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            wordList = reader.lines().collect(Collectors.toList());
            targetWord = pickRandomWord();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load words from file.");
        }
    }

    public void initialize() {
        loadStatistics();
        loadWordList();
        System.out.println("The target word is: " + targetWord);
        rowHBoxes = gridRoot.getChildren().subList(1, gridRoot.getChildren().size())
                .stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .collect(Collectors.toList());
    }
    public void render() {
        initializeKeyboardButtons();
    }

    private void initializeKeyboardButtons() {
        keyboardButtons.clear();
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            String buttonId = "#button" + ch;
            Button button = (Button) gridRoot.getScene().lookup(buttonId);
            if (button != null) {
                keyboardButtons.add(button);
            }
        }
    }

    private String pickRandomWord() {
        Random random = new Random();
        return wordList.isEmpty() ? "FLUFF" : wordList.get(random.nextInt(wordList.size())).toUpperCase();
    }

    @FXML
    public void onKeyPressed(ActionEvent event) {
        if (!gameActive) return;

        Button button = (Button) event.getSource();
        if (currentRow < rowHBoxes.size() && currentCol < 5) {
            HBox currentRowHBox = rowHBoxes.get(currentRow);
            Label label = (Label) currentRowHBox.getChildren().get(currentCol);
            label.setText(button.getText().toUpperCase());
            currentGuess.add(button.getText().toUpperCase());
            currentCol++;
            logMove(button.getText().toUpperCase());
        }
    }

    private void logMove(String letter) {
        String timestamp = LocalDateTime.now().toString();
        try (FileWriter fw = new FileWriter("game_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(timestamp + ": " + letter);

            for (HBox row : rowHBoxes) {
                for (Node node : row.getChildren()) {
                    if (node instanceof Label lbl) {
                        out.print(lbl.getText() + ";" + lbl.getStyle() + "|");
                    }
                }
                out.println();
            }

            for (Button button : keyboardButtons) {
                out.println(button.getText() + ";" + button.getStyle() + ";" + button.isDisabled());
            }
            out.println("---");
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }

    @FXML
    private void onEnterPressed() {
        if (!gameActive || currentGuess.size() != 5) return;

        String guessedWord = String.join("", currentGuess).toUpperCase();
        processGuess(guessedWord);
        checkGameEnd(guessedWord);
    }


    private void processGuess(String guessedWord) {
        HBox currentRowHBox = rowHBoxes.get(currentRow);
        Map<Character, Integer> targetLetterCounts = getLetterCounts(targetWord.toUpperCase());
        List<String> resultStyles = new ArrayList<>(Collections.nCopies(5, ""));


        for (int i = 0; i < 5; i++) {
            char guessChar = guessedWord.charAt(i);
            if (guessChar == targetWord.toUpperCase().charAt(i)) {
                targetLetterCounts.put(guessChar, targetLetterCounts.getOrDefault(guessChar, 0) - 1);
                resultStyles.set(i, "green");
            }
        }


        for (int i = 0; i < 5; i++) {
            if (resultStyles.get(i).equals("")) {
                char guessChar = guessedWord.charAt(i);
                Label label = (Label) currentRowHBox.getChildren().get(i);
                Button button = findButtonByChar(guessChar);
                if (targetLetterCounts.getOrDefault(guessChar, 0) > 0) {
                    label.setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-alignment: center;");
                    button.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                    targetLetterCounts.put(guessChar, targetLetterCounts.get(guessChar) - 1);
                    resultStyles.set(i, "yellow");
                } else {
                    label.setStyle("-fx-background-color: darkgray; -fx-text-fill: white; -fx-alignment: center;");
                    button.setStyle("-fx-background-color: darkgray; -fx-text-fill: white;");
                    resultStyles.set(i, "gray");
                    button.setDisable(true);
                }

                label.setStyle(label.getStyle() + "-fx-border-color: black;");
            } else if (resultStyles.get(i).equals("green")) {
                Label label = (Label) currentRowHBox.getChildren().get(i);
                Button button = findButtonByChar(guessedWord.charAt(i));
                label.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-alignment: center;");
                button.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            }
        }


    }


    private Button findButtonByChar(char ch) {
        String buttonId = "#button" + Character.toUpperCase(ch);
        return (Button) gridRoot.getScene().lookup(buttonId);
    }


    private void checkGameEnd(String guessedWord) {

        if (guessedWord.equalsIgnoreCase(targetWord)) {
            gamesWon++;
            currentStreak++;
            guessWins[currentRow]++;
            System.out.println("Congratulations! You guessed the word!");
            gameActive = false;
        } else if (currentRow > 4) {
            currentStreak = 0;
            System.out.println("Game Over! The word was: " + targetWord);
            gameActive = false;
        }

        if (!gameActive) {
            gamesPlayed++;
            maxStreak = Math.max(maxStreak, currentStreak);
            saveStatistics();
            showStatistics();
            resetGame();
        } else {
            currentRow++;
            currentCol = 0;
            currentGuess.clear();
        }
    }

    private void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("statistics.fxml"));
            VBox statisticsScreen = loader.load();
            Scene scene = new Scene(statisticsScreen);

            StatisticsController statsController = loader.getController();
            statsController.setStatistics(gamesPlayed, gamesWon, currentStreak, maxStreak,guessWins);

            Stage statsStage = new Stage();
            statsStage.setScene(scene);
            statsStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveStatistics() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("statsFile.txt"))) {
            writer.write(gamesPlayed + "\n");
            writer.write(gamesWon + "\n");
            writer.write(currentStreak + "\n");
            writer.write(maxStreak + "\n");
            for (int wins : guessWins) {
                writer.write(wins + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving statistics: " + e.getMessage());
        }
    }
    private void loadStatistics() {
        try (BufferedReader reader = new BufferedReader(new FileReader("statsFile.txt"))) {
            gamesPlayed = Integer.parseInt(reader.readLine());
            gamesWon = Integer.parseInt(reader.readLine());
            currentStreak = Integer.parseInt(reader.readLine());
            maxStreak = Integer.parseInt(reader.readLine());
            for (int i = 0; i < guessWins.length; i++) {
                guessWins[i] = Integer.parseInt(reader.readLine());
            }
        } catch (IOException e) {
            System.out.println("No statistics found, initializing default stats.");
            Arrays.fill(guessWins, 0);
        }
    }



    @FXML
    private void onDeletePressed() {
        if (!gameActive || currentCol == 0) return;

        currentCol--;
        currentGuess.removeLast();
        HBox currentRowHBox = rowHBoxes.get(currentRow);
        Label label = (Label) currentRowHBox.getChildren().get(currentCol);
        label.setText("");
    }

    @FXML
    private void onResetPressed() {
        resetGame();

    }

    private void resetGame() {
        for (HBox hbox : rowHBoxes) {
            for (Node node : hbox.getChildren()) {
                if (node instanceof Label lbl) {

                    lbl.setText("");
                    lbl.setStyle("-fx-background-color: #F4F4F4; -fx-text-fill: black; -fx-alignment: center; -fx-border-color: black;");
                }
            }
        }
        resetKeyboardButtons();
        currentRow = 0;
        currentCol = 0;
        currentGuess.clear();
        targetWord = pickRandomWord();
        gameActive = true;
        System.out.println("The target word is: " + targetWord);

    }

    private void resetKeyboardButtons() {
        for (Button button : keyboardButtons) {
            button.setDisable(false);
            button.setStyle("-fx-background-color: #D4D6DA; -fx-text-fill: black;");
        }
    }

    private Map<Character, Integer> getLetterCounts(String word) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : word.toCharArray()) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }
        return counts;
    }

    @FXML
    public void onSavePressed() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("wordle_save.txt"))) {

            writer.write(targetWord + "\n" + currentRow + "\n");


            for (HBox row : rowHBoxes) {
                for (Node node : row.getChildren()) {
                    if (node instanceof Label) {
                        Label lbl = (Label) node;

                        writer.write(lbl.getText() + ";" + lbl.getStyle() + "\n");
                    }
                }
            }


            for (Button button : keyboardButtons) {
                writer.write(button.getText() + ";" + button.getStyle() + ";" + button.isDisabled() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onLoadPressed() {
        try (BufferedReader reader = new BufferedReader(new FileReader("wordle_save.txt"))) {
            targetWord = reader.readLine();
            currentRow = Integer.parseInt(reader.readLine());

            for (int rowIndex = 0; rowIndex < rowHBoxes.size(); rowIndex++) {
                HBox row = rowHBoxes.get(rowIndex);
                for (int j = 0; j < row.getChildren().size(); j++) {
                    if (row.getChildren().get(j) instanceof Label lbl) {
                        String line = reader.readLine();
                        if (line != null) {
                            String[] parts = line.split(";");
                            lbl.setText(parts[0]);
                            lbl.setStyle(parts[1]);
                        }
                    }
                }
            }

            int i = 0;

            for (Button button : keyboardButtons) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(";");
                    button.setStyle(parts[1]);
                    if (i > 1){
                        String[] disabledProps = line.split(";;");
                        button.setDisable(Boolean.parseBoolean(disabledProps[1])); // Restore disabled state
                    }
                    i+=1;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
