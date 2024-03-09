module com.example.taskandquizscheduler {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.taskandquizscheduler to javafx.fxml;
    exports com.example.taskandquizscheduler;
}