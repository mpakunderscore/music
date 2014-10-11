import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
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

    private static final int vk_timeout = 10000;
    private static Map<String, String> vk_cookies;
    
    private long find_url_wait = 5000;
    private static int save_url_wait = 5000;

    protected String login;
    protected String password;


    public VKConnector(String login, String password) throws IOException {
        this.login = login;
        this.password = password;
        getVKCookies();
    }

    protected Map<String,String> getArtistTracks(final String artist) throws Exception {

        Map<String, String> artist_tracks = new HashMap<String, String>();

        final String vk = "http://vk.com/search?c%5Bq%5D=" + artist.replace(" ", "%20") + "&c%5Bsection%5D=audio";
        System.out.println("vk request: "+vk);
        final org.jsoup.nodes.Document doc = Jsoup.connect(vk).cookies(vk_cookies).timeout(vk_timeout).get();
        final Elements divs = doc.select("div[class=area clear_fix]");

        if (divs.size() == 0)
            throw new Exception("vk.com time ban");

        for (Element div : divs) {
            if (div.select("b").first().text().equals(artist)) {
                final String track_url = div.select("input").first().attr("value").split(",")[0];
                final String track = div.select("span[class=title]").text();
                artist_tracks.put(track, track_url);
            }
        }

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

    protected void findTrackURLs(final Map<String, List<Track>> artists_tracks, final boolean lyrics, String library_path) throws Exception {

        for (final String artist : artists_tracks.keySet()) {

            final String artist_path = library_path+artist + "/";

            if (!new File(artist_path).exists())
                new File(artist_path).mkdir();

            if (getList(artist_path+"/tracks.txt") != null) {
                artists_tracks.put(artist, getList(artist_path+"/tracks.txt"));
                continue;
            }

            Map<String, String> artist_tracks = getArtistTracks(artist);

            for (final Track track : artists_tracks.get(artist)) {

                try {

                    final String track_url;
                    if (!artist_tracks.containsKey(track.getName())) {

                        //http://vk.com/search?c%5Blyrics%5D=1&c%5Bq%5D=Carbon%20Based%20Lifeforms&c%5Bsection%5D=audio
                        final String vk = "http://vk.com/search?c%5Bq%5D=" + (track.getArtist()+"%20-%20"+track.getName()).replace(" ", "%20") + "&c%5Bsection%5D=audio" + (lyrics ? "c%5Blyrics%5D=1"  : ""); //TODO
                        System.out.println("vk request: "+vk);
                        final org.jsoup.nodes.Document doc = Jsoup.connect(vk).cookies(vk_cookies).timeout(vk_timeout).get();
                        final Elements divs = doc.select("div[class=area clear_fix]");

                        if (divs.size() == 0) continue;
                        if (divs.select("input").size() == 0) continue;

                        track_url = divs.select("input").first().attr("value").split(",")[0];

                    } else track_url = artist_tracks.get(track.getName());

                    track.setUrl(track_url);

                    System.out.println("track_url: "+track_url);
    //                this.wait(find_url_wait);

                } catch (IOException e) {
                    System.out.println("Error: "+track);
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    //            } catch (InterruptedException e) {
    //                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
//                i++;
            }

            FileOutputStream fout = new FileOutputStream(artist_path+"/tracks.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(artists_tracks.get(artist));
            oos.close();
        }
    }

    protected void saveTracks(final Map<String, List<Track>> artists_tracks, String library_path) throws IOException, FileNotFoundException, InterruptedException, TagException {

        for (final String artist : artists_tracks.keySet()) {

            final String artist_path = library_path+artist + "/";

            if (!new File(artist_path).exists())
                new File(artist_path).mkdir();

            for (final Track track : artists_tracks.get(artist)) {

                final String file_string = artist_path + track.getName() + ".mp3";
                final String url = track.getUrl();
                if (url.length() == 0) return;
                final File file = new File(file_string);
                if(file.exists()) {
                    System.out.println("Exist, skip "+track);
                    return;
                }

                final URL website = new URL(url);
                final ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                final FileOutputStream fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, 1 << 24);
                fos.close();
                rbc.close();

                MP3File mp3_file = new MP3File(file);
                mp3_file.getBitRate();
            }
        }
    }

    protected void getVKCookies() throws IOException {

        System.out.println("Get vk auth.");
        Connection.Response res = Jsoup
                .connect("https://login.vk.com/?act=login")
                .data("email", login)
                .data("pass", password)
                .method(Connection.Method.POST)
                .execute();

        vk_cookies = res.cookies();
    }
}