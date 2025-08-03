package com.appicacious.solword.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appicacious.solword.constants.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GuessQueryData implements Parcelable {
    private static final String TAG = "GuessQueryData";

    @NonNull
    private final String query;

    private final int colCount;

    private final List<String> greens, yellows, greys;


    public GuessQueryData(@NonNull String query, List<String> greens,
                          List<String> yellows, List<String> greys, int colCount) {
        this.query = query;
        this.greens = greens;
        this.yellows = yellows;
        this.greys = greys;
        this.colCount = colCount;
    }

    // Copy constructor
    public GuessQueryData(GuessQueryData other) {
        this.query = other.query;
        this.greens = other.greens;
        this.yellows = other.yellows;
        this.greys = other.greys;
        this.colCount = other.colCount;
    }

    protected GuessQueryData(Parcel in) {
        query = in.readString();
        colCount = in.readInt();
        greens = in.createStringArrayList();
        yellows = in.createStringArrayList();
        greys = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(query);
        dest.writeInt(colCount);
        dest.writeStringList(greens);
        dest.writeStringList(yellows);
        dest.writeStringList(greys);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GuessQueryData> CREATOR = new Creator<GuessQueryData>() {
        @Override
        public GuessQueryData createFromParcel(Parcel in) {
            return new GuessQueryData(in);
        }

        @Override
        public GuessQueryData[] newArray(int size) {
            return new GuessQueryData[size];
        }
    };

    @NonNull
    public String getQuery() {
        return query;
    }

    public int getColCount() {
        return colCount;
    }

    @NonNull
    public List<String> getMissingAlphas() {
        List<String> missingAlphas = new ArrayList<>();
        HashSet<String> combined = new HashSet<>();
        if (greens != null) combined.addAll(greens);
        if (yellows != null) combined.addAll(yellows);
        if (greys != null) combined.addAll(greys);

        for (String alpha : Constants.ALL_ALPHAS) {
            if (!combined.contains(alpha)) missingAlphas.add(alpha);
        }

        Log.d(TAG, "getMissingAlphas: " + missingAlphas);

        return missingAlphas;
    }

    @NonNull
    @Override
    public String toString() {
        return "GuessQueryData{" +
                "query='" + query + '\'' +
                ", greens=" + greens +
                ", yellows=" + yellows +
                ", greys=" + greys +
                ", colCount=" + colCount +
                '}';
    }
}
