package com.example.taskandquizscheduler;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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


public class QuizGeneratorController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private ArrayList<ArrayList<String>> questionList;
    private BufferedWriter bw = null;
    private PrintWriter pw;
    private BufferedReader br = null;
    private ClientController clientController;
    private Socket clientSocket;
    private String quizGenInput;
    private String quizGenOutput;

    @FXML
    private Label webBlockerLabel;

    @FXML
    private Label scheduleLabel;

    @FXML
    private Label quizGenLabel;

    @FXML
    private Label friendsLabel;

    @FXML
    private Button generateButton;

    @FXML
    private Button startQuizButton = new Button();

    @FXML
    private Button saveQuizButton = new Button();

    @FXML
    private ComboBox<String> quizComboBox = new ComboBox<>();

    @FXML
    private TextArea quizGenInputArea;

    @FXML
    private TextArea quizGenOutputArea;

    public QuizGeneratorController() {
        //set up client
        try {
            clientController = new ClientController();
            clientSocket = new Socket("localhost", 3007);

            //set up to write to Python server
            pw = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //disable Start Quiz button initially because the output area will be empty
        startQuizButton.setDisable(true);
        //disable Save Quiz button initially because the output area will be empty
        saveQuizButton.setDisable(true);

    }

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

        //allow scheduleLabel on sidebar to be clicked to show Scheduler page
        scheduleLabel.setOnMousePressed(scheduleHandler);
    }

    //handle mouse event of clicking scheduleLabel
    EventHandler<? super MouseEvent> scheduleHandler = this::scheduleClick;

    @FXML
    protected void scheduleClick(MouseEvent event){
        //go to schedule page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("schedule-view.fxml"));
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


    @FXML
    protected void loadQuizClick(){
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


    @FXML
    protected void generateClick() {
        //retrieve the user inputted text
        quizGenInput = quizGenInputArea.getText();
        //send to ClientSocket to send to Python server
        clientController.setQuizGenInput(quizGenInput);
        clientController.sendInfo(pw);
        //get outputted quiz from ClientSocket from Python server
        quizGenOutput = clientController.retrieveInfo(clientSocket);

        //display string from Python file in quiz generator output area
        quizGenOutputArea.setText(String.valueOf(quizGenOutput));
        //enable save and start quiz buttons because an output will have been generated
        saveQuizButton.setDisable(false);
        startQuizButton.setDisable(false);
    }

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
        System.out.println("0: " + questionList.get(0));
        System.out.println("1: " + questionList.get(1));
        System.out.println("2: " + questionList.get(2));
    }

    @FXML
    protected void saveQuizClick() {
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


    @FXML
    protected void startQuizClick(ActionEvent event) {
        //convert quiz to ArrayList so it can be sent to QuizController to start quiz
        prepareQuiz();

        //start the quiz by showing the quiz page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("quiz-view.fxml"));
            root = loader.load();
            //pass on the question list for the quiz to start
            QuizController quizController = loader.getController();
            quizController.setQuestionList(questionList);
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


}