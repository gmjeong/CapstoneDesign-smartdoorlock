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

public class GpsThread extends Thread implements LocationListener {

    private LocationManager mLocationManager;
    private Location homeArea;
    private Handler locHandler;
    private float distance;

    private Context permissionContext;
    private Handler serviceHandler;

    private static final int STATE_TRUE = 1;
    private static final int STATE_FALSE = 2;

    public GpsThread(LocationManager locationService, Location homeArea, Context context) {
        mLocationManager = (LocationManager) locationService;
        this.homeArea = homeArea;
        permissionContext = context;
        distance = 10000;
    }

    public void run() {
        Looper.prepare();
        locHandler = new Handler();
        if (ActivityCompat.checkSelfPermission(permissionContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(permissionContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    5000, // 통지사이의 최소 시간간격 (miliSecond)
                    0, // 통지사이의 최소 변경거리 (m)
                    this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    5000, // 통지사이의 최소 시간간격 (miliSecond)
                    0, // 통지사이의 최소 변경거리 (m)
                    this);
        }
        Looper.loop();
    }

    public void finish() {
        if(locHandler != null) {
            locHandler.getLooper().quit();
        }
        mLocationManager.removeUpdates(this);
    }

    public int getStatus() {
        if (distance > 1000) {
            return STATE_FALSE;
        }
        else {
            return STATE_TRUE;
        }
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public void onLocationChanged(Location location) {
        //여기서 위치값이 갱신되면 이벤트가 발생한다.
        //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
        Log.d("siba", "onLocationChanged, location:" + location);
        double longitude = location.getLongitude(); //경도
        double latitude = location.getLatitude();   //위도
        double altitude = location.getAltitude();   //고도
        float accuracy = location.getAccuracy();    //정확도
        String provider = location.getProvider();   //위치제공자
        Location temp = new Location("temp");
        temp.setLongitude(longitude);
        temp.setLatitude(latitude);
        float distance = homeArea.distanceTo(temp);
        this.distance = distance;

        //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
        //Network 위치제공자에 의한 위치변화
        //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
//        tv.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
//                + "\n고도 : " + altitude + "\n정확도 : "  + accuracy + "\n208관과의 거리 : " + distance);
        Log.d("siba", "208관과의 거리 : " + String.valueOf(distance));
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
