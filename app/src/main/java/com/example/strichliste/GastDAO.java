package com.example.strichliste;

        import androidx.room.Dao;
        import androidx.room.Delete;
        import androidx.room.Insert;
        import androidx.room.Query;
        import androidx.room.Update;

        import java.util.Date;
        import java.util.List;
@Dao
public interface GastDAO {
    @Insert
    public void addGast(Gast gast);
    @Update
    public void updateGast(Gast gast);
    @Delete
    public void deleteGast(Gast gast);
    @Delete
    public void deleteAllGast(List<Gast> gast);
    @Query("select * from gast")
    public List<Gast> getAllGast();
    @Query("select * from gast where name==:name")
    public Gast getGast(String name);
    @Query("select * from gast where name==:name AND getraenk==:getraenk")
    public Gast getGastGetraenk(String name, String getraenk);
    @Query("select SUM(anzahl) as gesamtmenge from gast where name==:name AND getraenk==:getraenk AND zeitpunkt >= :start_zeitpunkt AND zeitpunkt < :end_zeitpunkt")
    public int getSummeGastGetraenkZeitpunkt(String name, String getraenk, Date start_zeitpunkt, Date end_zeitpunkt);
}
