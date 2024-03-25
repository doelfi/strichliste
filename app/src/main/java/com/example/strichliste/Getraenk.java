package com.example.strichliste;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Getraenk {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="drink_id")
    int did;
    @ColumnInfo(name = "name")
    public String name;

    public Getraenk(String name) {
        this.name = name;
        this.did = 0;
    }
}
