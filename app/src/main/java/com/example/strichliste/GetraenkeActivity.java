package com.example.strichliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetraenkeActivity extends AppCompatActivity {

    Button newBtn;
    Space newSpace;
    String gastName;
    GastDatabase gastDB;
    GetraenkDatabase getraenkDB;
    String TAG = "ExcelActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excel);

        receiveDatabase();

        createGetraenkeButtonsInBackground();

        Intent intent = getIntent();
        gastName = intent.getStringExtra("gastName");
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
        getraenkDB = Room.databaseBuilder(getApplicationContext(), GetraenkDatabase.class, "GetraenkDB").addCallback(mainCallBack).build();
    }

    public void createGetraenkeButtonsInBackground(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                List<String> liste = getraenkDB.getGetraenkDAO().getAllGetraenkeNames();
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(GetraenkeActivity.this, "Created Liste", Toast.LENGTH_LONG).show();
                        createButtons(liste);
                    }
                });
            }
        });
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
        final EditText etAnzahlGetraenk = new EditText(GetraenkeActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        etAnzahlGetraenk.setLayoutParams(lp);
        etAnzahlGetraenk.setPadding(100,30,100,30);
        etAnzahlGetraenk.setText("1");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Möchtest du, " + gastName + ", wirklich Folgendes bestellen?\n" +
                        getraenkeName)
                .setTitle("Bestellbestätigung")
                .setView(etAnzahlGetraenk)
                .setCancelable(false)
                .setNegativeButton("Abbrechen", ((dialog, which) -> dialog.dismiss()))
                .setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int anzahl = Integer.valueOf(etAnzahlGetraenk.getText().toString());
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
