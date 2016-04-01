package com.xinay.droid.fm.util;

import android.content.Context;

import com.xinay.droid.fm.R;

/**
 * Created by luisvivero on 3/6/16.
 */
public class Controls {

    public static void playControl(Context context) {
        sendMessage(context.getResources().getString(R.string.play));
    }

    public static void pauseControl(Context context) {
        sendMessage(context.getResources().getString(R.string.pause));
    }

    private static void sendMessage(String message) {
        try{
            Constants.PLAY_PAUSE_HANDLER.sendMessage(Constants.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
        }catch(Exception e){}
    }
}
