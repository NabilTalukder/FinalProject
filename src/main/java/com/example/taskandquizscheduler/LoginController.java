package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;

public class LoginController{

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    private String email;
    private String password;

    private MouseEvent lastClick;
    //queries database for users
    private UserDataAccessor userDataAccessor = new UserDataAccessor();

    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private MFXButton signInButton;
    @FXML
    private MFXButton registerButton;
    @FXML
    private Label emailValidation;
    @FXML
    private Label passwordValidation;


    @FXML
    protected void initialize(){
        emailValidation.setTextFill(Paint.valueOf("red"));
        passwordValidation.setTextFill(Paint.valueOf("red"));
        emailValidation.setVisible(false);
        passwordValidation.setVisible(false);

        emailField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                login();
            }
        });

        passwordField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                login();
            }
        });

        signInButton.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                login();
            }
        });
    }

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }


    @FXML
    protected void login() {
        email = emailField.getText();
        password = passwordField.getText();
        validateDetails();
    }

    private void validateDetails(){
        if (email.isBlank() || password.isBlank()){
            if (email.isBlank()){
                emailValidation.setVisible(true);
                emailValidation.setText("Please enter email");
                emailField.setStyle("-fx-border-color: -mfx-red");
            }
            else {
                emailValidation.setVisible(false);
                emailField.setStyle("-fx-border-color: -mfx-purple");
            }
            if (password.isBlank()){
                passwordValidation.setVisible(true);
                passwordValidation.setText("Please enter password");
                passwordField.setStyle("-fx-border-color: -mfx-red");
            }
            else {
                passwordValidation.setVisible(false);
                passwordField.setStyle("-fx-border-color: -mfx-purple");
            }
        }
        else {
            //should query for user_ID and email
            //if valid, return user_ID (will need to change these booleans)
            //use user_ID in new User class which should be used by ScheduleController to get relevant data
            boolean validEmail = userDataAccessor.validateEmailDB(email);
            if (!validEmail){
                emailValidation.setVisible(true);
                emailValidation.setText("Email not found");
                emailField.setStyle("-fx-border-color: -mfx-red");
            }
            else {
                emailValidation.setVisible(false);
                emailField.setStyle("-fx-border-color: -mfx-purple");
                boolean validPassword = userDataAccessor.validatePasswordDB(email, password);
                if (!validPassword) {
                    passwordValidation.setVisible(true);
                    passwordValidation.setText("Wrong password. Please try again");
                    passwordField.setStyle("-fx-border-color: -mfx-red");
                }
                else {
                    viewHandler.openView("QuizGenerator");
                }
            }
        }
    }

}
