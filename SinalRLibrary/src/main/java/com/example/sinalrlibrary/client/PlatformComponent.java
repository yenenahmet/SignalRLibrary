/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.example.sinalrlibrary.client;


import com.example.sinalrlibrary.client.http.HttpConnection;

public interface PlatformComponent {
    /**
     * Returns a platform-specific HttpConnection
     */
    public HttpConnection createHttpConnection(Logger logger);

    /**
     * Returns a platform-specific Operating System name
     */

    public String getOSName();
}
