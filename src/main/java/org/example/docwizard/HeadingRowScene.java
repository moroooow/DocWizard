package org.example.docwizard;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class HeadingRowScene {
    private final Stage stage;
    private final TextField fieldHeading;
    private final TextField fieldRow;
    private static boolean isScanned = false;
    public static boolean isScanned(){
        return isScanned;
    }
    public static void resetIsScanned(){
        isScanned = false;
    }
    public HeadingRowScene(){
        isScanned = false;
        stage = new Stage();
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        Scene scene = new Scene(pane, 400, 200);
        stage.setScene(scene);
        stage.setTitle("Введите заголовочной строки и номер строки с данными");
        Label lblHeading = new Label("Введите номер заголовка:");
        Label lblRow = new Label("Введите номер строки с данными:");
        fieldHeading = new TextField();
        fieldRow = new TextField();
        Button submitButton = new Button("Подтвердить");
        pane.add(lblHeading, 0, 0);
        pane.add(fieldHeading, 0, 1);
        pane.add(lblRow, 1, 0);
        pane.add(fieldRow, 1, 1);
        pane.add(submitButton, 2, 1);
        pane.setAlignment(Pos.CENTER);
        submitButton.setOnAction(_ -> {
            int valueFromHeading = getValueFromHeading();
            int valueFromRow = getValueFromRow();
            if (valueFromHeading != 0 && valueFromRow != 0){
                ResourceExcel.setHeadingNumber(getValueFromHeading() - 1);
                ResourceExcel.setRowNumber(getValueFromRow() - 1);
                isScanned = true;
            }
            stage.close();
        });
    }

    public void showScene(){
        stage.showAndWait();
    }
    public int getValueFromHeading(){
        return Integer.parseInt(fieldHeading.getText());
    }
    public int getValueFromRow(){
        return Integer.parseInt(fieldRow.getText());
    }

}
