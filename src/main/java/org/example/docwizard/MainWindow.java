package org.example.docwizard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Toolkit;
import java.awt.Dimension;
import org.example.docwizard.eventHandlers.MainWindowEventHandler;

import java.io.File;
import java.util.ArrayList;

public class MainWindow extends Application {
     private ArrayList<String> needToSwap = new ArrayList<>();
     private final ArrayList<String> wordToSwap = new ArrayList<>();

     private File selectedDir;

    @Override
    public void start(Stage stage) {
        stage.setTitle("DocWizard");
        // create a File chooser
        DirectoryChooser inputDirChooser = new DirectoryChooser();
        inputDirChooser.setTitle("Выберите папку: ");

        DirectoryChooser outputDirChooser = new DirectoryChooser();

        TreeView<String> treeView = new TreeView<>();
        treeView.setShowRoot(false);

        ArrayList<File> files = new ArrayList<>();

        Button chooseButton = new Button("Open");

        chooseButton.setOnAction(event -> {
            selectedDir = getInputDir(inputDirChooser, stage);
            TreeItem<String> rootItem = new TreeItem<>(selectedDir.getAbsolutePath());
            addFilesAndSubdirectories(selectedDir, rootItem);
            treeView.setRoot(rootItem);
        });

        // create a Button
        Button scanButton = new Button("Scan");

        Button createButton = new Button("Save changes");

        //create a toolBar
        ToolBar toolBar = new ToolBar();

        toolBar.getStyleClass().add("custom-toolbar");
        toolBar.getItems().add(chooseButton);
        toolBar.getItems().add(scanButton);
        toolBar.getItems().add(createButton);


        HBox hbox = new HBox();
        VBox vbox = new VBox(toolBar, hbox);
        hbox.getChildren().add(treeView);
        VBox.setVgrow(hbox,Priority.ALWAYS);

        scanButton.setOnAction(event -> {
                    needToSwap = (ArrayList<String>) MainWindowEventHandler.handleScan(files);
                    MainWindowEventHandler.handleSwap(files,hbox,needToSwap, wordToSwap);
                }
        );

        createButton.setOnAction(event -> MainWindowEventHandler.handleCreate(files, outputDirChooser, stage, needToSwap, wordToSwap));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Scene scene = new Scene(vbox, screenSize.getWidth(), screenSize.getHeight());
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

    private static File getInputDir(DirectoryChooser directoryChooser,Stage stage){
        return directoryChooser.showDialog(stage);
    }

    private static void configureFileChooser(final FileChooser fileChooser){
        fileChooser.setInitialDirectory( new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("docx", "*.docx")
        );
    }

    private static void addFilesAndSubdirectories(File directory, TreeItem<String> parentItem) {
        File[] files = directory.listFiles();
        if (files == null ) {
            return;
        }
        for (File file : files) {
            TreeItem<String> item = new TreeItem<>(file.getName());
            parentItem.getChildren().add(item);

            if (file.isDirectory()) {
                addFilesAndSubdirectories(file, item);
            }
        }
    }

}