package com.example.strichliste;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Gast.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class GastDatabase extends RoomDatabase {
    public abstract GastDAO getGastDao();
}
