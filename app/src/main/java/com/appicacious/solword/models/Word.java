package com.appicacious.solword.models;

import androidx.annotation.NonNull;
import com.appicacious.solword.architecture.GuessRepository;

public class Word {

    private final long _id;

    @NonNull
    private final String wordTitle;

    /**
     * It is populated via Room with random values that are either 0 or 1.
     * See {@link GuessRepository#GUESSES_QUERY_PREFIX}
     *
     * This is used to hide word title of random words, in case of free user
     */
    private final boolean isHidden;


    public Word(long _id, @NonNull String wordTitle, boolean isHidden) {
        this._id = _id;
        this.wordTitle = wordTitle;
        this.isHidden = isHidden;
    }

    // Copy constructor
    public Word(Word other) {
        this._id = other._id;
        this.wordTitle = other.wordTitle;
        this.isHidden = other.isHidden;
    }

    public long get_id() {
        return _id;
    }

    @NonNull
    public String getWordTitle() {
        return wordTitle;
    }

    public boolean isHidden() {
        return isHidden;
    }

    @NonNull
    @Override
    public String toString() {
        return get_id() + ","
                + getWordTitle().toUpperCase() + ","
                + isHidden() + "\n";
    }
}
