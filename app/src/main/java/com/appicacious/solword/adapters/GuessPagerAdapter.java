package com.appicacious.solword.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.R;
import com.appicacious.solword.interfaces.OnLongClickViewHolderListener;
import com.appicacious.solword.models.Word;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class GuessPagerAdapter extends BasePagedListAdapter<Word, GuessPagerAdapter.GuessViewHolder> {

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

    public GuessPagerAdapter(@NonNull OnAdapterInteractionListener listener, int colCount) {
        super(DIFF);
        this.wordSize = colCount;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GuessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guess, parent,
                false);
        return new GuessViewHolder(view, wordSize, new OnLongClickViewHolderListener() {
            @Override
            public void onItemLongClicked(int position) {
                listener.onWordLongClicked(getItem(position));
            }

            @Override
            public void onItemClicked(int position) {
                listener.onWordClicked(getItem(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull GuessViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        Word word = getItem(position);
        if (word != null) holder.bindViews(word);
    }

    @Override
    public void notifyHolders() {

    }


    public interface OnAdapterInteractionListener {
        void onWordClicked(Word word);
        void onWordLongClicked(Word word);
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
