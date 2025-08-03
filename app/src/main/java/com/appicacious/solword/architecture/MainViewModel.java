package com.appicacious.solword.architecture;

import static android.view.View.NO_ID;

import static com.appicacious.solword.constants.Constants.URL_PING;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.appicacious.solword.R;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.models.Dictionary;
import com.appicacious.solword.utilities.MyAsyncTask;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    final List<Dictionary> dictionaries;
    final SharedPreferences sharedPreferences;

    private final MutableLiveData<Boolean> isAdNetworkInitializedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInternetAvailableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> vibrationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> wordSizeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> isInterstitialSeenLiveData = new MutableLiveData<>();

    private int currentWordSize;

    private String clickedGuess;

    private final ReviewManager reviewManager;
    private ReviewInfo reviewInfo;

    public MainViewModel(@NonNull Application application) {
        super(application);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);

        dictionaries = new ArrayList<>();
        createDictionaries();

        reviewManager = ReviewManagerFactory.create(application);

        reviewManager.requestReviewFlow()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) reviewInfo = task.getResult();
                });

        /*
         Retrieve persisted word size and save it to local variable for use in the app. This is
         being done to avoid retrieving data from preferences and thereby avoiding some jank.
         */
        int intValue = Constants.DEF_WORD_SIZE;
        try {
            intValue = Integer.parseInt(sharedPreferences.getString(getApplication().getString(
                            R.string.pref_key_word_size),
                    String.valueOf(Constants.DEF_WORD_SIZE)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        setCurrentWordSize(intValue);

        // Retrieve persisted vibration
        boolean isEnabled = sharedPreferences.getBoolean(getApplication().getString(
                        R.string.pref_key_vibration), true);
        setVibrationEnabled(isEnabled);

    }

    private void createDictionaries() {
        String[] dict_ids = getApplication().getResources().getStringArray(R.array.dictionary_keys);
        String[] dict_names = getApplication().getResources().getStringArray(R.array.dictionary_values);
        String[] dict_urls = getApplication().getResources().getStringArray(R.array.dictionary_url);

        if (dict_ids.length == dict_names.length && dict_ids.length == dict_urls.length) {
            for (int x = 0; x < dict_ids.length; x++) {
                int id = NO_ID;
                try {
                    id = Integer.parseInt(dict_ids[x]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                if (id != NO_ID) {
                    Dictionary dict = new Dictionary(id, dict_names[x], dict_urls[x]);
                    dictionaries.add(dict);
                }
            }
        }
    }

    public LiveData<Boolean> getAdNetworkInitializedLiveData() {
        return isAdNetworkInitializedLiveData;
    }

    public LiveData<Boolean> getIsInternetAvailableLiveData() {
        return isInternetAvailableLiveData;
    }

    public LiveData<Event<Boolean>> getIsInterstitialSeenLiveData() {
        return isInterstitialSeenLiveData;
    }

    public void setInterstitialSeen(boolean isSeen) {
        isInterstitialSeenLiveData.postValue(new Event<>(isSeen));
    }

    public boolean isAdNetworkInitialized() {
        return isAdNetworkInitializedLiveData.getValue() != null
                && isAdNetworkInitializedLiveData.getValue();
    }

    public void setAdNetworkInitialized(boolean isInitialized) {
        isAdNetworkInitializedLiveData.postValue(isInitialized);
    }

    public void setVibrationEnabled(boolean isEnabled) {
        vibrationLiveData.setValue(isEnabled);
    }

    public boolean isVibrationEnabled() {
        Boolean isEnabled = vibrationLiveData.getValue();
        return isEnabled == null || isEnabled;
    }

    public void setWordSizeLiveData(int size) {
        wordSizeLiveData.setValue(size);
    }

    public LiveData<Integer> getWordSizeLiveData() {
        return wordSizeLiveData;
    }

    @NonNull
    public ReviewManager getReviewManager() {
        return reviewManager;
    }

    @Nullable
    public ReviewInfo getReviewInfo() {
        return reviewInfo;
    }

    @NonNull
    public List<Dictionary> getDictionaries() {
        return dictionaries;
    }

    @Nullable
    public Dictionary findDictionaryById(int id) {
        Log.d(TAG, "findDictionaryById: id=" + id);
        for (Dictionary dictionary : getDictionaries()) {
            if (dictionary.get_id() == id) return dictionary;
        }
        Log.d(TAG, "findDictionaryById: null dictionary returned");
        return null;
    }

    public int getCurrentWordSize() {
        return currentWordSize;
    }

    public void setCurrentWordSize(int currentWordSize) {
        this.currentWordSize = currentWordSize;
    }

    public String getClickedGuess() {
        return clickedGuess;
    }

    public void setClickedGuess(String clickedGuess) {
        this.clickedGuess = clickedGuess;
    }

    public void checkInternet(boolean isConnected) {
        if (isConnected) {
            checkInternet();
        } else {
            isInternetAvailableLiveData.postValue(isConnected);
        }
    }

    private void checkInternet() {
        new InternetCheckerTask(isInternetAvailableLiveData::setValue).execute(null);
    }









    private static class InternetCheckerTask extends MyAsyncTask<Void, Void, Boolean> {

        private final OnInternetCheckerTaskListener mListener;

        public InternetCheckerTask(OnInternetCheckerTaskListener mListener) {
            this.mListener = mListener;
        }

        @Override
        protected Boolean doInBackground(Void unused) {
            return checkInternetPingGoogle();
        }

        public boolean checkInternetPingGoogle() {
            boolean isConnected = false;
            try {
                URL url = new URL(URL_PING);
                HttpsURLConnection urlc = (HttpsURLConnection) url.openConnection();
                urlc.setConnectTimeout(300);
                urlc.connect();
                isConnected = urlc.getResponseCode() == 200;
            } catch (IOException e) {
                Log.d(TAG, "checkInternetPingGoogle: error=" + e.getMessage());
            }
            Log.d(TAG, "checkInternetPingGoogle: isConnected=" + isConnected);
            return isConnected;
        }

        @Override
        protected void onPostExecute(Boolean isAvailable) {
            super.onPostExecute(isAvailable);
            mListener.onInternetCheckCompleted((isAvailable != null && isAvailable));
        }
    }

    public interface OnInternetCheckerTaskListener {
        void onInternetCheckCompleted(boolean isAvailable);
    }
}
