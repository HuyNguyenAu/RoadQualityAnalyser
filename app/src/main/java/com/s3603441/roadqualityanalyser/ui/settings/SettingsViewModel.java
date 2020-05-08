package com.s3603441.roadqualityanalyser.ui.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.s3603441.roadqualityanalyser.db.AppDatabase;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<Integer> smoothing;
    private MutableLiveData<Integer> windowSize;
    private MutableLiveData<Integer> threshold;
    private MutableLiveData<Integer> sensitivity;

    private EditText editText_smoothing;
    private EditText editText_window_size;
    private EditText editText_threshold;
    private EditText editText_sensitivity;
    private Button button_save;

    public SettingsViewModel() {
    }

    public LiveData<Integer> getSmoothing() {
        if (this.smoothing == null) {
            this.smoothing = new MutableLiveData<>();
        }

        return this.smoothing;
    }

    public void setSmoothing(final int smoothing, final boolean mainThread) {
        if (this.smoothing == null) {
            this.smoothing = new MutableLiveData<>();
        }

        if (mainThread) {
           this.smoothing.setValue(smoothing);
       } else {
           this.smoothing.postValue(smoothing);
       }
    }

    public LiveData<Integer> getWindowSize() {
        if (this.windowSize == null) {
            this.windowSize = new MutableLiveData<>();
        }

        return this.windowSize;
    }

    public void setWindowSize(final int windowSize, final boolean mainThread) {
        if (this.windowSize == null) {
            this.windowSize = new MutableLiveData<>();
        }

        if (mainThread) {
            this.windowSize.setValue(windowSize);
        } else {
            this.windowSize.postValue(windowSize);
        }
    }

    public LiveData<Integer> getThreshold() {
        if (this.threshold == null) {
            this.threshold = new MutableLiveData<>();
        }

        return this.threshold;
    }

    public void setThreshold(final int threshold, final boolean mainThread) {
        if (this.threshold == null) {
            this.threshold = new MutableLiveData<>();
        }

        if (mainThread) {
            this.threshold.setValue(threshold);
        } else {
            this.threshold.postValue(threshold);
        }
    }

    public LiveData<Integer> getSensitivity() {
        if (this.sensitivity == null) {
            this.sensitivity = new MutableLiveData<>();
        }

        return this.sensitivity;
    }

    public void setSensitivity(final int sensitivity, final boolean mainThread) {
        if (this.sensitivity == null) {
            this.sensitivity = new MutableLiveData<>();
        }

        if (mainThread) {
            this.sensitivity.setValue(sensitivity);
        } else {
            this.sensitivity.postValue(sensitivity);
        }
    }

    public EditText getEditTextSmoothing() {
        return this.editText_smoothing;
    }

    public void setEditTextSmoothing(final EditText editText_smoothing) {
        this.editText_smoothing = editText_smoothing;
    }

    public EditText getEditTextWindowSize() {
        return this.editText_window_size;
    }

    public void setEditTextWindowSize(final EditText editText_window_size) {
        this.editText_window_size = editText_window_size;
    }

    public EditText getEditTextThreshold() {
        return this.editText_threshold;
    }

    public void setEditTextThreshold(final EditText editText_threshold) {
        this.editText_threshold = editText_threshold;
    }

    public EditText getEditTextSensitivity() {
        return this.editText_sensitivity;
    }

    public void setEditTextSensitivity(final EditText editText_sensitivity) {
        this.editText_sensitivity = editText_sensitivity;
    }

    public Button getButtonSave() {
        return this.button_save;
    }

    public void setButtonSave(final Button button_save) {
        this.button_save = button_save;
    }
}