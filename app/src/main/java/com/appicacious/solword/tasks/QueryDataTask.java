package com.appicacious.solword.tasks;

import static com.appicacious.solword.constants.Constants.AND;
import static com.appicacious.solword.constants.Constants.GREEN;
import static com.appicacious.solword.constants.Constants.GREY;
import static com.appicacious.solword.constants.Constants.SUBSTR;
import static com.appicacious.solword.constants.Constants.YELLOW;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appicacious.solword.models.Cell;
import com.appicacious.solword.models.GuessQueryData;
import com.appicacious.solword.models.Yellow;
import com.appicacious.solword.models.YellowPosition;
import com.appicacious.solword.utilities.MyAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class QueryDataTask extends MyAsyncTask<List<Cell>, Void, GuessQueryData> {

    private static final String TAG = QueryDataTask.class.getSimpleName();

    final OnTaskInteractionListener listener;
    final int collCount;

    public QueryDataTask(OnTaskInteractionListener listener, int collCount) {
        this.listener = listener;
        this.collCount = collCount;
    }

    @Override
    protected GuessQueryData doInBackground(List<Cell> cells) {
        return prepareAndGetQueryData(cells);
    }

    @NonNull
    private GuessQueryData prepareAndGetQueryData(List<Cell> cells) {
        LinkedHashMap<Integer, String> greens = new LinkedHashMap<>();
        List<YellowPosition> yellowPositions = new ArrayList<>();
        LinkedHashMap<String, Yellow> yellows = new LinkedHashMap<>();
//        List<String> greys = new ArrayList<>();
        HashMap<String, HashSet<Integer>> greyPosMap = new HashMap<>();

        LinkedHashMap<Integer, List<Cell>> rows = new LinkedHashMap<>();
        List<Cell> rowCells = null;

        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            int position = cell.getColId() + 1;

            // Create rows from cells
            if (cell.getColId() == 0 && !TextUtils.isEmpty(cell.getAlpha())) {
                rowCells = new ArrayList<>();
            }

            if (rowCells != null) {
                rowCells.add(cell);
                if (cell.getColId() == (collCount - 1)) {
                    // Finalize the row
                    rows.put(cell.getRowId(), rowCells);
                    rowCells = null;
//
//                        // Create a new instance of rowCells for next row, if required
//                        if (i < (cells.size() - 1)) rowCells = new ArrayList<>();
                }
            }


            if (cell.getStatus() == GREY) {
                String grey = cell.getAlpha();
                if (!TextUtils.isEmpty(grey)) {
//                    greys.add(grey);
                    HashSet<Integer> positions = greyPosMap.get(grey);
                    if (positions == null) {
                        positions = new HashSet<>();
                        positions.add(cell.getColId() + 1);
                        greyPosMap.put(grey, positions);
                    } else {
                        positions.add(cell.getColId() + 1);
                    }
                }
            } else if (cell.getStatus() == GREEN) {
                greens.put(position, cell.getAlpha());
            } else if (cell.getStatus() == YELLOW) {
                // TODO: 17/05/2022 This empty check should be removed as it is applicable to
                //  only the first entry in yellows.
                // Put values in yellowPositions
                if (!yellowPositions.isEmpty()) {
                    boolean isMatched = false;
                    for (YellowPosition yellowPosition : yellowPositions) {
                        if (yellowPosition.getIndex() == position) {
                            isMatched = true;
                            yellowPosition.addAlpha(cell.getAlpha());
                            break;
                        }
                    }
                    if (!isMatched) {
                        yellowPositions.add(new YellowPosition(position, cell.getAlpha()));
                    }
                } else {
                    yellowPositions.add(new YellowPosition(position, cell.getAlpha()));
                }

                // Put values in yellows
                if (yellows.containsKey(cell.getAlpha())) {
                    Yellow yellow = yellows.get(cell.getAlpha());
                    if (yellow != null) yellow.addPosition(position);
                } else {
                    Yellow yellow = new Yellow(cell.getAlpha(), position);
                    yellows.put(cell.getAlpha(), yellow);
                }
            }
        }

        Log.d(TAG, "prepareAndGetQueryData: " + rows);

        // Set greenCount & maxCountInWord fields for Yellow instances, if required
        for (String yellowAlpha : yellows.keySet()) {
            Yellow yellow = yellows.get(yellowAlpha);

            if (yellow != null) {
                int greenCount = 0;
                for (Map.Entry<Integer, String> entry : greens.entrySet()) {
                    if (entry.getValue().equals(yellowAlpha)) greenCount++;
                }
                if (greenCount > 0) yellow.setGreenCount(greenCount);


                int maxCountInWord = 1;
                for (Map.Entry<Integer, List<Cell>> row : rows.entrySet()) {
                    int floatingMaxCount = 0;
                    for (Cell cell : row.getValue()) {
                        if (yellowAlpha.equals(cell.getAlpha()) && cell.getStatus() != GREY) {
                            floatingMaxCount++;
                        }
                    }
                    if (floatingMaxCount > maxCountInWord) maxCountInWord = floatingMaxCount;
                }
                if (maxCountInWord > 1) yellow.setMaxCountInWord(maxCountInWord);
            }
        }

        Log.d(TAG, "prepareAndGetQueryData: yellows=" + yellows);
        Log.d(TAG, "prepareAndGetQueryData: yellowsPositions=" + yellowPositions);
        Log.d(TAG, "prepareAndGetQueryData: greyPosMap=" + greyPosMap);
//        if (!greens.isEmpty() || !yellowPositions.isEmpty() || !greys.isEmpty()) {
            return getQueryData(greens, yellowPositions, yellows, /*greys, */greyPosMap);
//        }

//        return null;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    private GuessQueryData getQueryData(@NonNull LinkedHashMap<Integer, String> greens,
                                        @NonNull List<YellowPosition> yellowPositions,
                                        @NonNull LinkedHashMap<String, Yellow> yellows,
//                                        @NonNull List<String> greys,
                                        @NonNull HashMap<String, HashSet<Integer>> greyPosMap) {
        StringBuilder query = new StringBuilder(" FROM wordles WHERE source != 2 " +
                "AND LENGTH(wordTitle) = ");
        query.append(collCount);

        if (!greens.isEmpty()) {
            query.append(AND);
            query.append("LOWER(wordTitle) LIKE '");
            for (int x = 1; x <= collCount; x++) {
                if (greens.containsKey(x)) query.append(greens.get(x));
                else query.append("_");
                if (x == collCount) query.append("'");
            }
        }

        List<Integer> notGreenPositions = new ArrayList<>();
        for (int i = 1; i <= collCount; i++) {
            if (!greens.containsKey(i)) notGreenPositions.add(i);
        }

//            LinkedHashSet<String> allYellowAlphas = new LinkedHashSet<>();
        if (!yellowPositions.isEmpty()) {
            for (int x = 1; x <= yellowPositions.size(); x++) {
                YellowPosition yellowPosition = yellowPositions.get(x - 1);
//                    allYellowAlphas.addAll(yellowPosition.getAlphas());
                if (!greens.containsKey(yellowPosition.getIndex())) {
                    String listString = getListString(yellowPosition.getAlphas());
                    // TODO: 09/09/2022 This new check is applied so that blanks e.g.,
                    //  l1 NOT IN do not get inserted in case the received listString is empty.
                    if (!listString.isEmpty()) {
                        query.append(AND);
                        query.append(String.format(SUBSTR, yellowPosition.getIndex()))
                                .append(" NOT IN ").append(listString);
//                        query.append("l").append(yellowPosition.getIndex()).append(" NOT IN ")
//                                .append(listString);
                    }
                }
            }

            for (Map.Entry<String, Yellow> yEntry : yellows.entrySet()) {
                // We would save possible positions of a misplaced alphabet in it.
                List<Integer> yPossiblePositions = new ArrayList<>();

                Yellow yellow = yEntry.getValue();
                if (yellow != null && (yellow.getGreenCount() < yellow.getMaxCountInWord())) {
                    // This if condition means that all instances of this alphabet in a word have
                    // not yet been turned into greens.
                    for (int i = 1; i <= collCount; i++) {
                        // To qualify for a possible position, following conditions need to be
                        // fulfilled:
                        // 1) the current yellow alphabet should not be at this position
                        // 2) there should be no green at this position
                        // 3) the current yellow alphabet should not have been input as a grey at
                        // this position
                        if (!yellow.getPositions().contains(i) && !greens.containsKey(i)) {
                            // The following check was added when this error occurred:
                            // https://www.dropbox.com/s/kopaq8hefpu5zfw/1669954736534.jpg?dl=0
                            HashSet<Integer> greyPositions = greyPosMap.get(yellow.getAlpha());
                            if (greyPositions == null || !greyPositions.contains(i))
                                yPossiblePositions.add(i);
                        }
                    }
                }
                if (!yPossiblePositions.isEmpty()) {
                    /*
                    trimmedWord is concatenation of those letter positions of the word where a
                    yellow letter can possibly be.
                    So, if yellow letter 'e' has possible positions 2, 3 & 5, trimmedWord would
                    be l2||l3||l5 where '||' is used as concatenation keyword in Sqlite3.
                     */
                    StringBuilder trimmedWord = new StringBuilder();
                    for (int i = 0; i < yPossiblePositions.size(); i++) {
                        trimmedWord.append(String.format(SUBSTR, yPossiblePositions.get(i)));
//                        trimmedWord.append("l").append(yPossiblePositions.get(i));
                        if (i < (yPossiblePositions.size() - 1)) trimmedWord.append("||");
                    }

                    /*
                    If an alphabet, for example 'a', is required more than once (double or
                    triple occurrence) in trimmedWord, concatAlpha would be %a%a% in case of 2
                    occurrences and %a%a%a% in case of 3 occurrences.
                     */
                    int times = yellow.getMaxCountInWord() - yellow.getGreenCount();
                    StringBuilder concatAlpha = new StringBuilder(yEntry.getKey());
                    for (int i = 1; i < times; i++) {
                        concatAlpha.append("%");
                        concatAlpha.append(yEntry.getKey());
                    }
//                        String concatAlpha = new String(new char[times]).replace("\0",
//                                yEntry.getKey());
                    String concatStr = "LOWER(" + trimmedWord + ") LIKE '%" + concatAlpha + "%'";
                    query.append(AND).append(concatStr);
                }
            }

        }

        if (!greyPosMap.keySet().isEmpty()) {
            LinkedHashSet<String> refinedGreys = new LinkedHashSet<>();
            for (String grey : greyPosMap.keySet()) {
                if (greens.containsValue(grey) || yellows.containsKey(grey)) {
                    if (yellows.containsKey(grey)) {
                        Yellow yellow = yellows.get(grey);
                        if (yellow != null
                                && (yellow.getGreenCount() == yellow.getMaxCountInWord())) {
                            // This if condition means that all yellow instances of this alphabet
                            // in a word have already been turned into greens
                            refinedGreys.add(grey);
                        }
                    } else {
                        refinedGreys.add(grey);
                    }
                } else {
                    refinedGreys.add(grey);
                }
            }

            String listString = getListString(new ArrayList<>(refinedGreys));
            // TODO: 09/09/2022 This new check is applied so that blanks e.g., l1 NOT IN do not
            //  get inserted in case the list is empty. It may happen in case user enters the
            //  same letter in all cells.
            if (!listString.isEmpty()) {
                for (int x = 0; x < notGreenPositions.size(); x++) {
                    int position = notGreenPositions.get(x);
                    query.append(AND);
                    query.append(String.format(SUBSTR, position)).append(" NOT IN ")
                            .append(listString);
//                    query.append("l").append(position).append(" NOT IN ").append(listString);
                }
            }
        }

        List<String> greens_ = new ArrayList<>(greens.values());
        List<String> yellows_ = new ArrayList<>(yellows.keySet());
        List<String> greys_ = new ArrayList<>(greyPosMap.keySet());
        GuessQueryData data = new GuessQueryData(query.toString(), greens_, yellows_, greys_,
                collCount);

        Log.d(TAG, "getQueryData: " + query);
        return data;
    }

    private String getListString(List<String> greys) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= greys.size(); i++) {
            if (i == 1) builder.append("(");
            builder.append("'").append(greys.get(i - 1)).append("'");
            if (i < greys.size()) {
                builder.append(",");
            } else {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    @Override
    protected void onPostExecute(GuessQueryData queryData) {
        super.onPostExecute(queryData);
        if (queryData != null) listener.onComplete(queryData);
    }

    public interface OnTaskInteractionListener {
        void onComplete(GuessQueryData queryData);
    }
}
