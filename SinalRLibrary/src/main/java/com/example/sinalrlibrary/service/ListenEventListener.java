package com.example.sinalrlibrary.service;

import androidx.annotation.NonNull;

public interface ListenEventListener<T> {
    void onResult(@NonNull final T response);
}
