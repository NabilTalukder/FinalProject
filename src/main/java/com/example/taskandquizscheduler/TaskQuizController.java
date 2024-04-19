package com.example.taskandquizscheduler;

import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class TaskQuizController extends TaskController {

    private QuizDataAccessor quizDataAccessor;

    @FXML
    private MFXFilterComboBox quizNameComboBox;

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
}
