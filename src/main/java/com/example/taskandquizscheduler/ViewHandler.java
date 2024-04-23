package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.ArrayList;

public class ViewHandler extends Application {

    //main window of JavaFX application
    private Stage stage;
    //container for organising UI elements in window
    private Scene scene;
    //top-level class for handling nodes (UI elements/containers) in JavaFX
    private Parent root;

    //used to access user-related data
    private User user;

    private String quizName;
    //questions from Python program to be used in Quiz
    private ArrayList<ArrayList<String>> questionList;
    //number of correct answers
    private int score = 0;
    //holds all user's answers
    private ArrayList<String> userAnswers = new ArrayList<>();
    //dueDate of a quiz assigned on the schedule page - required to identify it and mark it as complete
    private String dueDate;

    //allows user to return to the page they came from
    private String previousView;

    //flag to ensure scenes switch between pages instead of a new one being created
    private boolean createdInitialScene = false;


    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // ### start code block from MaterialFX library
        //apply CSS to all stages/scenes of the app
        UserAgentBuilder.builder()
                //adds JavaFX default theme
                .themes(JavaFXThemes.MODENA)
                //adds MaterialFX Theme with legacy controls as argument
                .themes(MaterialFXStylesheets.forAssemble(true))
                // Whether to deploy each theme's assets on a temporary dir on the disk
                .setDeploy(true)
                // Whether to try resolving @import statements and resources urls
                .setResolveAssets(true)
                // Assembles all the added themes into a single CSSFragment (very powerful class check its documentation)
                .build()
                // Finally, sets the produced stylesheet as the global User-Agent stylesheet
                .setGlobal();
        // ### end block

        openView("Login");
    }


    /*This method was adapted from code by Mortensen, T.
    * The basic structure is the same - FXMLLoader initialisation and the 2 lines directly beneath the
    * "locate the FXML file and load it" comment
    * as well as the basic idea of switching scenes */
    //used to switch between pages in response to user clicking navigation buttons across the application
    public void openView(String viewToOpen){
        FXMLLoader loader = new FXMLLoader();

        try {
            //locate the FXML file and load it
            loader.setLocation(getClass().getResource(viewToOpen + "View.fxml"));
            root = loader.load();

            /*This switch-case was initially an if-else for each page but IntelliJ suggestion
            * modified it. The basic structure is the same from the source;
            * initialising the relevant controller method for the requested page (string of FXML filename)
            * ExampleController view = loader.getController();
            * view.init(this);
            * Each case has specific lines pertaining to the View made by me
            * The stage.getScene().setRoot(root); are also by me
            * */
            switch (viewToOpen){
                case "Login" -> {
                    LoginController view = loader.getController();
                    view.init(this);
                    /*the scene (window) needs to be created only once
                    * the contents can then be switched out depending on the required*/
                    if (!createdInitialScene){
                        scene = new Scene(root, 1280, 720);
                        stage.setScene(scene);
                        createdInitialScene = true;
                    }
                    else {
                        stage.getScene().setRoot(root);
                    }
                }
                case "Register" -> {
                    RegisterController view = loader.getController();
                    view.init(this);
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
                case "QuizGenerator" -> {
                    QuizGeneratorController view = loader.getController();
                    view.init(this);
                    view.setUser(user);
                    view.initialiseComboBox();
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
                case "Quiz" -> {
                    QuizController view = loader.getController();
                    view.init(this);
                    view.setPreviousView(previousView);
                    view.setQuestionList(questionList);
                    view.beginQuiz();
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
                case "Schedule" -> {
                    ScheduleController view = loader.getController();
                    view.init(this);
                    view.setUser(user);
                    view.setupSchedule();
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
                case "Results" -> {
                    ResultsController view = loader.getController();
                    view.init(this);
                    //send results and related data
                    view.updateQuizName(quizName);
                    view.setQuestionList(questionList);
                    view.setUserAnswers(userAnswers);
                    view.setScore(score);
                    view.setScoreCounter();
                    //The previous page being the schedule means the quiz was taken from the schedule
                    //meaning it should be marked as complete
                    if (previousView.equals("Schedule")){
                        TaskDataAccessor taskDataAccessor = new TaskDataAccessor();
                        taskDataAccessor.completeTaskDB(quizName, dueDate, user);
                    }
                    //change scene to the new View
                    stage.getScene().setRoot(root);
                }
                case "WebsiteBlocker" -> {
                    WebsiteBlockerController view = loader.getController();
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

    public void setUser(User user) {
        this.user = user;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
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

    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public void setPreviousView(String previousView) { this.previousView = previousView; }
}
