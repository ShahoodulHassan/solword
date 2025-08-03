package com.appicacious.solword.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.appicacious.solword.models.Word;
import com.appicacious.solword.room.WordStore;

import java.util.List;

import kotlin.coroutines.Continuation;


/**
 * This is a sample implementation of custom PagingSource. I'm not using it now but it is here for
 * a future usage.
 * I'm guessing that it can be used to fetch paged FireStore data as well.
 */
// TODO: 29/05/2022 Use this custom class to fetch some FireStore paged data
public class WordPagingSource extends PagingSource<Integer, Word> {

    private final WordStore wordStore;
    private final SimpleSQLiteQuery query;

    public WordPagingSource(WordStore wordStore, SimpleSQLiteQuery query) {
        this.wordStore = wordStore;
        this.query = query;
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Word> state) {

        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) return null;

        LoadResult.Page<Integer, Word> anchorPage = state.closestPageToPosition(anchorPosition);
        if (anchorPage == null) return null;

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) return prevKey + 1;

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) return nextKey - 1;

        return null;

    }

    @Nullable
    @Override
    public LiveData<LoadResult<Integer, Word>> load(@NonNull LoadParams<Integer> loadParams,
                                                    @NonNull Continuation<? super
                                                            LoadResult<Integer, Word>> continuation) {
        MutableLiveData<LoadResult<Integer, Word>> mutableLiveData = new MutableLiveData<>();
        int page = loadParams.getKey() == null ? 0 : loadParams.getKey();
        List<Word> words = wordStore.getWordsByQuery(query);
        Integer prevKey = page == 0 ? null : page - 1;
        Integer nextKey = words.isEmpty() ? null : page + 1;
        try {
            mutableLiveData.setValue(new LoadResult.Page<>(words, prevKey, nextKey));
        } catch (Exception e) {
            e.printStackTrace();
            mutableLiveData.setValue(new LoadResult.Error<>(e));
        }

        return mutableLiveData;
    }
}
