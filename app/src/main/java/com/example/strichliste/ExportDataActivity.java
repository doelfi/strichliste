package com.example.strichliste;

import android.content.Context;
import android.os.Bundle;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportDataActivity extends AppCompatActivity {
    GastDatabase gastDB;
    String TAG = "ExportDataActivity";
    ArrayList<String> gaesteListe;
    ArrayList<String> liste;
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
    }
    public void extractGastDataInBackground(String name){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        Log.e(TAG, "ok ");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                gastDB.getGastDao().getSummeGastGetraenkZeitpunkt(name, );
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Daten für Gast " + name + "sortiert");
                    }
                });
            }
        });
    }

    public void createGetraenkeList(Context context, String NAME) {
        File file = new File(context.getExternalFilesDir(null), NAME);
        Log.e(TAG, "I got the file");
        //Log.e(TAG, "External Storage state: " + Environment.getExternalStorageState());

        try {
            liste = new ArrayList<String>();
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + fileInputStream);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            DataFormatter dataFormatter = new DataFormatter();

            Sheet sh = workbook.getSheetAt(0);

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
            }
            Log.e(TAG, "fertige Liste: " + liste);
            workbook.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}