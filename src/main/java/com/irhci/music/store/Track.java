package com.irhci.music.store;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/20/12
 * Time: 3:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class Track implements Serializable {

    private String name;
    private String artist;
    private String album;
    private Map<String, String> urls;
    private Map<String, String> tags;
    private String lyrics;
    private String bpm; //Tempo
    private String url;

    public void setName(String name) {
        this.name = name;
        //To change body of created methods use File | Settings | File Templates.
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getName() {
        return name;
    }

    public void setTags(Map<String,String> tags) {
        this.tags = tags;
    }

    public void setUrl(final String url) {

        this.url = url;
//        if (urls == null) {
//
//            urls = new HashMap<String, String>();
//            urls.put(url, "");
//
//        } else if (!urls.containsKey(url)) urls.put(url, "");
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return url;
    }
}
