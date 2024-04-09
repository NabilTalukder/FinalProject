package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class LoginController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    @FXML
    private MFXTextField emailTextField;
    @FXML
    private MFXPasswordField passwordTextField;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private MFXButton loginButton;

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    protected void clickLogin() {
        viewHandler.openView("QuizGenerator");
    }

}
