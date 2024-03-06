package com.example.strichliste;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "Gast")
public class Gast {

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "name")
    String name;

    @ColumnInfo(name = "getraenk")
    String getraenk;

    @ColumnInfo(name = "anzahl")
    Integer anzahl;

    @ColumnInfo(name = "zeitpunkt")
    Date zeitpunkt; //= Calendar.getInstance();



    @Ignore
    public Gast() {

    }

    public Gast(String name, String getraenk, Integer anzahl, Date zeitpunkt) {
        this.name = name;
        this.getraenk = getraenk;
        this.anzahl = anzahl;
        this.zeitpunkt = zeitpunkt;
        this.id = 0;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
