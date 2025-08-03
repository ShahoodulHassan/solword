package com.appicacious.solword.architecture;

import static android.view.View.NO_ID;
import static com.appicacious.solword.constants.Constants.BLANK;
import static com.appicacious.solword.constants.Constants.GREY;
import static com.appicacious.solword.constants.Constants.TAG_NAV;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.models.Cell;
import com.appicacious.solword.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InputViewModel extends SavedStateAndroidViewModel {

    private static final String KEY_LAST_FILLED = "last_filled";
    private static final String KEY_CELLS = "cells";
    private static final String KEY_WORD_SIZE = "word_size";
    private final InputRepository repository;

    private final MutableLiveData<List<Integer>> cellsParams = new MutableLiveData<>();
    private final LiveData<Event<List<Cell>>> rawCellsLiveData;

    private final MutableLiveData<Integer> randomWordParam = new MutableLiveData<>();
    private final MutableLiveData<Bundle> randomWordParams = new MutableLiveData<>();
    private final LiveData<Event<String>> randomWordLiveData;

    private final MutableLiveData<List<Cell>> cellsLiveData;
    /*
    Last cell that has some alphabet. If no alphabet is entered, the value is -1;
     */
    private int lastFilledCellNum;

//    private int currentWordSize;

    private long loadDelay = Constants.LOAD_DELAY;

    public InputViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application, savedStateHandle);
        Log.d(TAG_NAV, "InputViewModel: savedStateHandle=" + getSavedStateHandle());

        restoreState();

        cellsLiveData = getSavedStateHandle().getLiveData(KEY_CELLS);

        repository = new InputRepository(application);

        Function<List<Integer>, LiveData<Event<List<Cell>>>> cellFunc = input -> {
            repository.initCreateCells(input.get(0), input.get(1));
            return repository.getCellsLiveData();
        };

        Function<Integer, LiveData<Event<String>>> randomWordFunc = size -> {
            repository.initRandomWordTask(getCells(), size);
//            repository.getRandomWordBySize(size);
            return repository.getRandomWordMedLiveData();
        };

        Function<Bundle, LiveData<Event<String>>> randomWordFunc2 = input -> {
            repository.initRandomWordTask(input.getParcelableArrayList(KEY_CELLS),
                    input.getInt(KEY_WORD_SIZE));
            return repository.getRandomWordMedLiveData();
        };

        rawCellsLiveData = Transformations.switchMap(cellsParams, cellFunc);
//        randomWordLiveData = Transformations.switchMap(randomWordParam, randomWordFunc);
        randomWordLiveData = Transformations.switchMap(randomWordParams, randomWordFunc2);

        Log.d(TAG_NAV, "InputViewModel: cellsLiveData=" + cellsLiveData);
//        Log.d(TAG_NAV, "InputViewModel: cells=" + cellsLiveData.getValue());
    }

    @Override
    public void saveState() {
        getSavedStateHandle().set(KEY_LAST_FILLED, lastFilledCellNum);

        // No need to save a LiveData because it gets saved automatically
//        getSavedStateHandle().set(KEY_CELLS, cellsLiveData/*.getValue()*/);
    }

    @Override
    protected void restoreState() {
        Object last_filled = getSavedStateHandle().get(KEY_LAST_FILLED);
        if (last_filled instanceof Integer) this.lastFilledCellNum = (int) last_filled;
//        cellsLiveData.setValue(getSavedStateHandle().get(KEY_CELLS));
    }

    public void forceCreateNewCells(int rowCount, int colCount) {
        setLastFilledCellNum(NO_ID);
        List<Integer> params = new ArrayList<>();
        params.add(rowCount);
        params.add(colCount);
        cellsParams.setValue(params);
    }

    public void createNewCellsIfRequired(int rowCount, int colCount) {
        if (getCellsLiveData() == null || getCellsLiveData().getValue() == null ||
                getCellsLiveData().getValue().isEmpty()) {
            Log.d(TAG + TAG_NAV, "createNewCellsIfRequired: required");
            forceCreateNewCells(rowCount, colCount);
        } else {
            Log.d(TAG + TAG_NAV, "createNewCellsIfRequired: not required");
        }
    }

    public LiveData<Event<List<Cell>>> getRawCellsLiveData() {
        return rawCellsLiveData;
    }




    public LiveData<List<Cell>> getCellsLiveData() {
        return cellsLiveData;
    }

    public void setStatus(String id, int status) {
        List<Cell> newList = Utilities.getCopyList(cellsLiveData.getValue());
        if (newList != null) {
            for (Cell c : newList) {
                if (Objects.equals(c.getId(), id)) {
                    c.setStatus(status);
                    break;
                }
            }
            cellsLiveData.setValue(newList);
        }
    }

    public void setAlpha(String alpha) {
        List<Cell> newList = Utilities.getCopyList(cellsLiveData.getValue());
        if (newList != null) {
            int next = lastFilledCellNum + 1;
            for (Cell cell : newList) {
                if (newList.indexOf(cell) == next) {
                    cell.setAlpha(alpha);
                    cell.setStatus(GREY);
                    cell.setShouldAnimate(true);
                    lastFilledCellNum = next;
                    break;
                }
            }
            cellsLiveData.setValue(newList);
        }
    }

    public void setShouldAnimate(int colId, int rowId, boolean shouldAnimate) {
        List<Cell> newList = Utilities.getCopyList(cellsLiveData.getValue());
        if (newList != null) {
            for (Cell cell : newList) {
                if (cell.getColId() == colId && cell.getRowId() == rowId) {
                    cell.setShouldAnimate(shouldAnimate);
                    break;
                }
            }
            cellsLiveData.setValue(newList);
        }
    }

    public void delAlpha() {
        List<Cell> newList = Utilities.getCopyList(cellsLiveData.getValue());
        if (newList != null) {
            for (Cell cell : newList) {
                if (newList.indexOf(cell) == lastFilledCellNum) {
                    cell.setAlpha("");
                    cell.setStatus(BLANK);
                    cell.setShouldAnimate(false);
                    lastFilledCellNum--;
                    break;
                }
            }
            cellsLiveData.setValue(newList);
        }
    }

    public void deleteAll() {
        List<Cell> newCells = Utilities.getCopyList(getCells());
        if (newCells != null) {
            for (int i = 0; i <= getLastFilledCellNum(); i++) {
                Cell cell = newCells.get(i);
                cell.setAlpha("");
                cell.setStatus(BLANK);
                cell.setShouldAnimate(false);
            }
            setLastFilledCellNum(NO_ID);
            setCells(newCells);
        }
    }

    public int getLastFilledCellNum() {
        return lastFilledCellNum;
    }

    public void setLastFilledCellNum(int lastFilledCellNum) {
        this.lastFilledCellNum = lastFilledCellNum;
    }

    @NonNull
    public List<Cell> getCells() {
        return getCellsLiveData().getValue() == null ? new ArrayList<>() : getCellsLiveData().getValue();
    }

    public void setCells(List<Cell> cells) {
        cellsLiveData.setValue(cells);
    }

    @Deprecated
    public void fetchRandomWordBySize(int size) {
        randomWordParam.setValue(size);
    }

    public void fetchRandomWord(List<Cell> cells, int size) {
        Bundle params = new Bundle();
        params.putParcelableArrayList(KEY_CELLS,
                (cells == null ? new ArrayList<>() : new ArrayList<>(cells)));
        params.putInt(KEY_WORD_SIZE, size);
        randomWordParams.setValue(params);
    }

    public LiveData<Event<String>> getRandomWordLiveData() {
        return randomWordLiveData;
    }

    //    public int getCurrentWordSize() {
//        return currentWordSize;
//    }
//
//    public void setCurrentWordSize(int currentWordSize) {
//        this.currentWordSize = currentWordSize;
//    }

    public long getLoadDelay() {
        return loadDelay;
    }

    public void setLoadDelay(long loadDelay) {
        this.loadDelay = loadDelay;
    }
}
