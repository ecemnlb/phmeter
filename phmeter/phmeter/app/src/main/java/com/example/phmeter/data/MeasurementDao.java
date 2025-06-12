// src/main/java/com/example/phmeter/data/MeasurementDao.java
package com.example.phmeter.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MeasurementDao {
    @Insert
    void insert(Measurement m);

    @Query("SELECT * FROM Measurement ORDER BY timestamp DESC")
    List<Measurement> all();
}
