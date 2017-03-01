package com.example.tylerheers.molebuilderproto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by tylerheers on 2/27/17.
 */

public class PostRequest extends AsyncTask<String, String, Bitmap> {



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public Bitmap doInBackground(String... params)
    {
        String url = params[0];
        Bitmap bitmap = DownloadImage(url);

        return bitmap;
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


    @Override
    public void onPostExecute(Bitmap result){
        if(result != null){
            Log.i("Not Null Image", "Image not null");
        }
    }

    public String TextCon(String... params){
        String urlString = params[0];
        String resultToDisplay = "";

        InputStream in;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while(true)
            {
                String line = reader.readLine();
                if(line == null){
                    break;
                }

                resultToDisplay += line;
            }

        }
        catch (Exception e) {
            Log.e("Exception", e.getMessage());
            return e.getMessage();
        }
        finally {

            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return resultToDisplay;
    }

}
