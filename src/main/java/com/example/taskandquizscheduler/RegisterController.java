package com.example.taskandquizscheduler;

import javafx.fxml.FXML;

public class RegisterController extends LoginController {

    //initialize, init, login are same

    @FXML
    protected void goToLogin(){
        viewHandler.openView("Login");
    }

    @Override
    protected void validateDetails() {
        if (email.isBlank() || password.isBlank()){
            validateBlankFields();
        }
        else {
            //validate email is correct structure
            //validate password security constraints
        }
    }
}
