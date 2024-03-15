package com.example.strichliste;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrinkActivity extends AppCompatActivity {

    GastDatabase gastDB;

    List<Gast> gastList;

    Button newGast;

    Button btnExcelActivity;
    TextView tvGastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

        newGast = findViewById(R.id.btnNewPerson);

        tvGastName = findViewById(R.id.tvGastName);

        btnExcelActivity = findViewById(R.id.btnExcelActivity);

        RoomDatabase.Callback myCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                super.onDestructiveMigration(db);
            }
        };

        gastDB = Room.databaseBuilder(getApplicationContext(), GastDatabase.class, "AstDB").addCallback(myCallBack).build();

        newGast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = "Lisa";
                int anzahl = 0;
                String getraenk = "Cola";
                Date zeitpunkt = new Date();

                Gast g1 = new Gast(name, getraenk, anzahl, zeitpunkt);

                addGastInBackground(g1);

                getPersonListInBackground();
            }
        });

        btnExcelActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ExcelActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String gastName = intent.getStringExtra("gastName");

        tvGastName.setText(gastName);
        tvGastName.setVisibility(View.VISIBLE);
    }

    public void addGastInBackground(Gast gast){
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
                        Toast.makeText(DrinkActivity.this, "Added to Database", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }

    public void getPersonListInBackground(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                gastList = gastDB.getGastDao().getAllGast();

                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();

                        for (Gast g : gastList) {
                            sb.append(g.getName());
                            sb.append("\n");
                        }
                        String finalData = sb.toString();
                        Toast.makeText(DrinkActivity.this, finalData, Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }
}