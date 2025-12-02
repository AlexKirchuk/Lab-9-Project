module com.example.lab_9_project {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.lab_9_project.client to javafx.graphics;
    exports com.example.lab_9_project.protocol;
    exports com.example.lab_9_project.server;
}