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

import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private ArrayList<Task> taskList = new ArrayList<>();
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();
    private ScheduleController scheduleController;
    private BufferedReader br = null;
    private BufferedWriter bw = null;
    private String taskAction;
    private String oldTaskName;
    private String oldDueDate;

    @FXML
    private Label taskTypeLabel;

    @FXML
    private Button confirmTaskButton;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private TextField taskNameField;

    @FXML
    private Hyperlink cancelLink;

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

    //handle mouse event of clicking Add Task button
    EventHandler<? super ActionEvent> addTaskHandler = this::confirmAddTask;

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

        //allow Confirm (Edit) Task button to process an edited task
        confirmTaskButton.setOnAction((EventHandler<ActionEvent>) editTaskHandler);
    }

    //handle mouse event of clicking Edit Task button
    EventHandler<? super ActionEvent> editTaskHandler = this::confirmEditTask;


    @FXML
    protected void closeView(ActionEvent event){
        //get the stage from which the button that called this method was clicked
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        //close stage (task-view window)
        stage.close();
    }

    @FXML
    protected void confirmAddTask(ActionEvent event){
        //get the task name and due date
        String taskName = taskNameField.getText();
        String dueDate = dueDatePicker.getValue().toString();
        //create a new task and set the retrieved information
        Task task = new Task();
        task.setTaskName(taskName);
        //check if there are any tasks for the task's set due date
        //if there are no tasks, add the task to a new list to that date in tasksMap
        taskList = tasksMap.computeIfAbsent(dueDate, k -> new ArrayList<>());
        //either way, new task is added to taskList
        taskList.add(task);
        //send updated tasksMap back to schedulerController
        scheduleController.setTasksMap(tasksMap);
        //update calendar with new task
        scheduleController.calendarCalc();
        //update tasks file with newly created task
        updateTasksFile(taskName, dueDate);
        //process complete, so close task-view popup
        closeView(event);
    }

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
            taskList = tasksMap.get(newDueDate);

            //if date or both name and date was changed
            if (!oldDueDate.equals(newDueDate)){
                /*remove the task's old name for the due date
                * because changing date is effectively removing then adding new task*/
                taskList.removeIf(task -> task.getTaskName().equals(oldTaskName));
                //remove due date if there are no longer any tasks associated with it
                if (taskList.size() == 0){
                    tasksMap.remove(oldDueDate);
                }
                /*remove the task name from the tasks file for the old due date
                * so the change is preserved upon application restart*/
                removeTaskName(oldDueDate);
                /*old task has been removed, so now add new task
                * changing taskAction so the process for adding a task is followed
                * instead of editing a task, which doesn't apply here as the date
                * was changed*/
                taskAction = "Add";
                //add the task with its new name and/or new due date
                confirmAddTask(event);
            }
            //if only task name was changed
            else {
                for (int i = 0; i < taskList.size(); i++) {
                    //find the task with the same name
                    if (taskList.get(i).getTaskName().equals(oldTaskName)){
                        //replace the name with the new name
                        taskList.get(i).setTaskName(taskNameField.getText());
                    }
                }
                //send updated tasksMap back to schedulerController
                scheduleController.setTasksMap(tasksMap);
                //update calendar with new task
                scheduleController.calendarCalc();
                /*update tasks file by replacing old task name
                * newDueDate is interchangeable with oldDueDate here */
                updateTasksFile(newTaskName, newDueDate);
                //process complete, so close task-view popup
                closeView(event);
            }
        }
    }

    public void updateTasksFile(String taskName, String dueDate) {
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader("data/tasks.txt"));
            //if a task was added, add task name to corresponding due date in file
            if (taskAction.equals("Add")){
                appendTask(taskName, dueDate, sb);
            }
            //if a task was edited, replace the task with the new one
            else if (taskAction.equals("Edit")){
                replaceTaskName(taskName, dueDate, sb);
            }

            br.close();
            //deleting old file so it can be replaced
            File oldFile = new File("data\\tasks.txt");
            oldFile.delete();
            //create updated tasks file with new task added
            bw = new BufferedWriter(new FileWriter("data\\tasks.txt"));
            bw.write(sb.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendTask(String taskName, String dueDate, StringBuilder sb){
        String line;
        boolean dueDateFound = false;
        try {
            //search for dueDate to see if that date already has tasks assigned
            while ((line = br.readLine()) != null) {
                if (line.contains(dueDate)){
                    dueDateFound = true;
                    //edit the line containing the matching due date to include new task
                    //use comma delimiters to separate tasks
                    line = line.replace(line, line + "," + taskName);
                }
                //copy the line (both edited and unedited) with \n to preserve file's structure
                sb.append(line).append("\n");
            }
            //append dueDate and task name if there were no tasks for the chosen date
            //again, using \n to preserve structure
            if (!dueDateFound){
                sb.append(dueDate + "," + taskName + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replaceTaskName(String taskName, String dueDate, StringBuilder sb){
        String line;
        try {
            //search for dueDate so the old task name can be replaced
            while ((line = br.readLine()) != null) {
                if (line.contains(dueDate)){
                    //edit the line containing the matching due date to replace it
                    line = line.replace(oldTaskName, taskName);
                }
                //copy the line (both edited and unedited) with \n to preserve file's structure
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeTaskName(String dueDate){
        StringBuilder sb = new StringBuilder();
        String line;
        boolean dateHasTasks;
        try {
            br = new BufferedReader(new FileReader("data/tasks.txt"));
            //search for dueDate so the old task name can be removed
            while ((line = br.readLine()) != null) {
                dateHasTasks = true;
                if (line.contains(dueDate)){
                    /*account for oldTaskName appearing between other tasks by removing
                    * comma delimiter along with it*/
                    if (line.contains(oldTaskName + ",")){
                        line = line.replace(oldTaskName + ",", "");
                    }
                    //remove if oldTaskName appears at end of line
                    else if (line.contains(oldTaskName)){
                        line = line.replace(oldTaskName, "");
                    }
                }
                /*if there's only a due date remaining on the line (no more tasks)
                 * remove line*/
                if (line.length() == 11){
                    dateHasTasks = false;
                }
                /*copy the line (both edited and unedited) with \n to preserve file's structure
                 * but only for lines (due dates) that still have tasks associated with them*/
                if (dateHasTasks){
                    sb.append(line).append("\n");
                }
            }
            br.close();
            //deleting old file so it can be replaced
            File oldFile = new File("data\\tasks.txt");
            oldFile.delete();
            //create updated tasks file with task removed
            bw = new BufferedWriter(new FileWriter("data\\tasks.txt"));
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTasksMap(HashMap<String, ArrayList<Task>> tasksMap) {
        this.tasksMap = tasksMap;
    }

    public void setScheduleController(ScheduleController scheduleController) {
        this.scheduleController = scheduleController;
    }

    public void setTaskAction(String taskAction) {
        this.taskAction = taskAction;
        //updating labels used in the text-view popup for user benefit
        taskTypeLabel.setText(taskAction + " Task");
        confirmTaskButton.setText(taskAction + " Task");
    }
}
