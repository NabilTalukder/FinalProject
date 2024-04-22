package com.example.taskandquizscheduler;


import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;

public class ResultsController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    //the questions from the quiz the user just completed
    private ArrayList<ArrayList<String>> questionList;
    //question number to iterate through the results
    private int currentQuestion = 0;
    //number of questions user answered correctly
    private int score = 0;
    //used to show if user was correct or not
    private ArrayList<String> userAnswers = new ArrayList<>();

    @FXML
    private Label quizNameLabel;
    @FXML
    private Label questionDesc;

    //options for each question
    @FXML
    private Label option1Label;
    @FXML
    private Label option2Label;
    @FXML
    private Label option3Label;
    @FXML
    private Label option4Label;
    @FXML
    private Label[] optionLabels;

    //the number of questions the user answered correctly in the completed quiz
    @FXML
    private Label scoreCounter;

    @FXML
    private MFXButton nextQuestionButton;
    @FXML
    private MFXButton prevQuestionButton;

    @FXML
    public void initialize(){
        optionLabels = new Label[] {option1Label, option2Label, option3Label, option4Label};

    }

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    public void setScoreCounter(){
        //display current score;-1 because 0th element is the quiz title. Remaining elements are questions
        scoreCounter.setText("Score: " + score + "/" + (questionList.size() - 1));
        //increment question number from 0 to 1, so the results can be shown
        goToNextQuestion();
        //prevent going out of bounds of array indices
        prevQuestionButton.setDisable(true);
    }

    @FXML
    protected void goToWebsiteBlocker(){
        viewHandler.openView("WebsiteBlocker");
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

    @FXML
    protected void goToNextQuestion(){
        currentQuestion += 1;
        updateResults();
        prevQuestionButton.setDisable(false);
        if (currentQuestion == (questionList.size() - 1)){
            nextQuestionButton.setDisable(true);
        }
    }

    @FXML
    protected void goToPrevQuestion(){
        currentQuestion -= 1;
        updateResults();
        nextQuestionButton.setDisable(false);
        if (currentQuestion == 1){
            prevQuestionButton.setDisable(true);
        }
    }

    @FXML
    protected void updateResults(){
        //display the question
        questionDesc.setText(questionList.get(currentQuestion).get(0));
        //display current score. -1 because 0th element is the quiz title. Remaining elements are questions
        scoreCounter.setText("Score: " + score + "/" + (questionList.size() - 1));
        //remove unnecessary String from ChatGPT 3.5 prompt in Python program
        String answer = questionList.get(currentQuestion).get(5).replaceFirst("Answer: ", "");

        String userAnswer = userAnswers.get(currentQuestion);
        for (int i = 0; i < optionLabels.length; i++){
            Label optionLabel = optionLabels[i];
            //label shows corresponding text for the current question (i + 1 because of alignment differences)
            optionLabel.setText(questionList.get(currentQuestion).get(i + 1));
            String optionText = optionLabel.getText();
            String bgStyle = "-fx-background-color: ";

            //highlight answers accordingly
            if (optionText.equals(answer)){
                //correct answer
                bgStyle += "rgb(168, 235, 52)";
            }
            else if (optionText.equals(userAnswer)){
                //incorrect answer
                bgStyle += "rgb(230, 24, 9)";
            }
            else {
                //unselected option that is not the answer
                bgStyle += "rgb(255, 255, 255)";
            }
            optionLabel.setStyle(bgStyle);
        }
    }

    public void updateQuizName(String quizName) {
        quizNameLabel.setText(quizName);
    }

    public void setQuestionList(ArrayList<ArrayList<String>> questionList) {
        this.questionList = questionList;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setUserAnswers(ArrayList<String> userAnswers) {
        this.userAnswers = userAnswers;
    }
}
