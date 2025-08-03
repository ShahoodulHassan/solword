package com.appicacious.solword.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import com.appicacious.solword.R;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.utilities.SpanUtils;


public class MyListPreference extends ListPreference {
    private static final String TAG = "MyListPreference";
    private final Context mContext;

    public MyListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public MyListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MyListPreference(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void setDialogTitle(CharSequence dialogTitle) {
        Log.d(TAG, "setDialogTitle: called");
        super.setDialogTitle(getSpan(dialogTitle, true));
    }

    @Override
    public void setDialogMessage(CharSequence dialogMessage) {
        Log.d(TAG, "setDialogMessage: called");
        super.setDialogMessage(getSpan(dialogMessage, false));
    }

    @Override
    public void setPositiveButtonText(CharSequence positiveButtonText) {
        super.setPositiveButtonText(getSpan(positiveButtonText, true));
    }

    @Override
    public void setNegativeButtonText(CharSequence negativeButtonText) {
        super.setNegativeButtonText(getSpan(negativeButtonText, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), Constants.BASE_FONT_PATH);
        titleView.setTypeface(typeface, Typeface.BOLD);
        titleView.setTextColor(mContext.getResources().getColor(R.color.colorText));
        summaryView.setTypeface(typeface);
        summaryView.setTextColor(mContext.getResources().getColor(R.color.colorText));
    }

    private Spannable getSpan(CharSequence text, boolean isBold) {
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), Constants.BASE_FONT_PATH);
        Spannable spannable = SpanUtils.getTypefaceSpan(text, typeface);
        if (isBold) spannable = SpanUtils.getBoldSpan(spannable);
        return spannable;
    }
}
