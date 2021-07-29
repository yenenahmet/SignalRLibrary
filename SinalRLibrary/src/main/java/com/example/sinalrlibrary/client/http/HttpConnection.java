/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.example.sinalrlibrary.client.http;


/**
 * Interface that defines a generic HttpConnection
 */
public interface HttpConnection {

    /**
     * Executes an request
     * 
     * @param request
     *            The request to execute
     * @param responseCallback
     *            The callback to invoke when the response is returned
     * @return A Future for the operation
     */
    public HttpConnectionFuture execute(final Request request, HttpConnectionFuture.ResponseCallback responseCallback);
}
