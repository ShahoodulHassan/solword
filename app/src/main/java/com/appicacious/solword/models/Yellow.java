package com.appicacious.solword.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;


public class Yellow {

    @NonNull
    private final String alpha;

    private List<Integer> positions;

    /**
     * It tells us how many times an alphabet appears in a word. This includes both green and yellow
     * occurrences.
     */
    private int maxCountInWord = 1;

    /**
     * It is the count a yellow alphabet occurs as a green alphabet at different positions.
     */
    private int greenCount;


    public Yellow(@NonNull String alpha, int position) {
        this.alpha = alpha;
        addPosition(position);
    }




    @NonNull
    public String getAlpha() {
        return alpha;
    }

    public void addPosition(int position) {
        positions = getPositions();
        if (!positions.contains(position)) positions.add(position);
    }

    @NonNull
    public List<Integer> getPositions() {
        return positions == null ? new ArrayList<>() : positions;
    }

    public int getMaxCountInWord() {
        return maxCountInWord;
    }

    public void setMaxCountInWord(int maxCountInWord) {
        this.maxCountInWord = maxCountInWord;
    }

    public int getGreenCount() {
        return greenCount;
    }

    public void setGreenCount(int greenCount) {
        this.greenCount = greenCount;
    }

    @NonNull
    @Override
    public String toString() {
        return "Yellow{" +
                "alpha='" + alpha + '\'' +
                ", positions=" + positions +
                ", maxCountInWord=" + maxCountInWord +
                ", greenCount=" + greenCount +
                '}';
    }
}
