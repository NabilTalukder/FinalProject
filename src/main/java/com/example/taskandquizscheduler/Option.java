package com.example.taskandquizscheduler;

//used for handling data related to options (choices) in multiple choice quizzes
public class Option {

    private String text;
    //indicates if the option is the corrct choice for a question in a quiz
    private boolean correct;

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
