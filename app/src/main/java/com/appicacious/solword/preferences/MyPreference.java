package com.appicacious.solword.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.appicacious.solword.R;
import com.appicacious.solword.constants.Constants;

public class MyPreference extends Preference {

    private final Context mContext;

    public MyPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public MyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MyPreference(Context context) {
        super(context);
        mContext = context;
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
}
