package com.appicacious.solword.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static com.appicacious.solword.constants.Constants.FIELD_IS_HIDDEN;
import static com.appicacious.solword.constants.Constants.NATIVE_AD_ID;
import static com.appicacious.solword.constants.Constants.NATIVE_AD_INTERVAL;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.appicacious.solword.R;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.interfaces.OnLongClickViewHolderListener;
import com.appicacious.solword.models.Word;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.fastscroll.PopupTextProvider;

// TODO: 04/09/2022 Cleanse this class
/**
 * Uses paged data from Paging3 library and shows native ad after a set
 * {@link Constants#NATIVE_AD_INTERVAL}.
 * First item is always an ad but if the list has no item, then nothing
 * is shown.
 */
public class GuessPagingDataAdapter extends BasePagingDataAdapter<Word,
        ViewHolder> implements PopupTextProvider {

    private static final String TAG = GuessPagingDataAdapter.class.getSimpleName();

    private final static int GUESS_VIEW_TYPE = 10;
    public final static int AD_VIEW_TYPE = 11;

    private boolean areHiddenCalculated;
    private final HashSet<Integer> vhHashCodes = new HashSet<>();
    private final HashSet<GuessViewHolder> guessViewHolders = new HashSet<>();
    private final LinkedHashMap<Integer, Boolean> hiddenPositions = new LinkedHashMap<>();

    private final static DiffUtil.ItemCallback<Word> DIFF = new DiffUtil.ItemCallback<Word>() {
        @Override
        public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
//            if (oldItem.getWordTitle().equalsIgnoreCase("merit") ||
//                    newItem.getWordTitle().equalsIgnoreCase("merit"))
            Log.d(TAG, "areItemsTheSame: old=" + oldItem + ", new=" + newItem);
            return oldItem.get_id() == newItem.get_id();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
//            if (oldItem.getWordTitle().equalsIgnoreCase("merit") ||
//                    newItem.getWordTitle().equalsIgnoreCase("merit"))
            Log.d(TAG, "areContentsTheSame: old=" + oldItem + ", new=" + newItem);
            return Objects.equals(oldItem.isHidden(), newItem.isHidden());
//            return areItemsTheSame(oldItem, newItem);
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull Word oldItem, @NonNull Word newItem) {
            boolean isHiddenChanged = !Objects.equals(oldItem.isHidden(), newItem.isHidden());
            if (isHiddenChanged) {
                List<String> payloads = new ArrayList<>();
                payloads.add(FIELD_IS_HIDDEN);
                return payloads;
            } else {
                return super.getChangePayload(oldItem, newItem);
            }
        }
    };

    private final OnAdapterInteractionListener listener;
    private final int wordSize;

    public GuessPagingDataAdapter(@NonNull OnAdapterInteractionListener listener, int colCount) {
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
            GuessViewHolder gvh = new GuessViewHolder(view, wordSize, new GuessViewHolder.OnViewHolderListener() {
                @Override
                public void onItemLongClicked(int position/*, boolean isHidden*/) {

//                    if (isWordHidden(position)) {
//                        listener.removeAds();
//                    } else {
//                        listener.onWordLongClicked(getItemAt(position));
//                    }


                    Word word = getItemAt(position);
                    if (word != null) {
                        if (!isPurchased() && word.isHidden()) {
                            listener.removeAds();
                        } else {
                            listener.onWordLongClicked(word);
                        }
                    }
                }

                @Override
                public void onItemClicked(int position/*, boolean isHidden*/) {
//                    if (isWordHidden(position)) {
//                        listener.removeAds();
//                    } else {
//                        listener.onWordClicked(getItemAt(position));
//                    }


                    Word word = getItemAt(position);
                    if (word != null) {
                        if (!isPurchased() && word.isHidden()) {
                            listener.removeAds();
                        } else {
                            listener.onWordClicked(word);
                        }
                    }
                }

                @Override
                public boolean isPurchased() {
                    return listener.isPurchased();
                }

//                @Override
//                public boolean isItemHidden(int position) {
//                    return isWordHidden(position);
//                }
//
//                @Override
//                public void setPositionHidden(int position) {
//                    hiddenPositions.put(position, true);
//                }
            });
            Log.d(TAG, "onCreateViewHolder: hashCode=" + gvh.hashCode());
//            vhHashCodes.add(gvh.hashCode());
//            guessViewHolders.add(gvh);
            return gvh;
        } else {
            View view = layoutInflater.inflate(R.layout.item_guess_ad, parent, false);
            return new GuessAdViewHolder(view);
        }
    }

    private boolean isWordHidden(int position) {
        Log.d(TAG, "isWordHidden: called for pos=" + position);
        return hiddenPositions.containsKey(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        Log.d(TAG, "onBindViewHolder: called for pos=" + position);
        if (holder instanceof GuessViewHolder) {
            Word word = getItemAt(position);
            Log.d(TAG, String.format("onBindViewHolder: word=%s pos=%d, hashCode=%d", word, position, holder.hashCode()));
            if (word != null) {
//                if (!listener.isPurchased() && !vhHashCodes.contains(((GuessViewHolder) holder).hashCode())
//                        && new Random().nextBoolean()) {
//                    Log.d(TAG, "onBindViewHolder: hiddenPos=" + position);
//                    vhHashCodes.add(((GuessViewHolder) holder).hashCode());
//                    hiddenPositions.put(position, true);
//                }
                ((GuessViewHolder) holder).bindViews(word);
            }
        } else if (holder instanceof GuessAdViewHolder) {
            Log.d(TAG, "onBindViewHolder: called for adView at pos=" + position);
            ((GuessAdViewHolder) holder).loadAd();
        }
    }

    @Override
    public int getItemViewType(int position) {
//        Log.d(TAG, "getItemViewType: position=" + position);
        if (listener.isPurchased()) {
            return GUESS_VIEW_TYPE;
        } else {
            // Ad will be shown after every 30 guesses. 1 is added to interval because, first position
            // is for an Ad
            if ((position) % (NATIVE_AD_INTERVAL + 1) == 0) {
                return AD_VIEW_TYPE;
            } else {
                return GUESS_VIEW_TYPE;
            }
        }
//        return GUESS_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        if (listener.isPurchased()) {
            return super.getItemCount();
        } else {
            int actualCount = super.getItemCount();
            if (actualCount == 0) return actualCount;
            int count = actualCount + (actualCount / (NATIVE_AD_INTERVAL)) + 1;

//            if (!areHiddenCalculated) {
//                areHiddenCalculated = true;
//                for (int x = 1; x <= count; x++) {
//                    int pos = x - 1;
//                    if ((pos) % (NATIVE_AD_INTERVAL + 1) != 0) {
//                        if (new Random().nextBoolean()) hiddenPositions.put(pos, true);
//                    }
//                }
//            }

//        Log.d(TAG, String.format("getItemCount: actual=%d, count=%d", actualCount, count));
            return count;
        }
//        return super.getItemCount();
    }

    public int getActualCount() {
        return super.getItemCount();
    }

    private Word getItemAt(int position) {
        Log.d(TAG, "getItemAt: position=" + position);
        if (listener.isPurchased()) {
            return getItem(position);
        } else {
            if (position == 0) return null;
            try {
                int index = position - ((int) (position / (NATIVE_AD_INTERVAL + 1))) - 1;
                Log.d(TAG, "getItemAt: list index=" + index);
                return getItem(index);
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "getItemAt: error=" + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
//        return getItem(position);
    }

    /**
     * Study the logic of code by looking at the comments given where this method is actually used.
     */
    @Override
    public void notifyHolders() {
        int lastPosition = getItemCount() - 1;
        Log.d(TAG, "notifyHolders: holders" + getmHolders() + ", lastPos=" + lastPosition);
        for (ViewHolder holder : getmHolders()) {

            // If this method is called when app is purchased, it will remove the visible ad views.
            if (listener.isPurchased() && holder instanceof GuessAdViewHolder) {
                ((GuessAdViewHolder) holder).destroyAd();
            }

            // Whenever this method is called, it will rerun bindViews() on GuessViewHolder
            if (holder instanceof GuessViewHolder) {
                int position = holder.getBindingAdapterPosition();
                Log.d(TAG, "notifyHolders: holder=" + holder + ", pos=" + position);

                // The point where we are calling this in GuessFragment,
                // onViewRecycled() is not yet called. So, we will still
                // have recyclable view holders in this list and we don't
                // want to run bindViews() on them.
                // In order to cater that, we apply the position > NO_ID
                // check because recyclable holders have a position of NO_ID
                if (position > NO_ID && position <= lastPosition) {
                    Word word = getItemAt(holder.getBindingAdapterPosition());
                    Log.d(TAG, "notifyHolders: word=" + word);
                    if (word != null) ((GuessViewHolder) holder).bindViews(word);
                }
            }

//            if (!listener.isPurchased()) {
//                if (holder instanceof GuessViewHolder) {
//                    int position = holder.getBindingAdapterPosition();
//                    Log.d(TAG, "notifyHolders: holder=" + holder + ", pos=" + position);
//
//                    // The point where we are calling this in GuessFragment,
//                    // onViewRecycled() is not yet called. So, we will still
//                    // have recyclable view holders in this list and we don't
//                    // want to run bindViews() on them.
//                    // In order to cater that, we apply the position > NO_ID
//                    // check because recyclable holders have a position of NO_ID
//                    if (position > NO_ID && position <= lastPosition) {
//                        Word word = getItemAt(holder.getBindingAdapterPosition());
//                        Log.d(TAG, "notifyHolders: word=" + word);
//                        if (word != null) ((GuessViewHolder) holder).bindViews(word);
//                    }
//                }
//            } else {
//                // This code will remove the visible ad views.
//                if (holder instanceof GuessAdViewHolder) ((GuessAdViewHolder) holder).destroyAd();
//
//            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Log.d(TAG, "onViewRecycled: called for=" + holder);
        super.onViewRecycled(holder);
    }

    @NonNull
    @Override
    public String getPopupText(int position) {
        Word word = getItemAt(position);
        return word == null ? "" : String.valueOf(word.getWordTitle().charAt(0)).toUpperCase();
    }


    public interface OnAdapterInteractionListener {
        void removeAds();

        boolean isPurchased();

        void onWordClicked(Word word);

        void onWordLongClicked(Word word);
    }

    private static class GuessAdViewHolder extends ViewHolder {
        private final MaxNativeAdLoader nativeAdLoader;
        private final FrameLayout nativeAdContainer;
        private MaxAd nativeAd;

        public GuessAdViewHolder(@NonNull View itemView) {
            super(itemView);
            nativeAdContainer = itemView.findViewById(R.id.native_ad_layout);
            nativeAdLoader = new MaxNativeAdLoader(NATIVE_AD_ID, itemView.getContext());
            nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                @Override
                public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                    // Clean up any pre-existing native ad to prevent memory leaks.
                    destroyAd();

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

        public void destroyAd() {
            if (nativeAd != null) nativeAdLoader.destroy(nativeAd);
            nativeAdContainer.removeAllViews();
        }
    }

    public static class GuessViewHolder extends ViewHolder {

        private final MaterialTextView tvGuess;
        private final OnViewHolderListener vhListener;
        private boolean isHidden;

        public GuessViewHolder(@NonNull View itemView, int wordSize,
                               @NonNull OnViewHolderListener vhListener) {
            super(itemView);
            this.vhListener = vhListener;
            tvGuess = itemView.findViewById(R.id.tv_guess);
            tvGuess.setOnClickListener(view -> GuessViewHolder.this.vhListener.onItemClicked(
                    getBindingAdapterPosition()/*, this.isHidden*/));
            tvGuess.setOnLongClickListener(view -> {
                vhListener.onItemLongClicked(getBindingAdapterPosition()/*, this.isHidden*/);
                return true;
            });
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    tvGuess,
                    10,
                    24,
                    1,
                    TypedValue.COMPLEX_UNIT_SP);
//            tvGuess.setLetterSpacing(wordSize > 6 ? 0.075f : 0.15f);

//            if (!vhListener.isPurchased() && new Random().nextBoolean()) {
//                Log.d(TAG, "GuessViewHolder: hiddenPos=" + getBindingAdapterPosition());
//                vhListener.setPositionHidden(getBindingAdapterPosition());
//            }
        }


        public void bindViews(Word word) {
            Log.d(TAG, "bindViews: called for pos=" + getBindingAdapterPosition());
            String title;
            if (vhListener.isPurchased()) {
                title = word.getWordTitle().toUpperCase();
            } else {
                if (word.isHidden()) {
                    int size = word.getWordTitle().length();
                    title = new String(new char[size]).replace("\0", "*");
                } else {
                    title = word.getWordTitle().toUpperCase();
                }

//                if (vhListener.isItemHidden(getBindingAdapterPosition())) {
//                    int size = word.getWordTitle().length();
//                    title = new String(new char[size]).replace("\0", "*");
//                } else {
//                    title = word.getWordTitle().toUpperCase();
//                }

//                boolean shouldHide = new Random().nextBoolean();
//                if (shouldHide) {
//                    int size = word.getWordTitle().length();
//                    title = new String(new char[size]).replace("\0", "*");
//                } else {
//                    title = word.getWordTitle().toUpperCase();
//                }
            }
            tvGuess.setText(title);
        }

//        @NonNull
//        public String getDisplayedText() {
//            if (vhListener.isPurchased()) {
//                return "";
//            } else {
//                return !TextUtils.isEmpty(tvGuess.getText()) ? tvGuess.getText().toString() : "";
//            }
//        }

//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (!(o instanceof GuessViewHolder)) return false;
//            GuessViewHolder that = (GuessViewHolder) o;
//            return that.hashCode() == o.hashCode();
//        }

//        @Override
//        public int hashCode() {
//            return Objects.hash(itemView);
//        }

        public interface OnViewHolderListener extends OnLongClickViewHolderListener {

            void onItemClicked(int position/*, boolean isHidden*/);

            void onItemLongClicked(int position/*, boolean isHidden*/);

            boolean isPurchased();

//            boolean isItemHidden(int position);
//
//            void setPositionHidden(int position);


        }
    }
}
