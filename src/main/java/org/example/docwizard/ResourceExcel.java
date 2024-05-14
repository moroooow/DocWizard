package org.example.docwizard;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.DateUtil;
public class ResourceExcel {

    private static HashMap<String, String> infInRow = new HashMap<String, String>();

    public static void markData(XSSFWorkbook xlsx, int numberOfTitles, int numberOfRow){

        String[] tableTitles = scanExcelRow(xlsx, numberOfTitles);
        String[] rowData = scanExcelRow(xlsx, numberOfRow);

        for (int i = 0; i < tableTitles.length; i++){
            if (tableTitles[i] != null){
                infInRow.put(("##" + tableTitles[i].replace(" ","_")), rowData[i]);
            }else{
                infInRow.put(tableTitles[i], rowData[i]);
            }
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
            if (cell == null || cell.getCellType() == CellType.BLANK){

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

    public static HashMap<String, String> getMarkingColumns(){
        return infInRow;
    }
}
