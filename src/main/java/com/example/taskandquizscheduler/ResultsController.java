package com.example.taskandquizscheduler;

import com.jfoenix.controls.JFXButton;
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

    //sidebar labels to navigate to the main screens of the application
    @FXML
    private Label friendsLabel;
    @FXML
    private Label quizGenLabel;
    @FXML
    private Label scheduleLabel;
    @FXML
    private Label webBlockerLabel;


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

    @FXML
    private Label scoreCounter;

    @FXML
    private JFXButton nextQuestionButton;
    @FXML
    private JFXButton prevQuestionButton;

    @FXML
    public void initialize(){
        optionLabels = new Label[] {option1Label, option2Label, option3Label, option4Label};
    }

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    protected void setScoreCounter(){
        //display current score;-1 because 0th element is the quiz title. Remaining elements are questions
        scoreCounter.setText("Score: " + score + "/" + (questionList.size() - 1));
        //increment question number from 0 to 1, so the results can be shown
        clickNextQuestion();
    }

    //transition to schedule page
    @FXML
    protected void clickSchedule(){
        viewHandler.openView("Schedule");
    }

    //transition to quiz generator page
    @FXML
    protected void clickQuizGenerator(){
        viewHandler.openView("QuizGenerator");
    }

    @FXML
    protected void clickNextQuestion(){
        if (currentQuestion < (questionList.size() - 1)){
            currentQuestion += 1;
            updateResults();
            prevQuestionButton.setDisable(false);
        }
        else {
            nextQuestionButton.setDisable(true);
        }

    }

    @FXML
    protected void clickPrevQuestion(){
        if (currentQuestion > 1){
            currentQuestion -= 1;
            updateResults();
            nextQuestionButton.setDisable(false);
        }
        else {
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


    public void setQuestionList(ArrayList<ArrayList<String>> questionList) {
        this.questionList = questionList;
    }

    public void setScore(int score) {
        this.score = score;
        setScoreCounter();
    }

    public void setUserAnswers(ArrayList<String> userAnswers) {
        this.userAnswers = userAnswers;
    }
}