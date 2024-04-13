package com.example.taskandquizscheduler;


import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXRadioButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    //question number to iterate through the questions in the output area
    private int currentQuestion = 0;
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
    @FXML
    private MFXTextField questionDescField;

    @FXML
    private MFXTextField option1Field;
    @FXML
    private MFXTextField option2Field;
    @FXML
    private MFXTextField option3Field;
    @FXML
    private MFXTextField option4Field;
    @FXML
    private MFXTextField[] optionFields;

    @FXML
    private MFXRadioButton option1Radio;
    @FXML
    private MFXRadioButton option2Radio;
    @FXML
    private MFXRadioButton option3Radio;
    @FXML
    private MFXRadioButton option4Radio;
    @FXML
    private MFXRadioButton[] optionRadios;

    @FXML
    private MFXButton nextQuestionButton;
    @FXML
    private MFXButton prevQuestionButton;


    public QuizGenerator2Controller() {
        try {
            clientController = new ClientController();
            clientSocket = new Socket("localhost", 3007);

            //set up to write to Python program
            pw = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
//        clientController = this.viewHandler.getClientController();
//        clientSocket = this.viewHandler.getClientSocket();
//        pw = this.viewHandler.getPw();
    }

    @FXML
    public void initialize(){
        quizGenInputArea.setPromptText("Enter or paste text here. " +
                "\nYou can then edit the generated quiz's questions and answers on the right");
        initialiseComboBox();
        initialiseTextFields();
        disableButtons();
    }

    @FXML
    protected void initialiseComboBox(){
        //set up loadQuizComboBox by accessing directory
        File directory = new File("C:\\Users\\Nabil\\IdeaProjects\\FinalProject\\data\\Saved_Quiz");
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
    }

    @FXML
    protected void initialiseTextFields(){
        optionFields = new MFXTextField[] {option1Field, option2Field, option3Field, option4Field};
        optionRadios = new MFXRadioButton[] {option1Radio, option2Radio, option3Radio, option4Radio};

        // Add event listeners to text fields and radio buttons to save edits
        questionDescField.textProperty().addListener((obs, oldText, newText) -> saveOptionEdits(questionDescField, 0));
        for (int i = 0; i < optionFields.length; i++) {
            final int index = i + 1; // To capture the correct index in the lambda expression
            optionFields[i].textProperty().addListener((obs, oldText, newText)
                    -> saveOptionEdits(optionFields[index - 1], index));
            optionRadios[i].setOnAction(e -> saveAnswerEdit(optionFields[index - 1]));
        }
    }

    @FXML
    protected void disableButtons(){
        //should only be enabled once there's text provided as input
        generateQuizButton.setDisable(true);
        quizGenInputArea.textProperty().addListener((obs, oldText, newText) -> toggleGenerateQuizButton());
        //should only be enabled when a quiz has been loaded or generated
        saveQuizButton.setDisable(true);
        startQuizButton.setDisable(true);
        prevQuestionButton.setDisable(true);
        nextQuestionButton.setDisable(true);
    }

    @FXML
    private void toggleGenerateQuizButton(){
        if (quizGenInputArea.getText().isBlank()){
            generateQuizButton.setDisable(true);
        }
        else {
            generateQuizButton.setDisable(false);
        }
    }

    @FXML
    protected void goToSchedule(){
        pw.close();
        viewHandler.openView("Schedule");
    }

    @FXML
    protected void logout() {
        pw.close();
        viewHandler.setUser(null);
        viewHandler.openView("Login");
    }


    @FXML
    protected void startQuiz() {
        pw.close();
        viewHandler.setQuizName(quizName);
        viewHandler.setQuestionList(questionList);
        viewHandler.openView("Quiz");
    }

    @FXML
    protected void loadQuiz(){
        quizName = loadQuizComboBox.getSelectedItem().getQuizName();
        String selectedQuiz = "data/Saved_Quiz/" + quizName;

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
        displayQuizOutput();
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
        quizGenOutput = String.valueOf(clientController.retrieveInfo(clientSocket));
        displayQuizOutput();
    }

    @FXML
    protected void displayQuizOutput(){
        formatQuiz();
        //increment question number from 0 to 1, so the output can be shown
        nextQuestionButton.setDisable(false);
        goToNextQuestion();
        //prevent going out of bounds of array indices
        prevQuestionButton.setDisable(true);
        //enable save and start quiz buttons because an output will have been generated
        saveQuizButton.setDisable(false);
        startQuizButton.setDisable(false);
    }

    //formats the retrieved quiz, so it can be used by QuizController to start the quiz process
    private void formatQuiz(){
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
            //add corresponding question info to the ArrayList
            for (String line : splitQuestion){
                questionList.get(questionNumber).add(line);
            }
            //increment to make room for next question
            questionNumber += 1;

        }
    }

    @FXML
    protected void goToNextQuestion(){
        currentQuestion += 1;
        populateFields();
        prevQuestionButton.setDisable(false);
        if (currentQuestion == (questionList.size() - 1)){
            nextQuestionButton.setDisable(true);
        }
    }

    @FXML
    protected void goToPrevQuestion(){
        currentQuestion -= 1;
        populateFields();
        nextQuestionButton.setDisable(false);
        if (currentQuestion == 1){
            prevQuestionButton.setDisable(true);
        }
    }

    @FXML
    protected void populateFields(){
        //display the question
        questionDescField.setText(questionList.get(currentQuestion).get(0));
        //remove unnecessary String from ChatGPT 3.5 prompt in Python program
        String answer = questionList.get(currentQuestion).get(5).replaceFirst("Answer: ", "");
        //display options for the current question
        for (int i = 0; i < optionFields.length; i++){
            MFXTextField optionField = optionFields[i];
            MFXRadioButton optionRadio = optionRadios[i];
            //field shows corresponding text for the current question (i + 1 because of alignment differences)
            optionField.setText(questionList.get(currentQuestion).get(i + 1));
            //use radio buttons to indicate the correct answer
            String optionText = optionField.getText();
            if (optionText.equals(answer)){
                optionRadio.setSelected(true);
            }
        }
    }

    private void saveOptionEdits(MFXTextField optionField, int index) {
        //get the option's corresponding question
        ArrayList<String> currentQuestionList = questionList.get(currentQuestion);
        //replace the text for that option
        currentQuestionList.set(index, optionField.getText());
    }

    private void saveAnswerEdit(MFXTextField optionField) {
        //get the answer's corresponding question
        ArrayList<String> currentQuestionList = questionList.get(currentQuestion);
        //answers have this format in the questionList
        String answerText = "Answer: " + optionField.getText();
        currentQuestionList.set(5, answerText);
    }

    @FXML
    protected void saveQuiz() {
        /*
        * Once that works, test generating quiz
        * Once that works, add UI to generate specific number of questions
        * fix glitch with switching between loaded quizzes
        * Once that works, phase out text parsing for database connectivity
        * Adding/deleting questions may need to be postponed*/

        //temporary conversion of questionList to String, so it can be saved as text file
        //will be replaced with database saving instead of being reliant on # formatting
        StringBuilder sb = new StringBuilder();
        for (ArrayList<String> questions : questionList){
            for (String line : questions){
                if (line.endsWith("?")){
                    line = "#" + line;
                }
                sb.append(line).append("\n");
            }
            sb.append("\n");
        }

        String quizToSave = sb.toString();
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

        //add new quiz name to combo box
        loadQuizComboBox.getItems().add(new Quiz("Saved_Quiz" + fileNumber + ".txt"));
    }
}