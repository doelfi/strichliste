package com.example.strichliste;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetraenkeActivity extends AppCompatActivity {

    Button newBtn;
    Space newSpace;
    String gastName;
    GastDatabase gastDB;
    String TAG = "ExcelActivity";
    private static final String NAME = "/Belegung Cannstatter Hütte Edition 2.4.xlsm"; // "/getraenkeUndGaeste.xlsx";
    public static List<String> liste = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel);

        createGetraenkeListInBackground(GetraenkeActivity.this, NAME);

        Intent intent = getIntent();
        gastName = intent.getStringExtra("gastName");

        receiveDatabase();
        }

    public void receiveDatabase(){
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
                        Toast.makeText(GetraenkeActivity.this, "Created Liste", Toast.LENGTH_LONG).show();
                        createButtons(liste);
                    }
                });
            }
        });
    }

    public void createGetraenkeList(Context context, String NAME) {
        //Log.e(TAG, "External Storage state: " + Environment.getExternalStorageState());
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + NAME).toURI());
        Log.e(TAG, "I got the file " + file.getPath());
        //Log.e(TAG, "External Storage state: " + Environment.getExternalStorageState());

        try {
            liste = new ArrayList<String>();
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel " + fileInputStream);
            XSSFWorkbook workbook=(XSSFWorkbook) WorkbookFactory.create(file,"oli");
            //Workbook workbook = new XSSFWorkbook(fileInputStream);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter dataFormatter = new DataFormatter();
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            int abrechnungGast1 = workbook.getSheetIndex("Abrechnung Gast1");
            Sheet sh = workbook.getSheetAt(abrechnungGast1);
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
                    Cell cell = row.getCell(9);
                    String getraenkName;
                    try {
                        getraenkName = dataFormatter.formatCellValue(cell, evaluator);
                        if (rowNum >= 32 && getraenkName == "") {
                            break;
                        }
                        if (getraenkName.length() >= 2 && !getraenkName.startsWith("Verkaufspreise") && !getraenkName.startsWith("Knabbereien") && !getraenkName.startsWith("Vesper")) {
                            Log.e(TAG, "Getränke Name: " + getraenkName);
                            liste.add(getraenkName);
                        }
                    } catch (RuntimeException e) {
                        getraenkName = dataFormatter.formatCellValue(cell);
                    }
                }
            }
            fileInputStream.close();

            Log.e(TAG, "fertige Liste: " + liste);
            workbook.close();
            ((MyGlobalVariables) this.getApplication()).setGetraenkeListe((ArrayList<String>) liste);
            Log.e(TAG, "getraenke Liste " + ((MyGlobalVariables) this.getApplication()).getGetraenkeListe());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createButtons(List<String> liste) {

        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(-1, 200);
        TableLayout.LayoutParams layoutParams2 = new TableLayout.LayoutParams(0, 20);
        int column = 1;
        LinearLayout layout;
        // @ToDo: hardcoded, dass Einnahmen Verkaufspreise nicht mit drin sind
        for (int i=0; i < liste.size(); i++) {
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
            newBtn.setBackground(AppCompatResources.getDrawable(this, R.drawable.custom_button));
            newBtn.setTextColor(getColor(R.color.white));
            newBtn.setTextSize(20);
            String getraenkeName = newBtn.getText().toString();
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmBestellung(getraenkeName);
                }
            });
            newSpace = new Space(this);
            newSpace.setLayoutParams(layoutParams2);
            layout.addView(newSpace);
            layout.addView(newBtn);

            if (i % 9 == 0 && i != 0) {
                column ++;
            }
        }
    }

    private void confirmBestellung(String getraenkeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Möchtest du, " + gastName + ", wirklich Folgendes bestellen?\n" +
                        getraenkeName)
                .setTitle("Bestellbestätigung")
                .setCancelable(false)
                .setNegativeButton("Abbrechen", ((dialog, which) -> dialog.dismiss()))
                .setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int anzahl = 1;
                        Date zeitpunkt = new Date();

                        Gast bestellung = new Gast(gastName, getraenkeName, anzahl, zeitpunkt);
                        addGastInBackground(bestellung);

                        Intent intent = new Intent(GetraenkeActivity.this, MainActivity.class);
                        startActivity(intent);

                        dialog.dismiss();
                    }
                });
        builder.show();
    }
    public void addGastInBackground(Gast gast){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        Log.e(TAG, "ok ");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                gastDB.getGastDao().addGast(gast);
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Bestellung hinzugefügt " + gast.name);
                    }
                });
            }
        });
    }
}
