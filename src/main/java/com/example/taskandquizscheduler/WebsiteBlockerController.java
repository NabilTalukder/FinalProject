package com.example.taskandquizscheduler;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.*;

public class WebsiteBlockerController {

    //used to switch between scenes/pages
    private ViewHandler viewHandler;

    //used to take input (URLs) to block websites
    @FXML
    protected TextArea blockedSitesTextArea = new TextArea();

    public void init(ViewHandler viewHandler){
        this.viewHandler = viewHandler;
    }

    @FXML
    protected void goToSchedule(){
        viewHandler.openView("Schedule");
    }

    @FXML
    protected void goToQuizGenerator(){
        viewHandler.openView("QuizGenerator");
    }

    @FXML
    protected void logout() {
        viewHandler.setUser(null);
        viewHandler.openView("Login");
    }

    @FXML
    protected void blockWebsites() {
        String[] blockedSitesList = blockedSitesTextArea.getText().split("\n");

        // Path to the hosts file
        String hostsFilePath = "C:\\Windows\\System32\\drivers\\etc\\hosts";

        // Command to run the batch script
        String command = "cmd /c start \"\" \"run_as_admin.bat\" \"" + hostsFilePath + "\"";

        try {
            // Start the process
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();

            // Wait for the process to terminate
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Process executed successfully
                System.out.println("Hosts file opened with Notepad as admin");

                // Append websites to block to hosts file
                FileWriter writer = new FileWriter(hostsFilePath, true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                for (String website : blockedSitesList) {
                    bufferedWriter.write("127.0.0.1 " + website);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
                writer.close();

                System.out.println("Websites blocked successfully");
            } else {
                // Process failed to execute
                System.out.println("Failed to open hosts file");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
