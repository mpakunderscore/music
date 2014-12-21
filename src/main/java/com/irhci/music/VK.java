package com.irhci.music;

import de.umass.lastfm.Track;
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

    public static void findTrackURLs(final Map<String, Collection<Track>> artistsTracks) throws Exception {

        for (final Map.Entry<String, Collection<Track>> artist : artistsTracks.entrySet()) {

            for (Track track : artist.getValue()) {

                String fullTrackName = artist.getKey() + " - " + track.getName();

                final String url = "https://vk.com/search?c%5Bq%5D=" +
                        URLEncoder.encode(fullTrackName, "UTF-8") +
                        "&c%5Bsection%5D=audio";

                final org.jsoup.nodes.Document doc = Jsoup.connect(url).cookies(cookies).timeout(timeout).get();
                final Elements ai_body = doc.select("div[class=ai_body]");

                if (ai_body.size() == 0) {

                    System.err.println("Can't find tracks for: " + fullTrackName);
                    System.err.println("\t" + url);
                    //TODO

                    continue;
                }


//                String trackTime = (track.getDuration() == 0 ? "0:0" : track.getDuration()/60 + ":" + track.getDuration()%60);
//                System.out.println(fullTrackName + " " + trackTime);
                System.out.println(fullTrackName + " " + track.getDuration());


                for (Element element : ai_body) {

                    final String vkTrackArtist = element.select("span[class=ai_artist]").first().text();
                    final String vkTrackUrl = element.select("input").first().attr("value").split(",")[0];
                    final String vkTrackName = element.select("span[class=ai_title]").text();
                    final String vkTrackTime = element.select("div[class=ai_dur]").text();

                    final int vkTrackTimeInt = (vkTrackTime.contains(":") ? Integer.parseInt(vkTrackTime.split(":")[0]) * 60 + Integer.parseInt(vkTrackTime.split(":")[1]) : 0);

                    String status = (vkTrackArtist.toLowerCase().equals(artist.getKey().toLowerCase()) && vkTrackName.toLowerCase().equals(track.getName().toLowerCase()) ? "+" : "-") +
                            " " + (vkTrackTimeInt == track.getDuration() ? "+" : (track.getDuration() == 0 ? "?" : "-"));

                    System.out.println("\t" +
                            status + " " +
                            vkTrackArtist + " - " + vkTrackName + " " + vkTrackTimeInt);

                    if (status.equals("+ +"))
                        break;

//                    Thread.sleep(1000);
                }
            }
        }
    }


    public void saveTracks(final Map<String, Map<String, Track>> artistsTracks) throws MalformedURLException {

        for (final String artist : artistsTracks.keySet()) {

            final String artistPath = Music.prop.getProperty("libraryPath") + artist + "/";

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