package org.example.docwizard.eventHandlers;


import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindowEventHandler {
    private static boolean isScanned = false;
    private static boolean isDrawn = false;
    private static final GridPane root = new GridPane();
    public static void resetIsScanned(){
        isScanned = false;
        root.getChildren().clear();
    }

    private static List<File> getDocxFiles(TreeItem<File> root){
        List<File> res = new ArrayList<>();
        getDocxFile(root,res);
        return res;
    }

    private static void getDocxFile(TreeItem<File> root,List<File> res){
        for(TreeItem<File> file : root.getChildren()){
            if(file.getValue().isDirectory()){
                getDocxFile(file,res);
                continue;
            }

            if(file.getValue().getName().endsWith(".docx")){
                res.add(file.getValue());
            }
        }
    }

    public static void handleCreate (TreeItem<File> files, DirectoryChooser outputDirChooser, Stage stage, List<String> needToSwap, List<String> wordToSwap){

        if (files.getChildren().isEmpty()) {
            return;
        }

        if(!isScanned){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No replacement word found, check all fields!");
            alert.showAndWait();
            return;
        }

        File dir = outputDirChooser.showDialog(stage);

        if(dir == null) {
            return;
        }

        for (File file : getDocxFiles(files)) {

            try (FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "\\"
                    + "new_" + file.getName());
                 FileInputStream in = new FileInputStream(file.getAbsolutePath());
                 XWPFDocument inDoc = new XWPFDocument(in)) {

                XWPFDocument outDoc = replaceText(inDoc,needToSwap,wordToSwap);

                outDoc.write(out);
            } catch (IOException ignored) {}

        }
    }

    private static String[][] scanExcel(HSSFWorkbook xlsx){

        String[][] allValues = new String[0][];

        HSSFSheet sheet = xlsx.getSheetAt(0); //only first sheet will be taken(idk how to take all sheets)

        Iterator<Row> rowIter = sheet.rowIterator();
        int index = 0;

        while (rowIter.hasNext()) {

            HSSFRow row = (HSSFRow) rowIter.next();
            int count = 0;

            for (String cell : scanExcelRow(row)){

                allValues[index][count] = cell;
                count++;
            }
            index++;
        }

        return allValues;
    }

    private static String[] scanExcelRow(HSSFRow row){

        String[] cells = new String[0];
        int index = 0;
        Iterator<Cell> cellIter = row.cellIterator();
        while (cellIter.hasNext()) {
            HSSFCell cell = (HSSFCell) cellIter.next();
            cells[index] = cell.getStringCellValue();
            index++;
        }
        return cells;
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
        StringBuilder paragraphText = new StringBuilder(paragraph.getParagraphText());

        for(int i = 0; i < originalText.size(); i ++){
            if (!paragraphText.toString().contains(originalText.get(i))) {
                continue;
            }

            replaceAll(paragraphText,originalText.get(i), updatedText.get(i));

            while (!paragraph.getRuns().isEmpty()) {
                paragraph.removeRun(0);
            }

            XWPFRun newRun = paragraph.createRun();

            if(paragraphText.toString().contains("\n")){
                String[] lines = paragraphText.toString().split("\n");

                if(lines.length == 0){
                    return;
                }
                newRun.setText(lines[0], 0);

                for(int j=1; j<lines.length; j++){
                    newRun.addBreak();
                    newRun.setText(lines[j]);
                }
            } else {
                newRun.setText(paragraphText.toString());
            }

        }

    }
    private static void replaceAll(StringBuilder sb, String find, String replace){
        Pattern p = Pattern.compile(find);
        Matcher matcher = p.matcher(sb);
        int startIndex = 0;
        while(matcher.find(startIndex)){
            sb.replace(matcher.start(),matcher.end(),replace);

            startIndex = matcher.start() + replace.length();
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
    public static void handleSwap(HBox hbox, List<String> needToSwap, List<String> wordToSwap) {
        root.getChildren().clear();
        renderFields(needToSwap, wordToSwap);
        hbox.getChildren().clear();
        hbox.getChildren().add(root);
    }

    private static void renderFields(List<String> needToSwap, List<String> wordToSwap){
        root.setHgap(8);
        root.setVgap(8);
        root.setPadding(new Insets(5));

        Button submit = new Button("Подтвердить");

        for (int i = 0; i < needToSwap.size(); i++) {
            root.addRow(i, new Label(needToSwap.get(i)), new TextField());
        }

        submit.setOnAction(e -> validateAndSaveData(wordToSwap));
        if(!root.getChildren().isEmpty()) {
            root.addRow(needToSwap.size(), submit);
        }
    }

    private static void validateAndSaveData(List<String> wordToSwap){
        boolean isFieldFilled = true;
        for (Node ob : root.getChildren()) {
            if (ob instanceof TextField) {
                if(((TextField) ob).getText().isEmpty()) {
                    ((TextField) ob).setPromptText("Поле не заполнено");
                    isFieldFilled = false;
                } else {
                    wordToSwap.add(((TextField) ob).getText());
                }
            }
        }
        if(isFieldFilled){
            isScanned = true;
        }
    }

}