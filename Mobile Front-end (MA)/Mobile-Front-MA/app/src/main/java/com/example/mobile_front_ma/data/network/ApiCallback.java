package com.example.mobile_front_ma.data.network;

/**
 * Simple success/error callback the repository uses to report results back to a ViewModel,
 * keeping Retrofit types out of the ViewModel/UI layers.
 */
public interface ApiCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
