package com.appicacious.solword.architecture;

import static com.appicacious.solword.constants.Constants.ALL;
import static com.appicacious.solword.constants.Constants.ALL_FILTER;
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
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagedList;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.appicacious.solword.models.Cell;
import com.appicacious.solword.models.GuessFilter;
import com.appicacious.solword.models.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GuessViewModel extends SavedStateAndroidViewModel {

    private static final String KEY_SELECTED_FILTERS = "key_selected_filters";
    private final GuessRepository repository;
    private final String KEY_COL_COUNT = "key_col_count";
    private final String KEY_CELLS = "key_cells";
    private final String KEY_GUESS_PARAMS = "key_guess_params";
    private final String KEY_IS_PERFORMED = "key_is_performed";

    private final LiveData<PagedList<Word>> guessesPagedLiveData;

    private final LiveData<PagingData<Word>> guessesPaging3LiveData;

    private final LiveData<List<Word>> guessesLiveData;

    private boolean isDataLoadProceduresPerformed;

//    private HashSet<String> guessFilterSelectedTitles = new HashSet<>();
    private HashMap<String, GuessFilter> selectedGuessFilters = new HashMap<>();


    public GuessViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application, savedStateHandle);

        restoreState();

        MutableLiveData<Bundle> guessParams = getSavedStateHandle().getLiveData(KEY_GUESS_PARAMS);

        repository = new GuessRepository(application, getSavedStateHandle());

//        selectedGuessFilters.put(ALL, ALL_FILTER);

        Function<Bundle, LiveData<List<Word>>> guessFunc = input -> {
            repository.initGuessTask(input.getParcelableArrayList(KEY_CELLS),
                    input.getInt(KEY_COL_COUNT));
            return repository.getGuessesMedLiveData();
        };

        Function<Bundle, LiveData<PagedList<Word>>> guessFuncPaging2 = input -> {
            repository.initGuessTask(input.getParcelableArrayList(KEY_CELLS),
                    input.getInt(KEY_COL_COUNT));
            return repository.getGuessesPagedMedLiveData();
        };

        Function<Bundle, LiveData<PagingData<Word>>> guessFuncPaging3 = input -> {
            repository.initGuessTask(input.getParcelableArrayList(KEY_CELLS),
                    input.getInt(KEY_COL_COUNT));
            return PagingLiveData.cachedIn(
                    repository.getGuessesPaging3MedLiveData(),
                    ViewModelKt.getViewModelScope(GuessViewModel.this));
        };

        // No paging
        guessesLiveData = Transformations.switchMap(guessParams, guessFunc);

        // Paging 2
        guessesPagedLiveData = Transformations.switchMap(guessParams, guessFuncPaging2);

        // Paging 3
        guessesPaging3LiveData = Transformations.switchMap(guessParams, guessFuncPaging3);


        Log.d(TAG_NAV, "GuessViewModel: guessParams=" + guessParams.getValue());
    }


    @Override
    public void saveState() {
        repository.saveState();

        getSavedStateHandle().set(KEY_IS_PERFORMED, isDataLoadProceduresPerformed);

        Bundle filtersBundle = new Bundle();
        // Convert HashMap to Bundle
        for (Map.Entry<String, GuessFilter> entry : selectedGuessFilters.entrySet()) {
            filtersBundle.putParcelable(entry.getKey(), entry.getValue());
        }
        // Save the Bundle to handle
        getSavedStateHandle().set(KEY_SELECTED_FILTERS, filtersBundle);

        // No need to save the state of params LiveData; it is automatically saved
    }

    @Override
    protected void restoreState() {
        Object isPerformed = getSavedStateHandle().get(KEY_IS_PERFORMED);
        if (isPerformed instanceof Boolean) isDataLoadProceduresPerformed = (boolean) isPerformed;

        // Restore the Bundle from handle
        Bundle selectedFilters = getSavedStateHandle().get(KEY_SELECTED_FILTERS);
        // Convert the Bundle to HashMap
        if (selectedFilters == null || selectedFilters.isEmpty()) {
            selectedGuessFilters.put(ALL, ALL_FILTER);
        } else {
            for (String key : selectedFilters.keySet()) {
                selectedGuessFilters.put(key, selectedFilters.getParcelable(key));
            }
        }
    }



    public void setGuessParams(List<Cell> cells, int collCount) {
        Bundle params = new Bundle();
        params.putInt(KEY_COL_COUNT, collCount);
        params.putParcelableArrayList(KEY_CELLS, new ArrayList<>(cells));
//        guessParams.setValue(params);
        getSavedStateHandle().set(KEY_GUESS_PARAMS, params);
    }

//    public void runGuessRawQuery3(String qString) {
//        /*
//        We fetch guess filters only it is first time we are fetching the filters.
//        */
//        if (getGuessFilterLiveData() == null) {
//            repository.runGuessRawQuery3(qString);
//        }
//    }


    public void initGuessFilterTask() {
        repository.initGuessFilterTask();
    }

    public LiveData<List<Word>> getGuessesLiveData() {
        return guessesLiveData;
    }

    public LiveData<PagedList<Word>> getGuessesPagedLiveData() {
        return guessesPagedLiveData;
    }

    public LiveData<PagingData<Word>> getGuessesPaging3LiveData() {
        return guessesPaging3LiveData;
    }

    public LiveData<List<GuessFilter>> getGuessFilterLiveData() {
        return repository.getGuessFilterLiveData();
    }


//    public void setGuessFilters(List<GuessFilter> guessFilters) {
//        repository.setGuessFilters(guessFilters);
//    }

    public void initGuessFilterQueryTask(List<GuessFilter> guessFilters, String operator) {
        repository.initGuessFilterQueryTask(guessFilters, operator);
    }

//    public void setGuessFilterSelected(GuessFilter guessFilter) {
//        List<GuessFilter> guessFilters = getGuessFilterLiveData().getValue();
//        if (guessFilters != null) {
//            List<GuessFilter> newList = new ArrayList<>();
//            for (GuessFilter gf : guessFilters) {
//                GuessFilter copy = new GuessFilter(guessFilter);
//                if (Objects.equals(gf.getTitle(), guessFilter.getTitle())) {
//                    copy.setSelected(!copy.isSelected());
//                }
//                newList.add(copy);
//            }
//            setGuessFilters(newList);
//        }
//    }


    public boolean isDataLoadProceduresPerformed() {
        return isDataLoadProceduresPerformed;
    }

    public void setDataLoadProceduresPerformed(boolean dataLoadProceduresPerformed) {
        this.isDataLoadProceduresPerformed = dataLoadProceduresPerformed;
    }

    public boolean isGuessFilterSelected(String title) {
        return selectedGuessFilters.containsKey(title);
    }

    public void setGuessFilterSelected(GuessFilter guessFilter, boolean isSelected) {
        if (isSelected) {
            if (Objects.equals(guessFilter.getTitle(), ALL)) {
                selectedGuessFilters = new HashMap<>();
            } else {
                selectedGuessFilters.remove(ALL);
            }
            selectedGuessFilters.put(guessFilter.getTitle(), guessFilter);
        } else {
            if (!Objects.equals(guessFilter.getTitle(), ALL)) {
                selectedGuessFilters.remove(guessFilter.getTitle());
                if (selectedGuessFilters.size() == 0) selectedGuessFilters.put(ALL, ALL_FILTER);
            }
        }
    }

    public boolean isAllFilterSelected() {
        return selectedGuessFilters.containsKey(ALL);
    }

    public boolean isOperatorApplicable() {
        return selectedGuessFilters.size() > 1;
    }

    public ArrayList<GuessFilter> getSelectedGuessFilters() {
        return new ArrayList<>(selectedGuessFilters.values());
    }
}
