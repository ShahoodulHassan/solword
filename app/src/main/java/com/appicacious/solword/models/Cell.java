package com.appicacious.solword.models;

import static com.appicacious.solword.constants.Constants.BLANK;
import static com.appicacious.solword.constants.Constants.GREEN;
import static com.appicacious.solword.constants.Constants.GREY;
import static com.appicacious.solword.constants.Constants.YELLOW;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.util.Objects;


public class Cell implements Parcelable {

    @IntDef({BLANK, GREY, GREEN, YELLOW})
    public @interface CellStatus {}

    private final int rowId;

    private final int colId;

    private String alpha;

    @CellStatus
    private int status;

    // TODO: 26/03/2022 Consider deleting this unused member variable
    private boolean isRowActive;

    private boolean shouldAnimate;

    // For creating blank cells
    public Cell(int rowId, int colId) {
        this.rowId = rowId;
        this.colId = colId;
        this.status = BLANK;
    }

    public Cell(int rowId, int colId, @NonNull String alpha, @CellStatus int status,
                boolean shouldAnimate, boolean isRowActive) {
        this.rowId = rowId;
        this.colId = colId;
        this.alpha = alpha;
        this.status = status;
        this.shouldAnimate = shouldAnimate;
        this.isRowActive = isRowActive;
    }

    // Copy Constructor
    public Cell(Cell other) {
        this.rowId = other.rowId;
        this.colId = other.colId;
        this.alpha = other.alpha;
        this.status = other.status;
        this.shouldAnimate = other.shouldAnimate;
        this.isRowActive = other.isRowActive;
    }

    protected Cell(Parcel in) {
        rowId = in.readInt();
        colId = in.readInt();
        alpha = in.readString();
        status = in.readInt();
        isRowActive = in.readByte() != 0;
        shouldAnimate = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rowId);
        dest.writeInt(colId);
        dest.writeString(alpha);
        dest.writeInt(status);
        dest.writeByte((byte) (isRowActive ? 1 : 0));
        dest.writeByte((byte) (shouldAnimate ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Cell> CREATOR = new Creator<Cell>() {
        @Override
        public Cell createFromParcel(Parcel in) {
            return new Cell(in);
        }

        @Override
        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };

    public int getRowId() {
        return rowId;
    }

    public int getColId() {
        return colId;
    }

    public String getAlpha() {
        return alpha;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }

    public String getId() {
        return String.valueOf(rowId) + colId;
    }

    @CellStatus
    public int getStatus() {
        return status;
    }

    public void setStatus(@CellStatus int status) {
        this.status = status;
    }

    public boolean shouldAnimate() {
        return shouldAnimate;
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        this.shouldAnimate = shouldAnimate;
    }

    public boolean isRowActive() {
        return isRowActive;
    }

    public boolean areContentsTheSame(Cell newItem) {
        return this.status == newItem.status && Objects.equals(this.alpha, newItem.alpha)
                && this.isRowActive == newItem.isRowActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return getRowId() == cell.getRowId() && getColId() == cell.getColId()
                && getStatus() == cell.getStatus() && isRowActive() == cell.isRowActive()
                && shouldAnimate == cell.shouldAnimate && Objects.equals(getAlpha(), cell.getAlpha());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRowId(), getColId(), getAlpha(), getStatus(), isRowActive(),
                shouldAnimate);
    }

    @NonNull
    @Override
    public String toString() {
        return "\nCell{" +
                "rowId=" + rowId +
                ", colId=" + colId +
                ", alpha='" + alpha + '\'' +
                ", status=" + status +
                '}';
    }
}
