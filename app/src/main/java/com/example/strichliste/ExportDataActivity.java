package com.example.strichliste;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportDataActivity extends AppCompatActivity {
    GastDatabase gastDB;
    String TAG = "ExportDataActivity";
    ArrayList<String> gaesteListe;
    ArrayList<String> liste;
    private static final String NAME = "/KopieGetraenkeUndGaeste.xlsx";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        RoomDatabase.Callback mainCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                super.onDestructiveMigration(db);
            }
        };

        gastDB = Room.databaseBuilder(getApplicationContext(), GastDatabase.class, "AstDB").addCallback(mainCallBack).build();
        gaesteListe = ((MyGlobalVariables) ExportDataActivity.this.getApplication()).getGaesteListe();

        extractGastDataInBackground("Lisa");
    }
    public void extractGastDataInBackground(String name){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        Log.e(TAG, "ok ");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                String gastName = "Woody Allen";
                writeToGastGetraenkeList(ExportDataActivity.this, NAME, gastName);
                // gastDB.getGastDao().getSummeGastGetraenkZeitpunkt(name, "bla", new Date(), new Date());
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Done");
                    }
                });
            }
        });
    }


    public void writeToGastGetraenkeList(Context context, String NAME, String gastName) {
        //File file = new File(context.getExternalFilesDir(null), NAME);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + NAME).toURI());
        Log.e(TAG, "I got the file " + file.getPath());
        //Log.e(TAG, "External Storage state: " + Environment.getExternalStorageState());

        try {
            liste = new ArrayList<String>();
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel " + fileInputStream);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            DataFormatter dataFormatter = new DataFormatter();
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            Sheet sh = workbook.getSheetAt(0);
            int firstRowNumber =  sh.getFirstRowNum();
            int lastRowNumber = sh.getLastRowNum();
            int lastColumn = sh.getRow(0).getLastCellNum();
            Log.e(TAG, "Last Column: " + lastColumn);

            for (int rowNum = firstRowNumber+1; rowNum <= lastRowNumber; rowNum++) {
                Row row = sh.getRow(rowNum);
                if (row == null) {
                    // This whole row is empty
                    // Handle it as needed
                    continue;
                }
                else {
                    Cell cell = row.getCell(0);
                    String getraenkName = dataFormatter.formatCellValue(cell);
                    //Log.e(TAG, "Getränke Name: " + getraenkName);
                    for (int cn = 0; cn < lastColumn; cn++) {
                        Cell c = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        if (true) {
                            // The spreadsheet is empty in this cell
                            int anzahl;
                            try {
                                long day = dateToMilliseconds(dataFormatter.formatCellValue(sh.getRow(0).getCell(cn)));
                                try {
                                    anzahl = gastDB.getGastDao().getSummeGastGetraenkZeitpunkt(gastName, getraenkName, day, day+86400000);
                                } catch (NullPointerException e) {
                                    anzahl = 0;
                                }
                                Log.e(TAG, "Day in ms: " + day + "\n Amount of Drink " +
                                        getraenkName + ": " + anzahl);
                                c.setCellValue(anzahl);

                            } catch (IndexOutOfBoundsException e) {
                                continue;
                            }
                        } else {
                        // Do something useful with the cell's contents
                        }
                    }
                }
            }
            fileInputStream.close();
            Log.e(TAG, "fertige Liste: " + liste);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long dateToMilliseconds(String startDate) {
        // date = 19/10/23
        long millis = 0;
        String[] dateList = startDate.split("/");
        startDate = "20" + dateList[2] + "/" + dateList[1] + "/" + dateList[0];
        String myDate = startDate + " 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            Date date = sdf.parse(myDate);
            millis = date.getTime();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return millis;
    }
}