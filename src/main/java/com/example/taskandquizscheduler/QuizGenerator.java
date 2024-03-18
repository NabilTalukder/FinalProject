package com.example.taskandquizscheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

//temporarily functions as initial page
public class QuizGenerator extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //get FXML file for quiz generator and display
        Parent root = FXMLLoader.load(getClass().getResource("quiz-gen-view.fxml"));
        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("Task and Quiz Scheduler");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    //main is here for now but will be in separate Start class later
    public static void main(String[] args) {
        launch();
    }
}