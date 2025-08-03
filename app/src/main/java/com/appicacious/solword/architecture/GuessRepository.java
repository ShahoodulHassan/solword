package com.appicacious.solword.architecture;

import static com.appicacious.solword.constants.Constants.ALL_FILTER;
import static com.appicacious.solword.constants.Constants.AND;
import static com.appicacious.solword.constants.Constants.SUBSTR;
import static com.appicacious.solword.constants.Constants.TAG_NAV;

import android.annotation.SuppressLint;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.paging.PagedList;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.models.Cell;
import com.appicacious.solword.models.GuessFilter;
import com.appicacious.solword.models.GuessQueryData;
import com.appicacious.solword.models.Word;
import com.appicacious.solword.room.WordStore;
import com.appicacious.solword.tasks.QueryDataTask;
import com.appicacious.solword.utilities.MyAsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuessRepository extends SqliteRepository {

    private static final String TAG = GuessRepository.class.getSimpleName();

    /**
     * This new prefix is more flexible with respect to the random categorization of guess words as 
     * hidden or not.
     * abs(random % 2) means either 0 or 1. However, here the probability of 0 and 1 is 50% each.
     * I wanted to increase the probability of 0 so that the users get less hidden words.
     * In order to achieve that, I used abs(random % 3) which would return any of 0, 1 or 2, which
     * means that all the three values have a probability of 33% each.
     * However, 2 is not acceptable because isHidden is a boolean and only 0 and 1 are accepted.
     * Hence, the use of IF in the query. So, if the value is not 1, we use 0; otherwise we use 1.
     * What we achieved here is that the probability of 0 has now increased from 50% to 67% (because
     * both 2 and 0 would be represented by 0) and of 1 has decreased from 50% to 33%.
     * Secondly, we can use any number instead of 3 in random() modulus. A higher number will
     * increase the probability of 0 and a lower number will decrease it. For example, using 4 would
     * increased the probability of 0 to 75% and decrease that of 1 to 25%.
     */ 
    public final static String GUESSES_QUERY_PREFIX = "SELECT _id, wordTitle, " +
            "(CASE WHEN isHiddenTemp != 1 THEN 0 ELSE 1 END) AS isHidden FROM (SELECT _id, " +
            "wordTitle, abs(random() % 3) isHiddenTemp";
//    public final static String GUESSES_QUERY_PREFIX = "SELECT _id, wordTitle, abs(random() % 2) isHidden";
    private final static String GUESS_COUNT_QUERY_PREFIX = "SELECT COUNT(_id)";
    private static final String KEY_QUERY_DATA = "key_query_data";
    private static final String KEY_GUESS_FILTERS = "key_guess_filters";
    private static final String KEY_QUERY_STRING = "key_query_string";

    private final SavedStateHandle savedStateHandle;

    private String queryString;
    private GuessQueryData guessQueryData;
    private final WordStore wordStore;
    private final MediatorLiveData<List<Word>> guessesMedLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<PagingData<Word>> guessesPaging3MedLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<PagedList<Word>> guessesPagedMedLiveData = new MediatorLiveData<>();

    private final MutableLiveData<List<GuessFilter>> guessFilterLiveData;


    public GuessRepository(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);

        this.savedStateHandle = savedStateHandle;

        restoreState();

        guessFilterLiveData = this.savedStateHandle.getLiveData(KEY_GUESS_FILTERS);

        wordStore = db.wordStore();
    }

    /**
     *
     * guessQueryData is not saved because, in our ViewModel, guessParams are being saved as
     * LiveData in {@link GuessViewModel#setGuessParams(List, int)}. Since, it is being restored in
     * the constructor of ViewModel, it executes the Transformations.switchMap right when the
     * Activity is restored, thereby pulling fresh Paging data. Eventually when
     * {@link #fetchGuesses(GuessQueryData)} gets called, qussQueryData gets a fresh value.
     *
     * queryString is saved here but restored in {@link #fetchGuesses(GuessQueryData)}
     *
     * No need to save guessFilterLiveData as it is saved as a LiveData.
     *
     */
    void saveState() {
        savedStateHandle.set(KEY_QUERY_STRING, queryString);
//        savedStateHandle.set(KEY_QUERY_DATA, guessQueryData);
//        savedStateHandle.set(KEY_GUESS_FILTERS, guessFilterLiveData.getValue());
    }

    private void restoreState() {
//        queryString = savedStateHandle.get(KEY_QUERY_STRING);
//        guessQueryData = savedStateHandle.get(KEY_QUERY_DATA);
//        guessFilterLiveData.setValue(savedStateHandle.get(KEY_GUESS_FILTERS));
    }

//    private void initGuessFilterTask(@NonNull GuessQueryData queryData) {
//        new GuessFilterTask(wordStore, new GuessFilterTask.OnTaskInteractionListener() {
//            @Override
//            public void onComplete(List<GuessFilter> value) {
//                savedStateHandle.set(KEY_GUESS_FILTERS, value);
////                guessFilterLiveData.setValue(value);
//            }
//        }).execute(queryData);
//    }

    /**
     * We fetch guess filters only if it is the first time we are fetching the filters.
     *
     * guessFilterLiveData gets its value from here by way of savedStateHandle
     */
    void initGuessFilterTask() {
        if (!areGuessFiltersFetched()) {
            new GuessFilterTask(wordStore, value -> {
                savedStateHandle.set(KEY_GUESS_FILTERS, value);
//                    guessFilterLiveData.setValue(value);
            }).execute(guessQueryData);
        }
    }

    MutableLiveData<List<GuessFilter>> getGuessFilterLiveData() {
        return guessFilterLiveData;
    }

    void initGuessFilterQueryTask(List<GuessFilter> guessFilters, String operator) {
        new GuessFilterQueryTask(guessFilters, operator, this::runGuessRawQueryPaging3)
                .execute(guessQueryData);
    }

//    public void setGuessFilters(List<GuessFilter> guessFilters) {
//        getGuessFilterLiveData().setValue(guessFilters);
//    }

    static class GuessFilterQueryTask extends MyAsyncTask<GuessQueryData, Void, String> {

        private GuessQueryData queryData;
        private final String operator;
        private final List<GuessFilter> guessFilters;
        private final OnTaskInteractionListener listener;

        public GuessFilterQueryTask(List<GuessFilter> guessFilters, String operator,
                                    OnTaskInteractionListener listener) {
            this.guessFilters = guessFilters;
            this.operator = operator;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(GuessQueryData guessQueryData) {
            this.queryData = guessQueryData;
            return getQueryString();
        }

        private String getQueryString() {
            List<String> suffixes = new ArrayList<>();
            for (GuessFilter guessFilter : guessFilters) {
                if (!TextUtils.isEmpty(guessFilter.getQuerySuffix()))
                    suffixes.add(guessFilter.getQuerySuffix());
            }

            StringBuilder query2Builder = new StringBuilder();
            if (suffixes.size() > 0) {
                query2Builder.append(AND).append("(");
//                String baseQuery = GUESSES_QUERY_PREFIX + queryData.getQuery2();
//                StringBuilder query2Builder = new StringBuilder();
//                StringBuilder concat = new StringBuilder();
//                for (int i = 0; i < queryData.getColCount(); i++) {
//                    concat.append("l").append(i + 1);
//                    if (i < (queryData.getColCount() - 1)) concat.append("||");
//                }

                for (int i = 0; i < suffixes.size(); i++) {
                    String suffix = suffixes.get(i);
//                    query2Builder.append(String.format("LOWER(%s) LIKE '%%%s%%'", concat, suffix));
                    query2Builder.append(suffix);
                    // Just switch the operator between AND with OR
                    if (i < (suffixes.size() - 1)) query2Builder.append(operator);
                }
                query2Builder.append(")");
            }
            return GUESSES_QUERY_PREFIX + queryData.getQuery() + query2Builder.append(")");
        }

        @Override
        protected void onPostExecute(String qString) {
            super.onPostExecute(qString);
            listener.onComplete(qString);
        }

        public interface OnTaskInteractionListener {
            void onComplete(String qString);
        }
    }

    static class GuessFilterTask extends MyAsyncTask<GuessQueryData, Void, List<GuessFilter>> {

        private GuessQueryData queryData;
        private final WordStore wordStore;
        private final OnTaskInteractionListener listener;


        public GuessFilterTask(WordStore wordStore, OnTaskInteractionListener listener) {
            this.wordStore = wordStore;
            this.listener = listener;
        }

        @Override
        protected List<GuessFilter> doInBackground(GuessQueryData queryData) {
            this.queryData = queryData;
            List<GuessFilter> receivedFilters = getGuessFilters();
            List<GuessFilter> guessFilters = new ArrayList<>();

            if (receivedFilters.size() > 0) {
                for (GuessFilter guessFilter : receivedFilters) {
                    String qString = GUESS_COUNT_QUERY_PREFIX + queryData.getQuery() + AND +
                            guessFilter.getQuerySuffix();
                    SimpleSQLiteQuery query = new SimpleSQLiteQuery(qString);
                    int count = wordStore.getGuessCountByAlpha((query));
                    if (count > 0) {
                        guessFilter.setCount(count);
//                        guessFilter.setQuery(GUESSES_QUERY_PREFIX + guessFilter.getQuery());
                        guessFilters.add(guessFilter);
                    }
                }

                Collections.sort(guessFilters);
                guessFilters.add(0, ALL_FILTER);
            }

            return guessFilters;
        }

        @SuppressLint("DefaultLocale")
        private List<GuessFilter> getGuessFilters() {
            List<GuessFilter> guessFilters = new ArrayList<>();
//            List<String> queryStrings = new ArrayList<>();
            List<String> missingAlphas = queryData.getMissingAlphas();
            if (missingAlphas.size() > 0) {

//                String q1 = "SELECT '%s' alpha, COUNT(_id) count FROM wordles WHERE LENGTH(wordTitle) = "
//                        + queryData.getColCount();
                String q2 = queryData.getQuery();

                StringBuilder concat = new StringBuilder();
                for (int i = 0; i < queryData.getColCount(); i++) {
                    concat.append(String.format(SUBSTR, (i + 1)));
//                    concat.append("l").append(i + 1);
                    if (i < (queryData.getColCount() - 1)) concat.append("||");
                }

                for (String alpha : missingAlphas) {
//                    String query1 = String.format(q1, (alpha));
                    String querySuffix = String.format("LOWER(%s) LIKE '%%%s%%'", concat, alpha);
                    String query2 = q2 + AND + querySuffix;
                    GuessFilter guessFilter = new GuessFilter(alpha,
                            /*Collections.singletonList(alpha), query2, */querySuffix);
                    guessFilters.add(guessFilter);
//                    queryStrings.add(GUESS_COUNT_QUERY_PREFIX + query2);
                }
            }

            return guessFilters;
        }

        @Override
        protected void onPostExecute(List<GuessFilter> guessFilters) {
            super.onPostExecute(guessFilters);
            /*
            If the size of the list is 2, it means there are two filters: 'All' and one filter.
            Essentially, both would have the same guess results.
            So, there is no reason of showing these filters.
             */
            if (guessFilters != null && guessFilters.size() > 2) listener.onComplete(guessFilters);
        }

        public interface OnTaskInteractionListener {
            void onComplete(List<GuessFilter> guessFilters);
        }

    }

    void initGuessTask(List<Cell> cells, int collCount) {
        new QueryDataTask(this::fetchGuesses, collCount).execute(cells);
//        new QueryDataTask(this::fetchGuesses, collCount).execute(cells);
    }

    /**
     *
     * This method is called right after the activity is recreated. We retrieve the value of saved
     * queryString here and reset it.
     * <b>The benefit of this strategy is that, in case of filtered data, the saved queryString
     * would have the info about the filters as well.</b>
     *
     * If there is nothing to restore, we create the value as per normal. This value is saved
     * in the member variable soon after in {@link #runGuessRawQueryPaging3(String)}
     *
     * guessQueryData also gets its fresh value here
     *
     */
    private void fetchGuesses(final @NonNull GuessQueryData queryData) {
        guessQueryData = queryData;

        String qString;
        String restoredQuery = savedStateHandle.get(KEY_QUERY_STRING);
        if (!TextUtils.isEmpty(restoredQuery)) {
            // We use the saved value here
            qString = restoredQuery;

            // We reset the saved value, so that next time this method is called during this
            // session, we don't end up using the saved value again, instead of the fresh value
            // received via the method parameter.
            savedStateHandle.set(KEY_QUERY_STRING, "");
        } else {
            qString = GUESSES_QUERY_PREFIX + queryData.getQuery() + ")";
        }

//        String qString = GUESSES_QUERY_PREFIX + queryData.getQuery();

        // No paging
//        runGuessRawQuery(qString);

        // Paging 2
//        runGuessRawQueryPaging2(qString);

        // Paging 3
        runGuessRawQueryPaging3(qString);
    }

    void runGuessRawQuery(String qString) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(qString);
        LiveData<List<Word>> liveData = wordStore.getWordsLiveDataByQuery(query);
        guessesMedLiveData.addSource(liveData, guessesMedLiveData::setValue);
    }

    /**
     * queryString gets its value in this method
     *
     */
    void runGuessRawQueryPaging3(String qString) {
        Log.d(TAG_NAV, "runGuessRawQueryPaging3: query=" + qString);
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(this.queryString = qString);
        PagingConfig pagingConfig = new PagingConfig(Constants.PAGE_SIZE);
        Pager<Integer, Word> pager = new Pager<>(pagingConfig, null,
                () -> wordStore.getPaged3WordsByQuery(query));
        LiveData<PagingData<Word>> liveData  = PagingLiveData.getLiveData(pager);
        guessesPaging3MedLiveData.addSource(liveData, guessesPaging3MedLiveData::setValue);
    }

//    void runGuessRawQueryPaging2(String qString) {
//        SimpleSQLiteQuery query = new SimpleSQLiteQuery(qString);
//        DataSource.Factory<Integer, Word> factory = wordStore.getPagedWordsByQuery(query);
//        PagedList.Config config = new PagedList.Config.Builder()
//                .setPageSize(Constants.PAGE_SIZE)
//                .build();
//        LiveData<PagedList<Word>> guessesPagedLiveData = new LivePagedListBuilder<>(factory, config).build();
//
//        guessesPagedMedLiveData.addSource(guessesPagedLiveData, pagedGuesses -> {
//            guessesPagedMedLiveData.setValue(pagedGuesses);
//
//            /*
//            We fetch guess filters only if these total results are 2 or more. Less than two results
//            don't require any filtering.
//             */
//            if (pagedGuesses != null && pagedGuesses.size() > 1 /*&& pagedGuesses.size() > MIN_GUESSES*/
//                    && guessFilterLiveData.getValue() == null) {
//                initGuessFilterTask(guessQueryData);
////                initAlphaPresenceTask(queryData);
//            }
//        });
//    }

//    public LiveData<List<AlphaPresence>> getAlphaPresenceMedLiveData() {
//        return alphaPresenceMedLiveData;
//    }


    LiveData<List<Word>> getGuessesMedLiveData() {
        return guessesMedLiveData;
    }

    LiveData<PagingData<Word>> getGuessesPaging3MedLiveData() {
        return guessesPaging3MedLiveData;
    }

    LiveData<PagedList<Word>> getGuessesPagedMedLiveData() {
        return guessesPagedMedLiveData;
    }

    boolean areGuessFiltersFetched() {
        return guessFilterLiveData.getValue() != null;
    }


//    /**
//     * Use {@link com.appicacious.solword.tasks.QueryDataTask} instead
//     * This shifting was required because the above task is being used by
//     * {@link InputRepository#initRandomWordTask(List, int)} as well
//     *
//     */
//    @Deprecated
//    static class QueryDataTask extends MyAsyncTask<List<Cell>, Void, GuessQueryData> {
//
//        final OnTaskInteractionListener listener;
//        final int collCount;
//
//        public QueryDataTask(OnTaskInteractionListener listener, int collCount) {
//            this.listener = listener;
//            this.collCount = collCount;
//        }
//
//        @Override
//        protected GuessQueryData doInBackground(List<Cell> cells) {
//            return prepareAndGetQueryData(cells);
//        }
//
//        @Nullable
//        private GuessQueryData prepareAndGetQueryData(List<Cell> cells) {
//            LinkedHashMap<Integer, String> greens = new LinkedHashMap<>();
//            List<YellowPosition> yellowPositions = new ArrayList<>();
//            LinkedHashMap<String, Yellow> yellows = new LinkedHashMap<>();
//            List<String> greys = new ArrayList<>();
//
//            LinkedHashMap<Integer, List<Cell>> rows = new LinkedHashMap<>();
//            List<Cell> rowCells = null;
//
//            for (int i = 0; i < cells.size(); i++) {
//                Cell cell = cells.get(i);
//                int position = cell.getColId() + 1;
//
//                // Create rows from cells
//                if (cell.getColId() == 0 && !TextUtils.isEmpty(cell.getAlpha())) {
//                    rowCells = new ArrayList<>();
//                }
//
//                if (rowCells != null) {
//                    rowCells.add(cell);
//                    if (cell.getColId() == (collCount - 1)) {
//                        // Finalize the row
//                        rows.put(cell.getRowId(), rowCells);
//                        rowCells = null;
////
////                        // Create a new instance of rowCells for next row, if required
////                        if (i < (cells.size() - 1)) rowCells = new ArrayList<>();
//                    }
//                }
//
//
//                if (cell.getStatus() == GREY) {
//                    if (!TextUtils.isEmpty(cell.getAlpha())) greys.add(cell.getAlpha());
//                } else if (cell.getStatus() == GREEN) {
//                    greens.put(position, cell.getAlpha());
//                } else if (cell.getStatus() == YELLOW) {
//                    // TODO: 17/05/2022 This empty check should be removed as it is applicable to
//                    //  only the first entry in yellows.
//                    // Put values in yellowPositions
//                    if (!yellowPositions.isEmpty()) {
//                        boolean isMatched = false;
//                        for (YellowPosition yellowPosition : yellowPositions) {
//                            if (yellowPosition.getIndex() == position) {
//                                isMatched = true;
//                                yellowPosition.addAlpha(cell.getAlpha());
//                                break;
//                            }
//                        }
//                        if (!isMatched) {
//                            yellowPositions.add(new YellowPosition(position, cell.getAlpha()));
//                        }
//                    } else {
//                        yellowPositions.add(new YellowPosition(position, cell.getAlpha()));
//                    }
//
//                    // Put values in yellows
//                    if (yellows.containsKey(cell.getAlpha())) {
//                        Yellow yellow = yellows.get(cell.getAlpha());
//                        if (yellow != null) yellow.addPosition(position);
//                    } else {
//                        Yellow yellow = new Yellow(cell.getAlpha(), position);
//                        yellows.put(cell.getAlpha(), yellow);
//                    }
//                }
//            }
//
//            Log.d(TAG, "prepareAndGetQueryData: " + rows);
//
//            // Set greenCount & maxCountInWord fields for Yellow instances, if required
//            for (String yellowAlpha : yellows.keySet()) {
//                Yellow yellow = yellows.get(yellowAlpha);
//
//                if (yellow != null) {
//                    int greenCount = 0;
//                    for (Map.Entry<Integer, String> entry : greens.entrySet()) {
//                        if (entry.getValue().equals(yellowAlpha)) greenCount++;
//                    }
//                    if (greenCount > 0) yellow.setGreenCount(greenCount);
//
//
//                    int maxCountInWord = 1;
//                    for (Map.Entry<Integer, List<Cell>> row : rows.entrySet()) {
//                        int floatingMaxCount = 0;
//                        for (Cell cell : row.getValue()) {
//                            if (yellowAlpha.equals(cell.getAlpha()) && cell.getStatus() != GREY) {
//                                floatingMaxCount++;
//                            }
//                        }
//                        if (floatingMaxCount > maxCountInWord) maxCountInWord = floatingMaxCount;
//                    }
//                    if (maxCountInWord > 1) yellow.setMaxCountInWord(maxCountInWord);
//                }
//            }
//
//            Log.d(TAG, "prepareAndGetQueryData: yellows=" + yellows);
//
//            Log.d(TAG, "prepareAndGetQueryData: yellowsPositions=" + yellowPositions);
//            if (!greens.isEmpty() || !yellowPositions.isEmpty() || !greys.isEmpty()) {
//                return getQueryData(greens, yellowPositions, yellows, greys);
//            }
//
//            return null;
//        }
//
//        @SuppressLint("DefaultLocale")
//        private GuessQueryData getQueryData(@NonNull LinkedHashMap<Integer, String> greens,
//                                            @NonNull List<YellowPosition> yellowPositions,
//                                            @NonNull LinkedHashMap<String, Yellow> yellows,
//                                            @NonNull List<String> greys) {
//            StringBuilder query2 = new StringBuilder(" FROM wordles WHERE LENGTH(wordTitle) = ");
//            query2.append(collCount);
//
//            if (!greens.isEmpty()) {
//                query2.append(AND);
//                query2.append("LOWER(wordTitle) LIKE '");
//                for (int x = 1; x <= collCount; x++) {
//                    if (greens.containsKey(x)) query2.append(greens.get(x));
//                    else query2.append("_");
//                    if (x == collCount) query2.append("'");
//                }
//            }
//
//            List<Integer> notGreenPositions = new ArrayList<>();
//            for (int i = 1; i <= collCount; i++) {
//                if (!greens.containsKey(i)) notGreenPositions.add(i);
//            }
//
////            LinkedHashSet<String> allYellowAlphas = new LinkedHashSet<>();
//            if (!yellowPositions.isEmpty()) {
//                for (int x = 1; x <= yellowPositions.size(); x++) {
//                    YellowPosition yellowPosition = yellowPositions.get(x - 1);
////                    allYellowAlphas.addAll(yellowPosition.getAlphas());
//                    if (!greens.containsKey(yellowPosition.getIndex())) {
//                        String listString = getListString(yellowPosition.getAlphas());
//                        // TODO: 09/09/2022 This new check is applied so that blanks e.g.,
//                        //  l1 NOT IN do not get inserted in case the received listString is empty.
//                        if (!listString.isEmpty()) {
//                            query2.append(AND);
//                            query2.append(String.format(SUBSTR, yellowPosition.getIndex()))
//                                    .append(" NOT IN ").append(listString);
////                            query2.append("l").append(yellowPosition.getIndex()).append(" NOT IN ")
////                                    .append(listString);
//                        }
//                    }
//                }
//
//                for (Map.Entry<String, Yellow> yEntry : yellows.entrySet()) {
//                    // We would save possible positions of a misplaced alphabet in it.
//                    List<Integer> yPositions = new ArrayList<>();
//
//                    Yellow yellow = yEntry.getValue();
//                    if (yellow != null && (yellow.getGreenCount() < yellow.getMaxCountInWord())) {
//                        for (int i = 1; i <= collCount; i++) {
//                            if (!yellow.getPositions().contains(i) && !greens.containsKey(i))
//                                yPositions.add(i);
//                        }
//                    }
//                    if (!yPositions.isEmpty()) {
//                        /*
//                        trimmedWord is concatenation of those letter positions of the word where a
//                        yellow letter can possibly be.
//                        So, if yellow letter 'e' has possible positions 2, 3 & 5, trimmedWord would
//                        be l2||l3||l5 where '||' is used as concatenation keyword in Sqlite3.
//                         */
//                        StringBuilder trimmedWord = new StringBuilder();
//                        for (int i = 0; i < yPositions.size(); i++) {
//                            trimmedWord.append(String.format(SUBSTR, yPositions.get(i)));
////                            trimmedWord.append("l").append(yPositions.get(i));
//                            if (i < (yPositions.size() - 1)) trimmedWord.append("||");
//                        }
//
//                        /*
//                        If an alphabet, for example 'a', is required more than once (double or
//                        triple occurrence) in trimmedWord, concatAlpha would be %a%a% in case of 2
//                        occurrences and %a%a%a% in case of 3 occurrences.
//                         */
//                        int times = yellow.getMaxCountInWord() - yellow.getGreenCount();
//                        StringBuilder concatAlpha = new StringBuilder(yEntry.getKey());
//                        for (int i = 1; i < times; i++) {
//                            concatAlpha.append("%");
//                            concatAlpha.append(yEntry.getKey());
//                        }
////                        String concatAlpha = new String(new char[times]).replace("\0",
////                                yEntry.getKey());
//                        String concatStr = "LOWER(" + trimmedWord + ") LIKE '%" + concatAlpha + "%'";
//                        query2.append(AND).append(concatStr);
//                    }
//                }
//
//            }
//
//            if (!greys.isEmpty()) {
//                LinkedHashSet<String> refinedGreys = new LinkedHashSet<>();
//                for (String grey : greys) {
//                    if (greens.containsValue(grey) || yellows.containsKey(grey)) {
//                        if (yellows.containsKey(grey)) {
//                            Yellow yellow = yellows.get(grey);
//                            if (yellow != null
//                                    && (yellow.getGreenCount() == yellow.getMaxCountInWord())) {
//                                refinedGreys.add(grey);
//                            }
//                        } else {
//                            refinedGreys.add(grey);
//                        }
//                    } else {
//                        refinedGreys.add(grey);
//                    }
//                }
//
//                String listString = getListString(new ArrayList<>(refinedGreys));
//                // This new check is applied so that blanks e.g., l1 NOT IN do not
//                //  get inserted in case the list is empty. It may happen in case user enters the
//                //  same letter in all cells.
//                if (!listString.isEmpty()) {
//                    for (int x = 0; x < notGreenPositions.size(); x++) {
//                        int position = notGreenPositions.get(x);
//                        query2.append(AND);
//                        query2.append(String.format(SUBSTR, position)).append(" NOT IN ").append(listString);
////                        query2.append("l").append(position).append(" NOT IN ").append(listString);
//                    }
//                }
//            }
//
//            List<String> greens_ = new ArrayList<>(greens.values());
//            List<String> yellows_ = new ArrayList<>(yellows.keySet());
//            List<String> greys_ = new ArrayList<>(greys);
//            GuessQueryData data = new GuessQueryData(query2.toString(), greens_, yellows_, greys_,
//                    collCount);
//
//            Log.d(TAG, "getQueryData: " + GUESSES_QUERY_PREFIX + query2);
//            return data;
//        }
//
//        private String getListString(List<String> greys) {
//            StringBuilder builder = new StringBuilder();
//            for (int i = 1; i <= greys.size(); i++) {
//                if (i == 1) builder.append("(");
//                builder.append("'").append(greys.get(i - 1)).append("'");
//                if (i < greys.size()) {
//                    builder.append(",");
//                } else {
//                    builder.append(")");
//                }
//            }
//            return builder.toString();
//        }
//
//        @Override
//        protected void onPostExecute(GuessQueryData queryData) {
//            super.onPostExecute(queryData);
//            if (queryData != null) listener.onComplete(queryData);
//        }
//
//        public interface OnTaskInteractionListener {
//            void onComplete(GuessQueryData queryData);
//        }
//    }
}
