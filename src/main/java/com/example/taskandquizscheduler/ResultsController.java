package com.example.taskandquizscheduler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class ResultsController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    //sidebar labels to navigate to the main screens of the application
    @FXML
    private Label friendsLabel;
    @FXML
    private Label quizGenLabel;
    @FXML
    private Label scheduleLabel;
    @FXML
    private Label webBlockerLabel;

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    //process for clicking schedule sidebar button
    @FXML
    protected void clickSchedule(MouseEvent event){
        viewHandler.openView("Schedule");
    }

    //transition to quiz generator page
    @FXML
    protected void clickQuizGenerator(MouseEvent event){
        viewHandler.openView("QuizGenerator");
    }
}
