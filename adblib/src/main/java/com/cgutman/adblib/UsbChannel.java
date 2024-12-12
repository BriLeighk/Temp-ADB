package com.cgutman.adblib;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles USB communication channel for ADB protocol.
 */
public class UsbChannel implements AdbChannel {

    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;
    private final UsbInterface mInterface;
    private final ReentrantLock channelLock = new ReentrantLock();

    private static final int DEFAULT_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 100;

    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<>();

    /**
     * Return an IN request to the pool
     */
    public void releaseInRequest(UsbRequest request) {
        synchronized (mInRequestPool) {
            mInRequestPool.add(request);
        }
    }

    /**
     * Get an IN request from the pool
     */
    public UsbRequest getInRequest() {
        synchronized (mInRequestPool) {
            if (mInRequestPool.isEmpty()) {
                UsbRequest request = new UsbRequest();
                request.initialize(mDeviceConnection, mEndpointIn);
                return request;
            } else {
                return mInRequestPool.removeFirst();
            }
        }
    }

    @Override
    public void readx(byte[] buffer, int length) throws IOException {
        channelLock.lock();
        try {
            int totalRead = 0;
            int retryCount = 0;
            boolean wasInterrupted = false;

            while (totalRead < length && !wasInterrupted) {
                try {
                    ByteBuffer buf = ByteBuffer.allocate(length - totalRead);
                    UsbRequest request = getInRequest();
                    if (request == null) {
                        throw new IOException("Failed to obtain USB request object");
                    }

                    try {
                        request.queue(buf, length - totalRead);

                        // Wait for the request with timeout
                        UsbRequest response = mDeviceConnection.requestWait();
                        if (response == null || response != request) {
                            if (++retryCount > MAX_RETRIES) {
                                throw new IOException("USB read failed after " + MAX_RETRIES + " retries");
                            }
                            Thread.sleep(RETRY_DELAY_MS);
                            continue;
                        }

                        int bytesRead = buf.position();
                        if (bytesRead > 0) {
                            buf.rewind();
                            buf.get(buffer, totalRead, bytesRead);
                            totalRead += bytesRead;
                            retryCount = 0; // Reset retry count on successful read
                        } else if (++retryCount > MAX_RETRIES) {
                            throw new IOException("No data read from USB after " + MAX_RETRIES + " attempts");
                        }

                    } finally {
                        releaseInRequest(request);
                    }

                } catch (InterruptedException e) {
                    wasInterrupted = true;
                    Thread.currentThread().interrupt();
                    throw new IOException("USB read interrupted", e);
                }
            }

            if (totalRead < length && !wasInterrupted) {
                throw new IOException("Incomplete read: expected " + length + " bytes, got " + totalRead);
            }
        } finally {
            channelLock.unlock();
        }
    }

    @Override
    public void writex(AdbMessage message) throws IOException {
        channelLock.lock();
        try {
            writex(message.getMessage());
            if (message.getPayload() != null) {
                writex(message.getPayload());
            }
        } finally {
            channelLock.unlock();
        }
    }

    private void writex(byte[] buffer) throws IOException {
        int offset = 0;
        int retryCount = 0;

        while (offset < buffer.length) {
            byte[] tmp = new byte[buffer.length - offset];
            System.arraycopy(buffer, offset, tmp, 0, tmp.length);

            int transferred = mDeviceConnection.bulkTransfer(mEndpointOut, tmp, tmp.length, DEFAULT_TIMEOUT);

            if (transferred < 0) {
                if (++retryCount > MAX_RETRIES) {
                    throw new IOException("Bulk transfer failed after " + MAX_RETRIES + " retries");
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("USB write interrupted", e);
                }
                continue;
            }

            offset += transferred;
            retryCount = 0; // Reset retry count on successful write
        }
    }

    @Override
    public void close() throws IOException {
        channelLock.lock();
        try {
            // Clear the request pool
            synchronized (mInRequestPool) {
                mInRequestPool.clear();
            }
            mDeviceConnection.releaseInterface(mInterface);
            mDeviceConnection.close();
        } finally {
            channelLock.unlock();
        }
    }

    public UsbChannel(UsbDeviceConnection connection, UsbInterface intf) {
        mDeviceConnection = connection;
        mInterface = intf;

        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        // Look for our bulk endpoints
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }
        if (epOut == null || epIn == null) {
            throw new IllegalArgumentException("Not all endpoints found");
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;
    }
}