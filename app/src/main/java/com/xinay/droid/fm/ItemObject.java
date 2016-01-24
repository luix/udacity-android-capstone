package com.xinay.droid.fm;

/**
 * Created by luisvivero on 1/24/16.
 */
public class ItemObject {

    private int screenShot;
    private String musicName;
    private String musicAuthor;

    public ItemObject(int screenShot, String musicName, String musicAuthor) {
        this.screenShot = screenShot;
        this.musicName = musicName;
        this.musicAuthor = musicAuthor;
    }

    public int getScreenShot() {
        return screenShot;
    }

    public String getMusicName() {
        return musicName;
    }

    public String getMusicAuthor() {
        return musicAuthor;
    }
}
