package com.appicacious.solword.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.R;
import com.appicacious.solword.interfaces.OnClickViewHolderListener;
import com.appicacious.solword.models.Dictionary;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class DictionaryAdapter extends BaseListAdapter<Dictionary, DictionaryAdapter.DictionaryViewHolder>
         {

    private final static DiffUtil.ItemCallback<Dictionary> DIFF = new DiffUtil.ItemCallback<Dictionary>() {
        @Override
        public boolean areItemsTheSame(@NonNull Dictionary oldItem, @NonNull Dictionary newItem) {
            return oldItem.get_id() == newItem.get_id();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Dictionary oldItem, @NonNull Dictionary newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    };

    private final OnAdapterInteractionListener listener;
    private final DictionaryViewHolder.OnDictionaryViewHolderListener vhListener;

    public DictionaryAdapter(@NonNull OnAdapterInteractionListener listener, int defaultDictId) {
        super(DIFF);
        this.listener = listener;
        vhListener = new DictionaryViewHolder.OnDictionaryViewHolderListener() {
            @Override
            public int getDefaultDictId() {
                return defaultDictId;
            }

            @Override
            public void onItemClicked(int position) {
                listener.onDictionaryClicked(getItem(position));
            }
        };
    }

    @NonNull
    @Override
    public DictionaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dictionary, parent,
                false);
        return new DictionaryViewHolder(view, vhListener);

    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        Dictionary dictionary = getItem(position);
        holder.bindViews(dictionary);
    }

    @Override
    public void notifyHolders() {

    }

    public interface OnAdapterInteractionListener {
        void onDictionaryClicked(Dictionary dictionary);
    }

    public static class DictionaryViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView tvName;
        private final OnDictionaryViewHolderListener vhListener;

        public DictionaryViewHolder(@NonNull View itemView,
                                    @NonNull OnDictionaryViewHolderListener vhListener) {
            super(itemView);
            this.vhListener = vhListener;
            tvName = itemView.findViewById(R.id.tv_dictionary_name);
            tvName.setOnClickListener(view -> this.vhListener.onItemClicked(
                    getBindingAdapterPosition()));
        }


        public void bindViews(Dictionary dictionary) {
            tvName.setText(dictionary.getDictionaryName());
            if (dictionary.get_id() == vhListener.getDefaultDictId()) {
               tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                       R.drawable.ic_baseline_done_24, 0);
            } else {
                tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }

        interface OnDictionaryViewHolderListener extends OnClickViewHolderListener {
            int getDefaultDictId();
        }
    }
}
