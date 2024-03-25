package com.example.strichliste;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
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

    @Insert
    void insertAll(BesucherIn... besucherIns);

    @Delete
    void delete(BesucherIn besucherIn);
    @Delete
    public void deleteAllBesucherIn(List<BesucherIn> besucherIns);
}
