package com.s3603441.roadqualityanalyser.db.settings;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SettingDao {
    @Query("SELECT * FROM setting")
    List<Setting> getAll();

    @Query("SELECT * FROM setting WHERE name = :name")
    Setting getSetting(final String name);

    @Insert
    void addSetting(final Setting... setting);

    @Query("DELETE FROM setting WHERE name = :name")
    void deleteSetting(final String name);

    @Query("UPDATE setting SET name = :name, value = :value WHERE name = :name")
    void updateSetting(final String name, final int value);
}
