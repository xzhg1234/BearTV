package com.fongmi.android.tv.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.fongmi.android.tv.App;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.security.MessageDigest;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static File getRootFile(String path) {
        return new File(getRootPath() + File.separator + path);
    }

    public static File getCacheDir() {
        return App.get().getCacheDir();
    }

    public static File getCacheDir(String folder) {
        return new File(getCachePath() + File.separator + folder);
    }

    public static String getCachePath() {
        return getCacheDir().getAbsolutePath();
    }

    public static File getCacheFile(String fileName) {
        return new File(getCacheDir(), fileName);
    }

    public static File getJar(String fileName) {
        return getCacheFile(Utils.getMd5(fileName).concat(".jar"));
    }

    public static File getWall(int index) {
        return getCacheFile("wallpaper_" + index);
    }

    public static File getLocal(String path) {
        if (path.contains(getRootPath())) return new File(path);
        return new File(path.replace("file:/", getRootPath()));
    }

    private static Uri getShareUri(File file) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N ? Uri.fromFile(file) : FileProvider.getUriForFile(App.get(), App.get().getPackageName() + ".provider", file);
    }

    private static String getMimeType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        return TextUtils.isEmpty(mimeType) ? "*/*" : mimeType;
    }

    public static File write(File file, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
        chmod(file);
        return file;
    }

    public static String read(String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getLocal(path))));
            StringBuilder sb = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null) sb.append(text).append("\n");
            br.close();
            return Utils.substring(sb.toString());
        } catch (Exception e) {
            return "";
        }
    }

    public static void unzip(File target, String path) {
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(target)))) {
            ZipArchiveEntry entry;
            while ((entry = in.getNextZipEntry()) != null) {
                File out = new File(path, entry.getName());
                if (entry.isDirectory()) out.mkdirs();
                else copy(in, out);
            }
        } catch (Exception ignored) {
        }
    }

    public static void copy(File in, File out) {
        try {
            IOUtils.copy(new FileInputStream(in), new FileOutputStream(out));
        } catch (Exception ignored) {
        }
    }

    public static void copy(InputStream in, File out) {
        try {
            IOUtils.copy(in, new FileOutputStream(out));
        } catch (Exception ignored) {
        }
    }

    public static String getMd5(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int count;
            while ((count = fis.read(byteArray)) != -1) digest.update(byteArray, 0, count);
            fis.close();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean equals(String jar, String md5) {
        return getMd5(getJar(jar)).equalsIgnoreCase(md5);
    }

    public static void clearDir(File dir) {
        if (dir == null) return;
        if (dir.isDirectory()) for (File file : dir.listFiles()) clearDir(file);
        if (dir.delete()) Log.d(TAG, "Deleted:" + dir.getPath());
    }

    public static void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(getShareUri(file), FileUtil.getMimeType(file.getName()));
        App.get().startActivity(intent);
    }

    public static File chmod(File file) {
        try {
            Process process = Runtime.getRuntime().exec("chmod 777 " + file);
            process.waitFor();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }
}
