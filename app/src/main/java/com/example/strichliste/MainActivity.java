package com.example.strichliste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnHueWa;
    Button btnSettings;
    Button btnHint;
    TextView tvHint;
    EditText edText;
    ImageView ivLogoGrueneSchleife;

    // DataBase
    GastDatabase gastDB;

    //TextView tvHint;

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
        setContentView(R.layout.activity_main);

        ivLogoGrueneSchleife = findViewById(R.id.ivLogoGrueneSchleife);

        tvHint = findViewById(R.id.tvHint);

        edText = findViewById(R.id.edText);

        btnHueWa = findViewById(R.id.btnHueWa);
        btnSettings = findViewById(R.id.btnSettings);
        btnHint = findViewById(R.id.btnHint);

        btnHueWa.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnHint.setOnClickListener(this);

        if (firstAppStart()){
            createDatabase();
        }

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

        loadLevel();
    }

    @Override
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
            Intent intent = new Intent(this, GastActivity.class);
            startActivity(intent);
        }
        else if (view == btnSettings){
            //Toast.makeText(getApplicationContext(), "Lösung: " + companyName, Toast.LENGTH_LONG).show();
            String newName;
            String getraenk;
            newName = edText.getText().toString();

            getraenk = "Cola";
            Date zeitpunkt = new Date();

            Gast g1 = new Gast(newName, getraenk, 7, zeitpunkt);

            updateGastInBackground(g1);

        }
        else if (view == btnHint){
            tvHint.setText("Erster Buchstabe: " + companyName.substring(0, 1));
            tvHint.setVisibility(View.VISIBLE);
        }
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

    public void animateLevelCompleted(){

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

    public void createDatabase(){
        SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE " + databaseTableName + " (id INTEGER, company TEXT, imageName TEXT)");
        database.execSQL("INSERT INTO " + databaseTableName + " VALUES('1', 'Nasa', 'nasa')");
        database.execSQL("INSERT INTO " + databaseTableName + " VALUES('2', 'YouTube', 'youtube')");
        database.execSQL("INSERT INTO " + databaseTableName + " VALUES('3', 'Instagram', 'instagram')");
        database.execSQL("INSERT INTO " + databaseTableName + " VALUES('4', 'Audi', 'audi')");
        database.close();
    }
    public void updateGastInBackground(Gast gast){
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                gastDB.getGastDao().addGast(gast);

                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Updated Database", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }
}