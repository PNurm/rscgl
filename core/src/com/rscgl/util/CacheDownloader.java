package com.rscgl.util;

import com.badlogic.gdx.Gdx;
import com.rscgl.Config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Un-used cache updater / downloader code
 */
public class CacheDownloader {

    private static final String CACHE_URL = "";
    private Properties localChecksumTable = new Properties();
    private Properties remoteChecksumTable = new Properties();
    private String statusTextStr;

    public void update() {
        Gdx.files.local(Config.CACHE_DIR).mkdirs();
        loadRemoteChecksumTable();
        generateLocalChecksumTable();
        updateCacheFiles();
        verifyCacheFiles();

        publishProgress("Initializing login screen");
    }

    private void loadRemoteChecksumTable() {
        try {
            publishProgress("Fetching remote checksum table...");
            downloadFile("MD5CHECKSUM");
            FileInputStream f2 = (FileInputStream) Gdx.files.local(Config.CACHE_DIR + "MD5CHECKSUM").read();
            remoteChecksumTable.load(f2);
            f2.close();
        } catch (Exception e) {
            publishProgress("Unable to fetch remote checksum table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateLocalChecksumTable() {
        publishProgress("Generating local checksum table...");
        for (Map.Entry<Object, Object> entry : remoteChecksumTable.entrySet()) {
            if (!Gdx.files.local(Config.CACHE_DIR + entry.getKey()).exists()) {
                localChecksumTable.put(entry.getKey(), "doesntexist");
                publishProgress(entry.getKey() + " does not exist locally");
            } else {
                String localFileChecksum = getMD5Checksum((String) entry.getKey());
                localChecksumTable.put((String) entry.getKey(), localFileChecksum);
                //publishProgress(entry.getKey() + " > " + localFileChecksum);
            }
        }
    }

    private void updateCacheFiles() {
        publishProgress("Checking game-cache files");
        try {
            /* Update cache file */
            for (Map.Entry<Object, Object> e : localChecksumTable.entrySet()) {
                Iterator<Map.Entry<Object, Object>> itr = remoteChecksumTable.entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry<Object, Object> e1 = itr.next();
                    String localChecksum = (String) e.getValue();
                    String serverChecksum = (String) e1.getValue();

                    if (e1.getKey().equals(e.getKey()) && !localChecksum.equalsIgnoreCase(serverChecksum)) {
                        deleteFile((String) e1.getKey());
                        downloadFile((String) e1.getKey());
                        publishProgress("Updating " + e.getKey() + " ...\n");
                    }
                }
            }
        } catch (Exception e) {
            publishProgress("Unable to update cache files: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void verifyCacheFiles() {
        publishProgress("Verifying game-cache files");
        try {
            for (Map.Entry<Object, Object> entrySet : remoteChecksumTable.entrySet()) {
                String filename = (String) entrySet.getKey();
                String hash = (String) entrySet.getValue();
                boolean verified = false;

                while (!verified) {
                    verified = verifyFile(filename, hash);
                    if (!verified) {
                        publishProgress("Re-downloading " + filename);
                        deleteFile(filename);
                        downloadFile(filename);
                    }
                }
            }
        } catch (Exception e) {
            publishProgress("Unable to verify data files > " + e.getMessage());
            e.printStackTrace();
        }
    }

    boolean verifyFile(String filename, String checksum) {
        try {
            String downloadedChecksum = getMD5Checksum(filename);
            System.out.print(filename);
            if (downloadedChecksum.equalsIgnoreCase(checksum)) {
                // publishProgress(
                //        "Verified: " + (filename + "(" + downloadedChecksum + " = " + checksum + ") -> OK"));
                return true;
            }

            //publishProgress("Verification failed, re-downloading " + filename + "...");
            // publishProgress("Downloaded file: " + filename + " hash:" + downloadedChecksum
            //        + " doesn't match official MD5: " + checksum + " re-downloading");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            publishProgress("Verification Error " + e.getCause().getMessage());
        }
        return false;
    }

    private void publishProgress(String s) {
        statusTextStr = s;
    }

    private void setProgress(int i) {
       // progressBar.setValue(i);
    }


    public static void deleteFile(String key) {
        Gdx.files.local(Config.CACHE_DIR + key).delete();
    }

    public String getMD5Checksum(String filename) {
        InputStream fis = null;
        try {
            fis = Gdx.files.local(Config.CACHE_DIR + filename).read();
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            byte[] b = complete.digest();
            String result = "";
            for (int i = 0; i < b.length; i++) {
                result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void downloadFile(String filename) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(CACHE_URL + filename).openConnection();
            connection.connect();
            int fileLength = connection.getContentLength();

            FileOutputStream fos = (FileOutputStream) Gdx.files.local(Config.CACHE_DIR + filename).write(false);
            try {
                InputStream in = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int total = 0;
                int len = 0;
                while ((len = in.read(buffer)) > 0) {
                    total += len;
                    if (fileLength > 0) {
                        int progress = (total * 100) / fileLength;

                        setProgress(progress);
                    }
                    fos.write(buffer, 0, len);
                }
                fos.flush();
            } finally {
                fos.close();
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
