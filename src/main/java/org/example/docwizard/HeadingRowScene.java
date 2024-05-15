package org.example.docwizard;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class HeadingRowScene {
    private Stage stage;
    private Scene scene;
    private GridPane pane;
    private Label lblHeading;
    private Label lblRow;
    private TextField fieldHeading;
    private TextField fieldRow;
    private Button submitButton;

    public HeadingRowScene(AtomicInteger headingNumber, AtomicInteger rowNumber){
        stage = new Stage();
        pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        scene = new Scene(pane,400,200);
        stage.setScene(scene);
        stage.setTitle("Введите заголовочной строки и номер строки с данными");
        lblHeading = new Label("Введите номер заголовка:");
        lblRow = new Label("Введите номер строки с данными:");
        fieldHeading = new TextField();
        fieldRow = new TextField();
        submitButton = new Button("Подтвердить");
        pane.add(lblHeading, 0, 0);
        pane.add(fieldHeading, 0, 1);
        pane.add(lblRow, 1, 0);
        pane.add(fieldRow, 1, 1);
        pane.add(submitButton, 2, 1);
        pane.setAlignment(Pos.CENTER);
        submitButton.setOnAction(_ -> {
            headingNumber.set(getValueFromHeading() - 1);
            rowNumber.set(getValueFromRow() - 1);
            stage.close();
        });
    }
    public void showScene(){
        stage.showAndWait();
    }
    public void closeScene(){
        stage.close();
    }
    public Button getButton(){
        return submitButton;
    }
    public int getValueFromHeading(){
        return Integer.parseInt(fieldHeading.getText());
    }
    public int getValueFromRow(){
        return Integer.parseInt(fieldRow.getText());
    }

}
