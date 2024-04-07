module org.example.docwizard {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.docwizard to javafx.fxml;
    exports org.example.docwizard;
}