package com.irhci.music.lastfm;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.irhci.music.store.Track;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/20/12
 * Time: 2:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class FMConnector {

    protected String api_key;
    protected String api_url = "http://ws.audioscrobbler.com/2.0/?format=json&api_key="; //&method=user.gettopartists
    
    public FMConnector(String api_key) {
        this.api_key = api_key;
        api_url += api_key;
    }

    public static String sendPostRequest(final String url) throws IOException {

        DefaultHttpClient client = new DefaultHttpClient();

        HttpPost get = new HttpPost(url);

        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();

        String out = "";
        
        if (entity != null) {
            out = EntityUtils.toString(entity);
        }

        client.getConnectionManager().shutdown();
        return out;
    }

    public List<String> user_getTopArtists(final String user, final String count) throws ParseException, IOException {

//        period (Optional) : overall | 7day | 1month | 3month | 6month | 12month - The time period over which to retrieve top artists for.
//        limit (Optional) : The number of results to fetch per page. Defaults to 50.
//        page (Optional) : The page number to fetch. Defaults to first page.

        final String url_string = api_url + "&method=user.gettopartists&user=" + user.replace(" ", "+") + "&limit="+count;
        String in = sendPostRequest(url_string);
        JSONObject json = (JSONObject) new JSONParser().parse(in);

        JSONArray artists_array = (JSONArray) ((JSONObject) json.get("topartists")).get("artist");

        List<String> artists = new ArrayList<>();

        for (Object anArtists_array : artists_array) {
            artists.add((String) ((JSONObject) anArtists_array).get("name"));
        }

        return artists;
    }

    public Map<String, Track> artist_getTopTracks(final String artist) throws ParseException, IOException {

//        startTimestamp (Optional) : An unix timestamp to start at.
//        page (Optional) : The page number to fetch. Defaults to first page.
//        endTimestamp (Optional) : An unix timestamp to end at.

        final String url_string = api_url + "&method=artist.gettoptracks&artist=" + artist.replace(" ", "+");
        String in = sendPostRequest(url_string);
        JSONObject json = (JSONObject) new JSONParser().parse(in);

        JSONArray tracks_array = (JSONArray) ((JSONObject) json.get("toptracks")).get("track");

        Map<String, Track> tracks = new HashMap<>();

        for (Object aTracks_array : tracks_array) {

            final String name = (String) ((JSONObject) aTracks_array).get("name");

            Track track = new Track();
            track.setName(name);
            track.setArtist(artist);
//            track.setAlbum((String) ((JSONObject) ((JSONObject) aTracks_array).get("album")).get("#text"));
//            track.setTags(track_getTopTags(track.getName(), artist)); //TODO tags count

            tracks.put(name, track);
        }

        return tracks;
    }

    public Map<String, Track> user_getArtistTracks(final String user, final String artist) throws ParseException, IOException {

//        startTimestamp (Optional) : An unix timestamp to start at.
//        page (Optional) : The page number to fetch. Defaults to first page.
//        endTimestamp (Optional) : An unix timestamp to end at.

        final String url_string = api_url + "&method=user.getartisttracks&user=" + user.replace(" ", "+") + "&artist=" + artist.replace(" ", "+");
        String in = sendPostRequest(url_string);
        JSONObject json = (JSONObject) new JSONParser().parse(in);

        JSONArray tracks_array = (JSONArray) ((JSONObject) json.get("artisttracks")).get("track");

        Map<String, Track> tracks = new HashMap<>();
        List<String> track_names = new ArrayList<>();

        for (Object aTracks_array : tracks_array) {

            final String name = (String) ((JSONObject) aTracks_array).get("name");
            if (track_names.contains(name))
                continue; //TODO plays count

            track_names.add(name);
            Track track = new Track();
            track.setName(name);
            track.setArtist(artist);
            track.setAlbum((String) ((JSONObject) ((JSONObject) aTracks_array).get("album")).get("#text"));
//            track.setTags(track_getTopTags(track.getName(), artist)); //TODO tags count

            tracks.put(name, track);
        }

        return tracks;
    }

    public Map<String, String> track_getTopTags(final String track, final String artist) throws ParseException, IOException {

        Map<String, String> tags = new TreeMap<String, String>();

        final String url_string = api_url + "&method=track.gettoptags&track=" + track.replace(" ", "+") + "&artist=" + artist.replace(" ", "+");
        String in = sendPostRequest(url_string);
        JSONObject json = (JSONObject) new JSONParser().parse(in);

        JSONObject toptags = (JSONObject) json.get("toptags");

        JSONArray tags_array;
        try {

            tags_array = (JSONArray) toptags.get("tag");
            if (tags_array != null)
                for (Object aTags_array : tags_array)
                    tags.put((String) ((JSONObject) aTags_array).get("name"), (String) ((JSONObject) aTags_array).get("count"));

        } catch (ClassCastException e) {

            JSONObject tags_obj = (JSONObject) toptags.get("tag");
            tags.put((String) tags_obj.get("name"), (String) tags_obj.get("count"));
        }

        return tags;
    }
}
