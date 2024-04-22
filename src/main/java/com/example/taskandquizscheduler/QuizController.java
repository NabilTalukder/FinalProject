package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.ArrayList;

//handles the quiz process by showing a question and options for the user to select
public class QuizController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    //the questions generated and received from the Python program
    private ArrayList<ArrayList<String>> questionList;
    //question number to iterate through the quiz
    private int currentQuestion = 0;
    //number of questions user answered correctly
    private int score = 0;
    //the answer of the current question being displayed
    private String answer;
    //holds all user's answers
    private ArrayList<String> userAnswers = new ArrayList<>();
    //allows user to return to the page they came from
    private String previousView;

    //header showing the name of the quiz
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
    private MFXProgressBar quizProgress;
    //buttons for user to choose an answer
    @FXML
    private MFXRectangleToggleNode option1Button;
    @FXML
    private MFXRectangleToggleNode option2Button;
    @FXML
    private MFXRectangleToggleNode option3Button;
    @FXML
    private MFXRectangleToggleNode option4Button;


    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    //returns to previous page (Schedule or Quiz Generator) for QoL
    @FXML
    protected void exitQuiz(){
        viewHandler.openView(previousView);
    }

    @FXML
    protected void logout() {
        viewHandler.setUser(null);
        viewHandler.openView("Login");
    }

    //submit and compare user answer to actual answer
    @FXML
    protected void clickOption(){
        //get the text corresponding to the selected button
        if (option1Button.isSelected()){
            userAnswers.add(option1Button.getText());
        }
        else if (option2Button.isSelected()) {
            userAnswers.add(option2Button.getText());
        }
        else if (option3Button.isSelected()) {
            userAnswers.add(option3Button.getText());
        }
        else if (option4Button.isSelected()) {
            userAnswers.add(option4Button.getText());
        }

        if (userAnswers.get(currentQuestion).equals(answer)){
            score += 1;
        }

        //reset "selected" state of buttons to prepare for next question
        option1Button.setSelected(false);
        option2Button.setSelected(false);
        option3Button.setSelected(false);
        option4Button.setSelected(false);

        //cycle through questions as long as there are some left
        //-1 because 0th element is quiz title
        if (currentQuestion < questionList.size() - 1){
            nextQuestion();
        }
        //end of quiz - go to results
        else {
            viewHandler.setQuestionList(questionList);
            viewHandler.setScore(score);
            viewHandler.setUserAnswers(userAnswers);
            viewHandler.openView("Results");
        }
    }

    private void nextQuestion(){
        currentQuestion += 1;
        //update progress bar to represent percentage of questions
        //-1 because 0th element is the quiz title. Remaining elements are questions
        quizProgress.setProgress((float)currentQuestion / (float)(questionList.size() - 1));
        //set title of quiz to let the user know that they have selected the quiz they intended
        quizTitle.setText(questionList.get(0).get(0));
        //display the question description
        questionDesc.setText(questionList.get(currentQuestion).get(0));
        //display current score. -1 because 0th element is the quiz title. Remaining elements are questions
        scoreCounter.setText("Score: " + score + "/" + (questionList.size() - 1));
        //make the buttons show the possible answers / options
        option1Button.setText(questionList.get(currentQuestion).get(1));
        option2Button.setText(questionList.get(currentQuestion).get(2));
        option3Button.setText(questionList.get(currentQuestion).get(3));
        option4Button.setText(questionList.get(currentQuestion).get(4));
        //set the correct answer for the current question
        //remove "Answer: " for easier comparison. It was added for the GPT-3.5 prompt in Python
        answer = questionList.get(currentQuestion).get(5).replaceFirst("Answer: ", "");
    }

    public void beginQuiz(){
        //set progress bar to 0 initially
        quizProgress.setProgress(0);
        userAnswers.add("0th value to align currentQuestion with user answer");
        //increment question number from 0 to 1, so the quiz can begin
        nextQuestion();
    }

    public ArrayList<ArrayList<String>> getQuestionList() {
        return questionList;
    }

    //set up retrieved questions to start quiz
    public void setQuestionList(ArrayList<ArrayList<String>> questionList) {
        this.questionList = questionList;
    }

    public void setPreviousView(String previousView) { this.previousView = previousView; }
}
