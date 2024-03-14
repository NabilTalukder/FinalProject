package com.example.taskandquizscheduler;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class ScheduleController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private boolean monthStart = false;
    private int day = 1;
    private int monthLength = 0;
    private int firstWeekdayCol = 0;
    private double xOffset, yOffset;
    private ArrayList<Task> taskList = new ArrayList<>();
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();
    private BufferedReader br = null;
    private String yearVal;
    private String monthVal;

    @FXML
    private Label friendsLabel;

    @FXML
    private Label quizGenLabel;

    @FXML
    private Label scheduleLabel;

    @FXML
    private Label webBlockerLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private GridPane weekdayGrid;

    @FXML
    private Label monthYearLabel;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private Button addTaskButton;

    @FXML
    protected void initialize(){
        //get all the set tasks
        String line;
        String dueDateKey;
        try {
            br = new BufferedReader(new FileReader("data/tasks.txt"));
            while ((line = br.readLine()) != null) {
                //tasks are delimited by commas in the file
                String[] taskNames = line.split(",");
                //the first element of each line is a date for which tasks have been set
                dueDateKey = taskNames[0];
                //collect tasks for the line being read
                ArrayList<Task> taskListValues = new ArrayList<>();
                //create new Task objects to reconstruct the HashMap used for updating calendar
                for (int i = 1; i < taskNames.length; i++){
                    Task task = new Task();
                    //completed tasks are delimited with semi-colon
                    String [] taskPair = taskNames[i].split(";");
                    //the first part of the pair is the task name
                    task.setTaskName(taskPair[0]);
                    //if there is another character after the task name
                    //the task was marked as complete in the file
                    if (taskPair.length > 1){
                        //set task as complete accordingly, so it can be marked on the calendar
                        task.setComplete(true);
                    }
                    taskListValues.add(task);
                }
                //add the tasks and date for which they have been assigned to HashMap
                tasksMap.put(dueDateKey, taskListValues);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set current month, year; update corresponding label
        yearVal = String.valueOf(LocalDate.now().getYear());
        monthVal = String.valueOf(LocalDate.now().getMonth());
        monthYearLabel.setText(monthVal + " " + yearVal);

        //add empty label to every cell in the calendar grid
        int cell = 0;
        //calendar has 42 cells to account for differences in position of first day of the month
        for (int row = 0; row <= 5; row++){
            for (int col = 0; col <= 6; col++){
                Label dateLabel = new Label();
                //every cell in calendar grid has a VBox which will contain labels
                Node node = calendarGrid.getChildren().get(cell);
                if (node instanceof VBox vBox){
                    //add empty date label to VBox
                    vBox.getChildren().add(dateLabel);
                }
                //align date label to top-left
                GridPane.setValignment(dateLabel, VPos.TOP);
                //cells are blank by default because they're dependent on the current month/year
                dateLabel.setText("");
                //next cell in calendar grid
                cell++;
            }
        }
        //calculate date positions on calendar grid
        calendarCalc();

        //allow scheduleLabel on sidebar to be clicked to show Scheduler page
        quizGenLabel.setOnMousePressed(quizGenHandler);

        //allow addTaskButton to start process for adding task
        //done this way instead of using SceneBuilder because of similar functionality to Editing Task
        addTaskButton.setOnAction((EventHandler<ActionEvent>) addTaskHandler);
    }

    //handle mouse event of clicking scheduleLabel
    EventHandler<? super MouseEvent> quizGenHandler = this::quizGenClick;

    //handle mouse event of clicking Add Task button
    EventHandler<? super ActionEvent> addTaskHandler = taskEvent -> showTaskView("Add", taskEvent);

    //handle mouse event of clicking taskLabel
    EventHandler<? super MouseEvent> editTaskHandler = taskEvent -> showTaskView("Edit", taskEvent);

    @FXML
    protected void quizGenClick(MouseEvent event){
        //go to schedule page
        try {
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

    @FXML
    protected void calendarCalc(){
        //flag for when dateTaskCalc() reaches first day of the month to start displaying dates
        monthStart = false;
        //cell number of calendar grid
        int cell = 0;
        //month always begins on day 1
        day = 1;
        //num days in month; check if leap year to add extra day if February
        monthLength = Month.valueOf(monthVal).length(Year.isLeap(Integer.parseInt(yearVal)));
        /*column containing the first weekday of the month
         * found by extracting month & year shown on calendar
         * and then -1 to match calendarGrid index, which starts from 0*/
        firstWeekdayCol = LocalDate.of(Integer.parseInt(yearVal),
                Month.valueOf(monthVal).getValue(),
                1).getDayOfWeek().getValue() - 1;
        //loop through each cell and add date label if it's a day of the month
        dateTaskCalc(cell);
    }

    @FXML
    protected void dateTaskCalc(int cell){

        //calendar has 42 cells to account for differences in position of first day of the month
        for (int row = 0; row <= 5; row++){
            for (int col = 0; col <= 6; col++){
                //get VBox from cell, containing date label
                Node node = calendarGrid.getChildren().get(cell);
                if (node instanceof VBox vBox){
                    //clear any task labels, so they aren't duplicated upon navigating months
                    //remove if >1 because if there's only 1 label, that is the date label (shouldn't remove)
                    if (vBox.getChildren().size() > 1){
                        vBox.getChildren().remove(1, vBox.getChildren().size());
                    }
                    //Vbox containing Labels should expand to properly show set tasks
                    GridPane.setVgrow(vBox, Priority.ALWAYS);
                    //VBox left with only index 0, containing dateLabel
                    Node dateNode = vBox.getChildren().get(0);
                    if (dateNode instanceof Label dateLabel){
                        //finding first weekday of the month means date can be shown in cell
                        if (cell == firstWeekdayCol){
                            monthStart = true;
                        }
                        //fill out calendar for every day in the month
                        if (monthStart && day <= monthLength){
                            //add day of the month to cell
                            dateLabel.setText(String.valueOf(day));
                            //add left padding to date label
                            Insets cellInsets = new Insets(0, 0, 0, 5);
                            dateLabel.setPadding(cellInsets);
                            //parse constructed date string to prepare for format conversion
                            LocalDate dueDateBase = LocalDate.parse(yearVal + "-"
                                    + Month.valueOf(monthVal).getValue() + "-"
                                    + day, DateTimeFormatter.ofPattern("yyyy-M-d"));
                            //format date so it can be used for tasksMap
                            String dueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dueDateBase);
                            //get tasks for current day
                            taskList = tasksMap.computeIfAbsent(dueDate, k -> new ArrayList<>());
                            //for each task, add a label (showing task name) to the cell
                            for (Task task : taskList) {
                                Label taskLabel = new Label();
                                taskLabel.setText(task.getTaskName());
                                //change colour of task label to mark completion
                                if (task.isComplete()){
                                    taskLabel.setStyle("-fx-background-color: rgb(168, 235, 52);");
                                }
                                //make the label occupy all available width in the cell
                                taskLabel.setMaxWidth(Double.MAX_VALUE);
                                taskLabel.setPrefWidth(Label.USE_COMPUTED_SIZE);
                                //add left padding to task label
                                taskLabel.setPadding(cellInsets);
                                //allow task label to be clicked so user can edit it
                                taskLabel.setOnMousePressed(editTaskHandler);
                                //add task to cell, contained within the cell's VBox
                                vBox.getChildren().add(taskLabel);
                            }
                            //next day
                            day++;
                        }
                        else {
                            //if it's not a day of the month, show empty cell
                            dateLabel.setText("");
                        }
                    }
                }
                //go to next cell in calendar grid
                cell++;
            }
        }
    }


    @FXML
    protected void prevMonthClick(){
        //decrement month
        monthVal = String.valueOf(Month.valueOf(monthVal).minus(1));
        monthYearLabel.setText(monthVal + " " + yearVal);
        //decrementing to December means year should decrement too
        if (Month.valueOf(monthVal) == Month.DECEMBER) {
            yearVal = Integer.toString(Integer.parseInt(yearVal) - 1);
            monthYearLabel.setText(monthVal + " " + yearVal);
        }
        //show dates for the current month
        calendarCalc();
    }

    @FXML
    protected void nextMonthClick(){
        //increment month
        monthVal = String.valueOf(Month.valueOf(monthVal).plus(1));
        monthYearLabel.setText(monthVal + " " + yearVal);
        //incrementing to January means year should increment too
        if (Month.valueOf(monthVal) == Month.JANUARY){
            yearVal = Integer.toString(Integer.parseInt(yearVal) + 1);
            monthYearLabel.setText(monthVal + " " + yearVal);
        }
        //show dates for the current month
        calendarCalc();
    }

    @FXML
    protected void showTaskView(String taskAction, Event taskEvent){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("task-view.fxml"));
            Parent root = loader.load();
            //send tasksMap to taskController, so it can be updated with a new task
            TaskController taskController = loader.getController();
            taskController.setTasksMap(tasksMap);
            //set task-view behaviour based on if user wants to add/edit/delete/complete task
            taskController.setTaskAction(taskAction);

            //determine process based on task action
            if (taskAction.equals("Add")){
                //make preparations in task-view for adding task
                taskController.addTask(monthVal, yearVal);
            }
            else if (taskAction.equals("Edit")){
                //retrieve relevant info for editing task
                taskController.editTask(taskEvent, monthVal, yearVal);
            }

            //set instantiated schedulerController object to taskController so the tasksMap can be received
            taskController.setScheduleController(this);

            //set border
            root.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
            Stage popupStage = new Stage();

            // Allow the window to be moved
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                popupStage.setX(event.getScreenX() - xOffset);
                popupStage.setY(event.getScreenY() - yOffset);
            });

            Scene popupScene = new Scene(root, 300, 325);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(popupScene);
            popupStage.setResizable(false);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, ArrayList<Task>> getTasksMap() {
        return tasksMap;
    }

    public void setTasksMap(HashMap<String, ArrayList<Task>> tasksMap) {
        this.tasksMap = tasksMap;
    }

}

