package com.example.strichliste;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
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
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportDataActivity extends AppCompatActivity {

    Button btnExportData;
    Button btnDeleteGuests;
    ImageView ivLogoGrueneSchleife;
    GastDatabase bestellungDB;
    BesucherInDatabase besucherInDB;
    GetraenkDatabase getraenkDB;
    String TAG = "ExportDataActivity";
    Long startTag;
    List<String> gaesteListe;
    ImageButton btnIconHome;
    ImageButton btnIconSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        ivLogoGrueneSchleife = findViewById(R.id.ivLogoGrueneSchleife);
        int imageID = getResources().getIdentifier("logo_gruene_schleife", "drawable", getPackageName());
        ivLogoGrueneSchleife.setImageResource(imageID);

        btnIconHome = findViewById(R.id.btnIconHome);
        btnIconHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExportDataActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnIconSettings = findViewById(R.id.btnIconSettings);
        btnIconSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExportDataActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


        btnExportData = findViewById(R.id.btnExportData);
        btnDeleteGuests = findViewById(R.id.btnDeleteGuests);

        btnExportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get file from global variable
                final MyGlobalVariables globalVariable = (MyGlobalVariables) getApplicationContext();
                Path path = globalVariable.getFileName();

                File file = path.toFile();

                if (!checkStoragePermissions()) {
                    requestForStoragePermissions();
                } else {
                    extractGastDataInBackground(file);
                }
            }
        });

        btnDeleteGuests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGuestDataInBackground();
            }
        });

        receiveDatabase();
    }

    public void receiveDatabase() {
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

        bestellungDB = Room.databaseBuilder(getApplicationContext(), GastDatabase.class, "BestellungDB").addCallback(mainCallBack).build();
        besucherInDB = Room.databaseBuilder(getApplicationContext(), BesucherInDatabase.class, "BesucherInDB").addCallback(mainCallBack).build();
        getraenkDB = Room.databaseBuilder(getApplicationContext(), GetraenkDatabase.class, "GetraenkDB").addCallback(mainCallBack).build();
    }

    public void deleteGuestDataInBackground(){
        // @ToDo: confirmation window
        // Orders deleted after 30 days
        Toast.makeText(ExportDataActivity.this, "Löschen", Toast.LENGTH_LONG).show();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // List<BesucherIn> allGuests = besucherInDB.getBesucherInDAO().getAll();
                    // besucherInDB.getBesucherInDAO().deleteAllBesucherIn(allGuests);
                    deleteOldDataBase("BesucherInDB");
                    deleteOldDataBase("GetraenkDB");
                } catch (IndexOutOfBoundsException e) {

                }
                deleteOldData();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ExportDataActivity.this, "Gästedaten gelöscht", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void extractGastDataInBackground(File file){
        Toast.makeText(ExportDataActivity.this, "Exportieren", Toast.LENGTH_LONG).show();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                gaesteListe = besucherInDB.getBesucherInDAO().getAllNames();
                writeToGastGetraenkeList(file);
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Done with exporting Data");
                        Toast.makeText(ExportDataActivity.this, "Daten exportiert", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    public void writeToGastGetraenkeList(File file) {
        Log.e(TAG, "I got the file " + file.getPath());

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel " + fileInputStream);
            XSSFWorkbook workbook=(XSSFWorkbook) WorkbookFactory.create(file,"oli");
            //Workbook workbook = new XSSFWorkbook(fileInputStream);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter dataFormatter = new DataFormatter();
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String gastName;

            for (int nameIndex = 0; nameIndex < gaesteListe.size(); nameIndex++) {
                gastName = gaesteListe.get(nameIndex);

                int abrechnungIndex = nameIndex+1;
                int abrechnungGast_ = workbook.getSheetIndex("Abrechnung Gast" + abrechnungIndex);
                Sheet sh = workbook.getSheetAt(abrechnungGast_);
                int firstRowNumber = sh.getFirstRowNum();
                int lastRowNumber = sh.getLastRowNum();
                int lastColumn = sh.getRow(0).getLastCellNum();

                for (int rowNum = firstRowNumber + 1; rowNum <= lastRowNumber; rowNum++) {
                    Row row = sh.getRow(rowNum);
                    if (row == null) {
                        // This whole row is empty
                        // Handle it as needed
                        continue;
                    } else {
                        Cell cell = row.getCell(9);
                        String getraenkName;
                        try {
                            getraenkName = dataFormatter.formatCellValue(cell, evaluator);
                            if (rowNum >= 32 && getraenkName == "") {
                                break;
                            }
                        } catch (RuntimeException e) {
                            getraenkName = dataFormatter.formatCellValue(cell);
                        }
                        for (int cn = 9; cn < lastColumn; cn++) {
                            Cell c = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            int anzahl;
                            try {
                                long day = dateToMilliseconds(dataFormatter.formatCellValue(sh.getRow(0).getCell(cn), evaluator));
                                if (cn == 9) {
                                    startTag = day;
                                }
                                try {
                                    anzahl = bestellungDB.getGastDao().getSummeGastGetraenkZeitpunkt(gastName, getraenkName, day, day + 86400000);
                                    if (anzahl != 0) {
                                        c.setCellValue(anzahl);
                                        //Log.e(TAG, "Day in ms: " + day + "\n Amount of Drink " + getraenkName + ": " + anzahl);
                                    }
                                } catch (NullPointerException e) {
                                    continue;
                                }
                            } catch (IndexOutOfBoundsException e) {
                                continue;
                            }
                        }
                    }
                }
                Log.e(TAG, "Daten exportiert für " + gastName);
            }
            fileInputStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(file); //Getraenke.xlsm");
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

    public void deleteOldDataBase(String nameDB){
        boolean deleted = this.deleteDatabase(nameDB);
        Log.e(TAG, String.valueOf(deleted));
    }

    public void deleteOldData() {
        Long vorDreissigTagen = dateToMilliseconds("23/09/2023");
        try {
            vorDreissigTagen = startTag - 86400000 * 30;
        }catch(NullPointerException e) {

        }
        List<Gast> alteBestellungen = bestellungDB.getGastDao().getAllOldBestellungen(vorDreissigTagen);
        bestellungDB.getGastDao().deleteAllGast(alteBestellungen);
        Log.e(TAG, "Alte Bestellungen gelöscht");
    }


    public boolean checkStoragePermissions(){
        // https://medium.com/@kezzieleo/manage-external-storage-permission-android-studio-java-9c3554cf79a7
        //Android is 11 (R) or above
        return Environment.isExternalStorageManager();
    }


    private static final int STORAGE_PERMISSION_CODE = 23;

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        try {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            storageActivityResultLauncher.launch(intent);
        }catch (Exception e){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            storageActivityResultLauncher.launch(intent);
        }

    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            //Android is 11 (R) or above
                            if(Environment.isExternalStorageManager()){
                                //Manage External Storage Permissions Granted
                                Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                            }else{
                                Toast.makeText(ExportDataActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

}