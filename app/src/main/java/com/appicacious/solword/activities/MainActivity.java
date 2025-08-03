package com.appicacious.solword.activities;

import static android.view.View.NO_ID;

import static com.appicacious.solword.constants.PreferenceKey.KEY_CLICK_COUNT;
import static com.appicacious.solword.constants.PreferenceKey.KEY_USAGE_COUNT;
import static com.appicacious.solword.constants.PreferenceKey.PREF_KEY_COUNTS;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.appicacious.solword.R;
import com.appicacious.solword.constants.PreferenceKey;
import com.appicacious.solword.models.Dictionary;

import java.util.List;

public class MainActivity extends AnalyticsActivity {
    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);

//        initAppLovinSdk();

    }


    @Override
    public void setStatusBarColor(@ColorRes int colorRes) {
        int color = ContextCompat.getColor(this, colorRes);
        getWindow().setStatusBarColor(color);
//        getWindow().setNavigationBarColor(color);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public boolean areTermsAccepted() {
        return getSharedPreferences(PreferenceKey.PREFS_MAIN, MODE_PRIVATE)
                .getBoolean(PreferenceKey.KEY_TERMS_ACCEPTED, false);
    }

    @Override
    public void setTermsAccepted(boolean isAccepted) {
        getSharedPreferences(PreferenceKey.PREFS_MAIN, MODE_PRIVATE).edit()
                .putBoolean(PreferenceKey.KEY_TERMS_ACCEPTED, isAccepted).apply();
    }

    @Override
    public int getClickCount() {
        return getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).getInt(KEY_CLICK_COUNT, 0);
    }

    @Override
    public void setClickCount(int count) {
        getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).edit().putInt(KEY_CLICK_COUNT, count)
                .apply();
    }

    @Override
    public int getUsageCount() {
        return getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).getInt(KEY_USAGE_COUNT, 0);
    }

    @Override
    public void setUsageCount(int count) {
        getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).edit().putInt(KEY_USAGE_COUNT, count)
                .apply();
    }

    @Override
    public void incrementUsageCount(int increment) {
        getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).edit().putInt(KEY_USAGE_COUNT,
                (getUsageCount() + increment)).apply();
    }

    //    @Override
//    public int getDictionaryCount() {
//        return getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).getInt(KEY_DICT_COUNT, 0);
//    }
//
//    @Override
//    public int getGuessPickCount() {
//        return getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).getInt(KEY_PICK_COUNT, 0);
//    }
//
//    @Override
//    public int getGuessFilterCount() {
//        return getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).getInt(KEY_FILTER_COUNT, 0);
//    }
//
//    @Override
//    public void setDictionaryCount(int count) {
//        getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).edit().putInt(KEY_DICT_COUNT, count)
//                .apply();
//    }
//
//    @Override
//    public void setGuessPickCount(int count) {
//        getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).edit().putInt(KEY_PICK_COUNT, count)
//                .apply();
//    }
//
//    @Override
//    public void setGuessFilterCount(int count) {
//        getSharedPreferences(PREF_KEY_COUNTS, MODE_PRIVATE).edit().putInt(KEY_FILTER_COUNT, count)
//                .apply();
//    }

    @Override
    @NonNull
    public List<Dictionary> getDictionaries() {
        return mainViewModel.getDictionaries();
    }

    @Override
    @Nullable
    public Dictionary getDefaultDictionary() {
        String key = getDefaultSharedPreferences().getString(getString(R.string.pref_key_dictionary), "");
        Log.d(TAG, "getDefaultDictionary: key=" + key);
        int id = NO_ID;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getDefaultDictionary: id=" + id);
        if (id != NO_ID) {
            return mainViewModel.findDictionaryById(id);
        } else {
            return null;
        }
//        int id = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE).getInt(KEY_DICTIONARY,
//                Constants.DEF_DICT_ID);
//        return mainViewModel.findDictionaryById(id);
    }

    @Override
    public void setDefaultDictionary(int id) {
        getDefaultSharedPreferences().edit().putString(getString(R.string.pref_key_dictionary),
                String.valueOf(id)).apply();
    }

    @Override
    public int getColCount() {
        return mainViewModel.getCurrentWordSize();
    }

    @Override
    public int getRowCount() {
        if (getColCount() > 6) {
            return 7;
        } else {
            return 6;
        }
    }

    @Override
    public int getCellCount() {
        return getColCount() * getRowCount();
    }

    @Override
    public int getLastCol() {
        return getColCount() - 1;
    }

    @Override
    public int getLastRow() {
        return getRowCount() - 1;
    }

    @Override
    public int getLastCell() {
        return getCellCount() - 1;
    }

}