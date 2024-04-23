package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

//handles scheduling and managing of tasks and quizzes on a calendar
public class ScheduleController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;
    //used to access user-related data
    private User user;

    //flag for when the days can start being displayed in the calendar cells
    private boolean monthStart = false;
    //used as an iterator for every day in the month being shown
    private int day = 1;
    //the max number of days in the month being shown on the calendar
    private int monthLength = 0;
    //used for helping the calendar algorithm to decide when to start showing the days of the month
    private int firstWeekdayCol = 0;
    //offset of the TaskManagementView popup which allow it to be dragged across the screen
    private double xOffset, yOffset;
    //holds tasks set for each day in the calendar - used for displaying and modifying them
    private ArrayList<Task> taskList = new ArrayList<>();
    //contains all dates on the calendar and their associated tasks
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();
    //month and year displayed on the calendar - used for calculations requiring dates
    private String yearVal;
    private String monthVal;

    //shows calendar dates and associated set tasks and quizzes
    @FXML
    private GridPane calendarGrid;
    //shows the month and year above the calendar
    @FXML
    private Label monthYearLabel;
    @FXML
    private MFXButton scheduleButton = new MFXButton();
    @FXML
    private MFXButton addTaskButton;
    @FXML
    private MFXButton addQuizButton;

    //This method was reused from Mortensen, T.
    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    //retrieve all set tasks and quizzes, so they can be displayed on the calendar
    @FXML
    protected void initialize(){
        //set current month, year; update corresponding label
        yearVal = String.valueOf(LocalDate.now().getYear());
        monthVal = String.valueOf(LocalDate.now().getMonth());
        monthYearLabel.setText(monthVal + " " + yearVal);

        /*Allow buttons to be able to start process for adding task
        * Manually coded this way instead of SceneBuilder because process is similar to Editing task
        * Thus, EventHandlers are explicitly coded with arguments*/
        addTaskButton.setOnAction((EventHandler<ActionEvent>) addTaskHandler);
        addQuizButton.setOnAction((EventHandler<ActionEvent>) addQuizHandler);
        scheduleButton.setDisable(true);
    }

    @FXML
    public void setupSchedule(){
        //get all set tasks from database
        TaskDataAccessor taskDataAccessor = new TaskDataAccessor();
        tasksMap = taskDataAccessor.querySchedule(user);
        //add empty label to every cell in calendar so retrieved tasks can be added to it
        prepareCalendar();
    }

    @FXML
    protected void prepareCalendar(){
        //add empty label to every cell in the calendar grid
        int cell = 0;
        //calendar has 42 cells to account for differences in position of first day of the month
        for (int row = 0; row <= 5; row++){
            for (int col = 0; col <= 6; col++){
                Label dateLabel = new Label();
                //every cell in calendar grid has a VBox which will contain labels
                VBox vBox = (VBox) calendarGrid.getChildren().get(cell);
                //add empty date label to VBox
                vBox.getChildren().add(dateLabel);
                //align date label to top-left
                GridPane.setValignment(dateLabel, VPos.TOP);
                //cells are blank by default because they're dependent on the current month/year
                dateLabel.setText("");
                //next cell in calendar grid
                cell++;
            }
        }
        //calculate date positions on calendar grid
        refreshCalendar();
    }

    //handle mouse event of clicking Add Task button
    EventHandler<? super ActionEvent> addTaskHandler = taskEvent ->
            showTaskManagementView("Add", taskEvent);

    //handle mouse event of clicking Add Quiz button
    EventHandler<? super ActionEvent> addQuizHandler = quizEvent ->
            showTaskQuizManagementView("Add", quizEvent);

    //handle mouse event of clicking taskLabel
    EventHandler<? super MouseEvent> editTaskOrQuizHandler = event ->  {
        //retrieve task label and its name
        Label taskLabel = (Label) event.getSource();
        //retrieve date label from cell (VBox) containing task label
        Label dateLabel = (Label) ((VBox) taskLabel.getParent()).getChildren().get(0);
        //reconstruct due date of task as string
        String selectedDueDate = LocalDate.parse(dateLabel.getText() + "-"
                + Month.valueOf(monthVal).getValue() + "-"
                + yearVal, DateTimeFormatter.ofPattern("d-M-yyyy")).toString();
        ArrayList<Task> taskList = tasksMap.get(selectedDueDate);
        //match label with Task object in taskMap and find out if tasktype is a quiz or not
        //so appropriate window can appear
        for (Task task : taskList) {
            if (task.getTaskType().equals("task")) {
                showTaskManagementView("Edit", event);
                break;
            }
            else if (task.getTaskType().equals("quiz")) {
                showTaskQuizManagementView("Edit", event);
                break;
            }
        }
    };


    @FXML
    protected void goToQuizGenerator(){
        viewHandler.openView("QuizGenerator");
    }

    @FXML
    protected void goToWebsiteBlocker(){
        viewHandler.openView("WebsiteBlocker");
    }

    @FXML
    protected void logout() {
        viewHandler.setUser(null);
        viewHandler.openView("Login");
    }

    //initialisations for calendar algorithm
    @FXML
    protected void refreshCalendar(){
        //flag for when calcDateTasks() reaches first day of the month to start displaying dates
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
        calcDateTasks(cell);
    }

    //iterate through calendar grid, adding dates and set tasks as necessary
    @FXML
    protected void calcDateTasks(int cell){

        //calendar has 42 cells to account for differences in position of first day of the month
        for (int row = 0; row <= 5; row++){
            for (int col = 0; col <= 6; col++){
                //get VBox from cell, containing date label
                VBox vBox = (VBox) calendarGrid.getChildren().get(cell);
                //clear any task labels, so they aren't duplicated upon navigating months
                //remove if >1 because if there's only 1 label, that is the date label (shouldn't remove)
                if (vBox.getChildren().size() > 1){
                    vBox.getChildren().remove(1, vBox.getChildren().size());
                }
                //Vbox containing Labels should expand to properly show set tasks
                GridPane.setVgrow(vBox, Priority.ALWAYS);
                //VBox left with only index 0, containing dateLabel
                Label dateLabel = (Label) vBox.getChildren().get(0);
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
                        taskLabel.setOnMousePressed(editTaskOrQuizHandler);
                        //add task to cell, contained within the cell's VBox
                        vBox.getChildren().add(taskLabel);
                    }
                    day++;
                }
                else {
                    //if it's not a day of the month, show empty cell
                    dateLabel.setText("");
                }
                //go to next cell in calendar grid
                cell++;
            }
        }
    }

    //show previous month and its associated tasks/quizzes
    @FXML
    protected void goToPrevMonth(){
        //decrement month
        monthVal = String.valueOf(Month.valueOf(monthVal).minus(1));
        monthYearLabel.setText(monthVal + " " + yearVal);
        //decrementing to December means year should decrement too
        if (Month.valueOf(monthVal) == Month.DECEMBER) {
            yearVal = Integer.toString(Integer.parseInt(yearVal) - 1);
            monthYearLabel.setText(monthVal + " " + yearVal);
        }
        //show dates for the current month
        refreshCalendar();
    }

    //show next month and its associated tasks/quizzes
    @FXML
    protected void goToNextMonth(){
        //increment month
        monthVal = String.valueOf(Month.valueOf(monthVal).plus(1));
        monthYearLabel.setText(monthVal + " " + yearVal);
        //incrementing to January means year should increment too
        if (Month.valueOf(monthVal) == Month.JANUARY){
            yearVal = Integer.toString(Integer.parseInt(yearVal) + 1);
            monthYearLabel.setText(monthVal + " " + yearVal);
        }
        //show dates for the current month
        refreshCalendar();
    }

    //show UI popup that allows user to manage a task
    @FXML
    protected void showTaskManagementView(String taskAction, Event taskEvent){
        try {
            //get FXML file for task popup and display
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskManagementView.fxml"));
            Parent root = loader.load();
            //send tasksMap to taskController, so it can be updated with a new task
            TaskController taskController = loader.getController();
            taskController.setTasksMap(tasksMap);
            taskController.setUser(user);

            //determine process based on task action
            if (taskAction.equals("Add")){
                //make preparations in task-view for adding task
                taskController.addTask(monthVal, yearVal, "Task");
            }
            else if (taskAction.equals("Edit")){
                //retrieve relevant info for editing task
                taskController.editTask(taskEvent, monthVal, yearVal, "Task");
            }

            //set instantiated schedulerController object to taskController so the tasksMap can be received
            taskController.setScheduleController(this);

            // ### AI-generated start (popupStage was called primaryStage in prompt output)
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
            // ### AI-generated end
            popupStage.setResizable(false);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void showTaskQuizManagementView(String taskAction, Event taskEvent){
        try {
            //get FXML file for task popup and display
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskQuizManagementView.fxml"));
            Parent root = loader.load();
            //send tasksMap to taskQuizController, so it can be updated with a new task
            TaskQuizController taskQuizController = loader.getController();
            taskQuizController.setTasksMap(tasksMap);
            taskQuizController.setUser(user);
            taskQuizController.setViewHandler(viewHandler);
            taskQuizController.initialiseComboBox();

            //determine process based on task action
            if (taskAction.equals("Add")){
                //make preparations in task-view for adding task
                taskQuizController.addTask(monthVal, yearVal, "Quiz");
            }
            else if (taskAction.equals("Edit")){
                //retrieve relevant info for editing task
                taskQuizController.editTask(taskEvent, monthVal, yearVal, "Quiz");
            }

            //set instantiated schedulerController object to taskQuizController so the tasksMap can be received
            taskQuizController.setScheduleController(this);

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

    public void setUser(User user) {
        this.user = user;
    }

    public void setTasksMap(HashMap<String, ArrayList<Task>> tasksMap) {
        this.tasksMap = tasksMap;
    }

}

