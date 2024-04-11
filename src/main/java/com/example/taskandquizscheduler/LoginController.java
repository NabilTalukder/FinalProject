package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Paint;

public class LoginController{

    //used to switch between scenes/pages
    protected ViewHandler viewHandler;

    protected String email;
    protected String password;

    //queries database for users
    private UserDataAccessor userDataAccessor = new UserDataAccessor();

    @FXML
    protected MFXTextField emailField;
    @FXML
    protected MFXPasswordField passwordField;
    @FXML
    protected Hyperlink forgotPasswordLink;
    @FXML
    protected MFXButton confirmButton;
    @FXML
    protected Hyperlink signInRegisterLink;
    @FXML
    protected Label emailValidation;
    @FXML
    protected Label passwordValidation;


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

        confirmButton.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                login();
            }
        });
    }

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    protected void goToRegister(){
        viewHandler.openView("Register");
    }


    @FXML
    protected void login() {
        email = emailField.getText();
        password = passwordField.getText();
        validateDetails();
    }

    @FXML
    protected void validateDetails(){
        if (email.isBlank() || password.isBlank()){
            validateBlankFields();
        }
        else {
            boolean validEmail = userDataAccessor.validateEmailDB(email);
            if (!validEmail){
                emailValidation.setVisible(true);
                emailValidation.setText("Email not found");
                emailField.setStyle("-fx-border-color: -mfx-red");
            }
            else {
                emailValidation.setVisible(false);
                emailField.setStyle("-fx-border-color: -mfx-purple");
                String retrievedUserID = userDataAccessor.validatePasswordDB(email, password);
                if (retrievedUserID.isBlank()) {
                    passwordValidation.setVisible(true);
                    passwordValidation.setText("Wrong password. Please try again");
                    passwordField.setStyle("-fx-border-color: -mfx-red");
                }
                else {
                    User loggedInUser = new User(retrievedUserID);
                    viewHandler.setUser(loggedInUser);
                    viewHandler.openView("Schedule");
                }
            }
        }
    }

    protected void validateBlankFields(){
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

}
