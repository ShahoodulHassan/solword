package com.appicacious.solword.architecture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * It serves as a single event LiveData wrapper
 *
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * Used in case where we want to avoid the receiving of already existing data. This class allows
 * usage of the underlying data once and then it starts returning null if the observer is attached
 * and existing LiveData is returned. A new value is returned only if a new value is actually
 * received by the LiveData.
 */
public class Event<T> {

    private final T mContent;

    private boolean hasBeenHandled = false;


    public Event(T content) {
        if (content == null) {
            throw new IllegalArgumentException("null values in Event are not allowed.");
        }
        mContent = content;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return mContent;
        }
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }

    @NonNull
    @Override
    public String toString() {
        return "Event{" +
//                "mContent=" + mContent +
                "hasBeenHandled=" + hasBeenHandled +
                '}';
    }
}