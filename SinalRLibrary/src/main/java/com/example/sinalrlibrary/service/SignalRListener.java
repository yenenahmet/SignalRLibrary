package com.example.sinalrlibrary.service;

public interface SignalRListener<T> {
    void onResult(T response);
    void onError(Exception ex);
}
