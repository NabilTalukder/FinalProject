package com.example.taskandquizscheduler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

//handles the quiz process by showing a question and options for the user to select
public class QuizExecutor {

    //main window of JavaFX application
    private Stage stage;
    //container for organising UI elements in window
    private Scene scene;
    //top-level class for handling nodes (UI elements/containers) in JavaFX
    private Parent root;
    //the questions generated and received from the Python program
    private ArrayList<ArrayList<String>> questionList;
    //question number to iterate through the quiz
    private int currentQuestion = 0;
    //number of questions user answered correctly
    private int score = 0;
    //the answer of the current question being displayed
    private String answer;
    //the option the user selected as their answer
    private String userAnswer;


    @FXML
    private Label exitQuizLabel;
    //header showing the type of quiz
    @FXML
    private Label quizTitle;
    //the actual text for the question being shown
    @FXML
    private Label questionDesc;
    //displays score out of number of questions
    @FXML
    private Label scoreCounter;
    //shows number of questions completed as a bar
    @FXML
    private ProgressBar quizProgress;
    //buttons for user to choose an answer
    @FXML
    private ToggleButton option1Button;
    @FXML
    private ToggleButton option2Button;
    @FXML
    private ToggleButton option3Button;
    @FXML
    private ToggleButton option4Button;

    public QuizExecutor(){
    }

    //returns to previous screen
    @FXML
    protected void clickExitQUiz(MouseEvent event){
        try {
            //get FXML file for quiz generator page and display
            FXMLLoader loader = new FXMLLoader(getClass().getResource("quiz-gen-view.fxml"));
            root = loader.load();
            //scene transition
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Task and Quiz Scheduler");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //submit and compare user answer to actual answer
    @FXML
    protected void clickOption(){
        //get the text corresponding to the selected button
        if (option1Button.isSelected()){
            userAnswer = option1Button.getText();
        }
        else if (option2Button.isSelected()) {
            userAnswer = option2Button.getText();
        }
        else if (option3Button.isSelected()) {
            userAnswer = option3Button.getText();
        }
        else if (option4Button.isSelected()) {
            userAnswer = option4Button.getText();
        }
        //if the selected button's text matches the answer, it's correct
        //remove "Answer: " for easier comparison. It was added for the GPT-3.5 prompt in Python
        if (userAnswer.equals(answer.replaceFirst("Answer: ", ""))){
            score += 1;
            System.out.println("score: " + score);
        }
        else {
            System.out.println("incorrect");
        }
        //reset "selected" state of buttons to prepare for next question
        option1Button.setSelected(false);
        option2Button.setSelected(false);
        option3Button.setSelected(false);
        option4Button.setSelected(false);
        nextQuestion();
    }

    //iterate through the questions
    private void nextQuestion(){
        currentQuestion += 1;
        System.out.println("current question: " + currentQuestion);
        //update progress bar to represent percentage of questions
        //-1 because 0th element is the quiz title. Remaining elements are questions
        quizProgress.setProgress((float)currentQuestion / (float)(questionList.size() - 1));
        //set title of quiz to let the user know it has started
        quizTitle.setText(questionList.get(0).get(0));
        //display the question
        questionDesc.setText(questionList.get(currentQuestion).get(0));
        //display current score. -1 because 0th element is the quiz title. Remaining elements are questions
        scoreCounter.setText("Score: " + score + "/" + (questionList.size() - 1));
        //make the buttons show the possible answers / options
        option1Button.setText(questionList.get(currentQuestion).get(1));
        option2Button.setText(questionList.get(currentQuestion).get(2));
        option3Button.setText(questionList.get(currentQuestion).get(3));
        option4Button.setText(questionList.get(currentQuestion).get(4));
        //set the correct answer for the current question
        answer = questionList.get(currentQuestion).get(5);
    }

    public ArrayList<ArrayList<String>> getQuestionList() {
        return questionList;
    }

    //set up retrieved questions to start quiz
    public void setQuestionList(ArrayList<ArrayList<String>> questionList) {
        this.questionList = questionList;
        //set progress bar to 0 initially
        quizProgress.setProgress(0);
        //increment question number from 0 to 1, so the quiz can begin
        nextQuestion();
    }
}
