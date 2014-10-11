import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
public class Main {

    private static final int fm_timeout = 10000;
    private static final String system_path = "system/";
    private static final String library_path = "library/";
    private static HashMap<String,String> params;
    private static List<Track> track_urls;
//    private VKConnector vk;
    private static VKConnector vk;
    private static LastFMConnector last_fm;

    public static void main(String[] argv) throws Exception {

//        final String type = "user";
//        final String value = "mpak_";

//        final String type = "artist";
//        final String value = "Solar Fields";

//        final String type = "similar";
//        final String value = "Solar Fields";

//        final String type = "user_artists";
        final String value = "mpak_";

        //--

        long time = System.currentTimeMillis();
        
        readConfig();
        last_fm = new LastFMConnector(params.get("lastfm_api_key"));
        vk = new VKConnector(params.get("vk_login"), params.get("vk_password"));

        test();

        Map<String, List<Track>> artists_tracks = new HashMap<String, List<Track>>();
        List<String> artists = last_fm.user_getTopArtists(value, "2");
        for (final String artist : artists)
            artists_tracks.put(artist, last_fm.user_getArtistTracks(value, artist));

        System.out.println("time: "+(System.currentTimeMillis() - time)/1000);
        time = System.currentTimeMillis();

//--last.fm (?)

//        if (type.equals("user")) track_urls = last_fm.userGetTopTracks(value);
//        else if (type.equals("user_artists")) track_urls = last_fm.getUserArtistTracks(value, page);
//        else if (type.equals("artist")) track_urls = last_fm.getArtistTracks(value, page, );
//        else if (type.equals("similar")) track_urls = last_fm.getSimilarTracks(value);
//        else return;

//        final int length = track_urls.size();
//        System.out.println("Find "+length+" tracks on last.fm.");


//--vk

        new File(library_path).mkdir();
//
        vk.findTrackURLs(artists_tracks, false, library_path);
        vk.saveTracks(artists_tracks, library_path);
        System.out.println("time: "+(System.currentTimeMillis() - time)/1000);
        System.out.println("");
   }

    private static void test() throws IOException, ParseException {

        List<String> user1 = last_fm.user_getTopArtists("mpak_", "1000");
        List<String> user2 = last_fm.user_getTopArtists("", "1000");
        for (final String a : user1) if (user2.contains(a)) System.out.println(a);
    }


    private static void readConfig() throws IOException {

//        new File(system_path).mkdirs(); //TODO?

        FileReader fileReader = new FileReader(system_path+"config.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        params = new HashMap<String, String>();

        while ((line = bufferedReader.readLine()) != null) {
            params.put(line.split("=")[0], line.split("=")[1]);
        }

        bufferedReader.close();
    }
}

//privet :D

