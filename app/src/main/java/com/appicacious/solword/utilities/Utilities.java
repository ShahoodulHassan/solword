package com.appicacious.solword.utilities;

import android.util.Log;

import androidx.annotation.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Utilities {

    private static final String TAG = Utilities.class.getSimpleName();


    @Nullable
    public static <T> List<T> getCopyList(@Nullable List<T> oldList) {
        if (oldList == null) return null;

        List<T> newList = new ArrayList<>();
        for (T item : oldList) {
            CopyConstructor<T> cc = new CopyConstructor<>();
            T copy = null;
            try {
                copy = cc.createCopy(item);
            } catch (Exception e) {
                copy = item;
                e.printStackTrace();
            } finally {
                newList.add(copy);
            }
        }
        return newList;
    }

    // Generic method for all needs
    public static String applyCommaSeparator(double value, int dec) {
        StringBuilder builder = new StringBuilder(",###");
        if (dec > 0 && dec <= 3) {
            builder.append(".");
            for (int i = 0; i < dec; i++) {
                builder.append("0");
            }
        }
        String numPattern = builder.toString();

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());
        DecimalFormat df = new DecimalFormat(numPattern, dfs);
        df.setRoundingMode(RoundingMode.HALF_UP);
        String resString = df.format(value);
        Log.d(TAG, String.format("applyCommaSeparator: input=%f, output %s", value, resString));
        return resString;
    }

}
