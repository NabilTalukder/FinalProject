module com.example.taskandquizscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;
    requires VirtualizedFX;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens com.example.taskandquizscheduler to javafx.fxml;
    exports com.example.taskandquizscheduler;
}