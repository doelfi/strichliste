package com.example.strichliste;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Getraenk {
    @PrimaryKey
    @ColumnInfo(name="drink_id")
    int did;
    @ColumnInfo(name = "name")
    public String name;

    public Getraenk(String name, int did) {
        this.name = name;
        this.did = did;
    }
}
