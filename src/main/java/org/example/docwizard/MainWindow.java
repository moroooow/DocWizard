package org.example.docwizard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
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

        TreeView<File> treeView = new TreeView<>();
        treeView.setCellFactory(param -> new FileTreeCell());
        treeView.setShowRoot(false);

        ArrayList<File> files = new ArrayList<>();

        Button chooseButton = new Button("Open");

        SplitPane mainPane = new SplitPane();
        HBox hbox = new HBox();
        mainPane.setDividerPosition(0,0.2);

        chooseButton.setOnAction(event -> {
            selectedDir = getInputDir(inputDirChooser, stage);
            if(selectedDir != null && selectedDir.canRead() && selectedDir.canWrite()){
                TreeItem<File> rootItem = new TreeItem<>(selectedDir.getAbsoluteFile());
                addFilesAndSubdirectories(selectedDir, rootItem);
                treeView.setRoot(rootItem);
                mainPane.getItems().addAll(treeView,hbox);
            }
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

        VBox vbox = new VBox(toolBar, mainPane);


        mainPane.setOrientation(Orientation.HORIZONTAL);
        VBox.setVgrow(mainPane,Priority.ALWAYS);


        scanButton.setOnAction(event -> {
                    needToSwap = (ArrayList<String>) MainWindowEventHandler.handleScan(treeView.getRoot());
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

    private static void addFilesAndSubdirectories(File directory, TreeItem<File> parentItem) {
        File[] files = directory.listFiles();
        if (files == null ) {
            return;
        }
        for (File file : files) {
            TreeItem<File> item = new TreeItem<>(file);
            parentItem.getChildren().add(item);

            if (file.isDirectory()) {
                addFilesAndSubdirectories(file, item);
            }
        }
    }

}