package com.example.taskandquizscheduler;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;


//handles adding, editing, deleting and completion of tasks
public class TaskController {
    //main window of JavaFX application
    private Stage stage;
    //container for organising UI elements in window
    private Scene scene;
    //top-level class for handling nodes (UI elements/containers) in JavaFX
    private Parent root;
    //holds tasks set for each day in the calendar - used for displaying and modifying them
    private ArrayList<Task> taskList = new ArrayList<>();
    //contains all dates on the calendar and their associated tasks
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();
    //used to send information processed in this class back to the calendar, so it can be updated
    private ScheduleController scheduleController;
    //the original name of a task before the user changes it
    private String oldTaskName;
    //the original due date of a task before the user changes it
    private String oldDueDate;

    //label for giving the user feedback if they're adding or editing a task
    @FXML
    private Label taskTypeLabel;
    //used for confirming changes done to a task, be it adding or editing
    @FXML
    private Button confirmTaskButton;
    //allows user to select the date a task should be due and thus,
    // where in the calendar it should be shown
    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private TextField taskNameField;
    //button for cancelling changes done to a task but styled as a link
    @FXML
    private Hyperlink cancelLink;

    @FXML
    private Hyperlink deleteTaskLink;
    //button for marking a task as complete, styled as a link
    @FXML
    private Hyperlink completeLink;

    //prepare UI elements to add a task to the calendar
    @FXML
    protected void addTask(String monthVal, String yearVal){
        /* set the date picker to the month being shown on the calendar for ease of use
        * Date picker will be blank by default (next/prev month buttons not clicked)*/
        if (Month.valueOf(monthVal) != LocalDate.now().getMonth()){
            dueDatePicker.setValue(LocalDate.parse("1" + "-"
                    + Month.valueOf(monthVal).getValue() + "-"
                    + yearVal, DateTimeFormatter.ofPattern("d-M-yyyy")));
        }

        //allow Confirm (Add) Task button to process a new task
        confirmTaskButton.setOnAction((EventHandler<ActionEvent>) addTaskHandler);
    }

    //handle event of clicking Add Task button
    EventHandler<? super ActionEvent> addTaskHandler = this::confirmAddTask;

    //Retrieve task information for the selected task on the calendar
    @FXML
    protected void editTask(Event taskEvent, String monthVal, String yearVal){
        //retrieve task label
        Label taskLabel = (Label) taskEvent.getSource();
        //retrieve date label from cell (VBox) containing task label
        Label dateLabel = (Label) ((VBox) taskLabel.getParent()).getChildren().get(0);
        //display task name in task-view popup's text field
        taskNameField.setText(taskLabel.getText());
        //task name of selected task label - required for editing task
        oldTaskName = taskLabel.getText();
        //reconstruct and display due date of task
        dueDatePicker.setValue(LocalDate.parse(dateLabel.getText() + "-"
                + Month.valueOf(monthVal).getValue() + "-"
                + yearVal, DateTimeFormatter.ofPattern("d-M-yyyy")));
        //due date of selected task label - required for editing task
        oldDueDate = dueDatePicker.getValue().toString();

        //show delete link because a created task should be able to be deleted
        deleteTaskLink.setVisible(true);
        //show complete link because a created task should be able to be marked as complete
        completeLink.setVisible(true);
        //allow Confirm (Edit) Task button to process an edited task
        confirmTaskButton.setOnAction((EventHandler<ActionEvent>) editTaskHandler);
    }

    //handle event of clicking Edit Task button
    EventHandler<? super ActionEvent> editTaskHandler = this::confirmEditTask;


    @FXML
    protected void closeView(ActionEvent event){
        //get the stage from which the button that called this method was clicked
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        //close stage (task-view window)
        stage.close();
    }

    //remove the selected task from the calendar
    @FXML
    protected void confirmDeleteTask(ActionEvent event){
        String enteredDueDate = dueDatePicker.getValue().toString();
        taskList = tasksMap.get(enteredDueDate);
        //remove the task name for the due date shown in the Edit Task popup
        removeTaskName();
        //delete task from database
        deleteTaskDB(oldTaskName, oldDueDate);
        //send updated tasksMap back to schedulerController
        scheduleController.setTasksMap(tasksMap);
        //update calendar with task removed
        scheduleController.calendarCalc();
        //process finished, so close task-view popup
        closeView(event);
    }

    //mark a task as complete on the calendar
    @FXML
    protected void completeTask(ActionEvent event){
        /*get the due date for which the task was set
        * using oldDueDate instead of the entered due date within field
        * because the user may have changed the date
        * but then decided to complete task*/
        taskList = tasksMap.get(oldDueDate);
        for (Task task : taskList) {
            //find the task with the same name and set as complete
            if (task.getTaskName().equals(oldTaskName)) {
                task.setStatus("complete");
                break;
            }
        }
        //send updated tasksMap back to scheduleController
        scheduleController.setTasksMap(tasksMap);
        //update calendar with task as complete
        scheduleController.calendarCalc();
        //update database with task marked as complete
        completeTaskDB(oldTaskName, oldDueDate);
        //process finished, so close task-view popup
        closeView(event);
    }

    //update the calendar with the new task
    @FXML
    protected void confirmAddTask(ActionEvent event){
        //get the task name and due date
        String taskName = taskNameField.getText();
        String dueDate = dueDatePicker.getValue().toString();
        //create a new task and set the retrieved information
        Task task = new Task();
        task.setTaskName(taskName);
        task.setStatus("incomplete");
        //check if there are any tasks for the task's set due date
        //if there are no tasks, add the task to a new list to that date in tasksMap
        taskList = tasksMap.computeIfAbsent(dueDate, k -> new ArrayList<>());
        //either way, new task is added to taskList
        taskList.add(task);
        //send updated tasksMap back to scheduleController
        scheduleController.setTasksMap(tasksMap);
        //update calendar with new task
        scheduleController.calendarCalc();
        //update database with newly created task
        addTaskDB(taskName, dueDate);
        //process complete, so close task-view popup
        closeView(event);
    }

    //update the calendar with the changes made to the task
    @FXML
    protected void confirmEditTask(ActionEvent event){
        //get entered task name and due date, so they can be compared with original values
        String newTaskName = taskNameField.getText();
        String newDueDate = dueDatePicker.getValue().toString();

        /* upon clicking Confirm (Edit) Task button, if task name and due date were unchanged
         * close view / Edit Task popup*/
        if (oldTaskName.equals(newTaskName) &&
                oldDueDate.equals(newDueDate)){
            closeView(event);
        }
        //if only either task name or due date was changed
        else {
            //get the due date for which the task was set
            taskList = tasksMap.get(oldDueDate);

            //if date or both name and date was changed
            if (!oldDueDate.equals(newDueDate)){
                /*editing due date is effectively removing then adding task to new date
                *except task status remains same*/

                //retrieve status of task
                String taskStatus = "";
                for (Task task : taskList) {
                    //check against old name because new name won't be in the list
                    if (task.getTaskName().equals(oldTaskName)) {
                        taskStatus = task.getStatus();
                    }
                }
                //remove then add new task
                removeTaskName();
                Task task = new Task();
                task.setTaskName(newTaskName);
                task.setStatus(taskStatus);
                //check if there are any tasks for the task's set due date
                //if there are no tasks, add the task to a new list to that date in tasksMap
                taskList = tasksMap.computeIfAbsent(newDueDate, k -> new ArrayList<>());
                //either way, new task is added to taskList
                taskList.add(task);
            }
            //if only task name was changed
            else {
                for (Task task : taskList) {
                    //find the task with the same name
                    if (task.getTaskName().equals(oldTaskName)) {
                        //replace the name with the new name
                        task.setTaskName(taskNameField.getText());
                    }
                }
            }
            //send updated tasksMap back to scheduleController
            scheduleController.setTasksMap(tasksMap);
            //update calendar with new task
            scheduleController.calendarCalc();
            //update database with edited task
            editTaskDB(oldTaskName, newTaskName, oldDueDate, newDueDate);
            //process complete, so close task-view popup
            closeView(event);
        }
    }

    public void addTaskDB(String taskName, String dueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "INSERT INTO task " +
                    "(`task_ID`, `assigner_ID`, `assignee_ID`, `task_name`, `due_date`, `completion_status`)" +
                    " VALUES (NULL, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, taskName);
            preparedStatement.setString(4, dueDate);
            preparedStatement.setString(5, "incomplete");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("added task");
    }

    public void editTaskDB(String oldTaskName, String newTaskName, String oldDueDate, String newDueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "UPDATE task SET task_name = ?, due_date = ? WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newTaskName);
            preparedStatement.setString(2, newDueDate);
            preparedStatement.setString(3, "1");
            preparedStatement.setString(4, "1");
            preparedStatement.setString(5, oldTaskName);
            preparedStatement.setString(6, oldDueDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("edited task");
    }

    public void deleteTaskDB(String taskName, String dueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "DELETE FROM task WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, taskName);
            preparedStatement.setString(4, dueDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("deleted task");
    }

    public void completeTaskDB(String taskName, String dueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "UPDATE task SET completion_status = ? WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "complete");
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, "1");
            preparedStatement.setString(4, taskName);
            preparedStatement.setString(5, dueDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("completed task");
    }

    //tasksMap (used for calendar) is updated with task removed
    @FXML
    protected void removeTaskName(){
        //remove the task's old name for the old due date in the tasks hashmap
        //This happens when either the task was deleted or had its name or date changed
        taskList = tasksMap.get(oldDueDate);
        taskList.removeIf(task -> task.getTaskName().equals(oldTaskName));
        //remove due date if there are no longer any tasks associated with it
        System.out.println("task list size after removal: " + taskList.size());
        if (taskList.size() == 0){
            tasksMap.remove(oldDueDate);
        }
    }

    public void setTasksMap(HashMap<String, ArrayList<Task>> tasksMap) {
        this.tasksMap = tasksMap;
    }

    public void setScheduleController(ScheduleController scheduleController) {
        this.scheduleController = scheduleController;
    }
}
