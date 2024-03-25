package com.example.strichliste;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    Button btnHueWa;
    Button btnPickFromFiles;

    String TAG = "SettingsActivity";

    public static List<String> liste = new ArrayList<String>();
    private static String FILE_NAME;

    Button btnExportData;
    GastDatabase gastDB;
    TextView tvHint;
    EditText edText;
    ImageView ivLogoGrueneSchleife;

    int currentLevel;
    String companyName;
    String imageName;

    final int maxLevel = 4;
    final String prefNameFirstStart = "firstAppStart";
    final String databaseName = "level.db";
    final String databaseTableName = "level";
    final String prefLevel = "currentLevel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ivLogoGrueneSchleife = findViewById(R.id.ivLogoGrueneSchleife);

        tvHint = findViewById(R.id.tvHint);

        edText = findViewById(R.id.edText);

        btnHueWa = findViewById(R.id.btnHueWa);
        btnPickFromFiles = findViewById(R.id.btnPickFromFiles);
        btnExportData = findViewById(R.id.btnHint);

        btnHueWa.setOnClickListener(this::onClick);
        btnPickFromFiles.setOnClickListener(this::onClick);
        btnExportData.setOnClickListener(this::onClick);

        receiveDatabase();
        int imageID = getResources().getIdentifier("logo_gruene_schleife", "drawable", getPackageName());
        ivLogoGrueneSchleife.setImageResource(imageID);
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

    public void onClick(View view) {
        if (view == btnHueWa) {
            /*
            if (edText.getText().toString().equalsIgnoreCase(companyName)) {
                currentLevel++;
                safeLevel();
                loadLevel();
            }
            else {
                    Toast.makeText(getApplicationContext(), "Leider falsch.", Toast.LENGTH_LONG).show();
                }*/
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if (view == btnPickFromFiles){
            //Toast.makeText(getApplicationContext(), "Lösung: " + companyName, Toast.LENGTH_LONG).show();
            //String newName;
            //newName = edText.getText().toString();

            mGetContent.launch("*/*");
        }
        else if (view == btnExportData){
            //tvHint.setText("Erster Buchstabe: " + companyName.substring(0, 1));
            //tvHint.setVisibility(View.VISIBLE);
            // getAllGuestInBackground(this);
            Intent intent = new Intent(this, ExportDataActivity.class);
            startActivity(intent);
        }
    }
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    File file;
                    String path = uri.getPath();
                    Log.e(TAG, path);
                    // @ToDo: hardcoded!!!
                    path = path.substring(path.lastIndexOf("/"));
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + path).toURI());
                    createGaesteListInBackground(file);
                }
            });
    public void createGaesteListInBackground(File file){
        ExecutorService executorService = Executors.newSingleThreadExecutor(); //
        Handler handler = new Handler(Looper.getMainLooper());
        liste = new ArrayList<String>();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                createGaesteList(file);

                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "Created Liste", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    public void createGaesteList(File file) {
        Log.e(TAG, "I got the file");

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + fileInputStream);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int huettenabrechnungUebersichtIndex = workbook.getSheetIndex("Hüttenabrechnung Übersicht");
            Sheet sh = workbook.getSheetAt(huettenabrechnungUebersichtIndex);
            Log.e(TAG, "Sheet name: " + sh.getSheetName());

            int firstRowNumber =  sh.getFirstRowNum();
            int lastRowNumber = sh.getLastRowNum();

            for (int rowNum = firstRowNumber+2; rowNum <= lastRowNumber; rowNum++) {
                Row row = sh.getRow(rowNum);
                if (row == null) {
                    // This whole row is empty
                    // Handle it as needed
                    continue;
                }
                else {
                    Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String gast = dataFormatter.formatCellValue(cell, evaluator);
                    if (gast.startsWith("Gast :")) {
                        gast = gast.replace("Gast : ", "");
                        if (gast.length() >= 2){
                            liste.add(gast);
                            Log.e(TAG, "Gast Name: " + gast);
                        }
                    }
                }
            }
            fileInputStream.close();
            Log.e(TAG, "fertige Liste: " + liste);
            ((MyGlobalVariables) this.getApplication()).setGaesteListe((ArrayList<String>) liste);
            Log.e(TAG, "globale Liste: " + ((MyGlobalVariables) this.getApplication()).getGaesteListe());
            workbook.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getAllGuestInBackground(Context context){
        ExecutorService executorService = Executors.newFixedThreadPool(1);//newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                GastDAO gastDAO = gastDB.getGastDao();
                List<Gast>  gaeste = gastDAO.getAllGast();
                Log.e(TAG, gaeste.toString());
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "Check your Log", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void loadLevel(){
        SharedPreferences preferenceLoad = getSharedPreferences(prefLevel, MODE_PRIVATE);
        currentLevel = preferenceLoad.getInt(prefLevel, 1);
        if (currentLevel <= maxLevel) {
            SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
            Cursor cursor = database.rawQuery("SELECT * FROM " + databaseTableName + " WHERE id = '" + currentLevel + "'", null);
            cursor.moveToFirst();
            if (cursor.getCount() == 1) {
                companyName = cursor.getString(1);
                imageName = cursor.getString(2);

                cursor.close();
                database.close();
            }
        }
        int imageID = getResources().getIdentifier("logo_gruene_schleife", "drawable", getPackageName());
        ivLogoGrueneSchleife.setImageResource(imageID);
    }

    public void safeLevel(){
        SharedPreferences preferencesLevel = getSharedPreferences(prefLevel, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencesLevel.edit();

        editor.putInt(prefLevel, currentLevel);
        editor.commit();
    }

    public boolean firstAppStart(){
        SharedPreferences preferences = getSharedPreferences(prefNameFirstStart, MODE_PRIVATE);
        if (preferences.getBoolean(prefNameFirstStart, true)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(prefNameFirstStart, false);
            editor.commit();
            return true;
        } else{
            return false;
        }
    }


}