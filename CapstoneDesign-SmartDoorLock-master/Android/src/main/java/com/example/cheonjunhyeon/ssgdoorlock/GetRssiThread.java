package com.example.cheonjunhyeon.ssgdoorlock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class GetRssiThread extends Thread {

    private BroadcastReceiver mReceiver;
    private BluetoothAdapter mAdapter;
    private Handler rssiHandler;
    private IntentFilter itf;
    private Context rcvContext;

    private int rssi;
    private boolean isUpdate;
    public boolean isStop;

    private static final int STATE_TRUE = 1;
    private static final int STATE_FALSE = 2;

    public GetRssiThread(BluetoothAdapter bleApt, Context context) {
        mAdapter = bleApt;
        rcvContext = context;
        itf = new IntentFilter();

        rssi = -100;
        isUpdate = false;
        isStop = false;
    }

    public void thrStartDiscovery() {
        mAdapter.startDiscovery();
    }

    private void registerReceiver() {
        if (mReceiver != null) return;

        itf = new IntentFilter();
        itf.addAction(BluetoothDevice.ACTION_FOUND);
        itf.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        itf.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);

//                if(BluetoothAdapter.SCAN_MODE_NONE == state) {
//                    Log.d("siba", "SCAN_MODE_NONE");
//                    unregisterReceiver();
//                    registerReceiver();
//                    mAdapter.startDiscovery();
//                    return;
//                }

                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    try{
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                            e.printStackTrace();
                    }
//                    Log.d("logic", "startDiscovery");
                    if(!isStop) {
                        mAdapter.startDiscovery();
                    }

                    return;
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    rssiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.cancelDiscovery();
//                            Log.d("logic", "cancelDiscovery");
                        }
                    }, 3000);
                }

                if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                    if (name == null) {
//                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//                        Log.d("logic", "null rssi : " + String.valueOf(rssi));
                        return;
                    }
                    else if (name.equals("nsl")) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        setFlag(true);
                        Log.d("logic", "flag true rssi " + String.valueOf(rssi));
//                        mAdapter.cancelDiscovery();
//                        try{
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                    else {
//                        Log.d("logic", "rssi " + name);
                    }
                }
            }
        };

        rcvContext.registerReceiver(this.mReceiver, itf);
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            rcvContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mAdapter.cancelDiscovery();
    }

    public void run() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        rssiHandler = new Handler();

        registerReceiver();
        mAdapter.startDiscovery();
        Looper.loop();
    }

    public void finish() {
        if(rssiHandler != null) {
            rssiHandler.getLooper().quit();

        }
        if(mReceiver != null) {
            unregisterReceiver();
        }
    }

    public int getStatus() {
        if (rssi > -75) return STATE_TRUE;
        else return STATE_FALSE;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public synchronized void setFlag(boolean bool) {
        isUpdate = bool;
    }

    public void setInitRSSI(int rssi) {
        this.rssi = rssi;
    }


}
