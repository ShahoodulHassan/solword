package com.appicacious.solword.dialog_fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appicacious.solword.interfaces.OnDialogInteractionListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BooleanDialogFragment extends BaseDialogFragment {
    private static final String TAG = "BooleanDialogFragment";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";

    private static final String ARG_CANCELABLE = "arg_cancelable";
    private static final String ARG_POSITIVE_TEXT = "arg_positive_text";
    private static final String ARG_NEGATIVE_TEXT = "arg_negative_text";
    private static final String ARG_NEUTRAL_TEXT = "arg_neutral_text";
    private static final String ARG_NEGATIVE_REQUIRED = "arg_negative_required";
    private static final String ARG_NEUTRAL_REQUIRED = "arg_neutral_required";
    private static final String ARG_TYPEFACE_PATH = "arg_typeface";
    private static final String ARG_APPLY_SPANS_ON_TITLE = "arg_apply_spans_on_title";
    private static final String ARG_APPLY_SPANS_ON_MSG = "arg_apply_spans_on_message";

    private OnDialogInteractionListener mListener;
    private String /*mTitle, mMessage, */mPositiveText, mNegativeText, mNeutralText, mTypefacePath;
    private boolean mIsNegativeRequired, mIsNeutralRequired, mIsCancelable, mApplySpansOnTitle,
            mApplySpansOnMessage;
    private CharSequence mTitle, mMessage;
//    private Typeface mTypeface;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);

        if (mArguments != null) {
            mTitle = mArguments.getString(ARG_TITLE);
            mMessage = mArguments.getCharSequence(ARG_MESSAGE);
            mPositiveText = mArguments.getString(ARG_POSITIVE_TEXT);
            mNegativeText = mArguments.getString(ARG_NEGATIVE_TEXT);
            mNeutralText = mArguments.getString(ARG_NEUTRAL_TEXT);
            mIsNegativeRequired = mArguments.getBoolean(ARG_NEGATIVE_REQUIRED);
            mIsNeutralRequired = mArguments.getBoolean(ARG_NEUTRAL_REQUIRED);
            mIsCancelable = mArguments.getBoolean(ARG_CANCELABLE);
            mApplySpansOnTitle = mArguments.getBoolean(ARG_APPLY_SPANS_ON_TITLE);
            mApplySpansOnMessage = mArguments.getBoolean(ARG_APPLY_SPANS_ON_MSG);

//            mRequestCode = mArguments.getInt(ARG_REQUEST_CODE);

            mTypefacePath = mArguments.getString(ARG_TYPEFACE_PATH);

        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: called");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mActivity);

        String positive = mPositiveText == null ? "Yes" : mPositiveText;
        String negative = mNegativeText == null ? "No" : mNegativeText;
        String neutral = mNeutralText == null ? "Cancel" : mNeutralText;

        // We don't need to check for null values of button texts here. It is just a safe play!
        builder.setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Log.d(TAG, "onClick: ");
                        if (mListener != null) mListener.onPositivePressed(mRequestCode);

                    }
                });
        if (mIsNegativeRequired) {
            builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (mListener != null) mListener.onNegativePressed(mRequestCode);
//                    dismiss();
                }
            });
        }
        if (mIsNeutralRequired) {
            builder.setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
        }

        Dialog dialog = builder.create();

        setCancelable(mIsCancelable);

//        if (dialog.getWindow() != null) {
//            dialog.getWindow().getAttributes().windowAnimations = R.style.RegularDialogAnimation;
//        }

        dialog.setOnShowListener(this);

        dialog.create();

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        mListener = null;
        super.onDismiss(dialog);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        Log.d(TAG, "onShow: called");
        super.onShow(dialog);
        if (mListener != null) mListener.onDialogShown(mRequestCode);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();

//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            Typeface mTypeface = Typeface.createFromAsset(mActivity.getApplicationContext().getAssets(),
//                    Constants.BASE_FONT_PATH);
//            ((TextView) dialog.findViewById(android.R.id.message))
//                    .setTypeface(mTypeface, Typeface.BOLD);
//        }
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof OnDialogInteractionListener) {
            mListener = (OnDialogInteractionListener) getParentFragment();
        } else {
            // Parent fragment may be null, which means it's an activity
            if (getParentFragment() == null) {
                if (context instanceof OnDialogInteractionListener) {
                    mListener = (OnDialogInteractionListener) context;
                } else {
                    throw new RuntimeException("OnDialogInteractionListener not found!");
                }
            } else {
                throw new RuntimeException("OnDialogInteractionListener not found!");
            }
        }
    }

//    public interface OnDialogInteractionListener {
//
//        void onDialogShown(int reqCode);
//
//        void onPositivePressed(int reqCode);
//
//        void onNegativePressed(int reqCode);
//    }

    public static class Builder {
        private boolean isNegativeButtonRequired, isNeutralButtonRequired, isCancelable,
                applySpansOnTitle, applySpansOnMessage;
        private String title, /*message, */positiveButtonText, negativeButtonText, neutralButtonText,
                typefacePath;
        private CharSequence message;
        private int requestCode;
//        private BaseFragment targetFragment;

        public BooleanDialogFragment build() {
            Bundle args = new Bundle();
            BooleanDialogFragment fragment = new BooleanDialogFragment();
            args.putString(ARG_TITLE, getTitle());
            args.putCharSequence(ARG_MESSAGE, getMessage());
            args.putString(ARG_POSITIVE_TEXT, getPositiveButtonText());
            args.putString(ARG_NEGATIVE_TEXT, getNegativeButtonText());
            args.putString(ARG_NEUTRAL_TEXT, getNeutralButtonText());
            args.putString(ARG_TYPEFACE_PATH, getTypefacePath());
            args.putBoolean(ARG_NEGATIVE_REQUIRED, isNegativeButtonRequired());
            args.putBoolean(ARG_NEUTRAL_REQUIRED, isNeutralButtonRequired());
            args.putBoolean(ARG_CANCELABLE, isCancelable());
            args.putBoolean(ARG_APPLY_SPANS_ON_TITLE, isApplySpansOnTitle());
            args.putBoolean(ARG_APPLY_SPANS_ON_MSG, isApplySpansOnMessage());
            args.putInt(ARG_REQUEST_CODE, getRequestCode());
            fragment.setArguments(args);
//            fragment.setTargetFragment(getTargetFragment(), getRequestCode());
            return fragment;
        }

        // Request code is compulsory
        public Builder(int reqCode) {
            // Set default values, where required
//            typefacePath = Constants.BASE_FONT_PATH;
            isNegativeButtonRequired = true;
            isNeutralButtonRequired = false;
            isCancelable = true;
//            applySpansOnTitle = true; // False means that title will come with custom spans
//            applySpansOnMessage = true; // False means that message will come with custom spans
            requestCode = reqCode;
//            this.targetFragment = targetFragment;
        }

        private String getTitle() {
            return title == null ? title = "" : title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        private CharSequence getMessage() {
            return message;
        }

        public Builder setMessage(CharSequence message) {
            this.message = message;
            return this;
        }

//        public Builder setMessage(String message) {
//            this.message = message;
//            return this;
//        }

        public Builder setNegativeButtonRequired(boolean negReq) {
            isNegativeButtonRequired = negReq;
            return this;
        }

        public Builder setNeutralButtonRequired(boolean neutReq) {
            isNeutralButtonRequired = neutReq;
            return this;
        }

        public Builder setPositiveButtonText(String posText) {
            positiveButtonText = posText;
            return this;
        }

        public Builder setNegativeButtonText(String negText) {
            negativeButtonText = negText;
            return this;
        }

        public Builder setNeutralButtonText(String neutText) {
            if (!TextUtils.isEmpty(neutText) && !isNeutralButtonRequired)
                isNeutralButtonRequired = true;
            neutralButtonText = neutText;
            return this;
        }

        private String getTypefacePath() {
            return typefacePath;
        }

        public Builder setTypefacePath(String typefacePath) {
            this.typefacePath = typefacePath;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            isCancelable = cancelable;
            return this;
        }

        public Builder setApplySpansOnTitle(boolean applySpansOnTitle) {
            this.applySpansOnTitle = applySpansOnTitle;
            return this;
        }

        public Builder setApplySpansOnMessage(boolean applySpansOnMessage) {
            this.applySpansOnMessage = applySpansOnMessage;
            return this;
        }

        private boolean isApplySpansOnTitle() {
            return applySpansOnTitle;
        }

        private boolean isApplySpansOnMessage() {
            return applySpansOnMessage;
        }

        private boolean isCancelable() {
            return isCancelable;
        }

        private boolean isNegativeButtonRequired() {
            return isNegativeButtonRequired;
        }

        private boolean isNeutralButtonRequired() {
            return isNeutralButtonRequired;
        }

        private String getPositiveButtonText() {
            if (!isNegativeButtonRequired && !isNeutralButtonRequired)
                return positiveButtonText == null ? "Ok" : positiveButtonText;
            return positiveButtonText == null ? "Yes" : positiveButtonText;
        }

        private String getNegativeButtonText() {
            return negativeButtonText == null ? "No" : negativeButtonText;
        }

        private String getNeutralButtonText() {
            return neutralButtonText == null ? "Cancel" : neutralButtonText;
        }

        private int getRequestCode() {
            return requestCode;
        }
//
//        private BaseFragment getTargetFragment() {
//            return targetFragment;
//        }

    }
}
