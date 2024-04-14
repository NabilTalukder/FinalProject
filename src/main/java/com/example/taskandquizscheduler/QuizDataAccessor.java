package com.example.taskandquizscheduler;

import java.sql.*;

public class QuizDataAccessor {

    //database access
    private String connectionURL = "jdbc:mysql://localhost/revision_scheduler";
    private String usernameDB = "root";
    private String passwordDB = "";

    /*
     * need to reconstruct quiz format by extracting from quiz table
     * every question starts with # so add it to retrieved question
     *   or just recreate questionList from database query
     *
     * select quiz_ID from quiz where user_ID = userID AND name = quizName // quiz
     * select quiz_question_ID, description from questions where user_ID = userID AND quiz_ID = quizID //questions
     * select quiz_option_ID, text, correct_option from quiz_option where quiz_question_ID = quizQuestionID //options
     * */

    /*
     * format:
     * 0: quiz name
     * 1 - n: questions
     *   each question is an arrayList [q, 1, 2, 3, 4, a]*/

    /*
     * initialise questionList []
     * first query for quizID to get corresponding questions and options
     *   questionList 0 = quizName
     *
     * query questions into arraylist
     * for each question, query options and store into new arrayList
     *
     * for (question : questions)
     *   options [] = query options for corresponding question
     *   for (option : options)
     *       if option is answer
     *           answerText = text for this option
     *   questionAndOptions.add(question)
     *   questionAndOptions.addAll(options)
     *   questionAndOptions.add(answerText)
     *   questionList.add(questionAndOptions)
     *   */

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizID;
    }
}
