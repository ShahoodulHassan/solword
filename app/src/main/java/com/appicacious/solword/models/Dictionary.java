package com.appicacious.solword.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Dictionary implements Parcelable {

    private final int _id;

    @NonNull
    private final String dictionaryName, definitionUrl;

    private String thesaurusUrl, synonymUrl, antonymUrl, sentenceUrl;


    public Dictionary(int _id, @NonNull String dictionaryName, @NonNull String definitionUrl) {
        this._id = _id;
        this.dictionaryName = dictionaryName;
        this.definitionUrl = definitionUrl;
    }


    protected Dictionary(Parcel in) {
        _id = in.readInt();
        dictionaryName = in.readString();
        definitionUrl = in.readString();
        thesaurusUrl = in.readString();
        synonymUrl = in.readString();
        antonymUrl = in.readString();
        sentenceUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(dictionaryName);
        dest.writeString(definitionUrl);
        dest.writeString(thesaurusUrl);
        dest.writeString(synonymUrl);
        dest.writeString(antonymUrl);
        dest.writeString(sentenceUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Dictionary> CREATOR = new Creator<Dictionary>() {
        @Override
        public Dictionary createFromParcel(Parcel in) {
            return new Dictionary(in);
        }

        @Override
        public Dictionary[] newArray(int size) {
            return new Dictionary[size];
        }
    };

    public int get_id() {
        return _id;
    }

    @NonNull
    public String getDictionaryName() {
        return dictionaryName;
    }

    @NonNull
    public String getDefinitionUrl() {
        return definitionUrl;
    }

    public String getThesaurusUrl() {
        return thesaurusUrl;
    }

    public String getSynonymUrl() {
        return synonymUrl;
    }

    public String getAntonymUrl() {
        return antonymUrl;
    }

    public String getSentenceUrl() {
        return sentenceUrl;
    }

    public void setThesaurusUrl(String thesaurusUrl) {
        this.thesaurusUrl = thesaurusUrl;
    }

    public void setSynonymUrl(String synonymUrl) {
        this.synonymUrl = synonymUrl;
    }

    public void setAntonymUrl(String antonymUrl) {
        this.antonymUrl = antonymUrl;
    }

    public void setSentenceUrl(String sentenceUrl) {
        this.sentenceUrl = sentenceUrl;
    }
}
