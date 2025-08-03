package com.appicacious.solword.adapters;

import static com.appicacious.solword.constants.Constants.NATIVE_AD_ID;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.appicacious.solword.R;
import com.appicacious.solword.interfaces.OnLongClickViewHolderListener;
import com.appicacious.solword.models.Word;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class GuessAdapter extends BaseListAdapter<Word, ViewHolder> {

    private final static int AD_INTERVAL = 20;
    private final static int GUESS_VIEW_TYPE = 10;
    public final static int AD_VIEW_TYPE = 11;

    private final static DiffUtil.ItemCallback<Word> DIFF = new DiffUtil.ItemCallback<Word>() {
        @Override
        public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
            return oldItem.get_id() == newItem.get_id();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    };

    private final OnAdapterInteractionListener listener;
    private final int wordSize;

    public GuessAdapter(@NonNull OnAdapterInteractionListener listener, int colCount) {
        super(DIFF);
        this.wordSize = colCount;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == GUESS_VIEW_TYPE) {
            View view = layoutInflater.inflate(R.layout.item_guess, parent, false);
            return new GuessViewHolder(view, wordSize, new OnLongClickViewHolderListener() {
                @Override
                public void onItemLongClicked(int position) {
                    listener.onWordLongClicked(getItemAt(position));
                }

                @Override
                public void onItemClicked(int position) {
                    listener.onWordClicked(getItemAt(position));
                }
            });
        } else {
            View view = layoutInflater.inflate(R.layout.item_guess_ad, parent, false);
            return new GuessAdViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (holder instanceof GuessViewHolder) {
            Word word = getItemAt(position);
            if (word != null) ((GuessViewHolder) holder).bindViews(word);
        } else if (holder instanceof GuessAdViewHolder) {
            ((GuessAdViewHolder) holder).loadAd();
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Ad will be shown after every 10 guesses
        if ((position) % (AD_INTERVAL + 1) == 0) {
            return AD_VIEW_TYPE;
        } else {
            return GUESS_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        int actualCount = super.getItemCount();
        if (actualCount == 0) return actualCount;
        int count = actualCount + (actualCount / (AD_INTERVAL)) + 1;
//        Log.d(TAG, String.format("getItemCount: actual=%d, count=%d", actualCount, count));
        return count;
//        return super.getItemCount();
    }

    public int getActualCount() {
        return super.getItemCount();
    }

    private Word getItemAt(int position) {
//        Log.d(TAG, "getItemAt: position=" + position);
        if (position == 0) return null;
        return getItem(position - (position / (AD_INTERVAL + 1)) - 1);
//        return getItem(position);
    }

    @Override
    public void notifyHolders() {

    }


    public interface OnAdapterInteractionListener {
        void onWordClicked(Word word);

        void onWordLongClicked(Word word);
    }

    private static class GuessAdViewHolder extends ViewHolder {
        private final MaxNativeAdLoader nativeAdLoader;
        private MaxAd nativeAd;

        public GuessAdViewHolder(@NonNull View itemView) {
            super(itemView);
            FrameLayout nativeAdContainer = itemView.findViewById(R.id.native_ad_layout);
            nativeAdLoader = new MaxNativeAdLoader(NATIVE_AD_ID, itemView.getContext());
            nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                @Override
                public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                    // Clean up any pre-existing native ad to prevent memory leaks.
                    if (nativeAd != null) nativeAdLoader.destroy(nativeAd);

                    // Save ad for cleanup.
                    nativeAd = ad;

                    // Add ad view to view.
                    nativeAdContainer.removeAllViews();
                    nativeAdContainer.addView(nativeAdView);
                }

                @Override
                public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                    // We recommend retrying with exponentially higher delays up to a maximum delay
                }

                @Override
                public void onNativeAdClicked(final MaxAd ad) {
                    // Optional click callback
                }
            });

        }

        public void loadAd() {
            nativeAdLoader.loadAd();
        }
    }

    public static class GuessViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView tvGuess;
        private final OnLongClickViewHolderListener vhListener;

        public GuessViewHolder(@NonNull View itemView, int wordSize,
                               @NonNull OnLongClickViewHolderListener vhListener) {
            super(itemView);
            this.vhListener = vhListener;
            tvGuess = itemView.findViewById(R.id.tv_guess);
            tvGuess.setOnClickListener(view -> this.vhListener.onItemClicked(
                    getBindingAdapterPosition()));
            tvGuess.setOnLongClickListener(view -> {
                vhListener.onItemLongClicked(getBindingAdapterPosition());
                return true;
            });
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    tvGuess,
                    11,
                    24,
                    1,
                    TypedValue.COMPLEX_UNIT_SP);
            tvGuess.setLetterSpacing(wordSize > 6 ? 0.075f : 0.15f);
        }


        public void bindViews(Word word) {
            tvGuess.setText(word.getWordTitle().toUpperCase());
        }
    }


}
