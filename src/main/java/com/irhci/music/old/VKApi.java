package com.irhci.music.old; /**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/20/12
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.TreeMap;

public class VKApi  {

    private String api_secret;
    private String api_id;
    private TreeMap param = new TreeMap();

    public VKApi(String api_secret, String api_id) {
        this.api_secret = api_secret;
        this.api_id = api_id;
    }

    public String  api(String Method ) {
        return buildMethod(Method);
    }

    public Object api(String method, TreeMap params) {

        params.putAll(params);
        return buildMethod(method);
    }

    private String buildURL(TreeMap param) {

        String api_url = "http://api.com.irhci.music.vk.com/api.php?";
        StringBuilder sig = new StringBuilder();
        Object[] keys =  param.keySet().toArray();

        for(int i = 0; i < param.size(); i++)
            sig.append(keys[i]+"="+param.get(keys[i]));

        sig.append(api_secret);
        sig = new StringBuilder(MD5.getHash(sig.toString()));
        param.put("sig", sig);

        try {

            keys = param.keySet().toArray();
            for(int i = 0; i < param.size(); i++)
                api_url+= keys[i]+"="+URLEncoder.encode((param.get(keys[i])).toString(), "UTF-8")+"&";

        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return query(api_url);
    }

    private String query(String url) {
        URLConnection query;
        InputStream result;
        String data;
        byte buf[] = new byte[1024*48];
        try {
            query = new URL(url).openConnection();
            result = query.getInputStream();
            int r = result.read(buf);
            data = new String(buf, 0, r);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        param.clear();
        return  data;
    }

    private String buildMethod(String method) {

        Date date = new Date();
        String data;
        long now = date.getTime()/1000;
        byte buf[] = new byte[1024*48];

        param.put("api_id", api_id);
        param.put("v","2.0");
        param.put("method", method);
        param.put("timestamp", now);
        param.put("format", "json");
        param.put("random", Math.round(Math.random()*1000));
        param.put("test_mode", "1");
        return buildURL(param);
    }
}