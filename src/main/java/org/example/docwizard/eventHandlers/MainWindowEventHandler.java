package org.example.docwizard.eventHandlers;


import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.example.docwizard.FileScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindowEventHandler {
    private static boolean isScanned = false;
    private static List<String> wordToSwap;
    private static final GridPane root = new GridPane();
    public static void resetIsScanned(){
        isScanned = false;
        root.getChildren().clear();
    }


    public static void handleCreate (FileScanner fileScanner, File dataExcelFile, DirectoryChooser outputDirChooser, Stage stage, List<String> needToSwap){
        if (fileScanner.isEmpty()) {
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

        for (File file : fileScanner.getDocxAndXlsxFiles()) {
            if (file != dataExcelFile) {
                if (file.getAbsolutePath().endsWith(".docx")) {
                    try (FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "\\"
                            + "new_" + file.getName());
                         FileInputStream in = new FileInputStream(file.getAbsolutePath());
                         XWPFDocument inDoc = new XWPFDocument(in)) {

                        XWPFDocument outDoc = replaceTextInDocx(inDoc, needToSwap, wordToSwap);
                        outDoc.write(out);

                    } catch (IOException ignored) {
                    }
                } else if (file.getAbsolutePath().endsWith(".xlsx")) {
                    try (FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "\\"
                            + "new_" + file.getName());
                         FileInputStream in = new FileInputStream(file.getAbsolutePath());
                         XSSFWorkbook inXlsx = new XSSFWorkbook(in)
                    ) {

                        XSSFWorkbook outXlsx = replaceTextInExcel(inXlsx, needToSwap, wordToSwap);
                        outXlsx.write(out);

                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
    private static XWPFDocument replaceTextInDocx(XWPFDocument doc, List<String> originalText, List<String> updatedText) {
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

    public static XSSFWorkbook replaceTextInExcel(XSSFWorkbook xlsx,List<String> originalText, List<String> updatedText) {
        for (int i = 0; i<xlsx.getNumberOfSheets();i++) {
            XSSFSheet sheet = xlsx.getSheetAt(i);

            Iterator<Row> rowIter = sheet.rowIterator();

            while (rowIter.hasNext()) {
                Row row = rowIter.next();
                Iterator<Cell> cellIter = row.cellIterator();

                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();
                    if (cell.getCellType() == CellType.STRING) {

                        for(int j = 0; j < originalText.size();j++) {
                            StringBuilder str = new StringBuilder(cell.getStringCellValue());
                            replaceAll(str, originalText.get(j),updatedText.get(j));
                            cell.setCellValue(str.toString());
                        }
                    }
                }
            }
        }
        return  xlsx;
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
            boolean flagHref = false;
            String hrefText = "<a href=\"" + updatedText.get(i) + "\">" + updatedText.get(i) + "</a>";

            if(updatedText.get(i).contains("//")){
                replaceAll(paragraphText, originalText.get(i),hrefText);
                flagHref = true;
            } else {
                replaceAll(paragraphText,originalText.get(i), updatedText.get(i));
            }

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

            if(flagHref){
                String[] splitedHyperLinks = paragraphText.toString().split(hrefText);

                for(int j = 0;  j < splitedHyperLinks.length; j++){
                    XWPFHyperlinkRun hyperlinkRun = paragraph.createHyperlinkRun(hrefText);
                    hyperlinkRun.setText(updatedText.get(i));
                    hyperlinkRun.setColor("0000FF");
                    hyperlinkRun.setUnderline(UnderlinePatterns.SINGLE);
                    paragraph.addRun(hyperlinkRun);
                }

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


    public static void handleSwap(HBox hbox, List<String> needToSwap) {
        root.getChildren().clear();
        renderFields(needToSwap);
        hbox.getChildren().clear();
        hbox.getChildren().add(root);
    }

    private static void renderFields(List<String> needToSwap){
        root.setHgap(8);
        root.setVgap(8);
        root.setPadding(new Insets(5));

        Button submit = new Button("Подтвердить");

        for (int i = 0; i < needToSwap.size(); i++) {
            root.addRow(i, new Label(needToSwap.get(i)), new TextField());
        }

        submit.setOnAction(e -> wordToSwap = validateAndSaveData());
        if(!root.getChildren().isEmpty()) {
            root.addRow(needToSwap.size(), submit);
        }
    }

    private static List<String> validateAndSaveData(){
        boolean isFieldFilled = true;
        List<String> temp = new ArrayList<>();
        for (Node ob : root.getChildren()) {
            if (ob instanceof TextField) {
                if(((TextField) ob).getText().isEmpty()) {
                    ((TextField) ob).setPromptText("Поле не заполнено");
                    isFieldFilled = false;
                } else {
                    temp.add(((TextField) ob).getText());
                }
            }
        }
        if(isFieldFilled){
            isScanned = true;
            return temp;
        } else {
            isScanned = false;
            return null;
        }
    }

    public static void settingLinesFromInformationFile(HashMap<String, String> markingColumns){
        String key = "";
        String value;
        for (Node ob : root.getChildren()) {

            if (ob instanceof Label){
                key = ((Label) ob).getText();
                if (key != null) {
                    key = key.toLowerCase();
                }
            }
            if (ob instanceof TextField) {
                value = findMarkingColumns(markingColumns,key);
                if (value != null) {
                    ((TextField) ob).setText(value);
                }

            }
        }
    }

    public static String findMarkingColumns (HashMap<String, String> markingColumns, String key){
        for (Map.Entry<String, String> entry : markingColumns.entrySet()) {
            String value = entry.getKey();
            if (entry.getKey() != null && value.toLowerCase().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

}