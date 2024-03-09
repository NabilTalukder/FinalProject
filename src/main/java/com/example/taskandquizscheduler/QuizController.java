package com.example.taskandquizscheduler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.util.ArrayList;

public class QuizController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private ArrayList<ArrayList<String>> questionList;
    private int currentQuestion = 0;
    private int score = 0;
    private String answer;
    private String userAnswer;

    @FXML
    private Label quizTitle;

    @FXML
    private Label questionDesc;

    @FXML
    private Label scoreCounter;

    @FXML
    private ProgressBar quizProgress;

    @FXML
    private ToggleButton option1Button;

    @FXML
    private ToggleButton option2Button;

    @FXML
    private ToggleButton option3Button;

    @FXML
    private ToggleButton option4Button;

    public QuizController(){
    }

    @FXML
    protected void optionClick(){
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

    private void nextQuestion(){
        //increment to go to next question
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

    public void setQuestionList(ArrayList<ArrayList<String>> questionList) {
        this.questionList = questionList;
        //set progress bar to 0 initially
        quizProgress.setProgress(0);
        //increment question number from 0 to 1, so the quiz can begin
        nextQuestion();
    }
}
