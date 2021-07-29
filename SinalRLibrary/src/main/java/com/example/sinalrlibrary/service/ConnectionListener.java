package com.example.sinalrlibrary.service;

public interface ConnectionListener {
    void onConnected();
    void onClosed();
    void onError(Throwable throwable);
}
