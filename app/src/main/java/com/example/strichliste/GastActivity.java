package com.example.strichliste;

import android.content.Intent;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class GastActivity extends AppCompatActivity {

    Button btnPickFromFiles;
    String TAG = "GastActivity";

    public static List<String> liste = new ArrayList<String>();
    private static String FILE_NAME;

    Button newBtn;
    Space newSpace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gast);

        btnPickFromFiles = findViewById(R.id.btnPickFromFiles);

        btnPickFromFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String externalStorage = Environment.getExternalStorageState();

                Toast.makeText(GastActivity.this, externalStorage, Toast.LENGTH_LONG);
                //showFileChooser();
                mGetContent.launch("*/*");
            }
        });
    }
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    File file;
                    String path = uri.getLastPathSegment(); // raw:/storage/emulated/0/Download/getraenkeUndGaeste.xlsx need file:/ ...

                    if (path.contains("raw")) {
                        path = path.replace("raw:/storage/emulated/0/", "");
                        file = new File(Environment.getExternalStorageDirectory(), path);
                    } else {
                        FILE_NAME = "/KopieCannstatterHütteDatenbank.xlsm"; // "/getraenkeUndGaeste.xlsx"
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + FILE_NAME).toURI());
                        Log.e(TAG, "real name " + (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + FILE_NAME).toURI()));
                    }
                    createGaesteListInBackground(file);
                }
            });

    public void createGaesteListInBackground(File file){
        ExecutorService executorService = Executors.newFixedThreadPool(1);//newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        liste = new ArrayList<String>();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                createGetraenkeList(file);

                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GastActivity.this, "Created Liste", Toast.LENGTH_LONG).show();
                        createButtons(liste);
                    }
                });
            }
        });
    }
    public void createGetraenkeList(File file) {
        Log.e(TAG, "I got the file");

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + fileInputStream);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            DataFormatter dataFormatter = new DataFormatter();
            // Get Datenbank sheet
            Sheet sh = workbook.getSheetAt(3);
            Log.e(TAG, "Sheet name: " + sh.getSheetName());

            Iterator<Row> iterator = sh.iterator();
            while (iterator.hasNext()) {
                String cellValue = ".";
                Row row = iterator.next();
                Iterator<Cell> cellIterator = row.iterator();
                while (cellIterator.hasNext()) {
                    // Call cellIterstor.next() twice because I need 2nd column
                    Cell cell = cellIterator.next();
                    cell = cellIterator.next();
                    cellValue = dataFormatter.formatCellValue(cell);
                    // Log.e(TAG, "Cell value: " + cellValue);
                    liste.add(cellValue);
                    break;
                }
                if (cellValue.startsWith("CONCATENATE") || cellValue.startsWith(".")){
                    Log.e(TAG, "No (more) values in Datenbank");
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

    private void createButtons(List<String> liste) {
        int i = 0;
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(-2, -2);
        TableLayout.LayoutParams layoutParams2 = new TableLayout.LayoutParams(0, 10);
        int column = 1;
        LinearLayout layout;
        // @ToDo: hardcoded, dass 0. Zeile nicht inkludiert ist
        for (i=1; i < liste.size()-1; i++) {
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
            String gastName = newBtn.getText().toString();
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GastActivity.this, DrinkActivity.class);
                    // @ToDo: Does this give me the value of newBtn at runtime ???
                    intent.putExtra("gastName", gastName);
                    startActivity(intent);
                }
            });
            newSpace = new Space(this);
            newSpace.setLayoutParams(layoutParams2);
            layout.addView(newSpace);
            layout.addView(newBtn);

            if (i % 10 == 0) {
                column ++;
            }
        }

    }
}