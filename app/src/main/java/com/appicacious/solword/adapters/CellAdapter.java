package com.appicacious.solword.adapters;

import static com.appicacious.solword.constants.Constants.FIELD_ALPHA;
import static com.appicacious.solword.constants.Constants.FIELD_STATUS;
import static com.appicacious.solword.constants.Constants.GREEN;
import static com.appicacious.solword.constants.Constants.PICK_GUESS_ANIM_DURATION;
import static com.appicacious.solword.constants.Constants.YELLOW;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.R;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.interfaces.OnClickViewHolderListener;
import com.appicacious.solword.models.Cell;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CellAdapter extends BaseListAdapter<Cell, CellAdapter.CellViewHolder> {

    static final DiffUtil.ItemCallback<Cell> DIFF = new DiffUtil.ItemCallback<Cell>() {
        @Override
        public boolean areItemsTheSame(@NonNull Cell oldItem, @NonNull Cell newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Cell oldItem, @NonNull Cell newItem) {
            return oldItem.areContentsTheSame(newItem);
        }

        @Nullable
        @Override
        public Object getChangePayload(@NonNull Cell oldItem, @NonNull Cell newItem) {
            boolean isAlphaChanged = !Objects.equals(oldItem.getAlpha(), newItem.getAlpha());
            boolean isStatusChanged = oldItem.getStatus() != newItem.getStatus();
            if (isAlphaChanged || isStatusChanged) {
                List<String> payloads = new ArrayList<>();
                if (isAlphaChanged) payloads.add(FIELD_ALPHA);
                if (isStatusChanged) payloads.add(FIELD_STATUS);
                return payloads;
            }
            return super.getChangePayload(oldItem, newItem);
        }
    };

    private final OnAdapterInteractionListener mListener;

    public CellAdapter(@NonNull OnAdapterInteractionListener listener) {
        super(DIFF);
        mListener = listener;
    }

    @NonNull
    @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cell, parent,
                false);
        return new CellViewHolder(view, new CellViewHolder.OnViewHolderListener() {
            @Override
            public int getWordSize() {
                return mListener.getColCount();
            }

            @Override
            public void onItemClicked(int position) {
                mListener.onCellClicked(getItem(position));
            }

            @Override
            public void setShouldAnimate(int colId, int rowId, boolean shouldAnimate) {
                mListener.setShouldAnimate(colId, rowId, shouldAnimate);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        Cell cell = getItem(position);

        if (payloads.size() > 0) {
            if (payloads.get(0) instanceof List<?>) {
                List<?> pLoads = (ArrayList<?>) payloads.get(0);
                for (Object p : pLoads) {
                    if (p instanceof String) {
                        String payload = (String) p;
                        switch (payload) {
                            case FIELD_ALPHA:
                                holder.setAlpha(cell);
                                break;
                            case FIELD_STATUS:
                                holder.setStatus(cell);
                                break;
                        }
                    }
                }
            } else {
                holder.bindViews(getItem(position));
            }
        } else {
            holder.bindViews(getItem(position));
        }
    }

    @Override
    public void notifyHolders() {

    }

    public interface OnAdapterInteractionListener {
        void onCellClicked(Cell cell);

        int getColCount();

        void setShouldAnimate(int colId, int rowId, boolean shouldAnimate);
    }

    public static class CellViewHolder extends RecyclerView.ViewHolder {

        private final OnViewHolderListener vhListener;
        //        private final MaterialButton mbButton;
        private final ConstraintLayout clCellContainer;
        private final MaterialTextView tvCell;

        private YoYo.YoYoString yoyo;

        public CellViewHolder(@NonNull View itemView, OnViewHolderListener listener) {
            super(itemView);
            vhListener = listener;
            clCellContainer = itemView.findViewById(R.id.cl_cell_container);
            tvCell = itemView.findViewById(R.id.tv_cell);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    tvCell,
                    10,
                    32,
                    1,
                    TypedValue.COMPLEX_UNIT_SP);
            /*
            1) Up to 6 letters, 1:1 will be applied
            2) For 7 and 8 letters, 1:1.25 will be applied
            3) For 9 and more letters, 1:1.5 will be applied
             */
            ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) tvCell.getLayoutParams();
            String ratio = "h,1:1";
            if (vhListener.getWordSize() > 8) {
                ratio = "h,1:1.5";
            } else if (vhListener.getWordSize() > 6) {
                ratio = "h,1:1.25";
            }

//            if (vhListener.getWordSize() > 6) {
//                if (vhListener.getWordSize() >= 9) {
//                    ratio = "1:1.5";
//                } else {
//                    ratio = "1:1.25";
//                }
//            }

            lParams.dimensionRatio = ratio;
            tvCell.setLayoutParams(lParams);

        }

        public void bindViews(Cell cell) {
            setAlpha(cell);
            setStatus(cell);
        }

        public void setAlpha(Cell cell) {
            String alpha = cell.getAlpha();
            tvCell.setText(alpha);
            tvCell.setOnClickListener(TextUtils.isEmpty(alpha) ? null
                    : view -> vhListener.onItemClicked(getBindingAdapterPosition()));
            if (cell.shouldAnimate()) {
                yoyo = YoYo.with(Techniques.BounceIn)
                        .duration(PICK_GUESS_ANIM_DURATION)
                        .playOn(itemView);
                vhListener.setShouldAnimate(cell.getColId(), cell.getRowId(), false);
            } else {
                if (yoyo != null && yoyo.isRunning()) yoyo.stop();
            }
        }

        public void setStatus(Cell cell) {
            int colorRes;
            Drawable drawable = clCellContainer.getBackground();
            if (cell.getStatus() == Constants.BLANK) {
                drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_cell_blank);
            } else {
                if (cell.getStatus() == GREEN) {
                    colorRes = R.color.color_green;
                } else if (cell.getStatus() == YELLOW) {
                    colorRes = R.color.color_yellow;
                } else {
                    colorRes = R.color.color_grey;
                }
                drawable.setColorFilter(ContextCompat.getColor(itemView.getContext(), colorRes),
                        PorterDuff.Mode.SRC_ATOP);
            }
            clCellContainer.setBackground(drawable);
//            tvCell.setBackgroundColor(ContextCompat.getColor(tvCell.getContext(), colorRes));
        }

        public interface OnViewHolderListener extends
                OnClickViewHolderListener {
            int getWordSize();

            void setShouldAnimate(int colId, int rowId, boolean shouldAnimate);
        }
    }
}
