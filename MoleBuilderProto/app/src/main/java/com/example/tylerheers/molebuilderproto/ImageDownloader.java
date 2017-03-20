package com.example.tylerheers.molebuilderproto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;

/**
 * Created by tylerheers on 2/27/17.
 */

public class ImageDownloader extends AsyncTask<String, String, MainActivity>
{
    private Bitmap bitmap;
    private boolean success;
    MainActivity activity = null;

    public ImageDownloader(MainActivity activity)
    {
        if(activity == null)
            throw new InvalidParameterException("Cant have null activity");

        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public MainActivity doInBackground(String... params)
    {
        success = false;
        String url = params[0];
        Bitmap bitmap = DownloadImage(url);

        if(bitmap != null){
            success = true;
            this.bitmap = bitmap;
        }

        return this.activity;
    }

    private static Bitmap DownloadImage(String URL)
    {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return bitmap;
    }

    private static InputStream OpenHttpConnection(String urlString)
            throws IOException
    {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }
        catch (Exception ex)
        {
            throw new IOException("Error connecting");
        }
        return in;
    }

    @Nullable
    public Bitmap getBitmap()
    {
        if(success){
            return bitmap;
        }

        return null;
    }

    @Override
    public void onPostExecute(MainActivity callingActivity) {
        //callingActivity.setDownloadImage(this.bitmap);
    }
}
