package com.xinay.droid.fm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.xinay.droid.fm.MainActivity;
import com.xinay.droid.fm.services.PlayerService;
import com.xinay.droid.fm.util.Constants;
import com.xinay.droid.fm.util.Controls;

/**
 * Created by luisvivero on 3/6/16.
 */
public class NotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if(!Constants.SONG_PAUSED){
                        Controls.pauseControl(context);
                    }else{
                        Controls.playControl(context);
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
            }
        }  else{
            if (intent.getAction().equals(PlayerService.NOTIFY_PLAY)) {
                Controls.playControl(context);
            } else if (intent.getAction().equals(PlayerService.NOTIFY_PAUSE)) {
                Controls.pauseControl(context);
            } else if (intent.getAction().equals(PlayerService.NOTIFY_DELETE)) {
                Intent i = new Intent(context, PlayerService.class);
                context.stopService(i);
                Intent in = new Intent(context, MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(in);
            }
        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }
}
