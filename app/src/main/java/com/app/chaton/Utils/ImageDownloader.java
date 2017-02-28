package com.app.chaton.Utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ImageDownloader extends AsyncTask<Void, Void, byte[]> {
    private static final String DOWNLOAD_URL = "https://chaton.ga/uploads/avatars/";

    private String image_url;
    private DbHelper dbHelper;

    public ImageDownloader(Context context, String image_url) {
        this.dbHelper = new DbHelper(context);
        this.image_url = image_url;
    }

    public static byte[] streamToByteArray(InputStream inputStream) throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    public static InputStream downloadImage(String image_url) throws IOException{
        URL url = new URL(DOWNLOAD_URL + image_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();

        return connection.getInputStream();
    }

    @Override
    protected byte[] doInBackground(Void... voids) {
        try {
            byte[] bm_data;

            if (!dbHelper.avatarInCache(image_url)) {
                bm_data = streamToByteArray(downloadImage(image_url));
                dbHelper.writeAvatarToCache(image_url, bm_data);
            } else bm_data = dbHelper.getAvatarFromCache(image_url);

            return bm_data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}