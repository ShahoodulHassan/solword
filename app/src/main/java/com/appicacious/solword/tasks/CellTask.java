package com.appicacious.solword.tasks;

import androidx.annotation.Nullable;

import com.appicacious.solword.models.Cell;
import com.appicacious.solword.utilities.MyAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class CellTask extends MyAsyncTask<Void, Void, List<Cell>> {

    private final OnCellTaskInteractionListener listener;
    private final int rowCount, colCount, cellCount;

    public CellTask(OnCellTaskInteractionListener listener, int rowCount, int colCount) {
        this.listener = listener;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.cellCount = rowCount * colCount;
    }

    @Override
    protected List<Cell> doInBackground(Void aVoid) {
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < cellCount; x++) {
            int[] ids = getCellIdsByCellNum(x);
            if (ids != null) cells.add(new Cell(ids[0], ids[1]));
        }
        return cells;
    }

    @Override
    protected void onPostExecute(List<Cell> cells) {
        super.onPostExecute(cells);
        listener.onComplete(cells);
    }

    @Nullable
    private int[] getCellIdsByCellNum(int cellNum) {
        int remainder = cellNum;
        if (cellNum >= 0) {
            for (int x = 0; x < (rowCount); x++) {
                if (remainder < colCount) {
                    return new int[]{x, remainder};
                }
                remainder = remainder - colCount;
            }
        }
        return null;
    }

    public interface OnCellTaskInteractionListener {
        void onComplete(List<Cell> cells);
    }
}
