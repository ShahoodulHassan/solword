package com.appicacious.solword.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.appicacious.solword.models.Word;
import com.appicacious.solword.models.Wordle;

import java.util.List;

@Dao
public interface WordStore {

    // No paging
    @RawQuery(observedEntities = Wordle.class)
    LiveData<List<Word>> getWordsLiveDataByQuery(SupportSQLiteQuery query);

    // Paging 2
    @RawQuery(observedEntities = Wordle.class)
    DataSource.Factory<Integer, Word> getPagedWordsByQuery(SupportSQLiteQuery query);

    // Paging 3
    @RawQuery(observedEntities = Wordle.class)
    PagingSource<Integer, Word> getPaged3WordsByQuery(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Wordle.class)
    List<Word> getWordsByQuery(SupportSQLiteQuery query);

    // Get a random word by word size
    @Deprecated
    @Query("SELECT LOWER(wordTitle) FROM wordles WHERE LENGTH(wordTitle) =:size ORDER BY RANDOM() LIMIT 1")
    LiveData<String> getRandomWordBySize(int size);

    // Get a random word by supplying a query
    @RawQuery(observedEntities = Wordle.class)
    LiveData<String> getRandomWordByQuery(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Wordle.class)
    int getGuessCountByAlpha(SupportSQLiteQuery query);








//    --------------------------UNUSED METHODS--------------------------//

//    @RawQuery(observedEntities = Wordle.class)
//    LiveData<AlphaPresence> getAlphaPresence(SupportSQLiteQuery query);


//    @Query("SELECT UPPER(l1) alpha, COUNT(_id) count " +
//            "FROM wordles " +
//            "WHERE LENGTH(wordTitle) =:length " +
//            "GROUP BY UPPER(l1) " +
//            "ORDER BY UPPER(l1)")
//    LiveData<List<WordCount>> getWordCountByLength(int length);

//    // TODO: 15/03/2022 Implement paging here
//    @Query("SELECT _id, wordTitle FROM wordles WHERE LENGTH(wordTitle) =:length ORDER BY l1")
//    LiveData<List<Word>> getWordsByLength(int length);
//
//    @Query("SELECT _id, wordTitle FROM wordles WHERE LENGTH(wordTitle) =:length ORDER BY l1")
//    DataSource.Factory<Integer, Word> getPagedWordsByLength(int length);

//    @RawQuery(observedEntities = Wordle.class)
//    LiveData<List<Word>> getWordsByQuery(SupportSQLiteQuery query);





}
