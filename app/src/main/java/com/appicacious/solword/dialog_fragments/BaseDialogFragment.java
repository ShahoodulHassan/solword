package com.appicacious.solword.dialog_fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;


/**
 * Always use getChildFragmentManager() from the ParentFragment in order to initialize this dialog
 * fragment
 */
public class BaseDialogFragment extends AppCompatDialogFragment implements View.OnClickListener,
        DialogInterface.OnShowListener {
    private static final String TAG = BaseDialogFragment.class.getSimpleName();
    AppCompatActivity mActivity;
    Bundle mArguments;
    int mRequestCode;
//    private boolean isNewlyCreated;


    static final String ARG_REQUEST_CODE = "arg_request_code";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        isNewlyCreated = true;
        mArguments = getArguments();
        if (mArguments != null) mRequestCode = mArguments.getInt(ARG_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Log.d(TAG, "onDismiss: called");
        super.onDismiss(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: called");
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity) {
            mActivity = (AppCompatActivity) context;
        } else {
            throw new RuntimeException("Activity is not AppCompatActivity!");
        }

    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: called");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: called");
//        isNewlyCreated = false;
        super.onDestroyView();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        // No-op
    }
}
