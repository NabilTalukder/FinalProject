package com.example.taskandquizscheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class ViewHandler extends Application {

    private Stage stage;
    private Parent root;
    private Scene scene;

    //offset of the TaskView popup which allow it to be dragged across the screen
    private double xOffset, yOffset;

    //questions from Python program to be used in Quiz
    private ArrayList<ArrayList<String>> questionList;

    private boolean started = false;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        openView("QuizGenerator");
    }

    public void openView(String viewToOpen){
        FXMLLoader loader = new FXMLLoader();

        try {
            loader.setLocation(getClass().getResource(viewToOpen + "View.fxml"));
            root = loader.load();

            switch (viewToOpen){
                case "QuizGenerator" -> {
                    QuizGeneratorController view = loader.getController();
                    view.init(this);
                    if (!started){
                        scene = new Scene(root, 1280, 720);
                        stage.setScene(scene);
                        started = true;
                    }
                    else {
                        stage.getScene().setRoot(root);
                    }
                }
                case "QuizExecutor" -> {
                    QuizExecutor view = loader.getController();
                    view.init(this);
                    view.setQuestionList(questionList);
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
                case "Schedule" -> {
                    ScheduleController view = loader.getController();
                    view.init(this);
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
            }
            stage.setTitle("Revision Scheduler");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setQuestionList(ArrayList<ArrayList<String>> questionList){
        this.questionList = questionList;
    }

}
