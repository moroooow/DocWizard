package org.example.docwizard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.docwizard.eventHandlers.EventHandlers;

import java.io.File;
import java.util.ArrayList;

public class HelloApplication extends Application {


     private ArrayList<String> needToSwap = new ArrayList<>();
     private final ArrayList<String> wordToSwap = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("DocWizard");
        // create a File chooser
        FileChooser inputFileChooser = new FileChooser();
        DirectoryChooser outputDirChooser = new DirectoryChooser();

        configureFileChooser(inputFileChooser);

        ArrayList<File> files = new ArrayList<>();

        // create a Label
        Label label = new Label("no files selected");
        label.setFont(new Font(15));

        //create a Button
        Button chooseButton = new Button("Open");

        chooseButton.setOnAction(event -> EventHandlers.handleChoose(inputFileChooser, stage, label, files));

        // create a Button
        Button scanButton = new Button("Scan");

        scanButton.setOnAction(event -> needToSwap = (ArrayList<String>) EventHandlers.handleScan(files));

        Button swapButton = new Button("Enter to swap");

        Button createButton = new Button("Save changes");

        //create a toolBar
        ToolBar toolBar = new ToolBar();

        toolBar.getStyleClass().add("custom-toolbar");
        toolBar.getItems().add(chooseButton);
        toolBar.getItems().add(scanButton);
        toolBar.getItems().add(swapButton);
        toolBar.getItems().add(createButton);

        // create a VBox
        //BorderPane bPane = new BorderPane();
        HBox hbox = new HBox();
        VBox vbox = new VBox(toolBar, hbox);
        hbox.getChildren().addFirst(label);
        HBox.setHgrow(label, Priority.ALWAYS);
        vbox.getStyleClass().add("custom-root");


        swapButton.setOnAction(event -> EventHandlers.handleSwap(files,hbox,needToSwap,wordToSwap));

        createButton.setOnAction(event -> EventHandlers.handleCreate(files, outputDirChooser, stage, needToSwap, wordToSwap));
        // create a scene
        Scene scene = new Scene(vbox, 600, 400);
        //Scene scene = new Scene(bPane, 960, 600);
        scene.getStylesheets().add("/style.css");

        // set the scene
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    private static void configureFileChooser(final FileChooser fileChooser){
        fileChooser.setInitialDirectory( new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("docx", "*.docx")
        );
    }

}