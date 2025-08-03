package com.appicacious.solword.architecture;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appicacious.solword.room.MyDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqliteRepository {

    final ExecutorService executorService;
    final Application application;
    final MyDatabase db;

    public SqliteRepository(@NonNull Application application) {
        this.application = application;
        db = MyDatabase.get(application);
        this.executorService = Executors.newFixedThreadPool(4);
    }
}
