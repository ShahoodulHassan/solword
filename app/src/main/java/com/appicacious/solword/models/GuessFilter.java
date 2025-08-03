package com.appicacious.solword.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class GuessFilter implements Comparable<GuessFilter>, Parcelable {

    @NonNull
    private final String title;

    private int count;

//    @NonNull
//    private final List<String> alphas;

    /*
    RawQuery required to retrieve the guess word count against this filter
     */
//    @NonNull
//    private String query;

    /*
    Last part of query needed to be added to the base query in order to retrieve words associated
    with this filter
     */
    @NonNull
    private final String querySuffix;

    /*
    Used by the adapter
     */
    private boolean isSelected;

    public GuessFilter(@NonNull String title, /*@NonNull List<String> alphas, @NonNull String query,*/
                       @NonNull String querySuffix) {
        this.title = title;
//        this.alphas = alphas;
//        this.query = query;
        this.querySuffix = querySuffix;
    }

    public GuessFilter(@NonNull String title, int count, /*@NonNull List<String> alphas,
                       @NonNull String query, */@NonNull String querySuffix, boolean isSelected) {
        this.title = title;
        this.count = count;
//        this.alphas = alphas;
//        this.query = query;
        this.querySuffix = querySuffix;
        this.isSelected = isSelected;
    }

    public GuessFilter(GuessFilter other) {
        this.title = other.title;
        this.count = other.count;
//        this.alphas = other.alphas;
        this.querySuffix = other.querySuffix;
        this.isSelected = other.isSelected;
    }

    protected GuessFilter(Parcel in) {
        title = in.readString();
        count = in.readInt();
        querySuffix = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(count);
        dest.writeString(querySuffix);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GuessFilter> CREATOR = new Creator<GuessFilter>() {
        @Override
        public GuessFilter createFromParcel(Parcel in) {
            return new GuessFilter(in);
        }

        @Override
        public GuessFilter[] newArray(int size) {
            return new GuessFilter[size];
        }
    };

    @NonNull
    public String getTitle() {
        return title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

//    @NonNull
//    public List<String> getAlphas() {
//        return alphas;
//    }

//    @NonNull
//    public String getQuery() {
//        return query;
//    }
//
//    public void setQuery(@NonNull String query) {
//        this.query = query;
//    }

    @NonNull
    public String getQuerySuffix() {
        return querySuffix;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean areContentsTheSame(GuessFilter oldFilter) {
        return this.isSelected == oldFilter.isSelected;
    }

    @NonNull
    @Override
    public String toString() {
        return "\nGuessFilter{" +
                "title='" + title + '\'' +
                ", count=" + count +
//                ", alphas=" + alphas +
//                ", query='" + query + '\'' +
                ", querySuffix='" + querySuffix + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }

    @Override
    public int compareTo(GuessFilter guessFilter) {
        int compareWith = guessFilter.getCount();

        // For ascending order:
        // this.getCount() - compareWith;

        return compareWith - this.getCount();
    }
}
