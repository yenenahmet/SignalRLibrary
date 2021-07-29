/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.example.sinalrlibrary.client.transport;

import com.example.sinalrlibrary.client.ConnectionBase;
import com.example.sinalrlibrary.client.Constants;
import com.example.sinalrlibrary.client.FutureHelper;
import com.example.sinalrlibrary.client.LogLevel;
import com.example.sinalrlibrary.client.Logger;
import com.example.sinalrlibrary.client.Platform;
import com.example.sinalrlibrary.client.SignalRFuture;
import com.example.sinalrlibrary.client.http.HttpConnection;
import com.example.sinalrlibrary.client.http.HttpConnectionFuture;
import com.example.sinalrlibrary.client.http.InvalidHttpStatusCodeException;
import com.example.sinalrlibrary.client.http.Request;
import com.example.sinalrlibrary.client.http.Response;

import java.io.IOException;


/**
 * ClientTransport base implementation over Http
 */
public abstract class HttpClientTransport implements ClientTransport {
    protected static final int BUFFER_SIZE = 1024;

    protected HttpConnection mHttpConnection;
    protected boolean mStartedAbort = false;
    protected SignalRFuture<Void> mAbortFuture = null;

    private Logger mLogger;

    /**
     * Initializes the HttpClientTransport with a logger
     * 
     * @param logger
     *            logger to log actions
     */
    public HttpClientTransport(Logger logger) {
        this(logger, Platform.createHttpConnection(logger));
    }

    public HttpClientTransport(Logger logger, HttpConnection httpConnection) {
        if (logger == null) {
            throw new IllegalArgumentException("logger");
        }

        mHttpConnection = httpConnection;
        mLogger = logger;
    }

    @Override
    public SignalRFuture<NegotiationResponse> negotiate(final ConnectionBase connection) {
        log("Start the negotiation with the server", LogLevel.Information);

        String url = connection.getUrl() + "negotiate" + TransportHelper.getNegotiateQueryString(connection);

        Request get = new Request(Constants.HTTP_GET);
        get.setUrl(url);
        get.setVerb(Constants.HTTP_GET);

        connection.prepareRequest(get);

        final SignalRFuture<NegotiationResponse> negotiationFuture = new SignalRFuture<NegotiationResponse>();

        log("Execute the request", LogLevel.Verbose);
        HttpConnectionFuture connectionFuture = mHttpConnection.execute(get, new HttpConnectionFuture.ResponseCallback() {

            public void onResponse(Response response) {
                try {
                    log("Response received", LogLevel.Verbose);
                    throwOnInvalidStatusCode(response);

                    log("Read response data to the end", LogLevel.Verbose);
                    String negotiationContent = response.readToEnd();

                    log("Trigger onSuccess with negotiation data: " + negotiationContent, LogLevel.Verbose);
                    negotiationFuture.setResult(new NegotiationResponse(negotiationContent, connection.getJsonParser()));

                } catch (Throwable e) {
                    log(e);
                    negotiationFuture.triggerError(new NegotiationException("There was a problem in the negotiation with the server", e));
                }
            }
        });

        FutureHelper.copyHandlers(connectionFuture, negotiationFuture);

        return negotiationFuture;
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, final DataResultCallback callback) {
        try {
            log("Start sending data to the server: " + data, LogLevel.Information);

            Request post = new Request(Constants.HTTP_POST);
            post.setFormContent("data", data);
            post.setUrl(connection.getUrl() + "send" + TransportHelper.getSendQueryString(this, connection));
            post.setHeaders(connection.getHeaders());
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");

            connection.prepareRequest(post);

            log("Execute the request", LogLevel.Verbose);
            HttpConnectionFuture future = mHttpConnection.execute(post, new HttpConnectionFuture.ResponseCallback() {

                @Override
                public void onResponse(Response response) throws Exception {
                    log("Response received", LogLevel.Verbose);
                    throwOnInvalidStatusCode(response);

                    log("Read response to the end", LogLevel.Verbose);
                    String data = response.readToEnd();

                    if (data != null) {
                        log("Trigger onData with data: " + data, LogLevel.Verbose);
                        callback.onData(data);
                    }
                }
            });

            return future;
        } catch (Throwable e) {
            log(e);

            SignalRFuture<Void> future = new SignalRFuture<Void>();
            future.triggerError(e);

            return future;
        }
    }

    @Override
    public SignalRFuture<Void> abort(ConnectionBase connection) {
        synchronized (this) {
            if (!mStartedAbort) {
                log("Started aborting", LogLevel.Information);
                mStartedAbort = true;
                try {
                    String url = connection.getUrl() + "abort" + TransportHelper.getSendQueryString(this, connection);

                    Request post = new Request(Constants.HTTP_POST);

                    post.setUrl(url);
                    post.setHeaders(connection.getHeaders());

                    connection.prepareRequest(post);

                    log("Execute request", LogLevel.Verbose);
                    mAbortFuture = mHttpConnection.execute(post, new HttpConnectionFuture.ResponseCallback() {

                        @Override
                        public void onResponse(Response response) {
                            log("Finishing abort", LogLevel.Verbose);
                            mStartedAbort = false;
                        }
                    });

                    return mAbortFuture;

                } catch (Throwable e) {
                    log(e);
                    log("Finishing abort", LogLevel.Verbose);
                    mStartedAbort = false;

                    SignalRFuture<Void> future = new SignalRFuture<Void>();
                    future.triggerError(e);

                    return future;
                }
            } else {
                return mAbortFuture;
            }
        }
    }

    protected void throwOnInvalidStatusCode(Response response) throws InvalidHttpStatusCodeException {
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            String responseContent;

            try {
                responseContent = response.readToEnd();
            } catch (IOException e) {
                responseContent = "";
            }

            StringBuilder headersString = new StringBuilder();

            for (String header : response.getHeaders().keySet()) {
                headersString.append("[");
                headersString.append(header);
                headersString.append(": ");
                for (String headerValue : response.getHeader(header)) {
                    headersString.append(headerValue);
                    headersString.append("; ");
                }
                ;
                headersString.append("]; ");
            }

            throw new InvalidHttpStatusCodeException(response.getStatus(), responseContent, headersString.toString());
        }

    }

    protected void log(String message, LogLevel level) {
        mLogger.log(getName() + " - " + message, level);
    }

    protected void log(Throwable error) {
        mLogger.log(getName() + " - Error: " + error.toString(), LogLevel.Critical);
    }

}
