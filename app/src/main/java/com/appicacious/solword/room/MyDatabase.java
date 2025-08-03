package com.appicacious.solword.room;

import static com.appicacious.solword.constants.Constants.DB_NAME;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.appicacious.solword.models.Wordle;

@Database(entities = {Wordle.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {

    private static final String TAG = MyDatabase.class.getSimpleName();
    private static volatile MyDatabase INSTANCE = null;

    public synchronized static MyDatabase get(Context context) {
        Log.d(TAG, "get: called");
        if (INSTANCE == null) INSTANCE = create(context);
        return INSTANCE;
    }


    private static MyDatabase create(Context context) {
        String dbFilePath = "databases/" + DB_NAME;
        return Room.databaseBuilder(context.getApplicationContext(), MyDatabase.class, DB_NAME)
                .createFromAsset(dbFilePath)
                .build();
    }


    public abstract WordStore wordStore();
}
