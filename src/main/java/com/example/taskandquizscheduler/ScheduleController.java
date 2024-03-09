package com.example.taskandquizscheduler;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
    private Label monthLabel;

    @FXML
    private Label yearLabel;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private Button addTaskButton;

    @FXML
    protected void initialize(){
        //get all the set tasks
        //seems to be a blank dueDate / extra line in tasks.txt
        String line;
        String dueDateKey;
        try {
            br = new BufferedReader(new FileReader("data/tasks.txt"));
            while ((line = br.readLine()) != null) {
                String[] taskNames = line.split(",");
                dueDateKey = taskNames[0];
                ArrayList<Task> taskListValues = new ArrayList<>();
                for (int i = 1; i < taskNames.length; i++){
                    Task task = new Task();
                    task.setTaskName(taskNames[i]);
                    taskListValues.add(task);
                }
                tasksMap.put(dueDateKey, taskListValues);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set current month, year
        yearLabel.setText(String.valueOf(LocalDate.now().getYear()));
        monthLabel.setText(String.valueOf(LocalDate.now().getMonth()));

        //add empty label to every cell in the calendar grid
        int cell = 0;
        for (int row = 0; row <= 5; row++){
            for (int col = 0; col <= 6; col++){
                Label dateLabel = new Label();
                //every cell in calendar grid has a VBox which will contain labels
                Node node = calendarGrid.getChildren().get(cell);
                if (node instanceof VBox){
                    VBox vBox = (VBox) node;
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

        /* problems
         * cells need GridPane.Vgrow to hold multiple tasks
         * datepicker should show current month/year on page because it's tedious and confusing otherwise
         * tasks need to be clickable, editable entities
         *  every label needs an event handler like sidebar labels
         *  event handler calls EditTask
         *  EditTask brings up same window as AddTask but is already filled in
         *
         *
         * */

        //allow scheduleLabel on sidebar to be clicked to show Scheduler page
        scheduleLabel.setOnMousePressed(quizGenHandler);
    }

    //handle mouse event of clicking scheduleLabel
    EventHandler<? super MouseEvent> quizGenHandler = actionEvent -> quizGenClick(actionEvent);

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


//    @FXML
//    protected void calendarCalc(){
//        //flag for when the for loop reaches the first day of the month to start displaying dates
//        monthStart = false;
//        //cell number of calendar grid
//        int cell = 1;
//        //month always begins on day 1
//        day = 1;
//        //num days in month; check if leap year to add extra day if February
//        monthLength = Month.valueOf(monthLabel.getText()).length(Year.isLeap(Integer.valueOf(yearLabel.getText())));
//        /*column containing the first weekday of the month
//         * found by extracting month & year shown on calendar*/
//        firstWeekdayCol = LocalDate.of(Integer.valueOf(yearLabel.getText()),
//                Month.valueOf(monthLabel.getText()).getValue(),
//                1).getDayOfWeek().getValue();
//        //loop through each cell and paste date if it's a day of the month
//        for (int row = 0; row <= 5; row++){
//            for (int col = 0; col <= 6; col++){
//                //get Label from cell
//                Node node = calendarGrid.getChildren().get(cell);
//
//                if (node instanceof Label){
//                    Label dateLabel = (Label) node;
//                    //finding first weekday of the month means date can be shown in cell
//                    if (cell == firstWeekdayCol){
//                        monthStart = true;
//                    }
//
//                    //fill out calendar for every day in the month
//                    //(monthStart || col == firstWeekdayCol - 1) && day <= monthLength
//                    if (monthStart && day <= monthLength){
//                        //add day of the month to cell
//                        dateLabel.setText(" " + String.valueOf(day));
//                        System.out.println("day : " + day + " cell: " + cell);
//                        //parse constructed date string to prepare for format conversion
//                        LocalDate dueDateBase = LocalDate.parse(yearLabel.getText() + "-"
//                                + Month.valueOf(monthLabel.getText()).getValue() + "-"
//                                + day, DateTimeFormatter.ofPattern("yyyy-M-d"));
//                        //format date so it can be used for tasksMap
//                        String dueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dueDateBase);
//                        //get tasks for current day
//                        taskList = tasksMap.computeIfAbsent(dueDate, k -> new ArrayList<>());
//                        //for each task, add a label (showing task name) to the cell
//                        for (int i = 0; i < taskList.size(); i++){
//                            Label taskLabel = new Label();
//                            taskLabel.setText(" " + taskList.get(i).getTaskName());
//                            //add task to cell (defined by column, row)
//                            calendarGrid.add(taskLabel, col, row);
//                            /*
//                            /* problems
//                            * label stays even when switching months
//                            * label pasted again when switching back. Need to set label instead of creating?
//                            * tasks need to be clickable, editable entities
//                            * */
//                        }
//                        //next day
//                        day++;
//                    }
//                    else {
//                        //if it's not a day of the month, show empty cell
//                        dateLabel.setText("");
//                    }
//                }
//                //go to next cell in calendar grid
//                cell++;
//            }
//        }
//
//        //delete
//        for (int i = 0; i <= 5; i++){
//            Node node = calendarGrid.getChildren().get(i);
//            if (node instanceof Label){
//                Label test = (Label) node;
//                System.out.println("day in cell: " + test.getText());
//                System.out.println("column: " + GridPane.getColumnIndex(test));
//            }
//        }
//
//
//    }

    @FXML
    protected void calendarCalc(){
        //flag for when the for loop reaches the first day of the month to start displaying dates
        monthStart = false;
        //cell number of calendar grid
        int cell = 0;
        //month always begins on day 1
        day = 1;
        //num days in month; check if leap year to add extra day if February
        monthLength = Month.valueOf(monthLabel.getText()).length(Year.isLeap(Integer.valueOf(yearLabel.getText())));
        /*column containing the first weekday of the month
         * found by extracting month & year shown on calendar
         *  and then -1 to match calendarGrid index, which starts from 0*/
        firstWeekdayCol = LocalDate.of(Integer.valueOf(yearLabel.getText()),
                Month.valueOf(monthLabel.getText()).getValue(),
                1).getDayOfWeek().getValue() - 1;
        //loop through each cell and add date label if it's a day of the month
        dateTaskCalc(cell);

        //delete this
        for (int i = 0; i <= 5; i++){
            Node node = calendarGrid.getChildren().get(i);
            if (node instanceof Label){
                Label test = (Label) node;
                System.out.println("day in cell: " + test.getText());
                System.out.println("column: " + GridPane.getColumnIndex(test));
            }
        }


    }

    @FXML
    protected void dateTaskCalc(int cell){
        for (int row = 0; row <= 5; row++){
            for (int col = 0; col <= 6; col++){
                //get VBox from cell, containing date label
                Node node = calendarGrid.getChildren().get(cell);
                if (node instanceof VBox){
                    VBox vBox = (VBox) node;
                    //clear any task labels so they aren't duplicated upon navigating months
                    if (vBox.getChildren().size() > 1){
                        vBox.getChildren().remove(1, vBox.getChildren().size());
                    }

                    //VBox should only have index 0, containing dateLabel
                    Node dateNode = vBox.getChildren().get(0);
                    if (dateNode instanceof Label){
                        Label dateLabel = (Label) dateNode;
                        //finding first weekday of the month means date can be shown in cell
                        if (cell == firstWeekdayCol){
                            monthStart = true;
                        }

                        //fill out calendar for every day in the month
                        if (monthStart && day <= monthLength){
                            //add day of the month to cell
                            dateLabel.setText(" " + String.valueOf(day));
                            //System.out.println("day : " + day + " cell: " + cell);
                            //parse constructed date string to prepare for format conversion
                            LocalDate dueDateBase = LocalDate.parse(yearLabel.getText() + "-"
                                    + Month.valueOf(monthLabel.getText()).getValue() + "-"
                                    + day, DateTimeFormatter.ofPattern("yyyy-M-d"));
                            //format date so it can be used for tasksMap
                            String dueDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dueDateBase);
                            //get tasks for current day
                            taskList = tasksMap.computeIfAbsent(dueDate, k -> new ArrayList<>());
                            //for each task, add a label (showing task name) to the cell
                            for (int i = 0; i < taskList.size(); i++){
                                Label taskLabel = new Label();
                                taskLabel.setText(" " + taskList.get(i).getTaskName());
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
        monthLabel.setText(String.valueOf(Month.valueOf(monthLabel.getText()).minus(1)));
        //decrementing to December means year should decrement too
        if (Month.valueOf(monthLabel.getText()) == Month.DECEMBER) {
            yearLabel.setText(Integer.toString(Integer.parseInt(yearLabel.getText()) - 1));
        }
        //show dates for the current month
        calendarCalc();
    }

    @FXML
    protected void nextMonthClick(){
        //increment month
        monthLabel.setText(String.valueOf(Month.valueOf(monthLabel.getText()).plus(1)));
        //incrementing to January means year should increment too
        if (Month.valueOf(monthLabel.getText()) == Month.JANUARY){
            yearLabel.setText(Integer.toString(Integer.parseInt(yearLabel.getText()) + 1));
        }
        //show dates for the current month
        calendarCalc();
    }

    @FXML
    protected void addTaskClick(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-task-view.fxml"));
            Parent root = loader.load();
            //send tasksMap to addTaskController so it can be updated with a new task
            AddTaskController addTaskController = loader.getController();
            addTaskController.setTasksMap(tasksMap);
            //set this schedulerController object to addTaskController so the tasksMap can be received
            addTaskController.setScheduleController(this);

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

