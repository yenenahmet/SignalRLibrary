package com.example.sinalrlibrary.platform;

import android.util.Base64;

import com.example.sinalrlibrary.client.http.BasicAuthenticationCredentials;


public class AndroidBase64Encoder implements BasicAuthenticationCredentials.Base64Encoder {
    @Override
    public String encodeBytes(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}