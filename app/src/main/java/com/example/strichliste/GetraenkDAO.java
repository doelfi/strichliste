package com.example.strichliste;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GetraenkDAO {
    @Query("SELECT * FROM getraenk")
    public List<Getraenk> getAllGetraenke();
    @Query("SELECT name FROM getraenk")
    public List<String> getAllGetraenkeNames();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addGetraenk(Getraenk getraenk);

    @Delete
    public void deleteAllGetraenke(List<Getraenk> getraenke);


}
