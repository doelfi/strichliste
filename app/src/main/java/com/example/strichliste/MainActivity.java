package com.example.strichliste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnHueWa;
    ImageView ivLogoGrueneSchleife;
    Button newBtn;
    Space newSpace;
    ArrayList<String> gaesteListe;

    // DataBase
    GastDatabase gastDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLogoGrueneSchleife = findViewById(R.id.ivLogoGrueneSchleife);
        int imageID = getResources().getIdentifier("logo_gruene_schleife", "drawable", getPackageName());
        ivLogoGrueneSchleife.setImageResource(imageID);

        btnHueWa = findViewById(R.id.btnHueWa);

        btnHueWa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

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
        gaesteListe = ((MyGlobalVariables) MainActivity.this.getApplication()).getGaesteListe();
        if (!gaesteListe.isEmpty()) {
            createButtons(gaesteListe);
        }
    }
    private void createButtons(List<String> liste) {
        int i = 0;
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(-2, 150);
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
                    Intent intent = new Intent(MainActivity.this, GetraenkeActivity.class);
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