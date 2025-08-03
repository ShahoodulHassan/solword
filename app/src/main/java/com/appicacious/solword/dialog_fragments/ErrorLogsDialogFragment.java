package com.appicacious.solword.dialog_fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.appicacious.solword.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

public class ErrorLogsDialogFragment extends BaseDialogFragment {

    private static final String TAG = ErrorLogsDialogFragment.class.getSimpleName();
    private static final String ARG_ERROR_LOG = "arg_error_log";

    private MaterialTextView tvErrorLog;
    private String errorLog;

    public static ErrorLogsDialogFragment newInstance(int reqCode, String errorLog) {
        Bundle args = new Bundle();
        ErrorLogsDialogFragment fragment = new ErrorLogsDialogFragment();
        args.putInt(ARG_REQUEST_CODE, reqCode);
        args.putString(ARG_ERROR_LOG, errorLog);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mArguments != null) errorLog = mArguments.getString(ARG_ERROR_LOG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: called");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mActivity, R.style.AlertDialogTheme);
        View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_error_logs,
                null);
        tvErrorLog = view.findViewById(R.id.tv_error_log);
        builder.setView(view)
                .setTitle(R.string.dialog_title_errors)
                .setPositiveButton(R.string.button_title_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        AlertDialog dialog = builder.create();
//        setCancelable(false);
//        if (dialog.getWindow() != null) {
//            dialog.getWindow().getAttributes().windowAnimations = R.style.RegularDialogAnimation;
//        }

        dialog.setOnShowListener(this);
        dialog.create();

        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        Log.d(TAG, "onShow: called");
        super.onShow(dialog);
        tvErrorLog.setText(errorLog);
    }

}
