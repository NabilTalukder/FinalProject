package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.util.StringConverter;

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
