package org.example.docwizard.eventHandlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindowEventHandler {
    private static boolean isScaned = false;

    private static List<String> swapData(final List<XWPFParagraph> paragraphs, List<String> needToSwap, List<String> wordToSwap) {
        List<String> res = new ArrayList<>();
        for (XWPFParagraph par : paragraphs) {
            String str = par.getText();
            for (int i = 0; i < needToSwap.size(); i++) {
                str = str.replace(needToSwap.get(i), wordToSwap.get(i));
            }
            res.add(str);
        }
        return res;
    }

    public static void handleCreate (List<File> files, DirectoryChooser outputDirChooser, Stage stage, List<String> needToSwap, List<String> wordToSwap){
        if (!files.isEmpty()) {
            File dir = outputDirChooser.showDialog(stage);
            if(dir == null) {
                return;
            }
            for (File file : files) {
                try (FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "\\" + "new_" + file.getName());
                     FileInputStream in = new FileInputStream(file.getAbsolutePath());
                     XWPFDocument outDoc = new XWPFDocument();
                     XWPFDocument inDoc = new XWPFDocument(in)) {

                    List<XWPFParagraph> paragraphs = inDoc.getParagraphs();
                    List<String> swapped = swapData(paragraphs, needToSwap, wordToSwap);

                    for (String str : swapped) {
                        XWPFParagraph p = outDoc.createParagraph();
                        XWPFRun run = p.createRun();
                        run.setText(str, 0);
                    }
                    outDoc.write(out);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void scanFile(XWPFDocument doc, ArrayList<String> res) {
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        for (XWPFParagraph par : paragraphs) {
            String str = par.getText();
            Pattern p = Pattern.compile("<.+>");
            Matcher m = p.matcher(str);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                res.add(str.substring(start, end));
            }
        }

    }
    private static void scanFiles(TreeItem<File> directory,List<String> collection) {
        for (TreeItem<File> children : directory.getChildren()) {
            if(children.getValue().isDirectory()){
                scanFiles(children,collection);
                continue;
            }
            if(!children.getValue().getAbsolutePath().endsWith(".docx") ){
                continue;
            }
            try (FileInputStream fis = new FileInputStream(children.getValue().getAbsolutePath())) {
                XWPFDocument doc = new XWPFDocument(fis);
                scanFile(doc, (ArrayList<String>) collection);
            } catch (IOException ignored) { }
        }
    }
    public static List<String> handleScan(TreeItem<File> rootItem) {
        ArrayList<String> res = new ArrayList<>();
        if (rootItem != null) {
            scanFiles(rootItem,res);
            return  res;
        }
        return null;
    }
    public static void handleSwap(List<File> files,HBox hbox, List<String> needToSwap, List<String> wordToSwap) {
        if (files.isEmpty() && !isScaned) {
            isScaned = true;
            GridPane root = new GridPane();
            root.setHgap(8);
            root.setVgap(8);
            root.setPadding(new Insets(5));
            Button submit = new Button("Подтвердить");
            for (int i = 0; i < needToSwap.size(); i++) {
                root.addRow(i, new Label(needToSwap.get(i)), new TextField());
            }
            root.addRow(needToSwap.size(), submit);
            hbox.getChildren().addLast(root);
            HBox.setHgrow(root, Priority.ALWAYS);
            EventHandler<ActionEvent> submitEvent = actionEvent -> {
                for (Node ob : root.getChildren()) {
                    if (ob instanceof TextField) {
                        wordToSwap.add(((TextField) ob).getText());
                    }
                }

                //swapStage.close();
            };

            submit.setOnAction(submitEvent);
        }
    }
}

