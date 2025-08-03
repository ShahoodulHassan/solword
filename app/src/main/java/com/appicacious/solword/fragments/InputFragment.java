package com.appicacious.solword.fragments;

import static android.view.View.NO_ID;
import static com.appicacious.solword.constants.Constants.AD_FREE_CLICK_QUOTA;
import static com.appicacious.solword.constants.Constants.BLANK;
import static com.appicacious.solword.constants.Constants.GREEN;
import static com.appicacious.solword.constants.Constants.GREY;
import static com.appicacious.solword.constants.Constants.MODE_HELP;
import static com.appicacious.solword.constants.Constants.RANDOM_WORD_CLICK_INCREMENT;
import static com.appicacious.solword.constants.Constants.RC_ERROR_LOG;
import static com.appicacious.solword.constants.Constants.TAG_ERROR_LOG;
import static com.appicacious.solword.constants.Constants.TAG_NAV;
import static com.appicacious.solword.constants.Constants.YELLOW;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.models.Cell;
import com.appicacious.solword.ItemOffsetDecoration;
import com.appicacious.solword.R;
import com.appicacious.solword.models.Word;
import com.appicacious.solword.models.YellowPosition;
import com.appicacious.solword.adapters.CellAdapter;
import com.appicacious.solword.architecture.InputViewModel;
import com.appicacious.solword.dialog_fragments.ErrorLogsDialogFragment;
import com.appicacious.solword.utilities.Utilities;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class InputFragment extends BannerAdFragment implements CellAdapter.OnAdapterInteractionListener,
        View.OnClickListener {

    private CellAdapter mAdapter;

    private Toast errorToast;

    private RecyclerView rvCells;
    private GridLayoutManager layoutManager;
    private AppCompatTextView tvLabelWordSize;

//    private CellTask.OnCellTaskInteractionListener listener;

//    private MaterialButton mbA, mbB, mbC, mbD, mbE, mbF, mbG, mbH, mbI, mbJ, mbK, mbL, mbM, mbN,
//            mbO, mbP, mbQ, mbR, mbS, mbT, mbU, mbV, mbW, mbX, mbY, mbZ, mbEnter, mbBack;

    private InputViewModel inputViewModel;

    private Handler cellHandler;

    private YoYo.YoYoString devAnim;
    private AppCompatTextView tvDevInfo;

    private Handler reviewHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputViewModel = new ViewModelProvider(mActivity).get(InputViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, container, false);

        initializeViews(view);

        mListener.setStatusBarColor(R.color.white);

//        if (isFragmentNewlyCreated()) createCells();

        return view;
    }

    @Override
    void initializeViews(View view) {
        super.initializeViews(view);

        LinkedHashMap<String, MaterialButton> buttons = new LinkedHashMap<>();
        buttons.put("a", view.findViewById(R.id.mb_a));
        buttons.put("b", view.findViewById(R.id.mb_b));
        buttons.put("c", view.findViewById(R.id.mb_c));
        buttons.put("d", view.findViewById(R.id.mb_d));
        buttons.put("e", view.findViewById(R.id.mb_e));
        buttons.put("f", view.findViewById(R.id.mb_f));
        buttons.put("g", view.findViewById(R.id.mb_g));
        buttons.put("h", view.findViewById(R.id.mb_h));
        buttons.put("i", view.findViewById(R.id.mb_i));
        buttons.put("j", view.findViewById(R.id.mb_j));
        buttons.put("k", view.findViewById(R.id.mb_k));
        buttons.put("l", view.findViewById(R.id.mb_l));
        buttons.put("m", view.findViewById(R.id.mb_m));
        buttons.put("n", view.findViewById(R.id.mb_n));
        buttons.put("o", view.findViewById(R.id.mb_o));
        buttons.put("p", view.findViewById(R.id.mb_p));
        buttons.put("q", view.findViewById(R.id.mb_q));
        buttons.put("r", view.findViewById(R.id.mb_r));
        buttons.put("s", view.findViewById(R.id.mb_s));
        buttons.put("t", view.findViewById(R.id.mb_t));
        buttons.put("u", view.findViewById(R.id.mb_u));
        buttons.put("v", view.findViewById(R.id.mb_v));
        buttons.put("w", view.findViewById(R.id.mb_w));
        buttons.put("x", view.findViewById(R.id.mb_x));
        buttons.put("y", view.findViewById(R.id.mb_y));
        buttons.put("z", view.findViewById(R.id.mb_z));
        buttons.put("guess", view.findViewById(R.id.mb_guess));

//        MaterialButton back = view.findViewById(R.id.mb_back);
//        buttons.put("back", back);

        for (MaterialButton button : buttons.values()) {
            button.setOnClickListener(this);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    button,
                    10,
                    15,
                    1,
                    TypedValue.COMPLEX_UNIT_SP);
        }

        AppCompatImageView ivBack = view.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);

        FloatingActionButton fabInput = view.findViewById(R.id.fab_input);
        fabInput.setOnClickListener(this);

        ivBack.setOnLongClickListener(view1 -> {
            vibrate();
            deleteAll();
            return true;
        });

//        GridLayout glRows = view.findViewById(R.id.gl_rows);
//
//        glRows.setColumnCount(getColCount());
//        glRows.setRowCount(getRowCount());

        rvCells = view.findViewById(R.id.rv_cells);
        layoutManager = new GridLayoutManager(rvCells.getContext(), getColCount());
        ItemOffsetDecoration decoration = new ItemOffsetDecoration(mActivity, R.dimen.rv_padding_x);
        rvCells.addItemDecoration(decoration);
        rvCells.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        rvCells.setLayoutManager(layoutManager);
        rvCells.setAdapter(mAdapter = new CellAdapter(this));

        tvLabelWordSize = view.findViewById(R.id.tv_label_word_size);
        tvLabelWordSize.setText(String.format(getString(R.string.label_word_size), getColCount()));

        tvDevInfo = view.findViewById(R.id.tv_dev_info);

//        startDevAnimation();

    }

    private void startDevAnimation() {
        devAnim = YoYo.with(Techniques.Pulse)
                .duration(5000)
                .repeat(-1)
                .playOn(tvDevInfo);
    }

    private void stopDevAnimation() {
        if (devAnim != null && devAnim.isRunning()) devAnim.stop();
    }

    private void deleteAll() {
        if (getLastFilledCellNum() <= -1) {
            showErrorToast("Nothing to delete!");
        } else {
            inputViewModel.deleteAll();
//            List<Cell> newCells = Utilities.getCopyList(getExistingCells());
//            if (newCells != null) {
//                for (int i = 0; i <= getLastFilledCellNum(); i++) {
//                    Cell cell = newCells.get(i);
//                    cell.setAlpha("");
//                    if (cell.getStatus() != GREY) cell.setStatus(GREY);
//                }
//                inputViewModel.setLastFilledCellNum(NO_ID);
//                inputViewModel.setCells(newCells);
//            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isFragmentNewlyCreated() && mListener.getUsageCount() >= Constants.REVIEW_USAGE_QUOTA) {
            destroyHandler(reviewHandler);
            reviewHandler = new Handler();
            reviewHandler.postDelayed(this::launchReviewFlow, 2000);
        }

        billingViewModel.getIsPurchasedLiveData().observe(getViewLifecycleOwner(), isPurchased -> {
            Log.d(TAG, "onViewCreated: isPurchased=" + isPurchased);
            String subtitle = Boolean.TRUE.equals(isPurchased) ? getString(R.string.subtitle_premium) : null;
            setActionBarSubTitle(subtitle);
        });

        // This should receive non-null data only once
        inputViewModel.getRawCellsLiveData().observe(getViewLifecycleOwner(), event -> {
            Log.d(TAG + TAG_NAV, "onViewCreated: raw cells event=" + event);
            if (event != null) {
                if (!event.hasBeenHandled()) {
                    Log.d(TAG + TAG_NAV, "onViewCreated: creating raw cells");
                    List<Cell> cells = event.getContentIfNotHandled();
                    inputViewModel.setCells(cells);
                } else {
                    Log.d(TAG + TAG_NAV, "onViewCreated: raw cells event already consumed");
                }
            }
        });

        // This is the cells data that roams around for usage in the adapter
        inputViewModel.getCellsLiveData().observe(getViewLifecycleOwner(),
                cells -> {
//                    Log.d(TAG + TAG_NAV, "onViewCreated: cells=" + cells);
                    destroyHandler(cellHandler);
                    cellHandler = new Handler();
                    cellHandler.postDelayed(() -> {
                        if (inputViewModel.getLoadDelay() != 0) inputViewModel.setLoadDelay(0);
                        mAdapter.submitList(cells);

                        insertClickedGuessIfRequired(cells);

                    }, inputViewModel.getLoadDelay());
                });

        mainViewModel.getWordSizeLiveData().observe(getViewLifecycleOwner(), size -> {
            if (size != null) {
                if (size != mainViewModel.getCurrentWordSize()) {
                    mainViewModel.setCurrentWordSize(size);
                    if (layoutManager.getSpanCount() != size) {
                        tvLabelWordSize.setText(String.format(getString(R.string.label_word_size), size));
                        layoutManager.setSpanCount(size);
                        rvCells.setLayoutManager(layoutManager);
                    }
                    createNewCells();
                }
            }
        });

        inputViewModel.getRandomWordLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Log.d(TAG + TAG_NAV, "onViewCreated: randomWord event=" + event);
                String word = event.getContentIfNotHandled();
                if (word != null) {
                    Log.d(TAG_NAV, "onViewCreated: randomWord=" + word);
                    if (word.length() == getColCount()) {
                        showRandomWord(word);
                    } else {
                        serveFreshToast("Random word unavailable!");
                    }
                }
            }
        });

        if (isFragmentNewlyCreated()) {
            createNewCells();
//            // This new method is being used so that new and raw cells are created only when there
//            // are no existing cells in the repository.
//            // Repository will be empty on newly created fragment but when this fragment is
//            // recreated from backStack, then it should utilize the same repository as already
//            // created, and so, new cells won't be required
//            inputViewModel.createNewCellsIfRequired(getRowCount(), getColCount());
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG + TAG_NAV, "onSaveInstanceState: called");
        inputViewModel.saveState();
        super.onSaveInstanceState(outState);
    }

    /**
     * This method is called right after supplying the list of cells to the adapter
     * Checks if there is a clicked guess; if yes, insert it in the list of cells
     * Following are the checks applied:
     * 1. the clickedGuess shouldn't be null or blank.
     * 2. the list of cells shouldn't be null or empty and shouldn't have blank cells only. Returning
     * from GuessFragment, the list of cells actually shouldn't be empty because we inserted at
     * least one word before going to GuessFragment. However, it is being observed that sometimes,
     * while in GuessFragment, if we push the app to the background, come back later and press back
     * button, we see all blank cells. No reason has been found yet. So, if that happens and we get
     * a blank list, we don't entertain the clicked guess.
     * 3. the size of clicked guess word should correspond with the current word size of the app.
     *
     * If the clickedGuess is neither null nor empty, we use its value and then reset it by putting
     * null value into it.
     *
     * @param cells the list of cells that's just been supplied to the adapter
     */
    private void insertClickedGuessIfRequired(List<Cell> cells) {
        String clickedGuess = mainViewModel.getClickedGuess();

        // Check # 1
        if (!TextUtils.isEmpty(clickedGuess)) {
            String[] alphas = clickedGuess.split("");
            mainViewModel.setClickedGuess(null);

            // Check # 2
            if (cells != null && !cells.isEmpty() && cells.get(0).getStatus() != BLANK) {

                // Check # 3
                if (alphas.length == mainViewModel.getCurrentWordSize()) {
                    if (getLastFilledCellNum() < getLastCell()) {
                        for (String alpha : alphas) {
                            setCellText(alpha);
                        }
                        showErrorToast("Guess picked!");
                    } else {
                        showErrorToast("No more attempts left!");
                    }
                }
            }
        }


//        if (cells != null && !cells.isEmpty() && cells.get(0).getStatus() != BLANK) {
//            String clickedGuess = mainViewModel.getClickedGuess();
//            if (!TextUtils.isEmpty(clickedGuess)) {
//                String[] alphas = clickedGuess.split("");
//                mainViewModel.setClickedGuess(null);
//                if (alphas.length == mainViewModel.getCurrentWordSize()) {
//                    if (getLastFilledCellNum() < getLastCell()) {
//                        for (String alpha : alphas) {
//                            setCellText(alpha);
//                        }
//                        showErrorToast("Guess picked!");
//                    } else {
//                        showErrorToast("No more attempts left!");
//                    }
//                }
//            }
//        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_input, menu);
    }

//    @Override
//    public void onPrepareOptionsMenu(@NonNull Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        // Visible to free user only
//        MenuItem getPremium = menu.findItem(R.id.menu_get_premium);
//        if (getPremium != null) getPremium.setVisible(!mListener.isPurchased_Billing());
//
//        // Visible to premium user while testing only
//        MenuItem consume = menu.findItem(R.id.menu_consume);
//        if (consume != null) consume.setVisible(isDebugMode() && mListener.isPurchased());
//
////        // Visible to user while testing only
////        MenuItem intro = menu.findItem(R.id.menu_help);
////        if (intro != null) intro.setVisible(isDebugMode());
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_error_log) {
            String errorLog = getErrorLog();
            if (errorLog.isEmpty()) {
                showErrorToast("No errors found!");
            } else {
                showErrorDialog(errorLog);
            }
            return true;
        }
        else if (item.getItemId() == R.id.menu_settings) {
            NavDirections action = InputFragmentDirections.actionInputFragmentToSettingsFragment();
            navigateTo(action);
            return true;
        }
        else if (item.getItemId() == R.id.menu_help) {
            InputFragmentDirections.ActionInputFragmentToIntroFragment action =
                    InputFragmentDirections.actionInputFragmentToIntroFragment();
            action.setMMode(MODE_HELP);
            navigateTo(action);
            return true;
        }
//        else if (item.getItemId() == R.id.menu_get_premium) {
//            showGetPremiumDialog();
//            return true;
//        }
        else if (item.getItemId() == R.id.menu_about) {
            NavDirections action = InputFragmentDirections.actionInputFragmentToAboutFragment();
            navigateTo(action);
            return true;
        }
        else if (item.getItemId() == R.id.menu_rate_app) {
            mListener.initRateApp();
            return true;
        }
        else if (item.getItemId() == R.id.menu_share_app) {
            mListener.initShareApp();
            return true;
        }
//        else if (item.getItemId() == R.id.menu_consume) {
//            mListener.initConsumeFlow();
//            return true;
//        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    private int[] getCellIdsByCellNum(int cellNum) {
        int remainder = cellNum;
        if (cellNum >= 0) {
            for (int x = 0; x < (getRowCount()); x++) {
                if (remainder < getColCount()) {
                    return new int[]{x, remainder};
                }
                remainder = remainder - getColCount();
            }
        }
        return null;
    }

    private int getAttemptCount() {
        int[] ids = getCellIdsByCellNum(getLastFilledCellNum());
        if (ids == null) return 0;
        return ids[0] + 1;
    }

    private void createNewCells() {
        Log.d(TAG, "forceCreateNewCells: called");
//        inputViewModel.setLastFilledCellNum(NO_ID);
        inputViewModel.forceCreateNewCells(getRowCount(), getColCount());
    }

    @Override
    public void onDestroyView() {
        if (inputViewModel != null && inputViewModel.getLoadDelay() == 0) {
            inputViewModel.setLoadDelay(Constants.LOAD_DELAY);
        }
        destroyHandlers(cellHandler, reviewHandler);
        stopDevAnimation();

        super.onDestroyView();
    }

    private String getErrorLog(List<Cell> _cells) {
        List<Cell> cells = _cells != null ? getExistingCells() : _cells;
        List<Word> greens = new ArrayList<>();
        List<YellowPosition> yellowPositions = new ArrayList<>();
        List<Word> greys = new ArrayList<>();
        LinkedHashSet<Integer> defectedGreenPositions = new LinkedHashSet<>();

        // First collect cells
        for (Cell cell : cells) {
            int position = cell.getColId() + 1;
            if (cell.getStatus() == GREY) {
                if (!TextUtils.isEmpty(cell.getAlpha()))
                    greys.add(new Word(position, cell.getAlpha(), false));
            } else if (cell.getStatus() == GREEN) {
                for (Word green : greens) {
                    if (green.get_id() == position) {
                        if (!Objects.equals(cell.getAlpha(), green.getWordTitle())) {
                            defectedGreenPositions.add(position);
                        }
                    }
                }
                greens.add(new Word(position, cell.getAlpha(), false));
            } else {
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
            }
        }

        // Now verify cells data
        StringBuilder builder = new StringBuilder();

        // Find out duplicate green errors
        if (!defectedGreenPositions.isEmpty()) {
            List<Integer> positions = new ArrayList<>(defectedGreenPositions);
            Collections.sort(positions);
            builder.append("Only one green alphabet be entered at each position.\nRecheck positions: ");
            for (Iterator<Integer> iterator = positions.iterator(); iterator.hasNext(); ) {
                int pos = iterator.next();
                builder.append(pos);
                if (iterator.hasNext()) {
                    builder.append(", ");
                } else {
                    builder.append("\n\n");
                }
            }
        }

        // TODO: 27/03/2022 Also check for the error where a yellow alphabet is entered at each
        //  position in one word or across multiple words.
        //  For example, 'i' can't be used at each position and marked yellow, because if it is in
        //  the word, there should be at least one position where it should be marked green.
        if (!yellowPositions.isEmpty() || !greys.isEmpty()) {
            if (!yellowPositions.isEmpty()) {
                List<Integer> defectedYellowGreenPositions = new ArrayList<>();
                for (YellowPosition yellowPosition : yellowPositions) {
                    int position = yellowPosition.getIndex();
                    String gAlpha = null;

                    // Find out yellow-green errors
                    if (!defectedYellowGreenPositions.contains(position)) {
                        for (Word green : greens) {
                            if (green.get_id() == position) {
                                gAlpha = green.getWordTitle();
                                break;
                            }
                        }

                        if (gAlpha != null) {
                            for (String yAlpha : yellowPosition.getAlphas()) {
                                if (yAlpha.equals(gAlpha)) {
                                    defectedYellowGreenPositions.add(position);
                                    builder.append("'").append(gAlpha.toUpperCase())
                                            .append("' is both yellow and green at position ")
                                            .append(position).append("\n\n");
                                    break;
                                }
                            }
                        }
                    }

                    // Find out yellow-grey error
//                    List<Integer> defectedYellowGreyPositions = new ArrayList<>();
//                    String greyAlpha;
//                    if (!defectedYellowGreyPositions.contains(position)) {
//                        for (Word grey : greys) {
//                            if (grey.get_id() == position) {
//                                greyAlpha = grey.getWordTitle();
//                                for (String yAlpha : yellow.getAlphas()) {
//                                    if (Objects.equals(greyAlpha, yAlpha)) {
//                                        defectedYellowGreyPositions.add(position);
//                                        builder.append("'").append(greyAlpha.toUpperCase())
//                                                .append("' is both yellow and grey at position ")
//                                                .append(position).append("\n\n");
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
            }

            // Find out grey-green errors
            if (!greys.isEmpty()) {
                List<Integer> defectedGreyGreenPositions = new ArrayList<>();
                for (Word grey : greys) {
                    int position = (int) grey.get_id();
                    String gAlpha = null;
                    if (!defectedGreyGreenPositions.contains(position)) {
                        for (Word green : greens) {
                            if (green.get_id() == position) {
                                gAlpha = green.getWordTitle();
                                break;
                            }
                        }
                        if (gAlpha != null) {
                            if (grey.getWordTitle().equals(gAlpha)) {
                                defectedGreyGreenPositions.add(position);
                                builder.append("'").append(gAlpha.toUpperCase())
                                        .append("' is both grey and green at position ")
                                        .append(position).append("\n\n");
                            }
                        }
                    }
                }
            }
        }
        return builder.toString();
    }

    @NonNull
    private String getErrorLog() {
        return getErrorLog(getExistingCells());
//        List<Cell> cells = getExistingCells();
//        List<Word> greens = new ArrayList<>();
//        List<YellowPosition> yellowPositions = new ArrayList<>();
//        List<Word> greys = new ArrayList<>();
//        LinkedHashSet<Integer> defectedGreenPositions = new LinkedHashSet<>();
//
//        // First collect cells
//        for (Cell cell : cells) {
//            int position = cell.getColId() + 1;
//            if (cell.getStatus() == GREY) {
//                if (!TextUtils.isEmpty(cell.getAlpha()))
//                    greys.add(new Word(position, cell.getAlpha(), false));
//            } else if (cell.getStatus() == GREEN) {
//                for (Word green : greens) {
//                    if (green.get_id() == position) {
//                        if (!Objects.equals(cell.getAlpha(), green.getWordTitle())) {
//                            defectedGreenPositions.add(position);
//                        }
//                    }
//                }
//                greens.add(new Word(position, cell.getAlpha(), false));
//            } else {
//                if (!yellowPositions.isEmpty()) {
//                    boolean isMatched = false;
//                    for (YellowPosition yellowPosition : yellowPositions) {
//                        if (yellowPosition.getIndex() == position) {
//                            isMatched = true;
//                            yellowPosition.addAlpha(cell.getAlpha());
//                            break;
//                        }
//                    }
//                    if (!isMatched) {
//                        yellowPositions.add(new YellowPosition(position, cell.getAlpha()));
//                    }
//                } else {
//                    yellowPositions.add(new YellowPosition(position, cell.getAlpha()));
//                }
//            }
//        }
//
//        // Now verify cells data
//        StringBuilder builder = new StringBuilder();
//
//        // Find out duplicate green errors
//        if (!defectedGreenPositions.isEmpty()) {
//            List<Integer> positions = new ArrayList<>(defectedGreenPositions);
//            Collections.sort(positions);
//            builder.append("Only one green alphabet be entered at each position.\nRecheck positions: ");
//            for (Iterator<Integer> iterator = positions.iterator(); iterator.hasNext(); ) {
//                int pos = iterator.next();
//                builder.append(pos);
//                if (iterator.hasNext()) {
//                    builder.append(", ");
//                } else {
//                    builder.append("\n\n");
//                }
//            }
//        }
//
//        // TODO: 27/03/2022 Also check for the error where a yellow alphabet is entered at each
//        //  position in one word or across multiple words.
//        //  For example, 'i' can't be used at each position and marked yellow, because if it is in
//        //  the word, there should be at least one position where it should be marked green.
//        if (!yellowPositions.isEmpty() || !greys.isEmpty()) {
//            if (!yellowPositions.isEmpty()) {
//                List<Integer> defectedYellowGreenPositions = new ArrayList<>();
//                for (YellowPosition yellowPosition : yellowPositions) {
//                    int position = yellowPosition.getIndex();
//                    String gAlpha = null;
//
//                    // Find out yellow-green errors
//                    if (!defectedYellowGreenPositions.contains(position)) {
//                        for (Word green : greens) {
//                            if (green.get_id() == position) {
//                                gAlpha = green.getWordTitle();
//                                break;
//                            }
//                        }
//
//                        if (gAlpha != null) {
//                            for (String yAlpha : yellowPosition.getAlphas()) {
//                                if (yAlpha.equals(gAlpha)) {
//                                    defectedYellowGreenPositions.add(position);
//                                    builder.append("'").append(gAlpha.toUpperCase())
//                                            .append("' is both yellow and green at position ")
//                                            .append(position).append("\n\n");
//                                    break;
//                                }
//                            }
//                        }
//                    }
//
//                    // Find out yellow-grey error
////                    List<Integer> defectedYellowGreyPositions = new ArrayList<>();
////                    String greyAlpha;
////                    if (!defectedYellowGreyPositions.contains(position)) {
////                        for (Word grey : greys) {
////                            if (grey.get_id() == position) {
////                                greyAlpha = grey.getWordTitle();
////                                for (String yAlpha : yellow.getAlphas()) {
////                                    if (Objects.equals(greyAlpha, yAlpha)) {
////                                        defectedYellowGreyPositions.add(position);
////                                        builder.append("'").append(greyAlpha.toUpperCase())
////                                                .append("' is both yellow and grey at position ")
////                                                .append(position).append("\n\n");
////                                        break;
////                                    }
////                                }
////                            }
////                        }
////                    }
//                }
//            }
//
//            // Find out grey-green errors
//            if (!greys.isEmpty()) {
//                List<Integer> defectedGreyGreenPositions = new ArrayList<>();
//                for (Word grey : greys) {
//                    int position = (int) grey.get_id();
//                    String gAlpha = null;
//                    if (!defectedGreyGreenPositions.contains(position)) {
//                        for (Word green : greens) {
//                            if (green.get_id() == position) {
//                                gAlpha = green.getWordTitle();
//                                break;
//                            }
//                        }
//                        if (gAlpha != null) {
//                            if (grey.getWordTitle().equals(gAlpha)) {
//                                defectedGreyGreenPositions.add(position);
//                                builder.append("'").append(gAlpha.toUpperCase())
//                                        .append("' is both grey and green at position ")
//                                        .append(position).append("\n\n");
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return builder.toString();
    }

    private boolean isDataValid(List<Cell> cells) {
        String errorLog = getErrorLog(cells);
        if (!TextUtils.isEmpty(errorLog)) showErrorDialog(errorLog);
        return TextUtils.isEmpty(errorLog);
    }

    private boolean isDataValid() {
        return isDataValid(getExistingCells());
//        String errorLog = getErrorLog();
//        if (!TextUtils.isEmpty(errorLog)) showErrorDialog(errorLog);
//        return TextUtils.isEmpty(errorLog);
    }

    private void showErrorDialog(String errorLog) {
        ErrorLogsDialogFragment fragment = ErrorLogsDialogFragment.newInstance(RC_ERROR_LOG,
                errorLog);
        fragment.show(getChildFragmentManager(), TAG_ERROR_LOG);
    }

    @Override
    public void onCellClicked(Cell cell) {
        int status = cell.getStatus();
        if (status != Constants.BLANK) {
            if (status == GREY) {
                status = GREEN;
            } else if (status == GREEN) {
                status = YELLOW;
            } else {
                status = GREY;
            }
            vibrate();
            inputViewModel.setStatus(cell.getId(), status);
        }

    }

    public int getLastFilledCellNum() {
        return inputViewModel.getLastFilledCellNum();
    }

    public void setLastFilledCellNum(int lastFilledCellNum) {
        inputViewModel.setLastFilledCellNum(lastFilledCellNum);
    }

    private void setCellText(String alpha) {
        if (getLastFilledCellNum() < getLastCell()) {
            inputViewModel.setAlpha(alpha);
        } else {
            showErrorToast("No more attempts left!");
        }
    }

    @Override
    public void setShouldAnimate(int colId, int rowId, boolean shouldAnimate) {
        inputViewModel.setShouldAnimate(colId, rowId, shouldAnimate);
    }

    private void delCellText() {
        if (getLastFilledCellNum() > NO_ID) {
            inputViewModel.delAlpha();
        } else {
//            showErrorToast("No alphabets to delete!");
        }
    }

    private void showErrorToast(String message) {
        if (errorToast != null) errorToast.cancel();
        errorToast = Toast.makeText(mActivity, message, Toast.LENGTH_SHORT);
        errorToast.show();
    }

    private boolean isRowComplete() {
        if (getLastFilledCellNum() == NO_ID) return false;
        int[] ids = getCellIdsByCellNum(getLastFilledCellNum());
        return ids != null && ids[1] == getLastCol();
    }

    @Override
    public void onClick(View view) {
        vibrate();
        if (view.getId() == R.id.mb_a) {
            setCellText("a");
        } else if (view.getId() == R.id.mb_b) {
            setCellText("b");
        } else if (view.getId() == R.id.mb_c) {
            setCellText("c");
        } else if (view.getId() == R.id.mb_d) {
            setCellText("d");
        } else if (view.getId() == R.id.mb_e) {
            setCellText("e");
        } else if (view.getId() == R.id.mb_f) {
            setCellText("f");
        } else if (view.getId() == R.id.mb_g) {
            setCellText("g");
        } else if (view.getId() == R.id.mb_h) {
            setCellText("h");
        } else if (view.getId() == R.id.mb_i) {
            setCellText("i");
        } else if (view.getId() == R.id.mb_j) {
            setCellText("j");
        } else if (view.getId() == R.id.mb_k) {
            setCellText("k");
        } else if (view.getId() == R.id.mb_l) {
            setCellText("l");
        } else if (view.getId() == R.id.mb_m) {
            setCellText("m");
        } else if (view.getId() == R.id.mb_n) {
            setCellText("n");
        } else if (view.getId() == R.id.mb_o) {
            setCellText("o");
        } else if (view.getId() == R.id.mb_p) {
            setCellText("p");
        } else if (view.getId() == R.id.mb_q) {
            setCellText("q");
        } else if (view.getId() == R.id.mb_r) {
            setCellText("r");
        } else if (view.getId() == R.id.mb_s) {
            setCellText("s");
        } else if (view.getId() == R.id.mb_t) {
            setCellText("t");
        } else if (view.getId() == R.id.mb_u) {
            setCellText("u");
        } else if (view.getId() == R.id.mb_v) {
            setCellText("v");
        } else if (view.getId() == R.id.mb_w) {
            setCellText("w");
        } else if (view.getId() == R.id.mb_x) {
            setCellText("x");
        } else if (view.getId() == R.id.mb_y) {
            setCellText("y");
        } else if (view.getId() == R.id.mb_z) {
            setCellText("z");
        } else if (view.getId() == R.id.mb_guess) {
            if (isRowComplete()) {
                if (isDataValid()) {
                    mListener.incrementUsageCount(1);
                    List<Cell> cells = getExistingCells();
                    int attemptCount = getAttemptCount();
                    Log.d(TAG, "onClick: attemptCount=" + attemptCount);
                    NavDirections action = InputFragmentDirections.actionInputFragmentToGuessFragment(
                            cells.toArray(new Cell[0]), attemptCount);
                    navigateTo(action);
                }
            } else {
                showErrorToast("Incomplete word!");
            }
        } else if (view.getId() == R.id.iv_back) {
            delCellText();
        } else if (view.getId() == R.id.fab_input) {
            vibrate();

            if (isPurchased()) {
                fetchRandomWord();
            } else {
                int count = mListener.getClickCount();
                if (count < AD_FREE_CLICK_QUOTA) {
                    mListener.setClickCount(count + RANDOM_WORD_CLICK_INCREMENT);
                    fetchRandomWord();
                } else {
                    if (mListener.showInterstitialIfReady()) {
                        interstitialRunnable = () -> {
                            if (mListener != null) mListener.setClickCount(0);
                            fetchRandomWord();
                        };
                    } else {
                        internetRunnable = this::fetchRandomWord;
                        checkInternet();
                    }
                }
            }
        }
    }

    @NonNull
    private List<Cell> getExistingCells() {
        return inputViewModel.getCells();
    }


    /**
     *
     * <b>If complete row has been input:</b>
     *  - if it is the last row, validation is performed on all cells except those of the last row.
     *  To achieve this, we reset the alpha of each cell of the last row.
     *
     *  - if it is not the last row, validation is performed on all rows
     *
     * <b>If complete row has not been input</b>, validation is performed on all cells except those
     * of the last row. To achieve this, we reset the alpha of each cell of the last row.
     *
     * If no cell has been entered yet, newCells will be empty and so {@link #isDataValid} will
     * return true.
     *
     */
    private void fetchRandomWord() {
        if (isRowComplete()) {
            List<Cell> existingCells = getExistingCells();
            int[] ids = getCellIdsByCellNum(getLastFilledCellNum());
            if (ids != null && ids.length > 0) {
                int rowId = ids[0];
                if (rowId == getLastRow()) {
                    List<Cell> newCells = new ArrayList<>();
                    for (Cell cell : existingCells) {
                        Cell copy = new Cell(cell);
                        if (cell.getRowId() == rowId) copy.setAlpha("");
                        newCells.add(copy);
                    }
                    if (isDataValid(newCells)) {
                        inputViewModel.fetchRandomWord(newCells, getColCount());
                    }
                } else {
                    if (isDataValid(existingCells)) {
                        inputViewModel.fetchRandomWord(existingCells, getColCount());
                    }
                }
            }
        } else {
            List<Cell> existingCells = Utilities.getCopyList(getExistingCells());
            if (existingCells != null) {
                List<Cell> newCells = new ArrayList<>();
                int[] ids = getCellIdsByCellNum(getLastFilledCellNum());
                if (ids != null && ids.length > 0) {
                    int rowId = ids[0];
                    for (Cell cell : existingCells) {
                        Cell copy = new Cell(cell);
                        if (cell.getRowId() == rowId) copy.setAlpha("");
                        newCells.add(copy);
                    }
//                    for (Cell cell : existingCells) {
//                        if (cell.getRowId() != rowId) newCells.add(cell);
//                    }
                }
                if (isDataValid(newCells)) {
                    inputViewModel.fetchRandomWord(newCells, getColCount());
                }
            }
        }
    }


    /**
     * Use {@link #fetchRandomWord()} instead
     *
     */
    @Deprecated
    private void fetchRandomWordBySize(int colCount) {
        inputViewModel.fetchRandomWordBySize(colCount);
    }

    /**
     * This method figures out where to insert the random word and then arranges to insert it
     *
     * If user is already on last row, the word will be inserted on that very row.
     * If user is not on last row and the row is incomplete, the word will be inserted on that
     * very row.
     * If user is not on last row and the row is complete, the word will be inserted on the next
     * row.
     *
     * @param word to be inserted
     */
    private void showRandomWord(@NonNull String word) {
        int[] ids = getCellIdsByCellNum(getLastFilledCellNum());
        if (ids != null) {
            int rowId = ids[0];
            int colId = ids[1];
//            if (colId == getLastCol()) {
//                if (rowId == getLastRow()) {
//                    setLastFilledCellNum(getLastFilledCellNum() - (colId + 1));
//                }
//            } else {
//                setLastFilledCellNum(getLastFilledCellNum() - (colId + 1));
//            }

            if (rowId == getLastRow() || !isRowComplete()) {
                setLastFilledCellNum(getLastFilledCellNum() - (colId + 1));
            }
//            String[] alphas = word.split("");
//            for (String alpha : alphas) setCellText(alpha);
//            showErrorToast("Random word inserted!");
        } else {
//            String[] alphas = word.split("");
//            for (String alpha : alphas) setCellText(alpha);
//            showErrorToast("Random word inserted!");
        }
        String[] alphas = word.split("");
        for (String alpha : alphas) setCellText(alpha);
        showErrorToast("Random word inserted!");
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
