package com.example.taskandquizscheduler;

import java.sql.*;

public class UserDataAccessor {

    //database access
    private String connectionURL = "jdbc:mysql://localhost/revision_scheduler";
    private String usernameDB = "root";
    private String passwordDB = "";

    public boolean validateEmailDB(String enteredEmail){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT email FROM user WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, enteredEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                if (resultSet.getString("email").equals(enteredEmail)) {
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //invalid email
        return false;
    }

    public String validatePasswordDB(String enteredEmail, String enteredPassword){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT user_ID, password FROM user WHERE email = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, enteredEmail);
            preparedStatement.setString(2, enteredPassword);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                if (resultSet.getString("password").equals(enteredPassword)) {
                    return resultSet.getString("user_ID");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //invalid password
        return "";
    }


    public void createAccountDB(String enteredEmail, String enteredPassword){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "INSERT INTO user (email, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, enteredEmail);
            preparedStatement.setString(2, enteredPassword);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
