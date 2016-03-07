package com.xinay.droid.fm.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by luisvivero on 8/23/15.
 */
public class Utilities {

    private static final String timerPrefix = "0:";
    private static final String timerSufix = "0";

    public static String formatTimer(int time) {
        time = time / 1000 % 60;
        return timerPrefix + ((time < 10) ? timerSufix + String.valueOf(time) : String.valueOf(time));
    }

    public static String makeUrlEncoded(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, "UTF-8");
    }

    public static String makeTransitionKeyName(int mayor, int minor) {
        return String.valueOf(mayor) + (minor > 10 ? String.valueOf(minor) : "0" + String.valueOf(minor));
    }

    public static long getAvailableMemory(Activity activity) {
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static boolean isServiceRunning(String serviceName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
