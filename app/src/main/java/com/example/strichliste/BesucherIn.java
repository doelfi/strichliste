package com.example.strichliste;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BesucherIn {
    @PrimaryKey
    @ColumnInfo(name = "gast_id")
    public int gid;
    @ColumnInfo(name = "name")
    public String name;

    public BesucherIn(int gid, String name) {
        this.gid = gid;
        this.name = name;
    }
}
