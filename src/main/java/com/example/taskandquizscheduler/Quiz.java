package com.example.taskandquizscheduler;

public class Quiz {

    private String quizName;

    public Quiz() {

    }

    public Quiz(String quizName){
        this.quizName = quizName;
    }

    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }
}
