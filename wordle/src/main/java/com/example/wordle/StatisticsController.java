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

public class StatisticsController {
    @FXML
    private Label played;

    @FXML
    private Label win;

    @FXML
    private Label streak;

    @FXML
    private Label maxStreak;

    @FXML
    private Label guess1;
    @FXML
    private Label guess2;
    @FXML
    private Label guess3;
    @FXML
    private Label guess4;
    @FXML
    private Label guess5;
    @FXML
    private Label guess6;


    public void setStatistics(int gamesPlayed, int gamesWon, int currentStreak, int maxStreak, int[] guessWins) {
            this.played.setText(String.valueOf(gamesPlayed));
            this.win.setText(String.format("%.1f%%", gamesWon * 100.0 / gamesPlayed));
            this.streak.setText(String.valueOf(currentStreak));
            this.maxStreak.setText(String.valueOf(maxStreak));
            this.guess1.setText(String.valueOf(guessWins[0]));
            this.guess2.setText(String.valueOf(guessWins[1]));
            this.guess3.setText(String.valueOf(guessWins[2]));
            this.guess4.setText(String.valueOf(guessWins[3]));
            this.guess5.setText(String.valueOf(guessWins[4]));
            this.guess6.setText(String.valueOf(guessWins[5]));
        }

    }



