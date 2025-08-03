package com.appicacious.solword.constants;

import com.appicacious.solword.models.GuessFilter;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String DB_NAME = "solword.db";

    public static final String URL_PING = "https://www.google.com";
    public static final String URL_PRIVACY = "https://appicacious.github.io/Solword/privacypolicy";
    public static final String URL_TERMS = "https://appicacious.github.io/Solword/termsandconditions";
    public static final String URL_TWITTER = "https://twitter.com/appicacious";
    public static final String BASE_URL_PLAY_STORE = "https://play.google.com/store/apps/details?id=";
    public static final String BASE_URL_PLAY_MARKET = "market://details?id=";

    public static final String NATIVE_AD_ID = "2dc9b7bb008e5fdf";
    public static final String INTERSTITIAL_ID = "42c01bed2622e9ec";

    public static final int NATIVE_AD_INTERVAL = 30;

    public static final String BASE_FONT_PATH = "fonts/open_sans.ttf";
    public static final String BOLD_FONT_PATH = "fonts/open_sans_bold.ttf";

//    public static final int DICT_AD_FREE_COUNT = 10;
//    public static final int PICK_AD_FREE_COUNT = 4;
//    public static final int FILTER_AD_FREE_COUNT = 12;


    public static final int AD_FREE_CLICK_QUOTA = 36;
    public static final int DICTIONARY_CLICK_INCREMENT = 1;
    public static final int GUESS_PICK_INCREMENT = 3;
    public static final int FILTER_CLICK_INCREMENT = 2;
    public static final int RANDOM_WORD_CLICK_INCREMENT = 4;
    public static final int REVIEW_USAGE_QUOTA = 100;


    public static final int BLANK = 0;
    public static final int GREY = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;

    public static final int MODE_INTRO = 11;
    public static final int MODE_HELP = 12;

    public static final int DEF_WORD_SIZE = 5;

    public static final long LOAD_DELAY = 0;
    public static final long PICK_GUESS_ANIM_DURATION = 250;

    public static final String FIELD_ALPHA = "alpha";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_HIDDEN = "isHidden";

    public static final int RC_ERROR_LOG = 11;
    public static final int RC_DICT = 12;
    public static final int RC_PREMIUM = 13;

    public static final String TAG_ERROR_LOG = "tag_error_log";
    public static final String TAG_DICt = "tag_dict";
    public static final String TAG_PREMIUM = "tag_premium";

    public final static Map<Integer, String> sources;

    static {
        sources = new HashMap<>();
        sources.put(1, "git"); // https://github.com/dwyl/english-words/blob/master/words_alpha.txt
        sources.put(2, "fox"); // You know which source these are from
        sources.put(3, "git113"); // https://github.com/dwyl/english-words/issues/113
        sources.put(4, "git135"); // https://github.com/dwyl/english-words/issues/135
    }

    public static final int DELAY = 250;
    public static final long VIBRATE_DURATION = 35;
    public static final long SPLASH_DELAY = 2000;

    public static final int DEF_DICT_ID = 0;
    public static final int MIN_GUESSES = 20;

    public final static String AND = " AND ";
    public final static String OR = " OR ";
    public final static String SUBSTR = "SUBSTR(wordTitle,%d,1)";

    public final static String ALL = "All";
    public final static GuessFilter ALL_FILTER = new GuessFilter(ALL, "");
    public static final int PAGE_SIZE = 50;


    public static String[] ALL_ALPHAS = "abcdefghijklmnopqrstuvwxyz".split("");
    public static String[] VOWELS = "aeiou".split("");




    public static final String TAG_NAV = "_nav";

}
