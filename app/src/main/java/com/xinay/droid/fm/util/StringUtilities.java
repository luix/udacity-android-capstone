package com.xinay.droid.fm.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by luisvivero on 8/23/15.
 */
public class StringUtilities {

    private static final String timerPrefix = "0:";
    private static final String timerSufix = "0";

    public static String formatTimer(int time) {
        time = time / 1000 % 60;
        return timerPrefix + ((time < 10) ? timerSufix + String.valueOf(time) : String.valueOf(time));
    }

    public static String makeUrlEncoded(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, "UTF-8");
    }
}
