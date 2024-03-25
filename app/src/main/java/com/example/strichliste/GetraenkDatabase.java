package com.example.strichliste;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {Getraenk.class}, version = 1)
public abstract class GetraenkDatabase extends RoomDatabase {
    public abstract GetraenkDAO getGetraenkDAO();
}
