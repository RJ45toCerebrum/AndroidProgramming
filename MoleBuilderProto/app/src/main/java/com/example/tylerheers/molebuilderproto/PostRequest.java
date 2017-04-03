package com.example.tylerheers.molebuilderproto;


import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by tylerheers on 2/27/17.
 */

class PostRequest extends AsyncTask<String, String, String>
{
    private IAsyncResult caller = null;

    PostRequest(IAsyncResult caller){
        this.caller = caller;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public String doInBackground(String... params)
    {
        String url = params[0];
        return DownloadSDF(url);
    }

    private static String DownloadSDF(String urlString)
    {
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
                resultToDisplay += line + '\n';
            }

        }
        catch (Exception e) {
            Log.e("Post Request Exception", e.getMessage());
            resultToDisplay = null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return resultToDisplay;
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
    public void onPostExecute(String result)
    {
        if(caller != null) {
            caller.onPostExecute(result);
        }
    }
}
