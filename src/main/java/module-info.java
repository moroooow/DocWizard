module org.example.docwizard {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens org.example.docwizard to javafx.fxml;
    exports org.example.docwizard;
}