package com.example.taskandquizscheduler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AddTaskController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private String taskName;
    private String dueDate;
    private ArrayList<Task> taskList = new ArrayList<>();
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();
    private ScheduleController scheduleController;
    private BufferedReader br = null;
    private BufferedWriter bw = null;
    private String yearVal;
    private String monthVal;

    @FXML
    private Button confirmTaskButton;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private TextField taskNameField;

    @FXML
    private Hyperlink cancelLink;

    @FXML
    protected void setDueDatePicker(){
        /* set the date picker to the month being shown on the calendar for ease of use
        * Date picker will be blank by default (next/prev month buttons not clicked)*/
        if (Month.valueOf(monthVal) != LocalDate.now().getMonth()){
            dueDatePicker.setValue(LocalDate.parse("1" + "-"
                    + Month.valueOf(monthVal).getValue() + "-"
                    + yearVal, DateTimeFormatter.ofPattern("d-M-yyyy")));
        }
    }

    @FXML
    protected void cancelClick(ActionEvent event){
        //get the stage from which the cancel button was clicked
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        //close stage (window)
        stage.close();
    }

    @FXML
    protected void confirmClick(ActionEvent event){
        //get the task name and due date
        taskName = taskNameField.getText();
        dueDate = dueDatePicker.getValue().toString();
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
        //update tasks file
        saveTask(task, dueDate);


        //get the stage from which the confirm button was clicked
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        //close stage because tasksMap has been updated
        stage.close();
    }

    public void saveTask(Task task, String dueDate) {
        String line;
        StringBuilder sb = new StringBuilder();
        boolean dueDateFound = false;
        //search for dueDate to see if that date already has tasks assigned
        try {
            br = new BufferedReader(new FileReader("data/tasks.txt"));
            while ((line = br.readLine()) != null) {
                if (line.contains(dueDate)){
                    dueDateFound = true;
                    //edit the line containing the matching due date to include new task
                    //use comma delimiters to separate tasks
                    line = line.replace(line, line + "," + task.getTaskName());
                }
                //copy the line (both edited and unedited) with \n to preserve file's structure
                sb.append(line + "\n");
            }
            //append dueDate and task name if there were no tasks for the chosen date
            //again, using \n to preserve structure
            if (!dueDateFound){
                sb.append(dueDate + "," + task.getTaskName() + "\n");
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


    public void setTasksMap(HashMap<String, ArrayList<Task>> tasksMap) {
        this.tasksMap = tasksMap;
    }

    public void setScheduleController(ScheduleController scheduleController) {
        this.scheduleController = scheduleController;
    }

    public void setYearVal(String yearVal) {
        this.yearVal = yearVal;
    }

    public void setMonthVal(String monthVal) {
        this.monthVal = monthVal;
    }
}
