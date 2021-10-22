package com.example.cheonjunhyeon.ssgdoorlock;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

public class SatelliteCountThread extends Thread implements LocationListener {

    private LocationManager mLocationManager;
    private Handler satelliteHandler;
    private GnssStatus.Callback mGnssStatusCallback;
    private int satCnt;

    private Context permissionContext;

    private static final int STATE_TRUE = 1;
    private static final int STATE_FALSE = 2;

    public SatelliteCountThread(LocationManager locationService, Context context) {
        mLocationManager = (LocationManager) locationService;
        permissionContext = context;
        satCnt = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mGnssStatusCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    super.onSatelliteStatusChanged(status);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (satCnt == status.getSatelliteCount()) {

                        } else {
                            Log.d("siba", "위성 개수 : " + String.valueOf(status.getSatelliteCount()));
//                            Toast.makeText(permissionContext, "위성 개수 : " + String.valueOf(status.getSatelliteCount()), Toast.LENGTH_SHORT).show();
                            satCnt = status.getSatelliteCount();
                        }
                    }
                }
            };
        }
    }

    public void run() {
        Looper.prepare();
        satelliteHandler = new Handler();
        if (ActivityCompat.checkSelfPermission(permissionContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        100,
                        0,
                        this);
            }
        }
        Log.d("siba", "siba");
        Looper.loop();
    }

    public void finish() {
        if(satelliteHandler != null) {
            satelliteHandler.getLooper().quit();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mLocationManager.removeUpdates(this);
            mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
        }
    }

    public int getStatus() {
        if (satCnt < 25 && satCnt >= 0) {
            return STATE_TRUE;
        }
        else {
            return STATE_FALSE;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
