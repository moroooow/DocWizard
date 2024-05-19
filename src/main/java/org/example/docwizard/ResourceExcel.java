package org.example.docwizard;

import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.DateUtil;
import org.example.docwizard.eventHandlers.MainWindowEventHandler;

public class ResourceExcel {

    private static final HashMap<String, String> infInRow = new HashMap<>();
    public static void resetInfInRow(){
        infInRow.clear();
    }
    private static int headingNumber;
    private static int rowNumber;
    public static void setHeadingNumber(int i) {
        headingNumber = i;
    }
    public static void setRowNumber(int i) {
        rowNumber = i;
    }

    public static void markData(XSSFWorkbook xlsx){

        String[] tableTitles = scanExcelRow(xlsx, headingNumber, true);
        String[] rowData = scanExcelRow(xlsx, rowNumber, false);

        for (int i = 0; i < tableTitles.length; i++){
            if (tableTitles[i] != null){
                infInRow.put(("##" + tableTitles[i].replace(" ", "_")
                        .replace("\n","_")), rowData[i]);
            } else {
                infInRow.put(tableTitles[i], rowData[i]);
            }
        }
    }

    private static String[] scanExcelRow(XSSFWorkbook xlsx, int numberOfRow, boolean isHeader){

        XSSFSheet sheet = xlsx.getSheetAt(0);
        XSSFRow row = sheet.getRow(numberOfRow);

        String[] cells = new String[row.getLastCellNum() - row.getFirstCellNum()];
        int index = 0;
        Iterator<Cell> cellIter = row.cellIterator();
        while (cellIter.hasNext()) {
            XSSFCell cell = (XSSFCell) cellIter.next();
            if ((cell == null || cell.getCellType() == CellType.BLANK) && isHeader){

                cell = getNotNullCell(xlsx, index, numberOfRow - 1);
            }

            switch (cell.getCellType()) {
                case STRING:
                    cells[index] = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        DataFormatter dataFormatter = new DataFormatter();
                        cells[index] = dataFormatter.formatCellValue(cell);
                    } else {
                        cells[index] = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                case FORMULA:
                    cells[index] = String.valueOf(cell.getCellFormula());
                    break;
                    default:
                    cells[index] = null;
                    break;
            }

            index++;
        }
        return cells;
    }

    private static XSSFCell getNotNullCell(XSSFWorkbook xlsx, int index, int numberOfRow){

        XSSFSheet sheet = xlsx.getSheetAt(0);
        XSSFRow row = sheet.getRow(numberOfRow);
        XSSFCell outCell = row.getCell(index);

        while (outCell == null || outCell.getCellType() == CellType.BLANK){

            outCell = getNotNullCell(xlsx, index, numberOfRow - 1);
        }

        return outCell;
    }

    public static void scanningInformationFile(File dataExcelFile, Stage primaryStage){
        if (dataExcelFile != null) {
            if (!HeadingRowScene.isScanned()) {
                try (
                        FileInputStream in = new FileInputStream(dataExcelFile.getAbsolutePath());
                        XSSFWorkbook inXlsx = new XSSFWorkbook(in)) {
                    HeadingRowScene scene = new HeadingRowScene();
                    scene.setOwner(primaryStage);
                    scene.showScene();

                    ResourceExcel.markData(inXlsx);
                } catch (IOException ignored) {
                }
            }
            if (FileScanner.isScanned()){
                MainWindowEventHandler.settingLinesFromInformationFile(ResourceExcel.getMarkingColumns());
            }

        }
    }
    public static HashMap<String, String> getMarkingColumns(){
        return infInRow;
    }
}
