package com.example.strichliste;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Button btnHueWa;
    ImageView ivLogoGrueneSchleife;
    Button newBtn;
    Space newSpace;
    String TAG = "MainActivity";
    BesucherInDatabase besucherInDB;


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

        receiveDatabase();

        createGaesteButtonsInBackground();
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
        besucherInDB = Room.databaseBuilder(getApplicationContext(), BesucherInDatabase.class, "BesucherInDB").addCallback(mainCallBack).build();
    }
    public void createGaesteButtonsInBackground(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // background task
                List<String> gaesteListe = besucherInDB.getBesucherInDAO().getAllNames();
                // on finishing task
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        createButtons(gaesteListe);
                    }
                });
            }
        });
    }
    private void createButtons(List<String> liste) {
        int i = 0;
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(-2, 200);
        TableLayout.LayoutParams layoutParams2 = new TableLayout.LayoutParams(0, 20);
        int column = 1;
        LinearLayout layout;
        for (i=0; i < liste.size(); i++) {
            if (column == 1) {
                layout = findViewById(R.id.column1);
            } else if (column == 2) {
                layout = findViewById(R.id.column2);
            } else {
                layout = findViewById(R.id.column3);
            }
            // @ToDo: change button syle via style="@android:style/Widget.Material3.Button"
            newBtn = new Button(this);
            newBtn.setText(liste.get(i));
            newBtn.setBackground(AppCompatResources.getDrawable(this, R.drawable.custom_button));
            newBtn.setLayoutParams(layoutParams);
            newBtn.setTextColor(getColor(R.color.white));
            newBtn.setTextSize(20);
            String gastName = newBtn.getText().toString();
            newSpace = new Space(this);
            newSpace.setLayoutParams(layoutParams2);
            layout.addView(newSpace);
            layout.addView(newBtn);
            newBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, GetraenkeActivity.class);
                    intent.putExtra("gastName", gastName);
                    startActivity(intent);
                }
            });


            if (i % 10 == 0 && i != 0) {
                column ++;
            }
        }
    }
}