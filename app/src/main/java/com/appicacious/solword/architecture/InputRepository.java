package com.appicacious.solword.architecture;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.appicacious.solword.models.Cell;
import com.appicacious.solword.models.GuessQueryData;
import com.appicacious.solword.tasks.CellTask;
import com.appicacious.solword.tasks.QueryDataTask;

import java.util.List;

public class InputRepository extends SqliteRepository
        implements CellTask.OnCellTaskInteractionListener {

    private static final String TAG = InputRepository.class.getSimpleName();

    public static final String RANDOM_WORD_QUERY_PREFIX = "SELECT LOWER(wordTitle)";
    public static final String RANDOM_WORD_QUERY_SUFFIX = " ORDER BY RANDOM() LIMIT 1";

    private final MutableLiveData<Event<List<Cell>>> cellsLiveData = new MutableLiveData<>();
    private MediatorLiveData<Event<String>> randomWordMedLiveData;
    private final MutableLiveData<Event<LiveData<String>>> randomWordLiveData = new MutableLiveData<>();

    public InputRepository(@NonNull Application application) {
        super(application);
    }

    void initCreateCells(int rowCount, int colCount) {
        new CellTask(this, rowCount, colCount).execute(null);
    }

    @Override
    public void onComplete(List<Cell> cells) {
        if (cells != null) cellsLiveData.setValue(new Event<>(cells));
    }

    public LiveData<Event<List<Cell>>> getCellsLiveData() {
        return cellsLiveData;
    }

    /**
     * Use {@link #getRandomWordByQuery(GuessQueryData)} instead
     *
     */
    @Deprecated
    void getRandomWordBySize(int size) {
        LiveData<String> randomWordLiveData = db.wordStore().getRandomWordBySize(size);
        randomWordMedLiveData = new MediatorLiveData<>();
        randomWordMedLiveData.addSource(randomWordLiveData, word ->
                randomWordMedLiveData.postValue(new Event<>(word)));
    }

    private void getRandomWordByQuery(final @NonNull GuessQueryData queryData) {
        String queryString = RANDOM_WORD_QUERY_PREFIX + queryData.getQuery() +
                RANDOM_WORD_QUERY_SUFFIX;
        Log.d(TAG, "getRandomWordByQuery: queryString=" + queryString);
        LiveData<String> randomWordLiveData = db.wordStore().getRandomWordByQuery(
                new SimpleSQLiteQuery(queryString));
//        randomWordMedLiveData = new MediatorLiveData<>();
        randomWordMedLiveData.addSource(randomWordLiveData, word -> {
            randomWordMedLiveData.postValue(new Event<>(!TextUtils.isEmpty(word) ? word : ""));
        });
    }

    void initRandomWordTask(List<Cell> cells, int size) {
        randomWordMedLiveData = new MediatorLiveData<>();
        new QueryDataTask(this::getRandomWordByQuery, size).execute(cells);
    }

    public LiveData<Event<String>> getRandomWordMedLiveData() {
        return randomWordMedLiveData;
    }
}
