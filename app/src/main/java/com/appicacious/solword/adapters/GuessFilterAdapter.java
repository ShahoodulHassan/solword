package com.appicacious.solword.adapters;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.R;
import com.appicacious.solword.interfaces.OnClickViewHolderListener;
import com.appicacious.solword.models.GuessFilter;
import com.appicacious.solword.utilities.Utilities;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

public class GuessFilterAdapter extends BaseListAdapter<GuessFilter,
        GuessFilterAdapter.GuessFilterViewHolder> {

    private static final DiffUtil.ItemCallback<GuessFilter> DIFF = new DiffUtil.ItemCallback<GuessFilter>() {
        @Override
        public boolean areItemsTheSame(@NonNull GuessFilter oldItem, @NonNull GuessFilter newItem) {
            return Objects.equals(newItem.getTitle(), oldItem.getTitle());
        }

        @Override
        public boolean areContentsTheSame(@NonNull GuessFilter oldItem, @NonNull GuessFilter newItem) {
            return newItem.areContentsTheSame(oldItem);
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull GuessFilter oldItem, @NonNull GuessFilter newItem) {
            return super.getChangePayload(oldItem, newItem);
        }
    };

    private final OnAdapterInteractionListener mListener;

    public GuessFilterAdapter(OnAdapterInteractionListener listener) {
        super(DIFF);
        mListener = listener;
    }

    @NonNull
    @Override
    public GuessFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guess_filter,
                parent, false);
        return new GuessFilterViewHolder(view, new GuessFilterViewHolder.OnViewHolderListener() {
            @Override
            public boolean isGuessFilterSelected(String title) {
                return mListener.isGuessFilterSelected(title);
            }

            @Override
            public void onItemClicked(int position) {
                mListener.onGuessFilterClicked(getItem(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull GuessFilterViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        holder.bindViews(getItem(position));
    }

    @Override
    public void notifyHolders() {
        for (RecyclerView.ViewHolder viewHolder : getmHolders()) {
            if (viewHolder instanceof GuessFilterViewHolder) {
                ((GuessFilterViewHolder) viewHolder).setSelected(getItem(viewHolder.getBindingAdapterPosition()));
            }
        }
    }

    public interface OnAdapterInteractionListener {
        void onGuessFilterClicked(GuessFilter guessFilter);
        boolean isGuessFilterSelected(String title);
    }

    static class GuessFilterViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout flMainContainer;
        private final MaterialTextView tvTitle;

        private final OnViewHolderListener vhListener;

        public GuessFilterViewHolder(@NonNull View itemView, OnViewHolderListener vhListener) {
            super(itemView);
            this.vhListener = vhListener;

            flMainContainer = itemView.findViewById(R.id.fl_main_container);
            ConstraintLayout clContentContainer = itemView.findViewById(R.id.cl_content_container);

            clContentContainer.setOnClickListener(view ->
                    vhListener.onItemClicked(getBindingAdapterPosition()));

            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        public void bindViews(GuessFilter guessFilter) {
            String title = guessFilter.getTitle().toUpperCase();
            if (guessFilter.getCount() > 0) title = title + "  |  " +
                    Utilities.applyCommaSeparator(guessFilter.getCount(), 0);
            tvTitle.setText(title);

            setSelected(guessFilter);
        }

        private void setSelected(GuessFilter guessFilter) {
            int colorRes = vhListener.isGuessFilterSelected(guessFilter.getTitle())
                    ? R.color.color_guess_filter : R.color.color_keys;
            Drawable drawable = flMainContainer.getBackground();
            drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), colorRes),
                    PorterDuff.Mode.SRC_ATOP);
            flMainContainer.setBackground(drawable);
        }
        
        public interface OnViewHolderListener extends
                OnClickViewHolderListener {
            boolean isGuessFilterSelected(String title);
        }

    }
}
