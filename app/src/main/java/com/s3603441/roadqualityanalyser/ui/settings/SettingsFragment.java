package com.s3603441.roadqualityanalyser.ui.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.settings.Setting;

import java.util.ArrayList;
import java.util.List;

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
                AppDatabase.class, "settings").build();

        settingsViewModel.setEditTextSmoothing((EditText) root.findViewById(R.id.editText_smoothing));
        settingsViewModel.setEditTextWindowSize((EditText) root.findViewById(R.id.editText_window_size));
        settingsViewModel.setEditTextThreshold((EditText) root.findViewById(R.id.editText_threshold));
        settingsViewModel.setEditTextSensitivity((EditText) root.findViewById(R.id.editText_sensitivity));
        settingsViewModel.setButtonSave((Button) root.findViewById(R.id.button_save));

        settingsViewModel.getButtonSave().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSaveClicked(v);
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
        final UpdateUITask updateUITask = new UpdateUITask(root);
        updateUITask.execute(root.getContext().getApplicationContext());
    }

    public void buttonSaveClicked(final View root) {
        updateSettings();
        setSettings(root);
    }

    public void updateSettings() {
        settingsViewModel.setSmoothing(Integer.parseInt(
                settingsViewModel.getEditTextSmoothing().getText().toString()));
        settingsViewModel.setWindowSize(Integer.parseInt(
                settingsViewModel.getEditTextWindowSize().getText().toString()));
        settingsViewModel.setThreshold(Integer.parseInt(
                settingsViewModel.getEditTextThreshold().getText().toString()));
        settingsViewModel.setSensitivity(Integer.parseInt(
                settingsViewModel.getEditTextSensitivity().getText().toString()));
    }

    public void setSettings(final View root) {
        final Snackbar savingSnackbar = Snackbar.make(root, "Saving settings...", Snackbar.LENGTH_INDEFINITE);
        final SetSettingsTask setSettingsTask = new SetSettingsTask(root, savingSnackbar);
        setSettingsTask.execute(root.getContext().getApplicationContext());
    }

    private class UpdateUITask extends AsyncTask<Context, Void, List<Setting>> {
        private View root;

        public UpdateUITask(final View root) {
            this.root = root;
        }

        @Override
        protected List<Setting> doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "settings").build();
            final List<Setting> settings = appDatabase.settingDao().getAll();
            appDatabase.close();

            return settings;
        }

        @Override
        protected void onPostExecute(final List<Setting> settings) {
            for (final Setting setting : settings) {
                final String name = setting.getName();
                final int value = setting.getValue();

                Log.d("onPostExecute", "onPostExecute: " + name);

                if (name.equalsIgnoreCase("smoothing")) {
                    settingsViewModel.setSmoothing(value);
                } else if (name.equalsIgnoreCase("window_size")) {
                    settingsViewModel.setWindowSize(value);
                }  else if (name.equalsIgnoreCase("threshold")) {
                    settingsViewModel.setThreshold(value);
                }  else if (name.equalsIgnoreCase("sensitivity")) {
                    settingsViewModel.setSensitivity(value);
                }
            }

            super.onPostExecute(settings);
        }
    }

    private class SetSettingsTask extends AsyncTask<Context, Void, Void> {
        private View root;
        private Snackbar savingSnackbar;

        public SetSettingsTask(final View root, final Snackbar savingSnackbar) {
            this.root = root;
            this.savingSnackbar = savingSnackbar;
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "settings").build();

            List<Setting> settings = new ArrayList<>();
            settings.add(new Setting("smoothing", settingsViewModel.getSmoothing().getValue()));
            settings.add(new Setting("window_size", settingsViewModel.getWindowSize().getValue()));
            settings.add(new Setting("threshold", settingsViewModel.getThreshold().getValue()));
            settings.add(new Setting("sensitivity", settingsViewModel.getSensitivity().getValue()));

            updateSettings(settings, appDatabase);
            appDatabase.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            savingSnackbar.dismiss();
            Snackbar.make(root, "Settings saved!", Snackbar.LENGTH_SHORT).show();

            super.onPostExecute(aVoid);
        }

        public void updateSettings(final List<Setting> settings, final AppDatabase appDatabase) {
            for (int i = 0; i < settings.size(); i++) {
                Setting setting = settings.get(i);
                final Setting oldSetting = appDatabase.settingDao().getSetting(setting.getName());

                if (oldSetting != null) {
                    setting.setId(oldSetting.getId());
                    appDatabase.settingDao().deleteSetting(oldSetting.getName());
                } else {
                    setting.setId(i);
                }

                appDatabase.settingDao().addSetting(setting);
            }
        }
    }
}
