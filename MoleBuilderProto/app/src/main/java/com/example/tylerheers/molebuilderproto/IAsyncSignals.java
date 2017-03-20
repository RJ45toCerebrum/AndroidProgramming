package com.example.tylerheers.molebuilderproto;

/**
 * Created by Tyler on 3/11/2017.
 */

// Use when you need updates from AsyncTask's
public interface IAsyncSignals<T, R> extends IAsyncResult<R> {
    void onPreExecute();
    void onBackgroundUpdate(T data);
    void onPostExecute(R result);
}
