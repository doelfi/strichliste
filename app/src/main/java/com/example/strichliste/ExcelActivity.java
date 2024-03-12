package com.example.strichliste;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExcelActivity extends AppCompatActivity {

    Button newBtn;
    Space newSpace;

    //private static final String NAME = "/storage/emulated/0/Download/getraenkeUndGaeste.xlsx";
    private static final String NAME = "/getraenkeUndGaeste.xlsx";
    public static List<String> liste = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel);

        // MyGlobalVariables myGlobalVariables;
        createGetraenkeListInBackground(ExcelActivity.this, NAME);
    }

    public void createGetraenkeListInBackground(Context context, String NAME){
        ExecutorService executorService = Executors.newFixedThreadPool(1);//newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                createGetraenkeList(context, NAME);
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ExcelActivity.this, "Created Liste", Toast.LENGTH_LONG).show();
                        createButtons(liste);
                    }
                });
            }
        });
    }

    public void createGetraenkeList(Context context, String NAME) {
        File file = new File(context.getExternalFilesDir(null), NAME);
        // File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/getraenkeUndGaeste.xlsx").toURI());
        //File file = new File(docsFolder.);
        //FileInputStream fileInputStream = null;
        String TAG = "ExcelActivity";
        Log.e(TAG, "I got the file");
        //Log.e(TAG, "External Storage state: " + Environment.getExternalStorageState());

        try {
            //fileInputStream = new FileInputStream(file);
            // Uri uri = Uri.parse(NAME);
            // FileInputStream fileInputStream = new FileInputStream(new File(uri.getPath()));
            // FileInputStream fileInputStream = new FileInputStream(new File(NAME));
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + fileInputStream);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheets = workbook.sheetIterator();
            //int sheet_number = 0;
            while (sheets.hasNext()) {
                        /*if (sheet_number == 0) {
                            liste = MyGlobalVariables.getGetraenkeListe();
                            sheet_number += 1;
                        } else {
                            liste = MyGlobalVariables.getGaesteListe();
                        }*/
                Sheet sh = sheets.next();
                //System.out.println("Sheet name is "+sh.getSheetName());
                //System.out.println("_______________");

                Iterator<Row> iterator = sh.iterator();
                while (iterator.hasNext()) {
                    Row row = iterator.next();
                    Iterator<Cell> cellIterator = row.iterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        //System.out.println(cellValue+"\t");
                        liste.add(cellValue);
                        break;
                    }
                    //System.out.println();
                }
                //System.out.println("Liste:");
                //System.out.println(liste);
                //System.out.println();
                Log.e(TAG, "fertige Liste: " + liste);
            }

            workbook.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createButtons(List<String> liste) {
        int i = 0;
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(-2, -2);
        TableLayout.LayoutParams layoutParams2 = new TableLayout.LayoutParams(0, 10);
        int column = 1;
        LinearLayout layout;
        // @ToDo: hardcoded, dass Einnahmen Verkaufspreise nicht mit drin sind und nur bis letztes Getränk
        //for (i=2; i < liste.size(); i++) {
        for (i=2; i < 31; i++) {
            if (column == 1) {
                layout = findViewById(R.id.column1);
            } else if (column == 2) {
                layout = findViewById(R.id.column2);
            } else {
                layout = findViewById(R.id.column3);
            }
            newBtn = new Button(this);
            newBtn.setText(liste.get(i));
            newBtn.setLayoutParams(layoutParams);
            newBtn.setBackgroundColor(getColor(R.color.gruene_schleife));
            newBtn.setTextColor(getColor(R.color.white));
            newBtn.setTextSize(20);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ExcelActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            newSpace = new Space(this);
            newSpace.setLayoutParams(layoutParams2);
            layout.addView(newSpace);
            layout.addView(newBtn);

            if ((i - 1) % 10 == 0) {
                column ++;
            }
        }

    }
}
