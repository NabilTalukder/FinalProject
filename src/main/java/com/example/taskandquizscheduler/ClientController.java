package com.example.taskandquizscheduler;

import java.io.*;
import java.net.Socket;

//handles sending and receiving data (quizzes as Strings) from the Python program
public class ClientController {

    //the input string to be used in the prompt to generate a quiz
    private String quizGenInput;
    //the outputted quiz string from the Python program
    private String quizGenOutput;

    //send user-inputted text to Python program to generate questions
    public void sendInfo(PrintWriter pw) {
        pw.println(quizGenInput);
        System.out.println("sent");
    }

    //retrieve the result of the prompt being sent to the Python program
    public String retrieveInfo(Socket clientSocket) {
        //reset output string to prepare for new output from Python server
        quizGenOutput = "";
        StringBuilder sb = new StringBuilder();

        //read Python server's input stream to get the outputted quiz
        try {
            //initialise BufferedReader to read input stream as a string instead of bytes
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("reading... ");

            String line;

            //read lines until none left or # reached (signifies end)
            while ((line = br.readLine()) != null) {
                //append \n so the output preserves line breaks from the original output
                sb.append(line).append("\n");
                if (line.endsWith("#")) {
                    break;
                }
            }
            br.close();

            //convert to string and remove "#" from the end
            quizGenOutput = sb.toString();
            quizGenOutput = quizGenOutput.substring(0, quizGenOutput.length() - 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return to QuizGeneratorController to display the output on screen
        return quizGenOutput;
    }

    public void setQuizGenInput(String quizGenInput) {
        this.quizGenInput = quizGenInput;
    }
}
