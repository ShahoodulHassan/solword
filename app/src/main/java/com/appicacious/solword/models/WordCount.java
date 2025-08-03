package com.appicacious.solword.models;

import androidx.annotation.NonNull;

public class WordCount {

    private final String alpha;

    private final int count;

    public WordCount(String alpha, int count) {
        this.alpha = alpha;
        this.count = count;
    }

    @NonNull
    @Override
    public String toString() {
        return alpha + " " + count + "\n";
    }
}
