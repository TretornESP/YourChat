package tretornesp.clickerchat3;

import android.graphics.Color;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Cypher {
    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    public static int[] uniqueColors(int colorsNumber) {
        int colors[] = new int[colorsNumber];

        Random rand = new Random();
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);

        int step = 256/colorsNumber;

        for (int i = 0; i < colorsNumber; i++) {
            r+=step;
            g+=step;
            b+=step;

            r = r % 256;
            g = g % 256;
            b = b % 256;

            colors[i] = Color.rgb(r,g,b);
        }

        return colors;
    }
}
