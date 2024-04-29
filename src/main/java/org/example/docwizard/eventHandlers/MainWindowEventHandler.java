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
import org.apache.poi.xwpf.usermodel.*;

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

    private static final GridPane root = new GridPane();

    public static void resetIsScaned(){
        isScaned = false;
        root.getChildren().clear();
    }

    public static void handleCreate (TreeItem<File> files, DirectoryChooser outputDirChooser, Stage stage, List<String> needToSwap, List<String> wordToSwap){
        if (!files.getChildren().isEmpty()) {
            File dir = outputDirChooser.showDialog(stage);

            if(dir == null) {
                return;
            }

            for (TreeItem<File> file : files.getChildren()) {

                try (FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "\\"
                        + "new_" + file.getValue().getName());
                     FileInputStream in = new FileInputStream(file.getValue().getAbsolutePath());
                     XWPFDocument inDoc = new XWPFDocument(in)) {

                    XWPFDocument outDoc = replaceText(inDoc,needToSwap,wordToSwap);

                    outDoc.write(out);
                } catch (IOException ignored) {}

            }
        }
    }

    private static void scanFile(XWPFDocument doc, ArrayList<String> res) {
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        for (XWPFParagraph par : paragraphs) {
            String str = par.getText();
            Pattern p = Pattern.compile("##+[^:,.\\s\\t\\n]+");
            Matcher m = p.matcher(str);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                res.add(str.substring(start, end));
            }
        }

    }

    private static XWPFDocument replaceText(XWPFDocument doc, List<String> originalText, List<String> updatedText) {
        replaceTextInParagraphs(doc.getParagraphs(), originalText, updatedText);
        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    replaceTextInParagraphs(cell.getParagraphs(), originalText, updatedText);
                }
            }
        }
        return doc;
    }
    private static void replaceTextInParagraphs(List<XWPFParagraph> paragraphs, List<String> originalText, List<String> updatedText) {
        paragraphs.forEach(paragraph -> replaceTextInParagraph(paragraph, originalText, updatedText));
    }
    private static void replaceTextInParagraph(XWPFParagraph paragraph, List<String> originalText, List<String> updatedText) {
        String paragraphText = paragraph.getParagraphText();
        for(int i = 0; i < originalText.size(); i ++){
            if (!paragraphText.contains(originalText.get(i))) {
                continue;
            }
            paragraphText = paragraphText.replace(originalText.get(i), updatedText.get(i));

            while (paragraph.getRuns().size() > 0) {
                paragraph.removeRun(0);
            }

        }

        XWPFRun newRun = paragraph.createRun();

        if(paragraphText.contains("\n")){
            String[] lines = paragraphText.split("\n");
            newRun.setText(lines[0], 0);

            for(int j=1; j<lines.length; j++){
                newRun.addBreak();
                newRun.setText(lines[j]);
            }
        } else {
            newRun.setText(paragraphText);
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
        ArrayList<String> res = new ArrayList<>(){
            @Override
            public boolean add(String s) {
                if (!contains(s)) {
                    return super.add(s);
                }
                return false;
            }
        };

        if (rootItem != null) {
            scanFiles(rootItem,res);
            return  res;
        }
        return null;
    }
    public static void handleSwap(TreeItem<File> files,HBox hbox, List<String> needToSwap, List<String> wordToSwap) {
        if (!files.getChildren().isEmpty() && !isScaned) {
            isScaned = true;
            root.setHgap(8);
            root.setVgap(8);
            root.setPadding(new Insets(5));
            Button submit = new Button("Подтвердить");
            for (int i = 0; i < needToSwap.size(); i++) {
                root.addRow(i, new Label(needToSwap.get(i)), new TextField());
            }
            root.addRow(needToSwap.size(), submit);
            hbox.getChildren().add(root);
            HBox.setHgrow(root, Priority.ALWAYS);
            EventHandler<ActionEvent> submitEvent = actionEvent -> {
                for (Node ob : root.getChildren()) {
                    if (ob instanceof TextField) {
                        wordToSwap.add(((TextField) ob).getText());
                    }
                }

            };

            submit.setOnAction(submitEvent);
        }
    }
}

