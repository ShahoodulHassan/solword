package com.appicacious.solword.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.appicacious.solword.R;
import com.appicacious.solword.architecture.MainViewModel;
import com.appicacious.solword.interfaces.OnFragmentInteractionListener;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    private AppCompatActivity mActivity;
    private MainViewModel mainViewModel;
    private OnFragmentInteractionListener mListener;
    private NavController mNavController;

    private Toast toast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainViewModel = new ViewModelProvider(mActivity).get(MainViewModel.class);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        SwitchPreferenceCompat vibration = findPreference(getString(R.string.pref_key_vibration));
        if (vibration != null) vibration.setOnPreferenceChangeListener(this);

        ListPreference wordSizes = findPreference(getString(R.string.pref_key_word_size));
        if (wordSizes != null) {
            if (wordSizes.getEntry() == null) {
                Log.d(TAG, "onCreatePreferences: default word size is null");
                // Sets the default value to second entry in the list which in our case is 5.
                wordSizes.setValueIndex(1);
            }
            wordSizes.setOnPreferenceChangeListener(this);
        }

        ListPreference dictionaries = findPreference(getString(R.string.pref_key_dictionary));
        if (dictionaries != null) dictionaries.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNavController = Navigation.findNavController(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            mActivity.setSupportActionBar(toolbar);
            NavigationUI.setupActionBarWithNavController(mActivity, mNavController);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mNavController != null) mNavController.navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity &&
                context instanceof OnFragmentInteractionListener) {
            mActivity = (AppCompatActivity) context;
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new IllegalArgumentException("Activity not configured properly!");
        }

    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_vibration))) {
            if (newValue instanceof Boolean) {
                mainViewModel.setVibrationEnabled((Boolean) newValue);
                String msg = "Vibration " + (((Boolean) newValue) ? "enabled" : "disabled");
                serveFreshToast(msg);
            }
        } else if (key.equals(getString(R.string.pref_key_word_size))) {
            try {
                if (newValue instanceof String) {
                    int size = Integer.parseInt((String) newValue);
                    mainViewModel.setWordSizeLiveData(size);
                    serveFreshToast(String.format(getString(R.string.label_word_size), size));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (key.equals(getString(R.string.pref_key_dictionary))) {
            if (preference instanceof ListPreference && newValue instanceof String) {
                ListPreference dictPreference = ((ListPreference) preference);
                int index = dictPreference.findIndexOfValue((String) newValue);
                CharSequence[] entries = dictPreference.getEntries();
                if (entries != null && index < entries.length) {
                    String msg = entries[index] + " selected";
                    serveFreshToast(msg);
                }
            }
        }
        return true;
    }

    private void serveFreshToast(String msg) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
