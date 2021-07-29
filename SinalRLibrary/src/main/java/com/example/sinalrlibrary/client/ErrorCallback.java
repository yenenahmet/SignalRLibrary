/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.example.sinalrlibrary.client;

public interface ErrorCallback {
    /**
     * Callback invoked when an error is found
     * 
     * @param error
     *            The error
     */
    public void onError(Throwable error);
}
