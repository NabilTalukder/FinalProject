package com.example.taskandquizscheduler;

import java.sql.*;
import java.util.ArrayList;

public class QuizDataAccessor {

    //database access
    private final String connectionURL = "jdbc:mysql://localhost/revision_scheduler";
    private final String usernameDB = "root";
    private final String passwordDB = "";


    public ArrayList<Quiz> retrieveQuizzesDB(User user){
        ArrayList<Quiz> quizzes = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT quiz_ID, name FROM quiz WHERE user_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUser_ID());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                Quiz quiz = new Quiz();
                quiz.setQuizID(resultSet.getString("quiz_ID"));
                quiz.setQuizName(resultSet.getString("name"));
                quizzes.add(quiz);
            }
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizzes;
    }

    public String retrieveQuizIDDB(User user, String quizName){
        String quizID = "";
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT quiz_ID FROM quiz WHERE user_ID = ? AND name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUser_ID());
            preparedStatement.setString(2, quizName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                quizID = resultSet.getString("quiz_ID");
            }
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizID;
    }

    public ArrayList<Question> retrieveQuestionsDB(String quizID){
        ArrayList<Question> questions = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT quiz_question_ID, description FROM quiz_question WHERE quiz_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, quizID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                //every question has an id and description
                //the id is needed to match a question to its corresponding options
                Question question = new Question();
                question.setQuestionID(resultSet.getString("quiz_question_ID"));
                question.setDescription(resultSet.getString("description"));
                questions.add(question);
            }
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public ArrayList<ArrayList<String>> reconstructQuizFromDB(ArrayList<Question> questions){
        //contains questions and answers
        //in the form [[question 1, option 1, 2, 3, 4, answer], ... [question n, option 1, 2, 3, 4, answer]]
        ArrayList<ArrayList<String>> wholeQuiz = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "SELECT quiz_option_ID, text, correct_option FROM quiz_option WHERE quiz_question_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            //associate each question with its options
            for (Question question : questions){
                preparedStatement.setString(1, question.getQuestionID());
                ResultSet resultSet = preparedStatement.executeQuery();
                ArrayList<String> questionAndOptions = new ArrayList<>();
                ArrayList<Option> options = new ArrayList<>();

                //each question has 4 options
                while (resultSet.next()){
                    Option option = new Option();
                    option.setText(resultSet.getString("text"));
                    if (resultSet.getString("correct_option").equals("1")){
                        option.setCorrect(true);
                    }
                    options.add(option);
                }

                //format into form [question, option 1, 2, 3, 4, answer]
                //first is question
                questionAndOptions.add(question.getDescription());
                String answer = "";
                //then the options
                for (Option option : options){
                    questionAndOptions.add(option.getText());
                    if (option.isCorrect()){
                        answer = option.getText();
                    }
                }
                //finally, the answer
                questionAndOptions.add(answer);
                //add formatted question to the quiz
                wholeQuiz.add(questionAndOptions);
            }
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wholeQuiz;
    }

    public String addQuizDB(User user, String quizName){
        String quizID = "";
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "INSERT INTO quiz (`quiz_ID`, `user_ID`, `name`) VALUES (NULL, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUser_ID());
            preparedStatement.setString(2, quizName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0){
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()){
                    generatedKeys.getInt(1);
                    quizID = String.valueOf(generatedKeys.getInt(1));
                }
            }

            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizID;
    }

    public String addQuestionDB(String quizID, String description){
        String questionID = "";
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "INSERT INTO quiz_question (`quiz_question_ID`, `quiz_ID`, `description`) VALUES (NULL, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, quizID);
            preparedStatement.setString(2, description);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0){
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()){
                    generatedKeys.getInt(1);
                    questionID = String.valueOf(generatedKeys.getInt(1));
                }
            }

            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questionID;
    }

    public void addOptionsDB(String questionID, ArrayList<String> questionAndOptions){
        try {
            Connection connection = DriverManager.getConnection(connectionURL,
                    usernameDB, passwordDB);
            String sql = "INSERT INTO quiz_option (`quiz_option_ID`, `quiz_question_ID`, `text`, `correct_option`)" +
                    " VALUES (NULL, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            /*iterate through options, adding them to the batch insertion
            * starting from index 1 to exclude the question description,
            * ending at size() - 1 to exclude the answer
            * so only options are added to the table*/
            for (int i = 1; i < questionAndOptions.size() - 1; i++){
                preparedStatement.setString(1, questionID);
                String optionText = questionAndOptions.get(i);
                preparedStatement.setString(2, optionText);
                //check if option is the answer for the question
                if (optionText.equals(questionAndOptions.get(4))){
                    preparedStatement.setString(3, "1");
                }
                else {
                    preparedStatement.setString(3, "0");
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
