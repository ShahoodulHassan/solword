package com.appicacious.solword;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/* set spacing for grid view */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    final private int spanCount, spacing, spacing_top;
    final private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing_left, int spacing_top) {
        this.spanCount = spanCount;
        this.spacing = spacing_left;
        this.includeEdge = true;
        this.spacing_top = spacing_top;
    }

    public GridSpacingItemDecoration(Context context, int spanCount, int spacing_left, int spacing_top) {
        this.spanCount = spanCount;
        this.spacing = context.getResources().getDimensionPixelSize(spacing_left);
        this.includeEdge = false;
        this.spacing_top = context.getResources().getDimensionPixelSize(spacing_top);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item phases_position
        int column = position % spanCount; // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing_top;
            }
            outRect.bottom = spacing_top; // item bottom
        } else {
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing_top; // item top
            }
        }
    }
}
