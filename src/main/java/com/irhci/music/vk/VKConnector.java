package com.irhci.music.vk;

import com.irhci.music.core.Music;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.irhci.music.store.Track;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/20/12
 * Time: 2:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class VKConnector {

    private static final int vkTimeout = 10000;
    private static Map<String, String> vkCookies;
    
    private long find_url_wait = 5000;
    private static int save_url_wait = 5000;

    protected String login;
    protected String password;


    public VKConnector(String login, String password) throws IOException {

        this.login = login;
        this.password = password;

        getVKCookies();
    }

    protected Map<String, String> getArtistTracks(final String artist) throws Exception {

        Map<String, String> artist_tracks = new HashMap<>();



        return artist_tracks;
    }

    protected List<Track> getList(String path) throws IOException, ClassNotFoundException {

        if (!new File(path).exists()) return null;

        FileInputStream fin = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fin);
        List<Track> tracks = (List<Track>) ois.readObject();
        ois.close();
        return tracks;
    }

    public void findTrackURLs(final Map<String, Map<String, Track>> artistsTracks) throws Exception {

        for (final Map.Entry<String, Map<String, Track>> artist : artistsTracks.entrySet()) {

            final String artist_path = Music.libraryPath + artist + "/";

            if (!new File(artist_path).exists())
                new File(artist_path).mkdir();

            final String vk = "http://vk.com/search?c%5Bperformer%5D=1&c%5Bq%5D=" + artist.getKey().replace(" ", "%20") + "&c%5Bsection%5D=audio";

            final org.jsoup.nodes.Document doc = Jsoup.connect(vk).cookies(vkCookies).timeout(vkTimeout).get();
            final Elements divs = doc.select("div[class=ai_body]");

            if (divs.size() == 0) {

                System.out.println("Can't find tracks for: " + artist.getKey());
                System.out.println(vk);
                //TODO

                continue;
            }

            for (Element div : divs) {

                final String trackArtist = div.select("span[class=ai_artist]").first().text();
                final String trackUrl = div.select("input").first().attr("value").split(",")[0];
                final String trackName = div.select("span[class=ai_title]").text();

                if (trackArtist.equals(artist.getKey()) &&
                        artist.getValue().containsKey(trackName) &&
                            artist.getValue().get(trackName).getUrl() == null) {

                    artist.getValue().get(trackName).setUrl(trackUrl);
                }
            }
        }
    }


    public void saveTracks(final Map<String, Map<String, Track>> artistsTracks) throws MalformedURLException {

        for (final String artist : artistsTracks.keySet()) {

            final String artistPath = Music.libraryPath + artist + "/";

            if (!new File(artistPath).exists())
                new File(artistPath).mkdir();

            for (final Track track : artistsTracks.get(artist).values()) {

                final String filePath = artistPath + track.getName().replace("/", "//") + ".mp3";
                final String urlString = track.getUrl();

                if (urlString == null || urlString.length() == 0) {

                    System.out.println("URL is null, skip: " + track.getArtist() + " - " + track.getName());
                    continue;
                }

                final File file = new File(filePath);

                if (file.exists()) {

                    System.out.println("Exist, skip: " + track.getArtist() + " - " + track.getName());
                    continue;
                }

                final URL url = new URL(urlString);

                try {

                    final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                    final FileOutputStream fos = new FileOutputStream(file);
                    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
                    fos.close();
                    rbc.close();

                } catch (IOException e) {

                    System.out.println("IO error: " + track.getArtist() + " - " + track.getName());
                    continue;
                }

                System.out.println("Downloaded: " + track.getArtist() + " - " + track.getName());

//                MP3File mp3_file = new MP3File(file);
//                mp3_file.getBitRate();
            }
        }
    }

    protected void getVKCookies() throws IOException {

        Connection.Response res = Jsoup
                .connect("https://login.vk.com/?act=login")
                .data("email", login)
                .data("pass", password)
                .method(Connection.Method.POST)
                .execute();

        vkCookies = res.cookies();
    }
}