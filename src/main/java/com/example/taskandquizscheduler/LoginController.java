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

    //queries database to check credentials
    protected UserDataAccessor userDataAccessor = new UserDataAccessor();

    @FXML
    protected MFXTextField emailField;
    @FXML
    protected MFXPasswordField passwordField;
    @FXML
    protected MFXButton confirmButton;

    //link that allows user to switch between login or register pages
    @FXML
    protected Hyperlink signInOrRegisterLink;

    //show error messages for the email and password fields
    @FXML
    protected Label emailValidationLabel;
    @FXML
    protected Label passwordValidationLabel;


    @FXML
    protected void initialize(){
        //validation labels should be hidden by default
        emailValidationLabel.setTextFill(Paint.valueOf("red"));
        passwordValidationLabel.setTextFill(Paint.valueOf("red"));
        emailValidationLabel.setVisible(false);
        passwordValidationLabel.setVisible(false);


        //allow ENTER key to be pressed to confirmDetails at any stage of inputting fields

        emailField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                confirmDetails();
            }
        });

        passwordField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                confirmDetails();
            }
        });

        confirmButton.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                confirmDetails();
            }
        });
    }

    //This method was reused from Mortensen, T.
    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    protected void goToRegister(){
        viewHandler.openView("Register");
    }

    @FXML
    protected void confirmDetails() {
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
            //check if email exists in database
            boolean validEmail = userDataAccessor.validateEmailDB(email);
            if (!validEmail){
                emailValidationLabel.setVisible(true);
                emailValidationLabel.setText("Email not found");
                emailField.setStyle("-fx-border-color: -mfx-red");
            }
            else {
                emailValidationLabel.setVisible(false);
                emailField.setStyle("-fx-border-color: -mfx-purple");
                //retrieve userID for future data access (if password is correct)
                String retrievedUserID = userDataAccessor.validatePasswordDB(email, password);
                if (retrievedUserID.isBlank()) {
                    passwordValidationLabel.setVisible(true);
                    passwordValidationLabel.setText("Wrong password. Please try again");
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

    @FXML
    protected void validateBlankFields(){
        if (email.isBlank()){
            emailValidationLabel.setVisible(true);
            emailValidationLabel.setText("Please enter email");
            emailField.setStyle("-fx-border-color: -mfx-red");
        }
        else {
            emailValidationLabel.setVisible(false);
            emailField.setStyle("-fx-border-color: -mfx-purple");
        }
        if (password.isBlank()){
            passwordValidationLabel.setVisible(true);
            passwordValidationLabel.setText("Please enter password");
            passwordField.setStyle("-fx-border-color: -mfx-red");
        }
        else {
            passwordValidationLabel.setVisible(false);
            passwordField.setStyle("-fx-border-color: -mfx-purple");
        }
    }

}
