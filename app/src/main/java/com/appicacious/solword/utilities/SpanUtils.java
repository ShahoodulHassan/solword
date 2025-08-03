package com.appicacious.solword.utilities;

import android.graphics.Typeface;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;

import androidx.annotation.NonNull;


public class SpanUtils {

    private static final int SPAN_FLAG = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

    public static Spannable getStrikeThroughSpan(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StrikethroughSpan span = new StrikethroughSpan();
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getBoldSpan(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StyleSpan span = new StyleSpan(Typeface.BOLD);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getItalicSpan(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StyleSpan span = new StyleSpan(Typeface.ITALIC);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getBoldItalicSpan(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        StyleSpan span = new StyleSpan(Typeface.BOLD_ITALIC);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getTypefaceSpan(CharSequence text, Typeface typeface) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        CustomTypefaceSpan span = new CustomTypefaceSpan("", typeface);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getBulletSpan(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        BulletSpan span = new BulletSpan(20);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getBulletSpan(CharSequence text, int gap) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        BulletSpan span = new BulletSpan(Math.max(gap, 20));
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getClickableSpan(String text, final Runnable executeOnClick) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(text);
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Selection.setSelection(builder, 0);
                executeOnClick.run();
            }
        };
        builder.setSpan(span,0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getTextColorSpan(CharSequence text, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }

    public static Spannable getTextSizeSpan(CharSequence text, float relativeSize) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        RelativeSizeSpan span = new RelativeSizeSpan(relativeSize);
        builder.setSpan(span, 0, builder.length(), SPAN_FLAG);
        return builder;
    }


}
