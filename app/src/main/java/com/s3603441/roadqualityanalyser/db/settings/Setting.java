package com.s3603441.roadqualityanalyser.db.settings;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "setting")
public class Setting {
    @Ignore
    public Setting(final int id, final String name, final int value) {
        setId(id);
        setName(name);
        setValue(value);
    }

    public Setting(final String name, final int value) {
        setName(name);
        setValue(value);
    }

    @PrimaryKey
    private int id;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="value")
    private int value;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        this.value = value;
    }
}
