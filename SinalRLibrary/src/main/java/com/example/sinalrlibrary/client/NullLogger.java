/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.example.sinalrlibrary.client;

/**
 * Null logger implementation
 */
public class NullLogger implements Logger {

    @Override
    public void log(String message, LogLevel level) {
    }
}
