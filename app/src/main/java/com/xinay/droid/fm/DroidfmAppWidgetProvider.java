package com.xinay.droid.fm;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xinay.droid.fm.services.PlayerService;

/**
 * Created by luisvivero on 3/5/16.
 */
public class DroidfmAppWidgetProvider extends AppWidgetProvider {

    private final String LOG_TAG = DroidfmAppWidgetProvider.class.getSimpleName();

    public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";

    private Bitmap albumArt;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(LOG_TAG, "onUpdate()");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        String albumArtUrl = PlayerManager.getInstance().getCurrentSong().getAlbumArtUrl();
        albumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.droid_fm_thumbnail);



        if (albumArtUrl != null && albumArtUrl.indexOf("dar.fm") == -1) {
            Picasso.with(context)
                    .load(albumArtUrl)
                    .resize(75, 75)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                            albumArt = bitmap;
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            // Nothing else to do in this case
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // Nothing else to do in this case
                        }
                    });
        }

        Log.v(LOG_TAG, "appWidgetIds.length: " + appWidgetIds.length);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];


            Intent active = new Intent(context, DroidfmAppWidgetProvider.class);
            active.setAction(ACTION_WIDGET_RECEIVER);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            //Intent intent = new Intent(context, PlayerService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.player_widget);
            views.setTextViewText(R.id.song_title, PlayerManager.getInstance().getCurrentSong().getSongTitle());
            views.setTextViewText(R.id.artist_name, PlayerManager.getInstance().getCurrentSong().getSongArtist());
            views.setTextViewText(R.id.station_call_sign, PlayerManager.getInstance().getCurrentSong().getCallSign());
            views.setImageViewBitmap(R.id.album_art, albumArt);
            views.setOnClickPendingIntent(R.id.play_button, pendingIntent);

            views.setOnClickPendingIntent(R.id.song_title, actionPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "onReceive()");

        final String action = intent.getAction();

        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            //The widget is being deleted off the desktop
            final int appWidgetId = intent.getExtras().getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                this.onDeleted(context, new int[] { appWidgetId });
            }
        } else {
            Log.v(LOG_TAG, "action: " + action);
            // check, if our Action was called
            if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {

                Log.v(LOG_TAG, "ACTION_WIDGET_RECEIVER");
                //Play the audio file
                //The audio file is in /res/raw/ and is an OGG file
//                MediaPlayer mPlay = MediaPlayer.create(context, R.raw.ff);
//                mPlay.start();
            } else {
                // do nothing
            }
        }

        super.onReceive(context, intent);
    }


}
