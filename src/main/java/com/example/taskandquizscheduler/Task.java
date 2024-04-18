package com.example.taskandquizscheduler;

//contains information that is shown on the calendar
public class Task {
    private String taskName;

    //used for marking the task as complete on the calendar if the user has completed it
    private String status;

    private String taskType;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status) { this.status = status;}

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
