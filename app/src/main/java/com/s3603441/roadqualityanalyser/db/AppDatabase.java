package com.s3603441.roadqualityanalyser.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;
import com.s3603441.roadqualityanalyser.db.accelerometer.AccelerometerDao;
import com.s3603441.roadqualityanalyser.db.settings.Setting;
import com.s3603441.roadqualityanalyser.db.settings.SettingDao;

@Database(entities = {Setting.class, Accelerometer.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SettingDao settingDao();
    public abstract AccelerometerDao accelerometerDao();
}
