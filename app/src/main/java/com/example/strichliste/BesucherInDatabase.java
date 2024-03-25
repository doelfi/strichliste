package com.example.strichliste;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {BesucherIn.class}, version = 1)
public abstract class BesucherInDatabase extends RoomDatabase {
    public abstract BesucherInDAO besucherInDAO();
}
