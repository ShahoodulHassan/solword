package com.appicacious.solword;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * No Predictive Animations GridLayoutManager
 */
public class NpaGridLayoutManager extends GridLayoutManager {
    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     *
     * If I don't use this in GuessFragment, I get the following error:
     * java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position
     * Found
     * this solution here {https://stackoverflow.com/a/33985508/7983864}
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    // Second method to get rid of above error, if you don't
    // want to set supportsPredictiveItemAnimations() to false.
//    @Override
//    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
//        try {
//            super.onLayoutChildren(recycler, state);
//        } catch (IndexOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//
//    }

    public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NpaGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }
}
