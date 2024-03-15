package com.example.taskandquizscheduler;

//contains information that is shown on the calendar
public class Task {
    private String taskName;

    //used for marking the task as complete on the calendar if the user has completed it
    private boolean complete;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
