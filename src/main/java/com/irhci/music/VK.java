package com.irhci.music;

import com.mpatric.mp3agic.*;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/20/12
 * Time: 2:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class VK {

    private static final int timeout = 10000;
    private static Map<String, String> cookies;

    public static void findTrackURLs2(final Map<String, Collection<Track>> artistsTracks) throws IOException, InvalidDataException, UnsupportedTagException {

        for (final Map.Entry<String, Collection<Track>> artist : artistsTracks.entrySet()) {

            for (Track track : artist.getValue()) {

                String fullTrackName = artist.getKey() + " - " + track.getName();

                final String url = "https://api.vk.com/method/audio.search.json?q=" +
                        URLEncoder.encode(fullTrackName, "UTF-8") +
                        "&access_token=" + token;

                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet getRequest = new HttpGet(url);
                getRequest.addHeader("accept", "application/json");

                HttpResponse response = httpClient.execute(getRequest);

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatusLine().getStatusCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

                StringBuilder result = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                JSONObject object = (JSONObject) JSONValue.parse(result.toString());

                httpClient.getConnectionManager().shutdown();

                System.out.println(fullTrackName + " " + track.getDuration());

                for (Object element : object.values()) {

                    final String vkTrackArtist = ;
                    final String vkTrackUrl = ;
                    final String vkTrackName = ;
                    final String vkTrackTime = ;

                    final int vkTrackTimeInt = (vkTrackTime.contains(":") ? Integer.parseInt(vkTrackTime.split(":")[0]) * 60 + Integer.parseInt(vkTrackTime.split(":")[1]) : 0);
                    final boolean timeEqual = (vkTrackTimeInt - track.getDuration()) >= -1 && (vkTrackTimeInt - track.getDuration()) <= 1;

                    String status = (vkTrackArtist.toLowerCase().equals(artist.getKey().toLowerCase()) && vkTrackName.toLowerCase().equals(track.getName().toLowerCase()) ? "+" : "-") +
                            " " + (timeEqual ? "+" : (track.getDuration() == 0 ? "?" : "-"));

                    System.out.println("\t" + status + " " +
                            vkTrackArtist + " - " + vkTrackName + " " + vkTrackTimeInt);

                    if (status.equals("+ +") || status.equals("+ ?")) {
                        saveTrack(track, vkTrackUrl);
                        break;
                    }
                }
            }
        }
    }

    public static void findTrackURLs(final Map<String, Collection<Track>> artistsTracks) throws IOException, InvalidDataException, UnsupportedTagException {

        for (final Map.Entry<String, Collection<Track>> artist : artistsTracks.entrySet()) {

            for (Track track : artist.getValue()) {

                String fullTrackName = artist.getKey() + " - " + track.getName();

                final String url = "https://vk.com/search?c%5Bq%5D=" +
                        URLEncoder.encode(fullTrackName, "UTF-8") +
                        "&c%5Bsection%5D=audio";

//                https://oauth.vk.com/blank.html#access_token=1bb4d3010ad1a62d8a11c1f3c5030072dfd0861753236b7a0cf9cccf904c94303a864dfacc44e9741a31d&expires_in=86400&user_id=20483167

                org.jsoup.nodes.Document doc;
                try {
                    doc = Jsoup.connect(url).cookies(cookies).timeout(timeout).get();
                } catch (Exception e) {
                    System.err.println("Can't connect to vc.com: " + fullTrackName);
                    System.err.println("\t" + url);
                    continue;
                    //TODO
                }

                final Elements ai_body = doc.select("div[class=ai_body]");

                if (ai_body.size() == 0) {
                    System.err.println("Can't find tracks for: " + fullTrackName);
                    System.err.println("\t" + url);
                    continue;
                    //TODO
                }

                System.out.println(fullTrackName + " " + track.getDuration());

                for (Element element : ai_body) {

                    final String vkTrackArtist = element.select("span[class=ai_artist]").first().text();
                    final String vkTrackUrl = element.select("input").first().attr("value").split(",")[0];
                    final String vkTrackName = element.select("span[class=ai_title]").text();
                    final String vkTrackTime = element.select("div[class=ai_dur]").text();

                    final int vkTrackTimeInt = (vkTrackTime.contains(":") ? Integer.parseInt(vkTrackTime.split(":")[0]) * 60 + Integer.parseInt(vkTrackTime.split(":")[1]) : 0);
                    final boolean timeEqual = (vkTrackTimeInt - track.getDuration()) >= -1 && (vkTrackTimeInt - track.getDuration()) <= 1;

                    String status = (vkTrackArtist.toLowerCase().equals(artist.getKey().toLowerCase()) && vkTrackName.toLowerCase().equals(track.getName().toLowerCase()) ? "+" : "-") +
                            " " + (timeEqual ? "+" : (track.getDuration() == 0 ? "?" : "-"));

                    System.out.println("\t" + status + " " +
                            vkTrackArtist + " - " + vkTrackName + " " + vkTrackTimeInt);

                    if (status.equals("+ +") || status.equals("+ ?")) {
                        saveTrack(track, vkTrackUrl);
                        break;
                    }
                }
            }
        }
    }

    public static void saveTrack(Track track, String trackUrl) throws IOException, InvalidDataException, UnsupportedTagException {

        final String artistPath = Music.prop.getProperty("libraryPath") + track.getArtist() + "/";

        if (!new File(artistPath).exists()) //TODO
            new File(artistPath).mkdir();

        final String filePath = artistPath + track.getName().replace("/", "|") + ".mp3"; //TODO

        final File file = new File(filePath);

        if (file.exists()) {
            System.out.println("Exist, skip: " + track.getArtist() + " - " + track.getName());
            return;
        }

        final URL url = new URL(trackUrl);

        try {

            final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            final FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();

        } catch (IOException e) {
            System.err.println("IO error: " + track.getArtist() + " - " + track.getName());;
        }

        checkId3v2Tags(track, filePath);
    }

    private static void checkId3v2Tags(Track track, String filePath) throws InvalidDataException, IOException, UnsupportedTagException {

        Collection<Tag> tags = Track.getTopTags(track.getArtist(), track.getMbid(), Music.prop.getProperty("lastfmApiKey"));
        track = Track.getInfo(track.getArtist(), track.getMbid(), Music.prop.getProperty("lastfmApiKey"));

        Mp3File mp3file = new Mp3File(filePath);

        System.out.println("Length of this mp3 is: " + mp3file.getLengthInSeconds() + " seconds");
        System.out.println("Bitrate: " + mp3file.getLengthInSeconds() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
        System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
        System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
        System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
        System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));

        if (mp3file.hasId3v1Tag()) {

            ID3v1 id3v1Tag = mp3file.getId3v1Tag();
            System.out.println("Track: " + id3v1Tag.getTrack());
            System.out.println("Artist: " + id3v1Tag.getArtist());
            System.out.println("Title: " + id3v1Tag.getTitle());
            System.out.println("Album: " + id3v1Tag.getAlbum());
            System.out.println("Year: " + id3v1Tag.getYear());
            System.out.println("Genre: " + id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")");
            System.out.println("Comment: " + id3v1Tag.getComment());
        }

        if (mp3file.hasId3v2Tag()) {

            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
            System.out.println("Track: " + id3v2Tag.getTrack());
            System.out.println("Artist: " + id3v2Tag.getArtist());
            System.out.println("Title: " + id3v2Tag.getTitle());
            System.out.println("Album: " + id3v2Tag.getAlbum());
            System.out.println("Year: " + id3v2Tag.getYear());
            System.out.println("Genre: " + id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")");
            System.out.println("Comment: " + id3v2Tag.getComment());
            System.out.println("Composer: " + id3v2Tag.getComposer());
            System.out.println("Publisher: " + id3v2Tag.getPublisher());
            System.out.println("Original artist: " + id3v2Tag.getOriginalArtist());
            System.out.println("Album artist: " + id3v2Tag.getAlbumArtist());
            System.out.println("Copyright: " + id3v2Tag.getCopyright());
            System.out.println("URL: " + id3v2Tag.getUrl());
            System.out.println("Encoder: " + id3v2Tag.getEncoder());

            byte[] albumImageData = id3v2Tag.getAlbumImage();
            if (albumImageData != null) {

                System.out.println("Have album image data, length: " + albumImageData.length + " bytes");
                System.out.println("Album image mime type: " + id3v2Tag.getAlbumImageMimeType());
            }
        }
    }

    static void setCookies(String user, String password) throws IOException {

        Connection.Response res = Jsoup
                .connect("https://login.vk.com/?act=login")
                .data("email", user)
                .data("pass", password)
                .method(Connection.Method.POST)
                .execute();

        cookies = res.cookies();
    }
}