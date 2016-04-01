package com.xinay.droid.fm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by luisvivero on 1/24/16.
 */
public class CustomAdapter extends BaseAdapter {

    private final String LOG_TAG = GenresFragment.class.getSimpleName();

    private LayoutInflater layoutinflater;
    private List<ItemObject> listStorage;
    private Context context;

    public CustomAdapter(Context context, List<ItemObject> customizedListView) {
        this.context = context;
        layoutinflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v(LOG_TAG, "getView");
        ViewHolder listViewHolder;
        if(convertView == null){
            Log.v(LOG_TAG, "new ViewHolder()");
            listViewHolder = new ViewHolder();
            convertView = layoutinflater.inflate(R.layout.card_song, parent, false);
            listViewHolder.screenShot = (ImageView)convertView.findViewById(R.id.album_art);
            listViewHolder.musicName = (TextView)convertView.findViewById(R.id.song_title);
            listViewHolder.musicAuthor = (TextView)convertView.findViewById(R.id.artist_name);
            Log.v(LOG_TAG, "screenShot: " + listViewHolder.screenShot.toString());
            Log.v(LOG_TAG, "musicName: " + listViewHolder.musicName.toString());
            Log.v(LOG_TAG, "musicAuthor: " + listViewHolder.musicAuthor.toString());

            convertView.setTag(listViewHolder);
            Log.v(LOG_TAG, "convertView.setTag");
        }else{
            Log.v(LOG_TAG, "convertView.getTag");
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        Log.v(LOG_TAG, "listViewHolder");
        listViewHolder.screenShot.setImageResource(listStorage.get(position).getScreenShot());
        listViewHolder.musicName.setText(listStorage.get(position).getMusicName());
        listViewHolder.musicAuthor.setText(listStorage.get(position).getMusicAuthor());
        Log.v(LOG_TAG, "music name: " + listStorage.get(position).getMusicName());
        Log.v(LOG_TAG, "music author: " + listStorage.get(position).getMusicAuthor());

        return convertView;
    }

    static class ViewHolder{
        ImageView screenShot;
        TextView musicName;
        TextView musicAuthor;
    }

}
