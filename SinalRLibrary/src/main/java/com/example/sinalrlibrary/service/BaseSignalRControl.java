package com.example.sinalrlibrary.service;

import android.os.Handler;
import android.util.Log;

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

    private static final String TAG = BaseSignalRControl.class.getSimpleName();
    private final Handler handler = new Handler();
    private @NonNull
    final HubConnection mHubConnection;
    private @NonNull
    final HubProxy mHubProxy;
    private ConnectionListener connectionEventListener;
    private final boolean debug;

    public BaseSignalRControl(@NonNull final String hubUrl,
                              @NonNull final String hubName,
                              final boolean autoStart, final boolean debug) {
        this.mHubConnection = new HubConnection(hubUrl);
        this.mHubProxy = mHubConnection.createHubProxy(hubName);
        if (autoStart)
            startSignalR();
        this.debug = debug;
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
            signalRFuture.done(obj -> handler.post(() -> {
                        if (debug) {
                            Log.e(TAG, obj.toString());
                        }
                        signalRListener.onResult(obj);
                    }
            ));
            signalRFuture.onError(error -> {
                if (debug) {
                    Log.e(TAG, error.toString());
                }
                signalRListener.onError((Exception) error);
            });
        } else {
            signalRListener.onError(new Exception("Not Connected"));
        }
    }

    public <T> void pushData(@NonNull final Class<T> resultClass,
                             @NonNull final String method,
                             @NonNull final Object... requestClasses) {
        if (mHubConnection.getState() == ConnectionState.Connected) {
            final SignalRFuture<T> signalRFuture = mHubProxy.invoke(resultClass, method, requestClasses);
            signalRFuture.done(obj -> {
                if (debug) {
                    Log.e(TAG, obj.toString());
                }
            });
            signalRFuture.onError(error -> {
                if (debug) {
                    Log.e(TAG, error.toString());
                }
            });
        }
    }

    public <T> void listenEvent(@NonNull final SignalRListener<T> signalRListener,
                                @NonNull final String eventName,
                                @NonNull final Class<T> resultClass) {
        mHubProxy.on(eventName, (SubscriptionHandler1<T>) p1 ->
                handler.post(() -> {
                            if (debug) {
                                Log.e(TAG, p1.toString());
                            }
                            signalRListener.onResult(p1);
                        }
                ), resultClass);
    }

    public <T> void listenEvent(@NonNull final ListenEventListener<T> listener,
                                @NonNull final String eventName,
                                @NonNull final Class<T> resultClass) {
        mHubProxy.on(eventName, (SubscriptionHandler1<T>) p1 ->
                handler.post(() -> {
                            if (debug) {
                                Log.e(TAG, p1.toString());
                            }
                            if (p1 != null) {
                                listener.onResult(p1);
                            }
                        }
                ), resultClass);
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

