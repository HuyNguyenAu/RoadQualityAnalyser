package com.s3603441.roadqualityanalyser.db.settings;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SettingDao {
    @Query("SELECT * FROM setting")
    List<Setting> getAll();

    @Query("SELECT * FROM setting WHERE setting = :setting")
    Setting getSetting(final String setting);

    @Insert
    void addSetting(final Setting... setting);

    @Query("DELETE FROM setting WHERE setting = :setting")
    void deleteSetting(final String setting);
}
