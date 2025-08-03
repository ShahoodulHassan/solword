package com.appicacious.solword;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    private final int mLeftOffset;
    private final int mTopOffset;
    private final int mRightOffset;
    private final int mBottomOffset;


    private ItemOffsetDecoration(int itemOffset) {
//        new ItemOffsetDecoration(itemOffset, itemOffset, itemOffset, itemOffset);
        this(itemOffset, itemOffset, itemOffset, itemOffset);
    }

    private ItemOffsetDecoration(int mLeftOffset, int mTopOffset, int mRightOffset, int mBottomOffset) {
        this.mLeftOffset = mLeftOffset;
        this.mTopOffset = mTopOffset;
        this.mRightOffset = mRightOffset;
        this.mBottomOffset = mBottomOffset;
    }

    public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    public ItemOffsetDecoration(@NonNull Context context,
                                @DimenRes int mLeftOffset,
                                @DimenRes int mTopOffset,
                                @DimenRes int mRightOffset,
                                @DimenRes int mBottomOffset) {
        this(context.getResources().getDimensionPixelSize(mLeftOffset),
                context.getResources().getDimensionPixelSize(mTopOffset),
                context.getResources().getDimensionPixelSize(mRightOffset),
                context.getResources().getDimensionPixelSize(mBottomOffset));
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(mLeftOffset, mTopOffset, mRightOffset, mBottomOffset);
    }
}
