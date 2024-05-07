package org.example.docwizard;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Iterator;

public class ResourceExcel {

    private static HashMap<String, String> infInRow = new HashMap<String, String>();

    public ResourceExcel(XSSFWorkbook xlsx, int numberOfRow){

        markData(xlsx, numberOfRow);
    }

    private static void markData(XSSFWorkbook xlsx, int numberOfRow){

        String[] tableTitles = scanExcelRow(xlsx, 0);
        String[] rowData = scanExcelRow(xlsx, numberOfRow);

        for (int i = 0; i < tableTitles.length; i++){

            infInRow.put(tableTitles[i], rowData[i]);
        }
    }

    private static String[] scanExcelRow(XSSFWorkbook xlsx, int numberOfRow){

        XSSFSheet sheet = xlsx.getSheetAt(0);
        XSSFRow row = sheet.getRow(numberOfRow);

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

    public static HashMap<String, String> getMarkingColumns(){
        return infInRow;
    }
}
