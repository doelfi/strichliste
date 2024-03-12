package com.example.strichliste;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

public class GuestActivity extends AppCompatActivity {

    private static final String NAME = "/getraenkeUndGaeste.xlsx";
    public static List<String> liste = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        createGaesteListInBackground(this, NAME);
    }

    public void createGaesteListInBackground(Context context, String NAME){
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
                        Toast.makeText(GuestActivity.this, "Created Liste", Toast.LENGTH_LONG).show();
                        //createButtons(liste);
                    }
                });
            }
        });
    }
    public void createGetraenkeList(Context context, String NAME) {
        // File file = new File(context.getExternalFilesDir(null), NAME);
        // File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/getraenkeUndGaeste.xlsx").toURI());
        StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        StorageVolume storageVolume = storageManager.getStorageVolumes().get(0);
        File file = new File(storageVolume.getDirectory().getPath() + "/Download/" + "/getraenkeUndGaeste.xlsx");
        String TAG = "ExcelActivity";
        Log.e(TAG, "I got the file");

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + fileInputStream);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheets = workbook.sheetIterator();
            while (sheets.hasNext()) {
                Sheet sh = sheets.next();

                Iterator<Row> iterator = sh.iterator();
                while (iterator.hasNext()) {
                    Row row = iterator.next();
                    Iterator<Cell> cellIterator = row.iterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        liste.add(cellValue);
                        break;
                    }
                }
                Log.e(TAG, "fertige Liste: " + liste);
            }

            workbook.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}