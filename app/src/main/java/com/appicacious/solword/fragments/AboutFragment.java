package com.appicacious.solword.fragments;

import static com.appicacious.solword.constants.Constants.URL_PRIVACY;
import static com.appicacious.solword.constants.Constants.URL_TERMS;
import static com.appicacious.solword.constants.Constants.URL_TWITTER;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appicacious.solword.BuildConfig;
import com.appicacious.solword.R;
import com.google.android.material.button.MaterialButton;

import de.psdev.licensesdialog.LicensesDialogFragment;

public class AboutFragment extends BaseFragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        initializeViews(view);

        return view;
    }

    @Override
    void initializeViews(View view) {
        super.initializeViews(view);

        TextView tvAppVersion = view.findViewById(R.id.tv_about_app_version);
        MaterialButton btPrivacy = view.findViewById(R.id.bt_about_privacy);
        MaterialButton btTerms = view.findViewById(R.id.bt_about_terms);
        MaterialButton btLicenses = view.findViewById(R.id.bt_about_licenses);
        tvAppVersion.setText(BuildConfig.VERSION_NAME);

        TextView tvTwitter = view.findViewById(R.id.tv_twitter);
        TextView tvAttributions = view.findViewById(R.id.tv_attributions);
        tvTwitter.setOnClickListener(this);
        tvAttributions.setMovementMethod(LinkMovementMethod.getInstance());

        btPrivacy.setOnClickListener(this);
        btTerms.setOnClickListener(this);
        btLicenses.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.tv_twitter) {
            initTwitter();
        } else if (id == R.id.bt_about_privacy) {
            openCustomTab(URL_PRIVACY);
        } else if (id == R.id.bt_about_terms) {
            openCustomTab(URL_TERMS);
        } else if (id == R.id.bt_about_licenses) {
            showLicensesDialog();
        }
    }

    private void initTwitter() {
        Intent intent;
        // no Twitter app, revert to browser ????
        Uri uri = Uri.parse(URL_TWITTER);
        intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            this.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            serveFreshToast("Twitter error!");
            Log.d(TAG, "initTwitter: Twitter couldn't be started!");
        }
    }

    private void showLicensesDialog() {
        LicensesDialogFragment fragment = new LicensesDialogFragment.Builder(mActivity)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true)
                .build();
        fragment.show(getChildFragmentManager(), null);
    }
}
