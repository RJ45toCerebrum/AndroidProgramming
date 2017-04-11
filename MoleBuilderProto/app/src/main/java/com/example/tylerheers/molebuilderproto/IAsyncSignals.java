package com.example.tylerheers.molebuilderproto;

/**
 * Created by Tyler on 3/11/2017.
 *
 * For recieving signals from PostRequest
 */

// Use when you need updates from AsyncTask's
public interface IAsyncSignals<T, R> extends IAsyncResult<R>
{
    // executed during request
    void onBackgroundUpdate(T data);

    // Excuting after request completed
    void onPostExecute(R result);
}
