package com.irhci.music.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.irhci.music.lastfm.FMConnector;
import com.irhci.music.store.Track;
import com.irhci.music.vk.VKConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/13/12
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Music {

    public static final String systemPath = "system/";
    public static final String libraryPath = "library/";

    private static HashMap<String, String> params;
    private static List<Track> track_urls;

    public static void main(String[] argv) throws Exception {

        final String value = "mpak_";

        readConfig();

        FMConnector last_fm = new FMConnector(params.get("lastfm_api_key"));

        Map<String, Map<String, Track>> artists_tracks = new HashMap<>();
        List<String> artists = last_fm.user_getTopArtists(value, "50");

        for (final String artist : artists)
            artists_tracks.put(artist, last_fm.artist_getTopTracks(artist));

        VKConnector vk = new VKConnector(params.get("vk_login"), params.get("vk_password"));
//
        vk.findTrackURLs(artists_tracks);
        vk.saveTracks(artists_tracks);
   }

    private static void readConfig() throws IOException {

        FileReader fileReader = new FileReader(systemPath + "main.conf");
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        params = new HashMap<>();

        while ((line = bufferedReader.readLine()) != null) {
            params.put(line.split("=")[0], line.split("=")[1]);
        }

        bufferedReader.close();
    }
}