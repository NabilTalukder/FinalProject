package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class TaskQuizController extends TaskController {

    private QuizDataAccessor quizDataAccessor;

    @FXML
    private MFXFilterComboBox<Quiz> quizNameComboBox = new MFXFilterComboBox<>();

    public TaskQuizController(){
        quizDataAccessor = new QuizDataAccessor();
    }

    @FXML
    protected void initialiseComboBox(){
        //retrieve all the user's saved quizzes
        ArrayList<Quiz> quizzesFromDB = quizDataAccessor.retrieveQuizzesDB(user);
        //add the quizzes to the combobox
        ObservableList<Quiz> loadedQuizzes = FXCollections.observableArrayList();
        loadedQuizzes.addAll(quizzesFromDB);
        //convert Quiz objects to Strings to be displayed in loadQuizComboBox
        StringConverter<Quiz> converter = FunctionalStringConverter.to(quiz -> (quiz == null) ? "" : quiz.getQuizName());
        //filter Quiz objects that match what was selected by the user
        Function<String, Predicate<Quiz>> filterFunction = s -> quiz -> StringUtils.containsIgnoreCase(converter.toString(quiz), s);
        quizNameComboBox.setItems(loadedQuizzes);
        quizNameComboBox.setConverter(converter);
        quizNameComboBox.setFilterFunction(filterFunction);
    }

    //Retrieve task information for the selected task on the calendar
    @FXML @Override
    protected void editTask(Event taskEvent, String monthVal, String yearVal, String taskType){
        //retrieve task label
        Label taskLabel = (Label) taskEvent.getSource();
        //retrieve date label from cell (VBox) containing task label
        Label dateLabel = (Label) ((VBox) taskLabel.getParent()).getChildren().get(0);
        //search for and display quiz name in combobox
        for (Quiz quiz : quizNameComboBox.getItems()){
            if (quiz.getQuizName().equals(taskLabel.getText())){
                quizNameComboBox.selectItem(quiz);
                break;
            }
        }
        //task name of selected task label - required for editing task
        oldTaskName = taskLabel.getText();
        //reconstruct and display due date of task
        dueDatePicker.setValue(LocalDate.parse(dateLabel.getText() + "-"
                + Month.valueOf(monthVal).getValue() + "-"
                + yearVal, DateTimeFormatter.ofPattern("d-M-yyyy")));
        //make dueDatePicker popup (when editing dueDate) show the task's assigned year/month for QoL
        int dueMonth = Month.valueOf(monthVal).getValue();
        dueDatePicker.setStartingYearMonth(YearMonth.of(Integer.parseInt(yearVal), dueMonth));
        //due date of selected task label - required for editing task
        oldDueDate = dueDatePicker.getValue().toString();

        taskTypeLabel.setText("Edit " + taskType);

        //show delete link because a created task should be able to be deleted
        deleteTaskLink.setVisible(true);
        //show complete link because a created task should be able to be marked as complete
        completeLink.setVisible(true);
        //allow Confirm (Edit) Task button to process an edited task
        confirmButton.setOnAction((EventHandler<ActionEvent>) editTaskHandler);
    }

    //update the calendar with the new task
    @FXML @Override
    protected void confirmAddTask(ActionEvent event){
        //get the task name and due date
        String quizName = quizNameComboBox.getSelectedItem().getQuizName();
        String dueDate = dueDatePicker.getValue().toString();
        //create a new task and set the retrieved information
        Task task = new Task();
        task.setTaskName(quizName);
        task.setStatus("incomplete");
        task.setTaskType("quiz");
        //check if there are any tasks for the task's set due date
        //if there are no tasks, add the task to a new list to that date in tasksMap
        taskList = tasksMap.computeIfAbsent(dueDate, k -> new ArrayList<>());
        //either way, new task is added to taskList
        taskList.add(task);
        //send updated tasksMap back to scheduleController
        scheduleController.setTasksMap(tasksMap);
        //update calendar with new task
        scheduleController.refreshCalendar();
        //update database with newly created task
        taskDataAccessor.addTaskDB(quizName, dueDate, user, "quiz");
        //process complete, so close TaskManagementView popup
        closeView(event);
    }
}
