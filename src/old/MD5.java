package old;

import java.security.MessageDigest;

/**
 * Created by IntelliJ IDEA.
 * User: pavelkuzmin
 * Date: 12/20/12
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
final class MD5 {

    public static String getHash(String input) {

        String md5 = null;

        try {

            StringBuilder code = new StringBuilder();
            MessageDigest messageDigest =  MessageDigest.getInstance("old.MD5");
            byte bytes[] = input.getBytes();
            byte digest[] = messageDigest.digest(bytes);

            for (int i = 0; i < digest.length; ++i)
                code.append(Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1));

            md5 = code.toString();

        } catch(Exception e) { }

        return md5;
    }
}
