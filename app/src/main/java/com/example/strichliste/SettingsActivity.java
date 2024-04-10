package com.example.strichliste;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    Button btnHueWa;
    Button btnPickFromFiles;
    String TAG = "SettingsActivity";
    Button btnExportData;
    BesucherInDatabase besucherInDB;
    GetraenkDatabase getraenkDB;
    TextView tvHint;
    EditText edText;
    ImageView ivLogoGrueneSchleife;
    final String prefNameFirstStart = "firstAppStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ivLogoGrueneSchleife = findViewById(R.id.ivLogoGrueneSchleife);

        tvHint = findViewById(R.id.tvHint);

        //edText = findViewById(R.id.edText);

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
        besucherInDB = Room.databaseBuilder(getApplicationContext(), BesucherInDatabase.class, "BesucherInDB").addCallback(mainCallBack).build();
        getraenkDB = Room.databaseBuilder(getApplicationContext(), GetraenkDatabase.class, "GetraenkDB").addCallback(mainCallBack).build();
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

            if (!checkStoragePermissions()) {
                requestForStoragePermissions();
            } else {
                mGetContent.launch("*/*");
            }
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
                    Toast.makeText(SettingsActivity.this, "Hochladen", Toast.LENGTH_LONG).show();

                    try {
                        File file = getFileFromUri(SettingsActivity.this, uri);

                        Path path = file.toPath();

                        // save file to global variable
                        final MyGlobalVariables globalVariable = (MyGlobalVariables) getApplicationContext();
                        globalVariable.setFileName(path);

                        createGaesteListInBackground(file);
                    } catch (Exception ee) {
                        Log.e(TAG, "I'm in catch");
                        String path = uri.getPath();
                        Log.e(TAG, path);
                        path = path.substring(path.lastIndexOf("/"));
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + path).toURI());
                        createGaesteListInBackground(file);
                        /*
                        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), path);
                        createGaesteListInBackground(file);*/
                    }
                    //}


                }
            });
    public void createGaesteListInBackground(File file){
        ExecutorService executorService = Executors.newSingleThreadExecutor(); //
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                createGaesteList(file);

                createGetraenkeList(file);

                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingsActivity.this, "Datenbanken erstellt", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    public void createGaesteList(File file) {
        Log.e(TAG, "I got the file " + file.getPath());

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Log.e(TAG, "Reading from Excel" + fileInputStream);
            XSSFWorkbook workbook=(XSSFWorkbook) WorkbookFactory.create(file,"oli");
            //Workbook workbook = new XSSFWorkbook(fileInputStream);
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int huettenabrechnungUebersichtIndex = workbook.getSheetIndex("Hüttenabrechnung Übersicht");
            Sheet sh = workbook.getSheetAt(huettenabrechnungUebersichtIndex);

            int firstRowNumber =  sh.getFirstRowNum();
            int lastRowNumber = sh.getLastRowNum();

            for (int rowNum = firstRowNumber+2; rowNum <= lastRowNumber; rowNum++) {
                Row row = sh.getRow(rowNum);
                if (row == null) {
                    // This whole row is empty
                    continue;
                }
                else {
                    Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String gast = dataFormatter.formatCellValue(cell, evaluator);
                    if (gast.startsWith("Gast :")) {
                        gast = gast.replace("Gast : ", "");
                        if (gast.length() >= 2){
                            BesucherIn besucherIn = new BesucherIn(rowNum-1, gast);
                            besucherInDB.getBesucherInDAO().addBesucherIn(besucherIn);
                        }
                    }
                }
            }
            fileInputStream.close();
            Log.e(TAG, "created Gäste Database");
            workbook.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createGetraenkeList(File file) {
        //Log.e(TAG, "External Storage state: " + Environment.getExternalStorageState());
        // File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + NAME).toURI());
        Log.e(TAG, "I got the file " + file.getPath());

        try {
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

            for (int rowNum = firstRowNumber+1; rowNum <= lastRowNumber; rowNum++) {
                Row row = sh.getRow(rowNum);
                if (row == null) {
                    // This whole row is empty
                    continue;
                }
                else {
                    Cell cell = row.getCell(9); // Spalte J enthält die Getränke Namen
                    try {
                        String getraenkName = dataFormatter.formatCellValue(cell, evaluator);
                        if (rowNum >= 32 && getraenkName == "") {
                            break;
                        }
                        if (getraenkName.length() >= 2 && !getraenkName.startsWith("Verkaufspreise") && !getraenkName.startsWith("Knabbereien") && !getraenkName.startsWith("Vesper")) {
                            Getraenk getraenk = new Getraenk(getraenkName, rowNum);
                            getraenkDB.getGetraenkDAO().addGetraenk(getraenk);
                        }
                    } catch (RuntimeException e) {
                        continue;
                    }
                }
            }
            fileInputStream.close();

            Log.e(TAG, "Getränke added to Database");
            workbook.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Get a file from a Uri.
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    public static File getFileFromUri(final Context context, final Uri uri) throws Exception {
        // https://gist.github.com/walkingError/915c73ae48882072dc0e8467a813046f

        String path = null;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) { // TODO: 2015. 11. 17. KITKAT


                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];


                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes

                } else if (isDownloadsDocument(uri)) { // DownloadsProvider

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    path = getDataColumn(context, contentUri, null, null);

                } else if (isMediaDocument(uri)) { // MediaProvider


                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    path = getDataColumn(context, contentUri, selection, selectionArgs);

                } else if (isGoogleDrive(uri)) { // Google Drive
                    String TAG = "isGoogleDrive";
                    path = TAG;
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(";");
                    final String acc = split[0];
                    final String doc = split[1];

                    /*
                     * @details google drive document data. - acc , docId.
                     * */

                    return saveFileIntoExternalStorageByUri(context, uri);


                } // MediaStore (and general)
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                path = getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                path = uri.getPath();
            }

            return new File(path);
        } else {

            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            return new File(cursor.getString(cursor.getColumnIndex("_data")));
        }

    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is GoogleDrive.
     */

    public static boolean isGoogleDrive(Uri uri) {
        return uri.getAuthority().equalsIgnoreCase("com.google.android.apps.docs.storage");
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static File makeEmptyFileIntoExternalStorageWithTitle(String title) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        return new File(root, title);
    }


    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    public static void saveBitmapFileIntoExternalStorageWithTitle(Bitmap bitmap, String title) throws Exception {

        FileOutputStream fileOutputStream = new FileOutputStream(makeEmptyFileIntoExternalStorageWithTitle(title + ".png"));
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.close();
    }


    public static File saveFileIntoExternalStorageByUri(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        int originalSize = inputStream.available();

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String fileName = getFileName(context, uri);
        File file = makeEmptyFileIntoExternalStorageWithTitle(fileName);
        bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(new FileOutputStream(
                file, false));

        byte[] buf = new byte[originalSize];
        bis.read(buf);
        do {
            bos.write(buf);
        } while (bis.read(buf) != -1);

        bos.flush();
        bos.close();
        bis.close();

        return file;

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
                                Toast.makeText(SettingsActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

}