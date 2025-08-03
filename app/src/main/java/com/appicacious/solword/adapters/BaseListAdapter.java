package com.appicacious.solword.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * Child adapters should always implement {@link #onBindViewHolder(ViewHolder, int, List)} and call
 * super method so that mHolders is updated, if required.
 *
 */
public abstract class BaseListAdapter<T, VH extends ViewHolder>
        extends ListAdapter<T, VH> {

    private final List<ViewHolder> mHolders;

    public BaseListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
        mHolders = new ArrayList<>();
    }

    @NonNull
    @Override
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Empty method required
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (mHolders != null && !mHolders.contains(holder)) mHolders.add(holder);
    }

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        if (mHolders != null) mHolders.remove(holder);
    }

    public abstract void notifyHolders();

    @NonNull
    protected List<ViewHolder> getmHolders() {
        return mHolders;
    }
}
