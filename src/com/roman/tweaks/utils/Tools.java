package com.roman.tweaks.utils;

import com.roman.tweaks.ShellInterface;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class Tools {

    public static void mountSystem() {

        String s = ShellInterface.getProcessOutput("mount | grep system");
        s = s.substring(0, s.indexOf(" "));
        s = "mount -o rw,remount " + s + " /system";

        ShellInterface.runCommand(s);

        Log.i("Bloater::Tools", "mounting: " + s);

    }

    public static void unMountSystem() {

        String s = ShellInterface.getProcessOutput("mount | grep system");
        s = s.substring(0, s.indexOf(" "));

        s = "mount -o ro,remount " + s + " /system";
        ShellInterface.runCommand(s);

        Log.i("Bloater::Tools", "unmounting: " + s);

    }

    public static String md5(File f) {
        try {
            InputStream is = new FileInputStream(f);

            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = is.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            is.close();
            byte[] digest = digester.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String h = Integer.toHexString(0xFF & digest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            // Log.i("md5", FilenameUtils.getName(f.getLocalFile().getName())
            // + ": " + hexString.toString());
            return hexString.toString().toLowerCase();
        } catch (Exception e) {

        }
        return null;
    }
}
