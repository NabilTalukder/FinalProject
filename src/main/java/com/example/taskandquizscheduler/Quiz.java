package com.example.taskandquizscheduler;

//used for handling data related to multiple choice quizzes
public class Quiz {

    private String quizName;

    private String quizID;

    public Quiz() {

    }

    public Quiz(String quizName){
        this.quizName = quizName;
    }

    public String getQuizName() {
        return quizName;
    }

    public String getQuizID() {
        return quizID;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    public void setQuizID(String quizID) {
        this.quizID = quizID;
    }
}
