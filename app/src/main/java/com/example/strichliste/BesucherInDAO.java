package com.example.strichliste;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BesucherInDAO {
    @Query("SELECT * FROM besucherin")
    List<BesucherIn> getAll();

    @Query("SELECT * FROM besucherin WHERE gast_id LIKE :gid")
    BesucherIn findByGID(int gid);

    @Query("SELECT name FROM besucherin WHERE gast_id ==:gid")
    String getNameFromGID(int gid);

    @Query("SELECT name FROM besucherin")
    public List<String> getAllNames();

    @Insert
    void insertAll(BesucherIn... besucherIns);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addBesucherIn(BesucherIn besucherIn);

    @Delete
    void delete(BesucherIn besucherIn);
    @Delete
    public void deleteAllBesucherIn(List<BesucherIn> besucherIns);
}
