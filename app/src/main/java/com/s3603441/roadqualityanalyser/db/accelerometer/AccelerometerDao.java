package com.s3603441.roadqualityanalyser.db.accelerometer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.s3603441.roadqualityanalyser.db.settings.Setting;

import java.util.List;

@Dao
public interface AccelerometerDao {
    @Query("SELECT * FROM accelerometer")
    List<Setting> getAll();

    @Query("SELECT * FROM accelerometer WHERE datetime = :datetime")
    Setting getData(final String datetime);

    @Insert
    void addData(final Accelerometer... accelerometers);

    @Query("DELETE FROM accelerometer WHERE datetime = :datetime")
    void deleteData(final String datetime);
}
