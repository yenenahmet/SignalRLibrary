package com.example.sinalrlibrary.service;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.example.sinalrlibrary.client.ConnectionState;
import com.example.sinalrlibrary.client.Platform;
import com.example.sinalrlibrary.client.SignalRFuture;
import com.example.sinalrlibrary.client.hubs.HubConnection;
import com.example.sinalrlibrary.client.hubs.HubProxy;
import com.example.sinalrlibrary.client.hubs.SubscriptionHandler1;
import com.example.sinalrlibrary.client.transport.ClientTransport;
import com.example.sinalrlibrary.client.transport.ServerSentEventsTransport;
import com.example.sinalrlibrary.platform.AndroidPlatformComponent;

public class BaseSignalRControl {

    private final Handler handler = new Handler();
    private @NonNull
    final HubConnection mHubConnection;
    private @NonNull
    final HubProxy mHubProxy;
    private ConnectionListener connectionEventListener;
    private static BaseSignalRControl baseSignalRControl;

    public synchronized BaseSignalRControl getInstance(@NonNull final String hubUrl,
                                                       @NonNull final String hubName,
                                                       final boolean autoStart) {
        if (baseSignalRControl == null) {
            baseSignalRControl = new BaseSignalRControl(hubUrl, hubName,autoStart);
        }
        return baseSignalRControl;
    }

    public BaseSignalRControl(@NonNull final String hubUrl,
                              @NonNull final String hubName,
                              final boolean autoStart) {
        mHubConnection = new HubConnection(hubUrl);
        mHubProxy = mHubConnection.createHubProxy(hubName);
        if (autoStart)
            startSignalR();
    }

    private void startSignalR() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        final ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        mHubConnection.closed(() -> handler.postDelayed(() -> {
            if (mHubConnection.getState() != ConnectionState.Connected) {
                if (connectionEventListener != null) {
                    connectionEventListener.onClosed();
                }
                mHubConnection.start(clientTransport);
            }
        }, 500));

        mHubConnection.connected(() -> {
            if (connectionEventListener != null &&
                    mHubConnection.getState() == ConnectionState.Connected) {
                connectionEventListener.onConnected();
            }
        });

        mHubConnection.error(error -> {
            if (connectionEventListener != null) {
                connectionEventListener.onError(error);
            }
        });

        mHubConnection.start(clientTransport);
    }

    public <T> void pushData(@NonNull final SignalRListener<T> signalRListener,
                             @NonNull final Class<T> resultClass,
                             @NonNull final String method,
                             @NonNull final Object... requestClasses) {
        if (mHubConnection.getState() == ConnectionState.Connected) {
            final SignalRFuture<T> signalRFuture = mHubProxy.invoke(resultClass, method, requestClasses);
            signalRFuture.done(obj -> handler.post(() ->
                    signalRListener.onResult(obj)));
            signalRFuture.onError(error -> signalRListener.onError((Exception) error));
        } else {
            signalRListener.onError(new Exception("Not Connected"));
        }
    }

    public <T> void listenEvent(@NonNull final SignalRListener<T> signalRListener,
                                @NonNull final String eventName,
                                @NonNull final Class<T> resultClass) {
        mHubProxy.on(eventName, (SubscriptionHandler1<T>) p1 ->
                handler.post(() ->
                        signalRListener.onResult(p1)), resultClass);
    }

    public void setConnectionEventListener(@NonNull ConnectionListener listener) {
        this.connectionEventListener = listener;
    }

    public void unBindListener() {
        connectionEventListener = null;
    }

    public void stop() {
        mHubConnection.stop();
    }

}

