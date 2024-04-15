package com.example.taskandquizscheduler;

public class Option {

    private String text;

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
