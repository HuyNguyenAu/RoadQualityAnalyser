package com.s3603441.roadqualityanalyser.db.accelerometer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.s3603441.roadqualityanalyser.db.settings.Setting;

import java.util.List;

@Dao
public interface AccelerometerDao {
    @Query("SELECT * FROM accelerometer")
    List<Accelerometer> getAll();

    @Query("SELECT * FROM accelerometer WHERE datetime = :datetime")
    List<Accelerometer> getData(final String datetime);

    @Query("SELECT DISTINCT datetime FROM accelerometer")
    List<String> getDateTimes();

    @Query("SELECT MAX(warnings) FROM accelerometer WHERE datetime = :datetime")
    int getWarnings(final String datetime);

    @Insert
    void addData(final Accelerometer... accelerometer);

    @Query("DELETE FROM accelerometer WHERE datetime = :datetime")
    void deleteData(final String datetime);
}
