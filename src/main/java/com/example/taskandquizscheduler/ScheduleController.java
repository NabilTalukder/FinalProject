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
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

//handles scheduling and managing of tasks and quizzes on a calendar
public class ScheduleController {

    //main window of JavaFX application
    private Stage stage;
    //container for organising UI elements in window
    private Scene scene;
    //top-level class for handling nodes (UI elements/containers) in JavaFX
    private Parent root;
    //flag for when the days can start being displayed in the calendar cells
    private boolean monthStart = false;
    //used as an iterator for every day in the month being shown
    private int day = 1;
    //the max number of days in the month being shown on the calendar
    private int monthLength = 0;
    //used for helping the calendar algorithm to decide when to start showing the days of the month
    private int firstWeekdayCol = 0;
    //offset of the task-view popup which allow it to be dragged across the screen
    private double xOffset, yOffset;
    //holds tasks set for each day in the calendar - used for displaying and modifying them
    private ArrayList<Task> taskList = new ArrayList<>();
    //contains all dates on the calendar and their associated tasks
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();
    //reads txt file containing set tasks information
    private BufferedReader br = null;
    //month and year displayed on the calendar - used for calculations requiring dates
    private String yearVal;
    private String monthVal;

    //sidebar labels to navigate to the main screens of the application
    @FXML
    private Label friendsLabel;
    @FXML
    private Label quizGenLabel;
    @FXML
    private Label scheduleLabel;
    @FXML
    private Label webBlockerLabel;

    //grid containing calendar dates and associated set tasks and quizzes
    @FXML
    private GridPane calendarGrid;
    //header grid to show weekdays above the calendar date cells
    @FXML
    private GridPane weekdayGrid;
    //label showing the month and year above the calendar
    @FXML
    private Label monthYearLabel;
    //navigate across months
    @FXML
    private Button prevMonthButton;
    @FXML
    private Button nextMonthButton;

    @FXML
    private Button addTaskButton;

    //retrieve all set tasks and quizzes, so they can be displayed on the calendar
    @FXML
    protected void initialize(){
        //get all the set tasks
        String line;
        String dueDateKey;
//        try {
//            br = new BufferedReader(new FileReader("data/tasks.txt"));
//            while ((line = br.readLine()) != null) {
//                //tasks are delimited by commas in the file
//                String[] taskNames = line.split(",");
//                //the first element of each line is a date for which tasks have been set
//                dueDateKey = taskNames[0];
//                //collect tasks for the line being read
//                ArrayList<Task> taskListValues = new ArrayList<>();
//                //create new Task objects to reconstruct the HashMap used for updating calendar
//                for (int i = 1; i < taskNames.length; i++){
//                    Task task = new Task();
//                    //completed tasks are delimited with semi-colon
//                    String [] taskPair = taskNames[i].split(";");
//                    //the first part of the pair is the task name
//                    task.setTaskName(taskPair[0]);
//                    //if there is another character after the task name
//                    //the task was marked as complete in the file
//                    if (taskPair.length > 1){
//                        //set task as complete accordingly, so it can be marked on the calendar
//                        task.setStatus("complete");
//                    }
//                    else {
//                        task.setStatus("incomplete");
//                    }
//                    taskListValues.add(task);
//                }
//                //add the tasks and date for which they have been assigned to HashMap
//                tasksMap.put(dueDateKey, taskListValues);
//            }
//            br.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //get all set tasks from database
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "SELECT due_date, task_name, completion_status FROM task WHERE" +
                    " assigner_ID = 1 AND assignee_ID = 1 ORDER BY due_date;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            String returnedDueDate;
            String prevReturnedDueDate = "";
            String dueDateKeyDB = "";
            ArrayList<Task> taskListValuesDB = new ArrayList<>();

            while (resultSet.next()){
                Task task = new Task();
                returnedDueDate = resultSet.getString("due_date");
                if (!returnedDueDate.equals(prevReturnedDueDate)){
                    dueDateKeyDB = returnedDueDate;
                    taskListValuesDB = new ArrayList<>();
                }
                task.setTaskName(resultSet.getString("task_name"));
                task.setStatus(resultSet.getString("completion_status"));
                taskListValuesDB.add(task);
                tasksMap.put(dueDateKeyDB, taskListValuesDB);
                prevReturnedDueDate = returnedDueDate;
            }

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("retrieved tasks");

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

        /*Add Quiz
        * similar to view for Add Task
        * has due date but no task name
        *   use different fxml so task-view isn't crowded
        *   use different controller because methods will need to adhere to fxml
        *
        * instead of entering task name, dropdown of quizzes
        *   should be changed later because dropdown is inconvenient for many quizzes
        *   maybe a search bar
        *
        * Added quiz appears same way as added task label but there should be a visual distinction
        *
        * quiz should be saved as a Task object in the hashmap but with a field indicating that
        * it's a quiz
        * In the tasks file, there should be another delimiter denoting it's a quiz
        *
        * Editing quiz allows user to take quiz, moving to Quiz page and returning to Scheduler
        * upon completion
        *
        * Quiz and Task inherit from Schedulable
        * QuizController and TaskController inherit from SchedulableController
        * reason - some methods are shared but most methods between QuizController and TaskController
        * while similar, will have different implementations because TaskController uses a text field
        * for Task name while QuizController will use a dropdown to select quizzes
        * If done this way, the algorithms used to handle Tasks will need to be refactored to handle
        * Schedulables so both Task and Quiz objects can be processed in the hashmap and txt file
        * Much clearer than making Quiz inherit from Task
        *
        * problem is existing algorithms for adding, editing and deleting task with respect to txt file
        * will need to account for quizzes.
        * Is all that extra work necessary or can I just implement database?
        * I implemented the txt file as a way to demonstrate persistence between application restarts
        * It ended up being more complicated than I thought but I did meet my objective
        * I thought that eventually, database queries would be for initial data retrieval and everything else
        * would be handled by txt file but that might be tedious now with quiz implementation
        * Is querying each time for data really that bad or should I still use a combination of txt file
        * and database as originally intended?
        *
        * */
    }

    //handle mouse event of clicking scheduleLabel
    EventHandler<? super MouseEvent> quizGenHandler = this::quizGenClick;

    //handle mouse event of clicking Add Task button
    EventHandler<? super ActionEvent> addTaskHandler = taskEvent -> showTaskView("Add", taskEvent);

    //handle mouse event of clicking taskLabel
    EventHandler<? super MouseEvent> editTaskHandler = taskEvent -> showTaskView("Edit", taskEvent);

    //transition to quiz generator page
    @FXML
    protected void quizGenClick(MouseEvent event){
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

    //initialisations for calendar algorithm
    @FXML
    protected void calendarCalc(){
        //flag for when dateTaskCalc() reaches first day of the month to start displaying dates
        monthStart = false;
        //iterator for cell number of calendar grid
        int cell = 0;
        //iterator for day of month - day always begins on day 1
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

    //iterate through calendar grid, adding dates and set tasks as necessary
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
                                if (task.getStatus().equals("complete")){
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

    //show previous month and its associated tasks/quizzes
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

    //show next month and its associated tasks/quizzes
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

    //show UI popup that allows user to manage a task
    @FXML
    protected void showTaskView(String taskAction, Event taskEvent){
        try {
            //get FXML file for task popup and display
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

