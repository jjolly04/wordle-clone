package com.example.wordle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WordleApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WordleApplication.class.getResource("wordle.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750, 750);
        stage.setScene(scene);
        stage.show();

        WordleController controller = fxmlLoader.getController();
        controller.render();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
