package com.irhci.music;

import de.umass.lastfm.Track;

/**
 * Created by pavelkuzmin on 21/12/14.
 */
public class MP3Track extends Track {

    String mp3;

    protected MP3Track(String name, String url, String artist) {
        super(name, url, artist);
    }

    protected MP3Track(String name, String url, String mbid, int playcount, int listeners, boolean streamable, String artist, String artistMbid, boolean fullTrackAvailable, boolean nowPlaying) {
        super(name, url, mbid, playcount, listeners, streamable, artist, artistMbid, fullTrackAvailable, nowPlaying);
    }

    public String getMp3() {
        return mp3;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }
}
