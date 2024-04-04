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
}
