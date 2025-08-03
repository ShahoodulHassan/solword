package com.appicacious.solword.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "wordles",
        indices = {@Index(value = {"wordTitle"}, unique = true)})
public class Wordle {

    @PrimaryKey
    private final long _id;

    @NonNull
    private final String wordTitle;

    private final int source;



    public Wordle(long _id, @NonNull String wordTitle, int source) {
        this._id = _id;
        this.wordTitle = wordTitle;
        this.source = source;
    }

    public long get_id() {
        return _id;
    }

    @NonNull
    public String getWordTitle() {
        return wordTitle;
    }

    public int getSource() {
        return source;
    }

    @NonNull
    @Override
    public String toString() {
        return get_id() + " - " + getWordTitle() + "\n";
    }
}
