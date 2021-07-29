package com.example.sinalrlibrary.platform;

import android.os.Build;

import com.example.sinalrlibrary.client.Logger;
import com.example.sinalrlibrary.client.Platform;
import com.example.sinalrlibrary.client.PlatformComponent;
import com.example.sinalrlibrary.client.http.HttpConnection;


public class AndroidPlatformComponent implements PlatformComponent {

    @Override
    public HttpConnection createHttpConnection(Logger logger) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            return new AndroidHttpConnection(logger);
        } else {
            return Platform.createDefaultHttpConnection(logger);
        }
    }

    @Override
    public String getOSName() {
        return "android";
    }

}