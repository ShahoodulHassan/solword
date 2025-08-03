package com.appicacious.solword.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import java.util.Objects;

public class IntroSlide implements Parcelable {

    private final String title;

    private final String description;

    @DrawableRes
    private int drawable;

    @ColorRes
    private final int bgColorRes;


    public IntroSlide(String title, String description, @ColorRes int bgColorRes) {
        this.title = title;
        this.description = description;
        this.bgColorRes = bgColorRes;
    }

    public IntroSlide(String title, String description, @DrawableRes int drawable,
                      @ColorRes int bgColorRes) {
        this.title = title;
        this.description = description;
        this.drawable = drawable;
        this.bgColorRes = bgColorRes;
    }


    protected IntroSlide(Parcel in) {
        title = in.readString();
        description = in.readString();
        drawable = in.readInt();
        bgColorRes = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(drawable);
        dest.writeInt(bgColorRes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IntroSlide> CREATOR = new Creator<IntroSlide>() {
        @Override
        public IntroSlide createFromParcel(Parcel in) {
            return new IntroSlide(in);
        }

        @Override
        public IntroSlide[] newArray(int size) {
            return new IntroSlide[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @DrawableRes
    public int getDrawable() {
        return drawable;
    }

    @ColorRes
    public int getBgColorRes() {
        return bgColorRes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntroSlide)) return false;
        IntroSlide that = (IntroSlide) o;
        return getDrawable() == that.getDrawable() && getBgColorRes() == that.getBgColorRes() &&
                getTitle().equals(that.getTitle()) && getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription(), getDrawable(), getBgColorRes());
    }
}
