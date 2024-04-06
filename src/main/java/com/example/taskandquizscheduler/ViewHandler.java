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

    //questions from Python program to be used in Quiz
    private ArrayList<ArrayList<String>> questionList;
    //number of correct answers
    private int score = 0;
    //holds all user's answers
    private ArrayList<String> userAnswers = new ArrayList<>();

    //flag to ensure scenes switch between pages instead of a new one being created
    private boolean createdInitialScene = false;

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
                    if (!createdInitialScene){
                        scene = new Scene(root, 1280, 720);
                        stage.setScene(scene);
                        createdInitialScene = true;
                    }
                    else {
                        stage.getScene().setRoot(root);
                    }
                }
                case "Quiz" -> {
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
                case "Results" -> {
                    ResultsController view = loader.getController();
                    view.init(this);
                    view.setQuestionList(questionList);
                    view.setUserAnswers(userAnswers);
                    view.setScore(score);
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

    public void setScore(int score) {
        this.score = score;
    }

    public void setUserAnswers(ArrayList<String> userAnswers) {
        this.userAnswers = userAnswers;
    }
}
