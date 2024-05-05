package org.example.docwizard;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Iterator;

public class ResourceExcel {
    private static HashMap<String, Integer> markingColumns = new HashMap<>();
    private static String[][] excelData;
    public ResourceExcel(XSSFWorkbook xlsx){
        scanResourceExcel(xlsx);
        markData();
    }
    private static void markData(){
        for (int i = 0; i < excelData.length; i++){
            markingColumns.put(excelData[0][i], i);
        }
    }
    private static void scanResourceExcel(XSSFWorkbook xlsx){
        XSSFSheet sheet = xlsx.getSheetAt(0); //only first sheet will be taken(IDK how to take all sheets)

        Iterator<Row> rowIter = sheet.rowIterator();
        int index = 0;

        while (rowIter.hasNext()) {
            XSSFRow row = (XSSFRow) rowIter.next();
            excelData[index] = scanExcelRow(row);
            index++;
        }
    }
    private static String[] scanExcelRow(XSSFRow row){

        String[] cells = new String[row.getLastCellNum() - row.getFirstCellNum()];
        int index = 0;
        Iterator<Cell> cellIter = row.cellIterator();
        while (cellIter.hasNext()) {
            XSSFCell cell = (XSSFCell) cellIter.next();
            cells[index] = cell.getStringCellValue();
            index++;
        }
        return cells;
    }

    public static String[][] getExcelData(){
        return excelData;
    }

    public static HashMap<String, Integer> getMarkingColumns(){
        return markingColumns;
    }
}
