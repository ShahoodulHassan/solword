package com.appicacious.solword.dialog_fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.R;
import com.appicacious.solword.adapters.DictionaryAdapter;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.models.Dictionary;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class DictionarySelectDialogFragment extends BottomSheetDialogFragment implements
        DictionaryAdapter.OnAdapterInteractionListener {

    private static final String TAG = DictionarySelectDialogFragment.class.getSimpleName();
    private static final String KEY_REQ_CODE = "key_req_code";
    private static final String KEY_DICTS = "key_dicts";

    private OnBottomSheetInteractionListener mListener;
//    private AppCompatActivity mActivity;
    private ArrayList<Dictionary> dictionaries;

//    private int mReqCode;


    public static DictionarySelectDialogFragment newInstance(int reqCode,
                                                             ArrayList<Dictionary> dictionaries) {
        Bundle args = new Bundle();
        DictionarySelectDialogFragment fragment = new DictionarySelectDialogFragment();
        args.putInt(KEY_REQ_CODE, reqCode);
        args.putParcelableArrayList(KEY_DICTS, dictionaries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_BottomSheetDialog);
        if (getArguments() != null) {
//            mReqCode = getArguments().getInt(KEY_REQ_CODE);
            dictionaries = getArguments().getParcelableArrayList(KEY_DICTS);
        }

        setShowsDialog(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_dictionary_select, container,
                false);
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        RecyclerView rvDictionaries = view.findViewById(R.id.rv_dictionaries);
        rvDictionaries.setLayoutManager(new LinearLayoutManager(rvDictionaries.getContext()));
        DictionaryAdapter mAdapter;
        Dictionary defaultDict = mListener.getDefaultDictionary();
        rvDictionaries.setAdapter(mAdapter = new DictionaryAdapter(this,
                (defaultDict == null ? Constants.DEF_DICT_ID : defaultDict.get_id())));
        mAdapter.submitList(dictionaries);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity) {
//            mActivity = (AppCompatActivity) context;
            Fragment fragment = getParentFragment();
            if (fragment instanceof OnBottomSheetInteractionListener) {
                mListener = (OnBottomSheetInteractionListener) fragment;
            } else {
                throw new RuntimeException("Fragment should implement BottomSheetInteractionListener!");
            }
        } else {
            throw new RuntimeException("Activity is not AppCompatActivity!");
        }
    }

    @Override
    public void onDictionaryClicked(Dictionary dictionary) {
        mListener.onDictionaryClicked(dictionary);
        dismiss();
    }



    public interface OnBottomSheetInteractionListener {

        Dictionary getDefaultDictionary();

        void onDictionaryClicked(Dictionary dictionary);
    }
}
