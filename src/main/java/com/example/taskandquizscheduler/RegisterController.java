package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Severity;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;
import java.util.regex.Pattern;

import static io.github.palexdev.materialfx.utils.StringUtils.containsAny;

public class RegisterController extends LoginController {

    // ### start code from MaterialFX library
    //validation
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    // ### start AI-generated code, except the regex from StackOverflow
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");
    // ### end AI-generated code

    private static final String[] upperChar = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z".split(" ");
    private static final String[] lowerChar = "a b c d e f g h i j k l m n o p q r s t u v w x y z".split(" ");
    private static final String[] digits = "0 1 2 3 4 5 6 7 8 9".split(" ");
    private static final String[] specialCharacters = "! @ # & ( ) – [ { } ]: ; ' , ? / * ~ $ ^ + = < > - .".split(" ");
    // ### MaterialFX end

    //All code in this method is from MaterialFX library unless otherwise stated to be inspired/modified
    //or fully written by me
    @FXML @Override
    protected void initialize(){
        //Register page has same fields as Login, so they will also need to be initialized
        super.initialize();

        //set constraints on email and password fields so the user inputs valid credentials
        //fields display error message if the user-input is invalid

        // ### start AI-generated code - inspired by the constraints from MaterialFX within this method
        // the output initially said !EMAIL_PATTERN, but I changed it, so it was usable properly
        Constraint formatConstraint = Constraint.Builder.build()
                .setSeverity(Severity.ERROR)
                .setMessage("Invalid email format")
                .setCondition(Bindings.createBooleanBinding(
                        () -> EMAIL_PATTERN.matcher(emailField.getText()).matches(),
                        emailField.textProperty()
                ))
                .get();
        // ### end AI-generated code

        //This was also inspired by the passwordField.getValidator() by MaterialFX further down in this method
        emailField.getValidator()
                .constraint(formatConstraint);
        
        //this line by me
        addValidationListener(emailField, emailValidationLabel);

        Constraint lengthConstraint = Constraint.Builder.build()
                .setSeverity(Severity.ERROR)
                .setMessage("Password must be at least 8 characters long")
                .setCondition(passwordField.textProperty().length().greaterThanOrEqualTo(8))
                .get();

        Constraint digitConstraint = Constraint.Builder.build()
                .setSeverity(Severity.ERROR)
                .setMessage("Password must contain at least one digit")
                .setCondition(Bindings.createBooleanBinding(
                        () -> containsAny(passwordField.getText(), "", digits),
                        passwordField.textProperty()
                ))
                .get();

        Constraint charactersConstraint = Constraint.Builder.build()
                .setSeverity(Severity.ERROR)
                .setMessage("Password must contain at least one capital and at least one lowercase character")
                .setCondition(Bindings.createBooleanBinding(
                        () -> containsAny(passwordField.getText(), "", upperChar) && containsAny(passwordField.getText(), "", lowerChar),
                        passwordField.textProperty()
                ))
                .get();

        Constraint specialCharactersConstraint = Constraint.Builder.build()
                .setSeverity(Severity.ERROR)
                .setMessage("Password must contain at least one special character")
                .setCondition(Bindings.createBooleanBinding(
                        () -> containsAny(passwordField.getText(), "", specialCharacters),
                        passwordField.textProperty()
                ))
                .get();

        passwordField.getValidator()
                .constraint(digitConstraint)
                .constraint(charactersConstraint)
                .constraint(specialCharactersConstraint)
                .constraint(lengthConstraint);

        //this line by me
        addValidationListener(passwordField, passwordValidationLabel);

    }


    /*This method was originally part of the intialize() method from MaterialFX library but
    * adapted by me to become a separate method so it can be used for email and password fields*/
    //adds listener for email and password fields to validate them
    @FXML
    private void addValidationListener(MFXTextField textField, Label validationLabel) {
        textField.getValidator().validProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                validationLabel.setVisible(false);
                textField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
            }
        });
    }

    /*This method was originally part of the intialize() method from MaterialFX library but
     * adapted by me to become a separate method so it can be used for email and password fields*/
    //checks changes (new user-entered text) against constraints
    @FXML
    private void validateField(MFXTextField textField, Label validationLabel) {
        List<Constraint> constraints = textField.validate();
        if (!constraints.isEmpty()) {
            //invalid pseudo class is CSS style to make fields appear red upon invalid input
            textField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
            //the 0th element is the constraint that is at the front of the list
            //only one error message can be shown at once
            validationLabel.setText(constraints.get(0).getMessage());
            validationLabel.setVisible(true);
        }
    }

    @FXML
    protected void goToLogin(){
        viewHandler.openView("Login");
    }

    @FXML @Override
    protected void validateDetails() {

        validateField(emailField, emailValidationLabel);
        validateField(passwordField, passwordValidationLabel);

        //create account if fields entered and no errors
        if (!(email.isBlank()) && !(password.isBlank()) && !(passwordValidationLabel.isVisible()) &&
                !(emailValidationLabel.isVisible())){

            userDataAccessor.createAccountDB(email, password);
            String retrievedUserID = userDataAccessor.validatePasswordDB(email, password);
            User loggedInUser = new User(retrievedUserID);
            viewHandler.setUser(loggedInUser);
            viewHandler.openView("Schedule");
        }
    }
}
