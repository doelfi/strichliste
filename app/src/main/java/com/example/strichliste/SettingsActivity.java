package com.example.strichliste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    Button btnHueWa;
    Button btnSettings;
    Button btnHint;
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
        btnSettings = findViewById(R.id.btnSettings);
        btnHint = findViewById(R.id.btnHint);

        btnHueWa.setOnClickListener(this::onClick);
        btnSettings.setOnClickListener(this::onClick);
        btnHint.setOnClickListener(this::onClick);
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
            Intent intent = new Intent(this, GastActivity.class);
            startActivity(intent);
        }
        else if (view == btnSettings){
            //Toast.makeText(getApplicationContext(), "Lösung: " + companyName, Toast.LENGTH_LONG).show();
            String newName;
            newName = edText.getText().toString();
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