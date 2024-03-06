package com.example.strichliste;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ObtainFromExcel {
    // @ToDo: make pwd flexible
    private static final String NAME = "/home/lisa/Downloads/Huette/getraenkeUndGaeste.xlsx";
    public static List<String> liste = new ArrayList<String>();
    MyGlobalVariables myGlobalVariables;

    public static void main(String[] args) {
        try {
            FileInputStream file = new FileInputStream(new File(NAME));
            Workbook workbook = new XSSFWorkbook(file);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheets = workbook.sheetIterator();
            int sheet_number = 0;
            while(sheets.hasNext()) {
                /*if (sheet_number == 0) {
                    liste = MyGlobalVariables.getGetraenkeListe();
                    sheet_number += 1;
                } else {
                    liste = MyGlobalVariables.getGaesteListe();
                }*/
                Sheet sh = sheets.next();
                System.out.println("Sheet name is "+sh.getSheetName());
                System.out.println("_______________");

                Iterator<Row> iterator = sh.iterator();
                while(iterator.hasNext()){
                    Row row = iterator.next();
                    Iterator<Cell> cellIterator = row.iterator();
                    while(cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        System.out.println(cellValue+"\t");
                        liste.add(cellValue);
                        break;
                    }
                    System.out.println();
                }
                System.out.println("Liste:");
                System.out.println(liste);
                System.out.println();
            }

            workbook.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}