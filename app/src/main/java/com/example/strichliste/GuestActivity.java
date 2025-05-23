package com.example.strichliste;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

    Button newBtn;
    Space newSpace;

    public int REQUEST_PERMISSION_CODE = 1001;

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        requestRuntimePermission();
        //createGaesteListInBackground(this, NAME);
    }
    private void requestRuntimePermission(){
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            createGaesteListInBackground(this, NAME);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        GuestActivity.this, READ_EXTERNAL_STORAGE)) {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no thanks" button that lets the user continue
                    // using your app without granting the permission.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires READ_EXTERNAL_STORAGE permission for particular feature to work as expected")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("Ok", ((dialog, which) -> {
                            ActivityCompat.requestPermissions(GuestActivity.this, new String[]{READ_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSION_CODE);
                            dialog.dismiss();
                        }))
                    .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
            builder.show();

        } else {
            // You can directly ask for the permission.
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE},REQUEST_PERMISSION_CODE);}
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createGaesteListInBackground(this, NAME);
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This feature is unavailable because this feature requires permission that you have denied." +
                        "Pease allow read from downloads permission from settings to proceed further.")
                        .setTitle("Permission Required")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);

                                dialog.dismiss();
                            }
                        });
                builder.show();
            } else {
                requestRuntimePermission();
            }
        }
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
                        createButtons(liste);
                    }
                });
            }
        });
    }
    public void createGetraenkeList(Context context, String NAME) {
        // File file = new File(context.getExternalFilesDir(null), NAME);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/getraenkeUndGaeste.xlsx").toURI());
        //StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        //StorageVolume storageVolume = storageManager.getStorageVolumes().get(0);
        //File file = new File(storageVolume.getDirectory().getPath() + "/Download/" + "/getraenkeUndGaeste.xlsx");
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
                    Intent intent = new Intent(GuestActivity.this, MainActivity.class);
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