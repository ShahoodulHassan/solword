package com.appicacious.solword.architecture;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.SavedStateHandle;

public abstract class SavedStateAndroidViewModel extends AndroidViewModel {

    protected final String TAG;

    private final SavedStateHandle savedStateHandle;

    public SavedStateAndroidViewModel(@NonNull Application application,
                                      SavedStateHandle savedStateHandle) {
        super(application);

        this.TAG = getClass().getSimpleName();

        this.savedStateHandle = savedStateHandle;

    }

    public abstract void saveState();

    protected abstract void restoreState();

    protected final SavedStateHandle getSavedStateHandle() {
        return savedStateHandle;
    }
}
