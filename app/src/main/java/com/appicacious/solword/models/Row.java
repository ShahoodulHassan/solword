package com.appicacious.solword.models;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;

public class Row {

    private final int id;

    private LinkedHashMap<Integer, Cell> cells;

    public Row(int id, Cell cell) {
        this.id = id;
        addCell(cell);
    }

    public int getId() {
        return id;
    }

    public LinkedHashMap<Integer, Cell> getCells() {
        return cells == null ? cells = new LinkedHashMap<>() : cells;
    }

    public void addCell(Cell cell) {
        if (cell != null) {
            getCells().put(cell.getColId(), cell);
        }
    }

    public void removeCellAt(int collId) {
        getCells().remove(collId);
    }

    @Nullable
    public Cell getCellAt(int collId) {
        return getCells().get(collId);
    }

    public boolean isRowComplete(int length) {
        return getCells().size() == length;
    }
}
