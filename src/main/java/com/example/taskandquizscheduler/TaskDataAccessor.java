package com.example.taskandquizscheduler;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskDataAccessor {

    //database access
    private String connectionURL = "jdbc:mysql://localhost/revision_scheduler";
    private String usernameDB = "root";
    private String passwordDB = "";

    //contains all dates on the calendar and their associated tasks
    private HashMap<String, ArrayList<Task>> tasksMap = new HashMap<>();

    //initial retrieval of tasks from database
    public HashMap<String, ArrayList<Task>> querySchedule(User user){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT due_date, task_name, task_type, completion_status FROM task WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? ORDER BY due_date;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUser_ID());
            preparedStatement.setString(2, user.getUser_ID());
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
                task.setTaskType(resultSet.getString("task_type"));
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

    //if a user has assigned a task, it's added to the database
    public void addTaskDB(String taskName, String dueDate, User user, String taskType){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "INSERT INTO task " +
                    "(`task_ID`, `assigner_ID`, `assignee_ID`, `task_name`, `due_date`, `task_type`, `completion_status`)" +
                    " VALUES (NULL, ?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUser_ID());
            preparedStatement.setString(2, user.getUser_ID());
            preparedStatement.setString(3, taskName);
            preparedStatement.setString(4, dueDate);
            preparedStatement.setString(5, taskType);
            preparedStatement.setString(6, "incomplete");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("added task");
    }

    //editing a task in the Schedule is updated in the database
    public void editTaskDB(String oldTaskName, String newTaskName, String oldDueDate, String newDueDate, User user){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "UPDATE task SET task_name = ?, due_date = ? WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newTaskName);
            preparedStatement.setString(2, newDueDate);
            preparedStatement.setString(3, user.getUser_ID());
            preparedStatement.setString(4, user.getUser_ID());
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

    public void deleteTaskDB(String taskName, String dueDate, User user){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "DELETE FROM task WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUser_ID());
            preparedStatement.setString(2, user.getUser_ID());
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

    public void completeTaskDB(String taskName, String dueDate, User user){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "UPDATE task SET completion_status = ? WHERE" +
                    " assigner_ID = ? AND assignee_ID = ? AND task_name = ? AND due_date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "complete");
            preparedStatement.setString(2, user.getUser_ID());
            preparedStatement.setString(3, user.getUser_ID());
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
