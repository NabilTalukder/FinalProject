package com.example.taskandquizscheduler;


import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

//handles loading and generating quizzes from user input
public class QuizGenerator2Controller {

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
    //input from the user to be used in the prompt to generate a quiz
    private String quizGenInput;
    //the output of the prompt (generated quiz)
    private String quizGenOutput;
    private String quizName = "Multiple Choice Quiz";

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

    @FXML
    private MFXButton generateQuizButton = new MFXButton();
    @FXML
    private MFXButton startQuizButton = new MFXButton();
    @FXML
    private MFXButton saveQuizButton = new MFXButton();
    //holds saved quizzes
    @FXML
    private MFXFilterComboBox<Quiz> loadQuizComboBox = new MFXFilterComboBox<>();
    //input area for user to enter text to be used in the prompt to generate a quiz
    @FXML
    private TextArea quizGenInputArea;

    //set up initial UI layout
    public QuizGenerator2Controller() {
        //set up client
//        try {
//            clientController = new ClientController();
//            clientSocket = new Socket("localhost", 3007);
//
//            //set up to write to Python program
//            pw = new PrintWriter(clientSocket.getOutputStream(), true);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //disable Start Quiz button initially because the output area will be empty
        startQuizButton.setDisable(true);
        //disable Save Quiz button initially because the output area will be empty
        saveQuizButton.setDisable(true);

    }

    public void init(ViewHandler viewhandler){
        this.viewHandler = viewhandler;
    }

    @FXML
    public void initialize(){
        quizGenInputArea.setPromptText("Enter or paste text here. The generated quiz will appear on the right");

        //set up loadQuizComboBox by accessing directory
        File directory = new File("C:\\Users\\Nabil\\IdeaProjects\\TaskAndQuizScheduler\\data\\Saved_Quiz");
        // List all files in the directory
        File[] files = directory.listFiles();

        ObservableList<Quiz> loadedQuizzes = FXCollections.observableArrayList();
        for (File file : files) {
            loadedQuizzes.add(new Quiz(file.getName()));
        }
        //convert Quiz objects to Strings to be displayed in loadQuizComboBox
        StringConverter<Quiz> converter = FunctionalStringConverter.to(quiz -> (quiz == null) ? "" : quiz.getQuizName());
        //filter Quiz objects that match what was selected
        Function<String, Predicate<Quiz>> filterFunction = s -> quiz -> StringUtils.containsIgnoreCase(converter.toString(quiz), s);
        loadQuizComboBox.setItems(loadedQuizzes);
        loadQuizComboBox.setConverter(converter);
        loadQuizComboBox.setFilterFunction(filterFunction);

        generateQuizButton.setDisable(true);
    }

    @FXML
    protected void goToSchedule(){
        viewHandler.openView("Schedule");
    }

    @FXML
    protected void logout() {
        viewHandler.setUser(null);
        viewHandler.openView("Login");
    }


    @FXML
    protected void startQuiz() {
        prepareQuiz();
        viewHandler.setQuizName(quizName);
        viewHandler.setQuestionList(questionList);
        viewHandler.openView("Quiz");
    }

    @FXML
    protected void loadQuiz(){
        //load quiz - get file name of selected quiz
        quizName = loadQuizComboBox.getText();
        System.out.println(quizName);
//        String selectedQuiz = "data/Saved_Quiz/" + quizName;
//
//        //read selected quiz text file
//        String line;
//        StringBuilder sb = new StringBuilder();
//        try {
//            br = new BufferedReader(new FileReader(selectedQuiz));
//            while ((line = br.readLine()) != null) {
//                //append \n so the output preserves line breaks from the original file
//                sb.append(line).append("\n");
//            }
//            br.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        convert to string so it can be displayed
//        quizGenOutput = sb.toString();
//        quizGenOutputArea.setText(quizGenOutput);
//        //enable save and start quiz buttons because an output will have been generated
//        saveQuizButton.setDisable(false);
//        startQuizButton.setDisable(false);

    }

    //sends the user input text as a prompt to the Python program to generate and display a quiz
    @FXML
    protected void generateQuiz() {
        //retrieve the user inputted text
        quizGenInput = quizGenInputArea.getText();
        //send to ClientSocket to send to Python program
        clientController.setQuizGenInput(quizGenInput);
        clientController.sendInfo(pw);
        //get outputted quiz from ClientSocket from Python program
        quizGenOutput = clientController.retrieveInfo(clientSocket);

//        //display string from Python file in quiz generator output area
//        quizGenOutputArea.setText(String.valueOf(quizGenOutput));
//        //enable save and start quiz buttons because an output will have been generated
//        saveQuizButton.setDisable(false);
//        startQuizButton.setDisable(false);
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

    @FXML
    protected void saveQuiz() {
//        String quizToSave = quizGenOutputArea.getText();
//        String fileName = "data\\Saved_Quiz\\Saved_Quiz.txt";
//
//        //change to type File so the while loop below can check if it exists
//        File f = new File(fileName);
//        int fileNumber = 1;
//        //if file exists, add a number to the name, repeat until file doesn't exist
//        while (f.isFile()){
//            //increment for the next cycle of the loop
//            fileNumber++;
//            //change name of file because its previously assigned name already exists
//            fileName = "data\\Saved_Quiz\\Saved_Quiz" + fileNumber + ".txt";
//            f = new File(fileName);
//        }
//
//        //save outputted quiz to text file
//        try {
//            bw = new BufferedWriter(new FileWriter(fileName));
//            bw.write(quizToSave);
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}