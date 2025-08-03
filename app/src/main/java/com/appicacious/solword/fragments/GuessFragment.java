package com.appicacious.solword.fragments;

import static com.appicacious.solword.constants.Constants.AD_FREE_CLICK_QUOTA;
import static com.appicacious.solword.constants.Constants.AND;
import static com.appicacious.solword.constants.Constants.DICTIONARY_CLICK_INCREMENT;
import static com.appicacious.solword.constants.Constants.FILTER_CLICK_INCREMENT;
import static com.appicacious.solword.constants.Constants.GUESS_PICK_INCREMENT;
import static com.appicacious.solword.constants.Constants.MODE_HELP;
import static com.appicacious.solword.constants.Constants.OR;
import static com.appicacious.solword.constants.Constants.TAG_NAV;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appicacious.solword.ItemOffsetDecoration;
import com.appicacious.solword.NpaGridLayoutManager;
import com.appicacious.solword.R;
import com.appicacious.solword.adapters.GuessAdapter;
import com.appicacious.solword.adapters.GuessFilterAdapter;
import com.appicacious.solword.adapters.GuessPagerAdapter;
import com.appicacious.solword.adapters.GuessPagingDataAdapter;
import com.appicacious.solword.architecture.GuessViewModel;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.models.Cell;
import com.appicacious.solword.models.Dictionary;
import com.appicacious.solword.models.GuessFilter;
import com.appicacious.solword.models.Word;
import com.appicacious.solword.utilities.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class GuessFragment extends BaseFragment implements
        GuessPagerAdapter.OnAdapterInteractionListener, GuessFilterAdapter.OnAdapterInteractionListener,
        GuessPagingDataAdapter.OnAdapterInteractionListener, GuessAdapter.OnAdapterInteractionListener {

    private static final int SPAN_COUNT = 2;
    private static final String KEY_OPERATOR = "key_operator";

//    private GuessAdapter mAdapter2;
    private GuessPagingDataAdapter mAdapter;
    private Function1<CombinedLoadStates, Unit> mLoadStateListener;
    List<Cell> mCells;
    private int mAttemptCount;

    private GuessViewModel mViewModel;

    private ConstraintLayout clOverlay;
    private RecyclerView rvGuesses;
    private Group gGuessFilters;
    private AppCompatTextView tvOperator;
    private GuessFilterAdapter guessFilterAdapter;

//    QueryTask.OnTaskInteractionListener listener;

    private Word clickedWord;

    private Handler fetchHandler, loadHandler;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCells = Arrays.asList(GuessFragmentArgs.fromBundle(getArguments()).getMCells());
        mAttemptCount = GuessFragmentArgs.fromBundle(getArguments()).getMAttemptCount();

        mViewModel = new ViewModelProvider(this).get(GuessViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guess, container, false);

        initializeViews(view);

        return view;
    }


    @Override
    void initializeViews(View view) {
        super.initializeViews(view);

        clOverlay = view.findViewById(R.id.cl_overlay);

        RecyclerView rvGuessFilters = view.findViewById(R.id.rv_guess_filters);
        rvGuessFilters.setLayoutManager(new LinearLayoutManager(rvGuessFilters.getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        ItemOffsetDecoration decoration1 = new ItemOffsetDecoration(rvGuessFilters.getContext(),
                R.dimen.rv_padding_x);
        rvGuessFilters.addItemDecoration(decoration1);
        rvGuessFilters.setAdapter(guessFilterAdapter = new GuessFilterAdapter(this));

        gGuessFilters = view.findViewById(R.id.g_guess_filters);
        tvOperator = view.findViewById(R.id.tv_operator);
        tvOperator.setOnClickListener(v -> {
            vibrate();
            mListener.incrementUsageCount(1);
            if (isPurchased()) {
                switchFilterOperator();
            } else {
                int count = mListener.getClickCount();
                if (count < AD_FREE_CLICK_QUOTA) {
                    mListener.setClickCount(count + FILTER_CLICK_INCREMENT);
                    switchFilterOperator();
                } else {
                    if (mListener.showInterstitialIfReady()) {
                        interstitialRunnable = () -> {
                            if (mListener != null) mListener.setClickCount(0);
                            switchFilterOperator();
                        };
                    } else {
                        internetRunnable = this::switchFilterOperator;
                        checkInternet();
                    }
                }

//                int count = mListener.getGuessFilterCount();
//                if (count < FILTER_AD_FREE_COUNT) {
//                    mListener.setGuessFilterCount(count + 1);
//                    switchFilterOperator();
//                } else {
//                    if (mListener.showInterstitialIfReady()) {
//                        interstitialRunnable = () -> {
//                            if (mListener != null) mListener.setGuessFilterCount(0);
//                            switchFilterOperator();
//                        };
//                    } else {
//                        internetRunnable = this::switchFilterOperator;
//                        checkInternet();
//                    }
//                }
            }
        });

        NpaGridLayoutManager gridLayoutManager = new NpaGridLayoutManager(mActivity, SPAN_COUNT);
        rvGuesses = view.findViewById(R.id.rv_guesses);
        rvGuesses.setLayoutManager(gridLayoutManager);
        ItemOffsetDecoration decoration2 = new ItemOffsetDecoration(rvGuesses.getContext(),
                R.dimen.rv_padding_x);
        rvGuesses.addItemDecoration(decoration2);

        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
//                Log.d(TAG, "getSpanSize: called for " + position);
                /* Try to get it: Here we calculate how much span size the item at this position
                 should have, out of the total span count assigned to this GridLayoutManager.
                 In this case, a span size of 1 means the item would get 1/4th of the total
                 span count which is 1. In order words, such items will be 2 (1+1) in a row.*/
                return mAdapter/*mAdapter2*/.getItemViewType(position) == GuessAdapter.AD_VIEW_TYPE
                        ? gridLayoutManager.getSpanCount() : 1;
            }
        };
        spanSizeLookup.setSpanIndexCacheEnabled(true);
        gridLayoutManager.setSpanSizeLookup(spanSizeLookup);

//        rvGuesses.setAdapter(mAdapter2 = new GuessAdapter(this, getColCount()));
        rvGuesses.setAdapter(mAdapter = new GuessPagingDataAdapter(this, getColCount()));

        FastScrollerBuilder builder = new FastScrollerBuilder(rvGuesses);
        builder.build();
    }

    private void switchFilterOperator() {
        if (mViewModel != null) {
            String currOp = getSelectedOperator();
            int strRes = Objects.equals(currOp, AND) ? R.string.sqlite_or : R.string.sqlite_and;
            String operator = Objects.equals(currOp, AND) ? OR : AND;
            tvOperator.setText(strRes);
            if (mViewModel.isOperatorApplicable()) {
                mViewModel.initGuessFilterQueryTask(getSelectedGuessFilters(), operator);
            }
        }
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null)
            tvOperator.setText(savedInstanceState.getString(KEY_OPERATOR));

        setActionBarSubTitle("Loading...");

//        // It fires an event whenever an interstitial is seen by the user. If there is any runnable
//        // to be executed, we may execute here.
//        mainViewModel.getIsInterstitialSeenLiveData().observe(getViewLifecycleOwner(), event -> {
//            if (event != null && Boolean.TRUE.equals(event.getContentIfNotHandled())) {
//                if (interstitialRunnable != null) {
//                    if (!isPurchased()) interstitialRunnable.run();
//                    interstitialRunnable = null;
//                }
//            }
//        });
//
//        mainViewModel.getIsInternetAvailableLiveData().observe(getViewLifecycleOwner(),
//                isAvailable -> {
//                    Log.d(TAG, "onViewStateRestored: isAvailable=" + isAvailable);
//                    if (isAvailable != null && isAvailable) {
//                        if (internetRunnable != null) {
//                            if (!isPurchased()) internetRunnable.run();
//                            internetRunnable = null;
//                        }
//                    } else {
//                        if (internetRunnable != null) {
//                            if (!isPurchased()) {
//                                if (internetToast != null) internetToast.cancel();
//                                internetToast = Toast.makeText(mActivity,
//                                        "This feature requires internet connection!",
//                                        Toast.LENGTH_SHORT);
//                                internetToast.show();
//                            }
//                            // Reset
//                            internetRunnable = null;
//                        }
//                    }
//                });

        billingViewModel.getIsPurchasedLiveData().observe(getViewLifecycleOwner(), isPurchased -> {
            Log.d(TAG, "onViewCreated: isPurchased=" + isPurchased);
//            if (Boolean.TRUE.equals(isPurchased)) {
                if (mAdapter != null) {
                    // We call this method before refreshing the adapter in order to remove the
                    // ads in active view holders.
                    mAdapter.notifyHolders();
                    mAdapter.refresh();
                }
//            }
        });

//        mainViewModel.getAdNetworkInitializedLiveData().observe(getViewLifecycleOwner(),
//                isInitialized -> {
//            if (Boolean.FALSE.equals(isInitialized)) {
//                if (mAdapter != null) {
//                    // We call this method before refreshing the adapter in order to remove the
//                    // ads in active view holders.
//                    mAdapter.notifyHolders();
//                    mAdapter.refresh();
//                }
//            }
//        });

        mViewModel.getGuessFilterLiveData().observe(getViewLifecycleOwner(), guessFilters -> {
            Log.d(TAG, "onChanged: guessFilters=" + guessFilters);
            if (guessFilters != null && guessFilters.size() > 0) {
                gGuessFilters.setVisibility(View.VISIBLE);
                guessFilterAdapter.submitList(guessFilters);
            } else {
                gGuessFilters.setVisibility(View.GONE);
            }
        });

        // Paging 2
//        mViewModel.getGuessesPagedLiveData().observe(getViewLifecycleOwner(), words -> {
//            mAdapter.submitList(words);
//            if (words != null) {
//                int count = words.size();
//                Log.d(TAG, "onViewStateRestored: guessCountReceived=" + count);
//                long delay = count == 0 ? Constants.LOAD_DELAY : 0;
//                if (countHandler != null) countHandler.removeCallbacksAndMessages(null);
//                countHandler = new Handler();
//                countHandler.postDelayed(() -> setActionBarSubTitle("(" + count + ")"), delay);
//            }
//        });

        /*
        PagingData that we received in case of Paging 3 library, doesn't tell the size of the items,
        which we require for initiating guess filter task and updating the subTitle of Toolbar.
        To cater this, problem, we add a listener to the PagingDataAdapter and listen to the event
        when data stop loading; we then get item count from the adapter and do our jobs.
         */
        mAdapter.addLoadStateListener(getmLoadStateListener());

        // No Paging
//        mViewModel.getGuessesLiveData().observe(getViewLifecycleOwner(), words -> {
//            mAdapter2.submitList(words);
//            int count = mAdapter2.getActualCount();
//
//            /*
//            We fetch guess filters only if count of total results is 2 or more. Less than two
//            results don't require any filtering anyway.
//            */
//            if (count > 1) mViewModel.initGuessFilterTask();
//
//            long delay = count == 0 ? Constants.LOAD_DELAY : 0;
//            if (countHandler != null) countHandler.removeCallbacksAndMessages(null);
//            countHandler = new Handler();
//            countHandler.postDelayed(() -> GuessFragment.this.setActionBarSubTitle("(" + count + ")"), delay);
//
//        });

        // Paging 3
        mViewModel.getGuessesPaging3LiveData().observe(getViewLifecycleOwner(), wordPagingData -> {
            Log.d(TAG + TAG_NAV, "onViewStateRestored: guesses=" + wordPagingData);
            mAdapter.submitData(getLifecycle(), wordPagingData);
        });

        if (isFragmentNewlyCreated()) fetchGuesses();


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG + TAG_NAV, "onSaveInstanceState: called");
        mViewModel.saveState();
        outState.putString(KEY_OPERATOR, tvOperator.getText().toString());
        super.onSaveInstanceState(outState);
    }



    /**
     * Loading state is reached only once on each data load.
     * NotLoading state is reached even while the list is being scrolled. So, the code called in
     * there gets called multiple times during scrolling of the same data list.
     *
     * In order to address this issue, isDataLoadProceduresPerformed is introduced which is reset
     * on every 'Loading' state and turned to true the first time NotLoading state is achieved.
     * That helps us run certain code that is required to be executed only the first time a list is
     * loaded.
     *
     */
    private Function1<CombinedLoadStates, Unit> getmLoadStateListener() {
        if (mLoadStateListener == null) {
            mLoadStateListener = combinedLoadStates -> {

                LoadState loadState = combinedLoadStates.getRefresh();
                Log.d(TAG + TAG_NAV, "getmLoadStateListener: called with=" + loadState);
                if (loadState instanceof LoadState.Loading) {
                    destroyHandler(loadHandler);
                    loadHandler = new Handler();
                    loadHandler.postDelayed(() -> clOverlay.setVisibility(View.VISIBLE),
                            800);

                    // Reset the flag
                    if (mViewModel.isDataLoadProceduresPerformed())
                        mViewModel.setDataLoadProceduresPerformed(false);
                } else if (loadState instanceof LoadState.NotLoading) {
                    destroyHandler(loadHandler);
                    if (clOverlay.getVisibility() == View.VISIBLE)
                        clOverlay.setVisibility(View.GONE);
                    // For some reason, if we are showing ads in the adapter and a new list is
                    // loaded after clicking a filter, onBindViewHolder() is not called for some
                    // view holders. In other words, if we click a filter, the count of guesses
                    // changes but some guesses show the old word text, because onBindViewHolder()
                    // was not called for those.
                    // In order to fix that, we manually call bindViews() on all the visible
                    // GuessViewHolders
                    mAdapter.notifyHolders();

                    int count = mAdapter.getActualCount();

                    // Show count in subtitle [NOTE: Don't move this code elsewhere]
                    String subtitle = "(" + Utilities.applyCommaSeparator(count, 0) + ")";
                    setActionBarSubTitle(subtitle);

                    if (!mViewModel.isDataLoadProceduresPerformed()) {

                        // Flag's cycle ends here
                        mViewModel.setDataLoadProceduresPerformed(true);

                        // Log the event
                        logGuessFetched(count);

                        /*
                        We fetch guess filters only if count of total results is 2 or more. Less
                        than two results don't require any filtering anyway.
                        */
                        if (count > 1) mViewModel.initGuessFilterTask();

//                        // Show count in subtitle
//                        String subtitle = "(" + Utilities.applyCommaSeparator(count, 0) + ")";
//                        setActionBarSubTitle(subtitle);

                    }
                }
                return null;
            };
        }

        return mLoadStateListener;
    }

    private void logGuessFetched(int count) {
        int filterCount = mViewModel.isAllFilterSelected() ? 0 :
                mViewModel.getSelectedGuessFilters().size();
        String operator = (mViewModel.isAllFilterSelected() ||
                !mViewModel.isOperatorApplicable()) ? "NONE" : getSelectedOperator();
        mListener.logGuessFetched(getColCount(), mAttemptCount, count,
                filterCount, operator);
    }

    private void fetchGuesses() {
        Log.d(TAG + TAG_NAV, "fetchGuesses: called");
        destroyHandler(fetchHandler);
        fetchHandler = new Handler();
        fetchHandler.postDelayed(() -> mViewModel.setGuessParams(mCells, getColCount()),
                Constants.LOAD_DELAY);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_guess, menu);

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.menu_dictionary);
        if (menuItem != null) {
            menuItem.setShowAsAction(getDefaultDictionary() == null
                    ? MenuItem.SHOW_AS_ACTION_NEVER
                    : MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_dictionary) {
            showDictionarySelectDialog();
            return true;
        }
        else if (item.getItemId() == R.id.menu_help) {
            GuessFragmentDirections.ActionGuessFragmentToIntroFragment action =
                    GuessFragmentDirections.actionGuessFragmentToIntroFragment();
            action.setMMode(MODE_HELP);
            navigateTo(action);
            return true;
        }
//        else if (item.getItemId() == R.id.menu_get_premium) {
//            showGetPremiumDialog();
//            return true;
//        }
//        else if (item.getItemId() == R.id.menu_consume) {
//            mListener.initConsumeFlow();
//            return true;
//        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void removeAds() {
        vibrate();
        showGetPremiumDialog();
    }

    @Override
    public void onWordClicked(Word word) {
        vibrate();
        mListener.incrementUsageCount(1);
        Dictionary dictionary = getDefaultDictionary();
        if (dictionary != null) {
            String url = String.format(dictionary.getDefinitionUrl(), word.getWordTitle()
                    .toLowerCase());
            // NEW TREATMENT
            /*
            In case of purchased app, it would simply perform the operation.
            In case of free app:
            - if current count is less than the ad-free interval,
              it would perform operation and increase the count by 1.

            - If current count is equal to or more than the ad-free interval,
              it would show the ad (if ready) first and after it is shown, it
              would reset the count to 0 and perform the operation.
              However, if the ad is not ready yet, it may be due to no connection / internet
              or slow ad network servers.
              In this scenario, we will check if connection and internet are available.
              - If available, we will let the ad load in due course of time but perform
                the operation without showing the ad this time. The loaded ad will be used
                next time.
              - If not available, we will not perform the operation and instead show a
                toast that this feature requires internet.
             */
            if (isPurchased()) {
                showWordDefinition(word, dictionary, url);
            } else {
                int count = mListener.getClickCount();
                if (count < AD_FREE_CLICK_QUOTA) {
                    mListener.setClickCount(count + DICTIONARY_CLICK_INCREMENT);
                    showWordDefinition(word, dictionary, url);
                } else {
                    if (mListener.showInterstitialIfReady()) {
                        interstitialRunnable = () -> {
                            if (mListener != null) mListener.setClickCount(0);
                            showWordDefinition(word, dictionary, url);
                        };
                    } else {
                        internetRunnable = () -> openCustomTab(url);
                        checkInternet();
                    }
                }


//                int count = mListener.getDictionaryCount();
//                if (count < DICT_AD_FREE_COUNT) {
//                    mListener.setDictionaryCount(count + 1);
//                    openCustomTab(url);
//                } else {
//                    if (mListener.showInterstitialIfReady()) {
//                        interstitialRunnable = () -> {
//                            if (mListener != null) mListener.setDictionaryCount(0);
//                            openCustomTab(url);
//                        };
//                    } else {
//                        internetRunnable = () -> openCustomTab(url);
//                        checkInternet();
//                    }
//                }
            }
        } else {
            clickedWord = word;
            showDictionarySelectDialog();
        }
    }

    private void showWordDefinition(Word word, Dictionary dictionary, String url) {
        mListener.logDictionary(dictionary.getDictionaryName(), word.getWordTitle(), getColCount());
        openCustomTab(url);
    }

    @Override
    public void onWordLongClicked(Word word) {
        vibrate();
        mListener.incrementUsageCount(1);
        if (isPurchased()) {
            showClickedGuess(word);
        } else {
            int count = mListener.getClickCount();
            if (count < AD_FREE_CLICK_QUOTA) {
                mListener.setClickCount(count + GUESS_PICK_INCREMENT);
                showClickedGuess(word);
            } else {
                if (mListener.showInterstitialIfReady()) {
                    interstitialRunnable = () -> {
                        if (mListener != null) mListener.setClickCount(0);
                        showClickedGuess(word);
                    };
                } else {
                    internetRunnable = () -> showClickedGuess(word);
                    checkInternet();
                }
            }

//            int count = mListener.getGuessPickCount();
//            if (count < PICK_AD_FREE_COUNT) {
//                mListener.setGuessPickCount(count + 1);
//                showClickedGuess(word);
//            } else {
//                if (mListener.showInterstitialIfReady()) {
//                    interstitialRunnable = () -> {
//                        if (mListener != null) mListener.setGuessPickCount(0);
//                        showClickedGuess(word);
//                    };
//                } else {
//                    internetRunnable = () -> showClickedGuess(word);
//                    checkInternet();
//                }
//            }
        }

    }

    private void showClickedGuess(Word word) {
        if (mainViewModel != null) mainViewModel.setClickedGuess(word.getWordTitle());
        handleBackPress();
    }

    @Override
    public void onDictionaryClicked(Dictionary dictionary) {
        super.onDictionaryClicked(dictionary);
        if (clickedWord != null) {
            Word word = new Word(clickedWord);
            clickedWord = null;
            onWordClicked(word);
        }
    }

    @Override
    public void onDestroyView() {
        destroyHandlers(fetchHandler, loadHandler);

        if (mAdapter != null && mLoadStateListener != null)
            mAdapter.removeLoadStateListener(mLoadStateListener);

        super.onDestroyView();
    }

    @Override
    public void onGuessFilterClicked(GuessFilter guessFilter) {
        vibrate();
        mListener.incrementUsageCount(1);
        if (isPurchased()) {
            applyGuessFilter(guessFilter);
        } else {
            int count = mListener.getClickCount();
            if (count < AD_FREE_CLICK_QUOTA) {
                mListener.setClickCount(count + FILTER_CLICK_INCREMENT);
                applyGuessFilter(guessFilter);
            } else {
                if (mListener.showInterstitialIfReady()) {
                    interstitialRunnable = () -> {
                        if (mListener != null) mListener.setClickCount(0);
                        applyGuessFilter(guessFilter);
                    };
                } else {
                    internetRunnable = () -> applyGuessFilter(guessFilter);
                    checkInternet();
                }
            }

//            int count = mListener.getGuessFilterCount();
//            if (count < FILTER_AD_FREE_COUNT) {
//                mListener.setGuessFilterCount(count + 1);
//                applyGuessFilter(guessFilter);
//            } else {
//                if (mListener.showInterstitialIfReady()) {
//                    interstitialRunnable = () -> {
//                        if (mListener != null) mListener.setGuessFilterCount(0);
//                        applyGuessFilter(guessFilter);
//                    };
//                } else {
//                    internetRunnable = () -> applyGuessFilter(guessFilter);
//                    checkInternet();
//                }
//            }
        }
    }

    private void applyGuessFilter(GuessFilter guessFilter) {
        if (mViewModel != null && guessFilterAdapter != null) {
//            clOverlay.setVisibility(View.VISIBLE);
            mViewModel.setGuessFilterSelected(guessFilter,
                    !isGuessFilterSelected(guessFilter.getTitle()));
            guessFilterAdapter.notifyHolders();
            mViewModel.initGuessFilterQueryTask(getSelectedGuessFilters(), getSelectedOperator());
        }
    }

    private String getSelectedOperator() {
        CharSequence text = tvOperator.getText();
        return Objects.equals(text, getString(R.string.sqlite_and)) ? AND : OR;
    }

    private void resetGuessAdapter() {
        if (mAdapter != null && mLoadStateListener != null)
            mAdapter.removeLoadStateListener(mLoadStateListener);
        rvGuesses.setAdapter(mAdapter = new GuessPagingDataAdapter(this,
                getColCount()));
        mAdapter.addLoadStateListener(getmLoadStateListener());
    }

    public boolean isGuessFilterSelected(String title) {
        return mViewModel.isGuessFilterSelected(title);
    }

    public ArrayList<GuessFilter> getSelectedGuessFilters() {
        return mViewModel.getSelectedGuessFilters();
    }

}
