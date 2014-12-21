package com.irhci.music;

import java.io.*;

import de.umass.lastfm.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/13/12
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Music {

    public static Properties prop;

    public static void main(String[] argv) throws Exception {

        readConfig();

        final String lastfmUser = "mpak_";
        final String lastfmPassword = "";

//        Session session = Authenticator.getMobileSession(lastfmUser, lastfmPassword,
//                prop.getProperty("lastfmApiKey"), prop.getProperty("lastfmSecret"));

        Map<String, Collection<Track>> artistsTracks = new HashMap<>();

//        PaginatedResult<Artist> artists = User.getRecommendedArtists(1, session);
        Collection<Artist> artists = User.getTopArtists(lastfmUser, Period.TWELVE_MONTHS, prop.getProperty("lastfmApiKey"));

        for (Artist artist : artists) {

            System.out.println(artist.getName());

            try {

                Collection<Track> tracks = Artist.getTopTracks(artist.getName(), prop.getProperty("lastfmApiKey"));

                artistsTracks.put(artist.getName(), tracks);

                for (Track track : tracks)
                    System.out.println("\t" + track.getName() + " " + track.getDuration());

            } catch (Exception e) {
                // -_-
            }
        }

//        VK.setCookies(vkUser, vkPassword);
        VK.findTrackURLs2(artistsTracks);

        System.out.println(artistsTracks);
   }

    private static void readConfig() throws IOException {

        prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = Music.class.getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        if (!new File(prop.getProperty("libraryPath")).exists()) //TODO
            new File(prop.getProperty("libraryPath")).mkdir();
    }
}