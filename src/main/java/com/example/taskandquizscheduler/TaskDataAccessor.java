package com.example.taskandquizscheduler;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskDataAccessor {

    //contains all dates on the calendar and their associated tasks
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();

    //initial retrieval of tasks from database
    public HashMap<String, ArrayList<Task>> querySchedule(){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "SELECT due_date, task_name, completion_status FROM task WHERE" +
                    " assigner_ID = 1 AND assignee_ID = 1 ORDER BY due_date;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            //used for key-value pairs in tasksMap
            String dueDateKeyDB;
            //used to compare against dueDateKeyDB to see if a new key should be created in tasksMap
            String prevDueDate = "";
            ArrayList<Task> taskListValuesDB = new ArrayList<>();

            //reconstruct tasksMap
            while (resultSet.next()){
                Task task = new Task();
                dueDateKeyDB = resultSet.getString("due_date");
                //every date has its own cell contents (list of tasks)
                if (!dueDateKeyDB.equals(prevDueDate)){
                    taskListValuesDB = new ArrayList<>();
                }
                task.setTaskName(resultSet.getString("task_name"));
                task.setStatus(resultSet.getString("completion_status"));
                taskListValuesDB.add(task);
                tasksMap.put(dueDateKeyDB, taskListValuesDB);
                //prepare for comparison in next row within returned query results
                prevDueDate = dueDateKeyDB;
            }

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasksMap;
    }

    public void addTaskDB(String taskName, String dueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "INSERT INTO task " +
                    "(`task_ID`, `assigner_ID`, `assignee_ID`, `task_name`, `due_date`, `completion_status`)" +
                    " VALUES (NULL, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, taskName);
            preparedStatement.setString(4, dueDate);
            preparedStatement.setString(5, "incomplete");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("added task");
    }

    public void editTaskDB(String oldTaskName, String newTaskName, String oldDueDate, String newDueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "UPDATE task SET task_name = ?, due_date = ? WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newTaskName);
            preparedStatement.setString(2, newDueDate);
            preparedStatement.setString(3, "1");
            preparedStatement.setString(4, "1");
            preparedStatement.setString(5, oldTaskName);
            preparedStatement.setString(6, oldDueDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("edited task");
    }

    public void deleteTaskDB(String taskName, String dueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "DELETE FROM task WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, taskName);
            preparedStatement.setString(4, dueDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("deleted task");
    }

    public void completeTaskDB(String taskName, String dueDate){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/revision_scheduler",
                    "root", "");
            String sql = "UPDATE task SET completion_status = ? WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "complete");
            preparedStatement.setString(2, "1");
            preparedStatement.setString(3, "1");
            preparedStatement.setString(4, taskName);
            preparedStatement.setString(5, dueDate);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("completed task");
    }
}
