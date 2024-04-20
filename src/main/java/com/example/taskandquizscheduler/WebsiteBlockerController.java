package com.example.taskandquizscheduler;

import javafx.fxml.FXML;

public class WebsiteBlockerController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    protected void goToSchedule(){
        viewHandler.openView("Schedule");
    }

    @FXML
    protected void goToQuizGenerator(){
        viewHandler.openView("QuizGenerator");
    }

    @FXML
    protected void logout() {
        viewHandler.setUser(null);
        viewHandler.openView("Login");
    }
}
