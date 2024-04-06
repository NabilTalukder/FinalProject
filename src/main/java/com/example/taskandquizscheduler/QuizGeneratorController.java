package com.example.taskandquizscheduler;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

//handles loading and generating quizzes from user input
public class QuizGeneratorController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    //main window of JavaFX application
    private Stage stage;
    //container for organising UI elements in window
    private Scene scene;
    //top-level class for handling nodes (UI elements/containers) in JavaFX
    private Parent root;
    //the questions generated and received from the Python program
    private ArrayList<ArrayList<String>> questionList;
    //used for saving generated quiz outputs
    private BufferedWriter bw = null;
    //used for writing to Python server
    private PrintWriter pw;
    //used for retrieving previously generated quizzes
    private BufferedReader br = null;
    //used for connecting to and requesting from Python program for generating quizzes
    private ClientController clientController;
    //used for enabling client-server communication to Python program
    private Socket clientSocket;
    //input from the user to be used in the prompt to generate a quiz
    private String quizGenInput;
    //the output of the prompt (generated quiz)
    private String quizGenOutput;

    //sidebar labels to navigate to the main screens of the application
    @FXML
    private Label webBlockerLabel;
    @FXML
    private Label scheduleLabel;
    @FXML
    private Label quizGenLabel;
    @FXML
    private Label friendsLabel;

    //button to generate a quiz
    @FXML
    private Button generateButton;
    //button to take the quiz that was loaded or generated
    @FXML
    private Button startQuizButton = new Button();
    //button to save the generated or edited quiz, so it can be accessed later
    @FXML
    private Button saveQuizButton = new Button();
    //holds saved quizzes
    @FXML
    private ComboBox<String> quizComboBox = new ComboBox<>();
    //input area for user to enter text to be used in the prompt to generate a quiz
    @FXML
    private TextArea quizGenInputArea;
    //output area showing the generated or loaded quiz that can be edited
    @FXML
    private TextArea quizGenOutputArea;

    //set up initial UI layout
    public QuizGeneratorController() {
        //set up client
        try {
            clientController = new ClientController();
            clientSocket = new Socket("localhost", 3007);

            //set up to write to Python program
            pw = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //disable Start Quiz button initially because the output area will be empty
        startQuizButton.setDisable(true);
        //disable Save Quiz button initially because the output area will be empty
        saveQuizButton.setDisable(true);

    }

    public void init(ViewHandler viewhandler){
        this.viewHandler = viewhandler;
    }

    //set up initial UI behaviours
    @FXML
    public void initialize(){
        //retrieve previously saved quizzes and insert into quizComboBox

        //set up quiz combo box by accessing directory
        File directory = new File("C:\\Users\\Nabil\\IdeaProjects\\TaskAndQuizScheduler\\data\\Saved_Quiz");
        // List all files in the directory
        File[] files = directory.listFiles();

        //add file names to list
        for (File file : files) {
            quizComboBox.getItems().add(file.getName());
        }
    }

    //process for clicking schedule sidebar button
    @FXML
    protected void clickSchedule(MouseEvent event){
        viewHandler.openView("Schedule");
    }

    //starts the quiz process by transitioning to the quiz page
    @FXML
    protected void clickStartQuiz(ActionEvent event) {
        prepareQuiz();
        viewHandler.setQuestionList(questionList);
        viewHandler.openView("Quiz");
    }


    //upon selecting a previously saved quiz, it is retrieved
    @FXML
    protected void clickLoadQuiz(){
        //load quiz - get file name of selected quiz
        String selectedQuiz = "data/Saved_Quiz/" + quizComboBox.getValue();

        //read selected quiz text file
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(selectedQuiz));
            while ((line = br.readLine()) != null) {
                //append \n so the output preserves line breaks from the original file
                sb.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //convert to string so it can be displayed
        quizGenOutput = sb.toString();
        quizGenOutputArea.setText(quizGenOutput);
        //enable save and start quiz buttons because an output will have been generated
        saveQuizButton.setDisable(false);
        startQuizButton.setDisable(false);

    }

    //sends the user input text as a prompt to the Python program to generate and display a quiz
    @FXML
    protected void clickGenerate() {
        //retrieve the user inputted text
        quizGenInput = quizGenInputArea.getText();
        //send to ClientSocket to send to Python program
        clientController.setQuizGenInput(quizGenInput);
        clientController.sendInfo(pw);
        //get outputted quiz from ClientSocket from Python program
        quizGenOutput = clientController.retrieveInfo(clientSocket);

        //display string from Python file in quiz generator output area
        quizGenOutputArea.setText(String.valueOf(quizGenOutput));
        //enable save and start quiz buttons because an output will have been generated
        saveQuizButton.setDisable(false);
        startQuizButton.setDisable(false);
    }

    //formats the retrieved quiz, so it can be used by QuizController to start the quiz process
    public void prepareQuiz(){
        //split up the string of questions into separate strings, using # as delimiter
        String[] questions = quizGenOutput.split("#");
        //list of all questions
        questionList = new ArrayList<>();
        //used as a counter to add the relevant information to the corresponding ArrayList
        int questionNumber = 0;
        //for every question, split up into lines as [question, 1, 2, 3, 4, answer]
        for (String question : questions){
            //split based on new lines
            String[] splitQuestion = question.split("\n");
            //add new ArrayList to hold new question and its contents
            questionList.add(new ArrayList<>());
            System.out.println(questionList.size());
            //add corresponding question info to the ArrayList
            for (String line : splitQuestion){
                questionList.get(questionNumber).add(line);
            }
            //increment to make room for next question
            questionNumber += 1;

        }
    }

    //stores generated or edited quiz, so it can be accessed later
    @FXML
    protected void clickSaveQuiz() {
        String quizToSave = quizGenOutputArea.getText();
        String fileName = "data\\Saved_Quiz\\Saved_Quiz.txt";

        //change to type File so the while loop below can check if it exists
        File f = new File(fileName);
        int fileNumber = 1;
        //if file exists, add a number to the name, repeat until file doesn't exist
        while (f.isFile()){
            //increment for the next cycle of the loop
            fileNumber++;
            //change name of file because its previously assigned name already exists
            fileName = "data\\Saved_Quiz\\Saved_Quiz" + fileNumber + ".txt";
            f = new File(fileName);
        }

        //save outputted quiz to text file
        try {
            bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(quizToSave);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}