package com.appicacious.solword.activities;

import static com.appicacious.solword.constants.MyAnalytics.Event.ON_DICTIONARY;
import static com.appicacious.solword.constants.MyAnalytics.Event.ON_GUESS_FETCHED;
import static com.appicacious.solword.constants.MyAnalytics.Param.ATTEMPT_COUNT;
import static com.appicacious.solword.constants.MyAnalytics.Param.DICT_NAME;
import static com.appicacious.solword.constants.MyAnalytics.Param.FILTER_COUNT;
import static com.appicacious.solword.constants.MyAnalytics.Param.FILTER_OPERATOR;
import static com.appicacious.solword.constants.MyAnalytics.Param.GUESS_COUNT;
import static com.appicacious.solword.constants.MyAnalytics.Param.WORD_SIZE;
import static com.appicacious.solword.constants.MyAnalytics.Param.WORD_TITLE;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;

public abstract class AnalyticsActivity extends BillingActivity {

    private static final String TAG = AnalyticsActivity.class.getSimpleName();

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }

    @Override
    public void logGuessFetched(int wordSize, int attemptCount, int guessCount, int filterCount,
                                String filterOperator) {
        Bundle bundle = new Bundle();
        bundle.putInt(WORD_SIZE, wordSize);
        bundle.putInt(ATTEMPT_COUNT, attemptCount);
        bundle.putInt(GUESS_COUNT, guessCount);
        bundle.putInt(FILTER_COUNT, filterCount);
        bundle.putString(FILTER_OPERATOR, filterOperator);
        Log.d(TAG, "logGuessFetched: params=" + bundle);
        mFirebaseAnalytics.logEvent(ON_GUESS_FETCHED, bundle);
    }

    @Override
    public void logDictionary(String dictName, String wordTitle, int wordSize) {
        Bundle bundle = new Bundle();
        bundle.putString(DICT_NAME, dictName);
        bundle.putString(WORD_TITLE, wordTitle);
        bundle.putInt(WORD_SIZE, wordSize);
        mFirebaseAnalytics.logEvent(ON_DICTIONARY, bundle);
    }
}
