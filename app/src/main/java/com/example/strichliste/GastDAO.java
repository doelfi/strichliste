package com.example.strichliste;

        import androidx.room.Dao;
        import androidx.room.Delete;
        import androidx.room.Insert;
        import androidx.room.Query;
        import androidx.room.Update;

        import java.util.List;
@Dao
public interface GastDAO {
    @Insert
    public void addGast(Gast gast);
    @Update
    public void updateGast(Gast gast);
    @Delete
    public void deleteGast(Gast gast);
    @Query("select * from gast")
    public List<Gast> getAllGast();
    @Query("select * from gast where name==:name")
    public Gast getGast(String name);
}
