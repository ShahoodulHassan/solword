package com.appicacious.solword.constants;

import androidx.annotation.StringDef;

public final class MyAnalytics {

    public static class Event {
        public static final String ON_GUESS_FETCHED = "on_guess_fetched";
        public static final String ON_DICTIONARY = "on_dictionary";

    }

    public static class Param {
        public static final String WORD_SIZE = "word_size";
        public static final String ATTEMPT_COUNT = "attempt_count";
        public static final String DICT_NAME = "dictionary_name";
        public static final String WORD_TITLE = "word_title";
        public static final String FILTER_OPERATOR = "filter_operator";
        public static final String FILTER_COUNT = "filter_count";

        // Size of guess list
        public static final String GUESS_COUNT = "guess_count";

        public static final String HAS_JOINED = "has_joined";


        @StringDef({GroupName.WHATSAPP})
        public @interface GroupName {
            String WHATSAPP = "Whatsapp";

        }

    }







}
