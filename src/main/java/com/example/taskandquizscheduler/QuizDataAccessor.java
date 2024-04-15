package com.example.taskandquizscheduler;

import java.sql.*;
import java.util.ArrayList;

public class QuizDataAccessor {

    //database access
    private final String connectionURL = "jdbc:mysql://localhost/revision_scheduler";
    private final String usernameDB = "root";
    private final String passwordDB = "";

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
}
