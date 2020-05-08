package com.s3603441.roadqualityanalyser.ui.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.settings.Setting;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_settings, container, false);

        initUI(root);
        initObservers();
        updateUI(root);

        return root;
    }

    public void initUI(final View root) {
        final AppDatabase appDatabase = Room.databaseBuilder(root.getContext().getApplicationContext(),
                AppDatabase.class, "data").build();

        settingsViewModel.setEditTextSmoothing((EditText) root.findViewById(R.id.editText_smoothing));
        settingsViewModel.setEditTextWindowSize((EditText) root.findViewById(R.id.editText_window_size));
        settingsViewModel.setEditTextThreshold((EditText) root.findViewById(R.id.editText_threshold));
        settingsViewModel.setEditTextSensitivity((EditText) root.findViewById(R.id.editText_sensitivity));
        settingsViewModel.setButtonSave((Button) root.findViewById(R.id.button_save));

        settingsViewModel.getButtonSave().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSaveClicked(v, appDatabase);
            }
        });

        appDatabase.close();
    }

    public void initObservers() {
        final Observer<Integer> smoothingObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                settingsViewModel.getEditTextSmoothing().setText(String.valueOf(integer));
            }
        };
        final Observer<Integer> windowSizeObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                settingsViewModel.getEditTextWindowSize().setText(String.valueOf(integer));
            }
        };
        final Observer<Integer> thresholdObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                settingsViewModel.getEditTextThreshold().setText(String.valueOf(integer));
            }
        };
        final Observer<Integer> sensitivityObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                settingsViewModel.getEditTextSensitivity().setText(String.valueOf(integer));
            }
        };

        settingsViewModel.getSmoothing().observe(getViewLifecycleOwner(), smoothingObserver);
        settingsViewModel.getWindowSize().observe(getViewLifecycleOwner(), windowSizeObserver);
        settingsViewModel.getThreshold().observe(getViewLifecycleOwner(), thresholdObserver);
        settingsViewModel.getSensitivity().observe(getViewLifecycleOwner(), sensitivityObserver);
    }

    public void updateUI(final View root) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final AppDatabase appDatabase = Room.databaseBuilder(root.getContext().getApplicationContext(),
                        AppDatabase.class, "data").build();

                settingsViewModel.setSmoothing(appDatabase.settingDao().getSetting("smoothing").getValue(), false);
                settingsViewModel.setWindowSize(appDatabase.settingDao().getSetting("window_size").getValue(), false);
                settingsViewModel.setThreshold(appDatabase.settingDao().getSetting("threshold").getValue(), false);
                settingsViewModel.setSensitivity(appDatabase.settingDao().getSetting("sensitivity").getValue(), false);

                appDatabase.close();
            }
        });
    }

    public void buttonSaveClicked(final View view, final AppDatabase appDatabase) {
        settingsViewModel.setSmoothing(Integer.parseInt(
                settingsViewModel.getEditTextSmoothing().getText().toString()), true);
        settingsViewModel.setWindowSize(Integer.parseInt(
                settingsViewModel.getEditTextWindowSize().getText().toString()), true);
        settingsViewModel.setThreshold(Integer.parseInt(
                settingsViewModel.getEditTextThreshold().getText().toString()), true);
        settingsViewModel.setSensitivity(Integer.parseInt(
                settingsViewModel.getEditTextSensitivity().getText().toString()), true);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                updateSetting("smoothing", settingsViewModel.getSmoothing().getValue(),
                        appDatabase);
                updateSetting("window_size", settingsViewModel.getWindowSize().getValue(),
                        appDatabase);
                updateSetting("threshold", settingsViewModel.getThreshold().getValue(),
                        appDatabase);
                updateSetting("sensitivity", settingsViewModel.getSensitivity().getValue(),
                        appDatabase);

                Snackbar.make(view, "Settings saved!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void updateSetting(final String settingName, final int newValue,
                              final AppDatabase appDatabase) {
        final boolean exists = appDatabase.settingDao().getSetting(settingName) != null;

        if (!exists) {
            return;
        }

        Setting setting = new Setting();
        setting.setId(appDatabase.settingDao().getSetting(settingName).getId());
        setting.setSetting(settingName);
        setting.setValue(newValue);

        appDatabase.settingDao().deleteSetting(settingName);
        appDatabase.settingDao().addSetting(setting);
    }
}
