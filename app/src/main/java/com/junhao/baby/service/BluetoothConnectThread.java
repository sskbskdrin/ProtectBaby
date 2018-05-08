package com.junhao.baby.service;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This thread runs while attempting to make an outgoing connection with a
 * device. It runs straight through; the connection either succeeds or
 * fails.
 */
public class BluetoothConnectThread extends Thread {
    private static final String TAG = "BluetoothConnectThread";
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mmSocket = null;
    private BluetoothDevice mmDevice;

    private InputStream mmInStream = null;
    private OutputStream mmOutStream = null;

    private boolean isRunning = false;

    public BluetoothConnectThread(BluetoothDevice device) {
        mmDevice = device;
    }

    public void run() {
        sendEvent(BluetoothService.ACTION_GATT_CONNECTING, null);
        setName("ConnectThread");
        isRunning = true;
        // Always cancel discovery because it will slow down a connection
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            mmSocket.connect();
            // Get the BluetoothSocket input and output streams
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            sendEvent(BluetoothService.ACTION_GATT_DISCONNECTED, null);
            return;
        }

        // Start the connected thread
        byte[] buffer = new byte[50];
        byte[] data = new byte[50];
        int len = 0;
        int bytes;
        // Keep listening to the InputStream while connected
        sendEvent(BluetoothService.ACTION_GATT_CONNECTED, null);
        while (isRunning) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                if (bytes > 0) {
                    System.arraycopy(buffer, 0, data, len, bytes);
                    len += bytes;
                }
                // Send the obtained bytes to the UI Activity
                if (data[len - 1] == '}') {
                    byte[] result = new byte[len];
                    System.arraycopy(data, 0, result, 0, len);
                    sendEvent(BluetoothService.ACTION_DATA_AVAILABLE, result);
                    len = 0;
                }
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                sendEvent(BluetoothService.ACTION_GATT_DISCONNECTED, null);
                break;
            }
        }
    }

    private synchronized void sendEvent(String action, byte[] data) {
        Intent intent = new Intent(action);
        intent.putExtra(BluetoothService.EXTRA_DATA, data);
        intent.putExtra("properties", 0);
        intent.putExtra("uuid", MY_UUID_SECURE);
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);
            // Share the sent message back to the UI Activity
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        try {
            isRunning = false;
            if (mmInStream != null)
                mmInStream.close();
            if (mmOutStream != null)
                mmOutStream.close();
            if (mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "close() of connect " + " socket failed", e);
        }
    }
}