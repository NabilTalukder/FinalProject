package com.example.taskandquizscheduler;

//used for handling data related to questions in multiple choice quizzes
public class Question {

    private String questionID;

    private String description;

    public String getQuestionID() {
        return questionID;
    }

    public String getDescription() {
        return description;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
