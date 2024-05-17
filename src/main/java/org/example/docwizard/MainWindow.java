package org.example.docwizard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.docwizard.eventHandlers.MainWindowEventHandler;

import javafx.scene.control.TextField;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MainWindow extends Application {
     private final ArrayList<String> needToSwap = new ArrayList<String>(){
        @Override
        public boolean add(String s) {
            if (!contains(s)) {
                return super.add(s);
            }
            return false;
        }
     };
     private TreeItem<File> rootItem;
     public final double minScreenWidth = 842.0;
     public final double minScreenHeight = 592.0;
     private Stage primaryStage;

    public static File getDataExcelFile() {
        return dataExcelFile;
    }

    private static File dataExcelFile;
    public static void resetDataExcelFile(){
        dataExcelFile = null;
    }
     private FileScanner fileScanner;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("DocWizard");
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(minScreenWidth);
        primaryStage.setMinHeight(minScreenHeight);

        DirectoryChooser inputDirChooser = new DirectoryChooser();
        inputDirChooser.setTitle("Выберите папку: ");
        DirectoryChooser outputDirChooser = new DirectoryChooser();

        TreeView<File> treeView = new TreeView<>();
        treeView.setCellFactory(event -> new FileTreeCell());
        treeView.setShowRoot(false);

        treeView.setOnMouseClicked((MouseEvent event) ->{
            if (event.getButton() == MouseButton.SECONDARY) {
                TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    treeView.setContextMenu(configureContextMenu(selectedItem));
                }
            }
        });

        TextField hintField = new TextField("Наведите кусор на кнопку, чтобы увидеть подсказку");
        hintField.setEditable(false);


        Button chooseButton = new Button("Open");

        SplitPane mainPane = new SplitPane();
        HBox hbox = new HBox();
        mainPane.setDividerPosition(0,0.2);
        mainPane.setVisible(false);

        chooseButton.setOnAction(event -> {
            File selectedDir = getInputDir(inputDirChooser, primaryStage);
            if(selectedDir != null && selectedDir.canRead() && selectedDir.canWrite()){
                rootItem = new TreeItem<>(selectedDir.getAbsoluteFile());
                addFilesAndSubdirectories(selectedDir, rootItem);
                fileScanner = new FileScanner(rootItem);
                treeView.setRoot(rootItem);
                ResourceExcel.resetInfInRow();
                reset();
                mainPane.setVisible(true);
            }
        });

        setHoverHintMessage(hintField, chooseButton, "Открыть папку с файлами");
        Button scanButton = new Button("Scan");

        Button createButton = new Button("Save changes");

        ToolBar toolBar = new ToolBar();

        toolBar.getStyleClass().add("custom-toolbar");
        toolBar.getItems().add(chooseButton);
        toolBar.getItems().add(scanButton);
        toolBar.getItems().add(createButton);

        VBox vbox = new VBox(toolBar, mainPane,hintField);

        mainPane.setOrientation(Orientation.HORIZONTAL);
        VBox.setVgrow(mainPane,Priority.ALWAYS);

        mainPane.getItems().addAll(treeView,hbox);

        scanButton.setOnAction(event -> {
                    if(dataExcelFile == null){
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Информационный файл не выбран");
                        alert.setHeaderText(null);
                        alert.setContentText("Пожалуйста, проверьте и выберите информационный файл.\n" +
                                "Нажмите ОК, чтобы продолжить работу без информационного файла.");
                        ButtonType buttonTypeOk = new ButtonType("ОК");
                        ButtonType buttonTypeCancel = new ButtonType("Отмена");

                        alert.getButtonTypes().setAll(buttonTypeOk,buttonTypeCancel);

                        Optional<ButtonType> res = alert.showAndWait();
                        if(res.isPresent() && res.get() == buttonTypeCancel){
                            return;
                        }
                    }
            try {
                needToSwap.clear();
                reset();
                fileScanner.handleScan(hbox, needToSwap);
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        });

        setHoverHintMessage(hintField, scanButton, "Найти все заполняемые поля в документах в текущей папке");

        createButton.setOnAction(event -> MainWindowEventHandler.handleCreate(fileScanner,dataExcelFile, outputDirChooser, primaryStage, needToSwap));
        Scene scene = new Scene(vbox,minScreenWidth , minScreenHeight);
        scene.getStylesheets().add("/style.css");

        setHoverHintMessage(hintField, createButton, "Сохранить все измененные поля и создать новый файл на их основе");

        // set the scene
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
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
            if(file.getName().startsWith("~")){
                continue;
            }
            parentItem.getChildren().add(item);

            if (file.isDirectory()) {
                addFilesAndSubdirectories(file, item);
            }
        }
    }

    public ContextMenu configureContextMenu(TreeItem<File> selectedItem){
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-font-family: 'Century Gothic'; -fx-font-size: 12px;");
        MenuItem openInExplorerItem = new MenuItem("Открыть в проводнике");
        openInExplorerItem.setOnAction(event -> {
            try{
                Desktop.getDesktop().open(new File(selectedItem.getValue().getParent()));
            } catch(IOException ignored) { }
        });

        MenuItem deleteItem = new MenuItem("Исключить файл из проекта");
        deleteItem.setOnAction(event -> {
            TreeItem<File> parentItem = selectedItem.getParent();
            if(selectedItem.getValue() == dataExcelFile){
                dataExcelFile = null;
            }
            fileScanner.removeItem(selectedItem);
            parentItem.getChildren().remove(selectedItem);
        });

        MenuItem setDataExcelFile = new MenuItem("Установить файл информационным");
        setDataExcelFile.setOnAction(event -> {
            dataExcelFile = selectedItem.getValue();
            HeadingRowScene.resetIsScanned();

            ResourceExcel.scanningInformationFile(dataExcelFile,primaryStage);
        });

        contextMenu.getItems().addAll(openInExplorerItem, deleteItem);
        if(selectedItem.getValue().getName().endsWith(".xlsx")){
            contextMenu.getItems().add(setDataExcelFile);
        }

        return contextMenu;
    }

    private void setHoverHintMessage(TextField hintField, Button button, String message){
        button.hoverProperty().addListener((old, er, newValue) -> {
            if (newValue) {
                hintField.setText(message);
            } else {
                hintField.setText("Наведите кусор на кнопку, чтобы увидеть подсказку");
            }
        });
    }

    private void reset(){
        FileScanner.resetIsScanned();
        HeadingRowScene.resetIsScanned();
        MainWindow.resetDataExcelFile();
        MainWindowEventHandler.resetIsScanned();
    }
}