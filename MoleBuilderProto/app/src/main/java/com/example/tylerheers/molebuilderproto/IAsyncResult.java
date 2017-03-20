package com.example.tylerheers.molebuilderproto;

/**
 * Created by Tyler on 3/13/2017.
 */

public interface IAsyncResult<T> {
    void onPostExecute(T result);
}
