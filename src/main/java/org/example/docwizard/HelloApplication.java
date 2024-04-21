package org.example.docwizard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // create an Event Handler
        EventHandler<ActionEvent> event =
                e -> {
                    // get the file selected
                    File file = inputFileChooser.showOpenDialog(stage);

                    if (file != null) {
                        label.setText(file.getAbsolutePath()
                                + "  selected");
                        files.add(file);
                    }
                };

        chooseButton.setOnAction(event);

        // create a Button
        Button scanButton = new Button("Scan");

        // create an Event Handler
        EventHandler<ActionEvent> event1 =
                e -> needToSwap = scanFiles(files);

        scanButton.setOnAction(event1);


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




        EventHandler<ActionEvent> swapEvent =
                e -> {
                    GridPane root = new GridPane();
                    root.setHgap(8);
                    root.setVgap(8);
                    root.setPadding(new Insets(5));
                    Button submit = new Button("Подтвердить");


                    Stage swapStage = new Stage();
                    swapStage.setTitle("Введите значения на замену");
                    for(int i = 0; i < needToSwap.size(); i++){
                        root.addRow(i, new Label(needToSwap.get(i)), new TextField());
                    }
                    root.addRow(needToSwap.size(), submit);
                    hbox.getChildren().addLast(root);
                    HBox.setHgrow(root, Priority.ALWAYS);

//                    swapStage.setScene(new Scene(root, 450, 450));
//                    swapStage.show();

                    EventHandler<ActionEvent> submitEvent = actionEvent -> {
                        for(Node ob : root.getChildren()){
                            if(ob instanceof TextField){
                                wordToSwap.add(((TextField) ob).getText());
                            }
                        }
                        //swapStage.close();
                    };

                    submit.setOnAction(submitEvent);
                };
        swapButton.setOnAction(swapEvent);


        EventHandler<ActionEvent> createEvent = actionEvent -> {
            File dir = outputDirChooser.showDialog(stage);
            for (File file : files){
                try(FileOutputStream out = new FileOutputStream(dir.getAbsolutePath()+"\\"+"new_"+file.getName());
                    FileInputStream in =  new FileInputStream(file.getAbsolutePath());
                    XWPFDocument outDoc = new XWPFDocument();
                    XWPFDocument inDoc = new XWPFDocument(in)){

                    List<XWPFParagraph> paragraphs = inDoc.getParagraphs();
                    List<String> swapped = swapData(paragraphs);

                    for(String str : swapped){
                        XWPFParagraph p = outDoc.createParagraph();
                        XWPFRun run =  p.createRun();
                        run.setText(str,0);
                    }
                    outDoc.write(out);
                }
                catch (IOException ignored){
                }
            }
        };

        createButton.setOnAction(createEvent);



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

    private ArrayList<String> scanFiles(ArrayList<File> files) {
        ArrayList<String> res = new ArrayList<>();
        for (File file : files) {
            try(FileInputStream fis = new FileInputStream(file.getAbsolutePath())){
                XWPFDocument doc = new XWPFDocument(fis);
                scanFile(doc,res);
            }
            catch (IOException ignored){
            }
        }
        return res;
    }

    private void scanFile(XWPFDocument doc, ArrayList<String> res){
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        for( XWPFParagraph par: paragraphs) {
            String str = par.getText();
            Pattern p = Pattern.compile("<.+>");
            Matcher m = p.matcher(str);
            while(m.find()){
                int start = m.start();
                int end = m.end();
                res.add(str.substring(start,end));
            }
        }

    }

    private List<String> swapData(final List<XWPFParagraph> paragraphs){
        List<String> res = new ArrayList<>();
        for(XWPFParagraph par: paragraphs){
            String str = par.getText();
            for(int i = 0; i <needToSwap.size();i++ ){
                str = str.replace(needToSwap.get(i), wordToSwap.get(i) );
            }
            res.add(str);
        }
        return res;
    }
}