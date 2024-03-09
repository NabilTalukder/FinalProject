package com.example.taskandquizscheduler;

import java.io.*;
import java.net.Socket;

public class ClientController {

    private String quizGenInput;
    private String quizGenOutput;

    public void sendInfo(PrintWriter pw) {
        //send user inputted text to Python server to generate questions
        pw.println(quizGenInput);
        System.out.println("sent");
    }

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
