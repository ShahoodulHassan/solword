package com.appicacious.solword.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YellowPosition {

    private final int index;

    private List<String> alphas;

    public YellowPosition(int index, String alpha) {
        this.index = index;
        addAlpha(alpha);
    }

    public void addAlpha(String alpha) {
        alphas = getAlphas();
        if (!TextUtils.isEmpty(alpha) && !alphas.contains(alpha)) alphas.add(alpha);
    }

    public int getIndex() {
        return index;
    }

    public List<String> getAlphas() {
        return alphas == null ? new ArrayList<>() : alphas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YellowPosition)) return false;
        YellowPosition yellowPosition = (YellowPosition) o;
        return getIndex() == yellowPosition.getIndex() && getAlphas().equals(yellowPosition.getAlphas());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getAlphas());
    }

    @NonNull
    @Override
    public String toString() {
        return "Yellow{" +
                "position=" + index +
                ", alphas=" + alphas +
                '}';
    }
}
