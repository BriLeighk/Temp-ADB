package com.htetznaing.adbotg;

import android.util.Base64;

import com.cgutman.adblib.AdbBase64;

/**
 * Provides Base64 encoding functionality for ADB communication.
 * Created by xudong on 2/28/14.
 */
class MyAdbBase64 implements AdbBase64 {
    /**
     * Encodes the given byte array to a Base64 string.
     * @param data The byte array to encode.
     * @return The Base64 encoded string.
     */
    @Override
    public String encodeToString(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }
}
