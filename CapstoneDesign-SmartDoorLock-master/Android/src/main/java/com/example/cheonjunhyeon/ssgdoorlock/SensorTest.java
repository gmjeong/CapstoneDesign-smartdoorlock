package com.example.cheonjunhyeon.ssgdoorlock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SensorTest extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager = null;

    public double[] mag_val, acc_val;
    public int mag_cnt;
    public int mag_sample_cnt = 20;
    public int move_cnt, stop_cnt, acc_cnt;
    public int acc_sample_cnt = 100;
    private boolean move_flag;
    private boolean mag_inout_flag;

    TextView mag, acc, result;


    TextView tv;
    ToggleButton tb;

    private double home_longitude; //경도
    private double home_latitude;  //위도

    private double lat208 = 37.5035436;
    private double lon208 = 126.95783929999999;
    private double latCen = 37.505088;
    private double lonCen = 126.9571012;

    private Location location208;
    LocationManager lm;
//    GnssStatus.Callback mGnssStatusCallback;
    private int satCnt;

    private Kalman mKalmanAccX, mKalmanAccY, mKalmanAccZ;

    private BluetoothAdapter mBluetoothAdapter;
    Button dis;

    ToggleButton thrTest;
    AccelerometerThread accThr;
    GeomagnetismThread magThr;
    GpsThread gpsThr;
    SatelliteCountThread satThr;
    GetRssiThread rssiThr;

    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;



    private final String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mag_val = new double[mag_sample_cnt];
        acc_val = new double[3];
        mag_cnt = 0;
        acc_cnt = 0;
        stop_cnt = 0;
        move_cnt = 0;
        move_flag = false;
        mag_inout_flag = false;

        mag = (TextView) findViewById(R.id.mag);
        acc = (TextView) findViewById(R.id.acc);
        result = (TextView) findViewById(R.id.result);

        mKalmanAccX = new Kalman(0.0f);
        mKalmanAccY = new Kalman(0.0f);
        mKalmanAccZ = new Kalman(0.0f);

//        +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        tv = (TextView) findViewById(R.id.textView2);
        tv.setText("위치정보 미수신중");

        tb = (ToggleButton) findViewById(R.id.toggle1);

        // LocationManager 객체를 얻어온다
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        location208 = new Location("208");
        location208.setLatitude(lat208);
        location208.setLongitude(lon208);

//        tb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    if (tb.isChecked()) {
//                        tv.setText("수신중..");
//                        // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
//                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
//                                100, // 통지사이의 최소 시간간격 (miliSecond)
//                                1, // 통지사이의 최소 변경거리 (m)
//                                mLocationListener);
//                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
//                                100, // 통지사이의 최소 시간간격 (miliSecond)
//                                1, // 통지사이의 최소 변경거리 (m)
//                                mLocationListener);
//                    } else {
//                        tv.setText("위치정보 미수신중");
//                        lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
//                    }
//                } catch (SecurityException ex) {
//                }
//            }
//        });


        satCnt = -1;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            mGnssStatusCallback = new GnssStatus.Callback() {
//                @Override
//                public void onSatelliteStatusChanged(GnssStatus status) {
//                    super.onSatelliteStatusChanged(status);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        if(satCnt == status.getSatelliteCount()) {
//
//                        }
//                        else {
//                            Log.d("위성", "위성 개수 : " + String.valueOf(status.getSatelliteCount()));
//                            Toast.makeText(getApplicationContext(), "위성 개수 : " + String.valueOf(status.getSatelliteCount()), Toast.LENGTH_SHORT).show();
//                            satCnt = status.getSatelliteCount();
//                        }
//
//
//                    }
//                }
//            };
//        }

//        ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable(); //강제 활성화
        }

        dis = (Button) findViewById(R.id.bthTestBut);
        dis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.startDiscovery();
                Log.d("Sibal", "start Discovery");
            }
        });

        thrTest = (ToggleButton) findViewById(R.id.toggle2);
        thrTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thrTest.isChecked()) {
//                    accThr = new AccelerometerThread((SensorManager) getSystemService(SENSOR_SERVICE));
//                    accThr.start();
//                    gpsThr = new GpsThread((LocationManager) getSystemService(LOCATION_SERVICE), location208, SensorTest.this);
//                    gpsThr.start();
//                    satThr = new SatelliteCountThread((LocationManager) getSystemService(LOCATION_SERVICE), SensorTest.this);
//                    satThr.start();
                    rssiThr = new GetRssiThread(mBluetoothAdapter, SensorTest.this);
                    rssiThr.start();
//                    magThr = new GeomagnetismThread((SensorManager) getSystemService(SENSOR_SERVICE));
//                    magThr.start();
                }
                else {
//                    accThr.finish();
//                    accThr = null;
//                    gpsThr.finish();
//                    gpsThr = null;
//                    satThr.finish();
//                    satThr = null;
                    rssiThr.finish();
                    rssiThr = null;
//                    magThr.finish();
//                    magThr = null;
                }
            }
        });

        if (!chkPermissions()) {
            ActivityCompat.requestPermissions(SensorTest.this, permissions, 100);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        mSensorManager.registerListener(this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                SensorManager.SENSOR_DELAY_UI);

//        mSensorManager.registerListener(this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
//                SensorManager.SENSOR_DELAY_FASTEST);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            if (lm != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    lm.registerGnssStatusCallback(mGnssStatusCallback);
//                }
//            }
//        }
        startHandlerThread();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mHandler.sendEmptyMessage(1);
//            }
//        }, 1000);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;
        int sensorType = sensorEvent.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (mag_cnt == mag_sample_cnt) {
                    for (int i = 0; i < mag_sample_cnt-1; i++) {
                        mag_val[i] = mag_val[i+1];
                    }
                    mag_val[mag_sample_cnt-1] = Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
                }
                else {
                    mag_val[mag_cnt++] = Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
                }

                double mag_avg = 0;
                double mag_var = 0;
                int d = 0;
                if (mag_cnt == 1) {
                    d = mag_cnt;
                }
                else d = mag_cnt;
                for (int i = 0; i < mag_cnt; i++) {
                    mag_avg += mag_val[i];
                }

                mag_avg /= d;

                for (int i = 0; i < mag_cnt; i++) {
                    mag_var = (mag_val[i] - mag_avg) * (mag_val[i] - mag_avg);
                }
                mag_var /= d;
                String mag_var_result = String.format("%.3f", mag_var);
                mag.setText(mag_var_result);
//                if (magCnt == magSampleCnt) {
//                    magCnt = 0;
//                    double mag_avg = 0;
//                    double mag_var = 0;
//                    for (int i = 0; i < magSampleCnt; i++) {
//                        mag_avg += magVal[i];
//                    }
//                    mag_avg = mag_avg/magSampleCnt;
//
//                    for (int i = 0; i < magSampleCnt; i++) {
//                        double temp = magVal[i] - mag_avg;
//                        mag_var += Math.pow(temp, 2);
//                    }
//                    mag_var = mag_var/magSampleCnt;
//
//                    String mag_var_result = String.format("%.3f", mag_var);
//
//                    mag.setText(mag_var_result);
//                } else {
//                    magVal[magCnt++] = Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
//                    String mag_val_result = String.format("%.1f", magVal[magCnt-1]);
//                    acc.setText(mag_val_result);
//                }
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (acc_cnt == acc_sample_cnt) {
                    acc_cnt = 0;
//                    acc.setText("move : " + String.valueOf(move_cnt));
                    result.setText("stop : " + String.valueOf(stop_cnt));
                    move_cnt = 0;
                    stop_cnt = 0;
                }
                else {
                    //                acc_val = Math.sqrt(values[0]*values[0]+values[2]*values[2]); //values[1]*values[1]+
                    double filteredX = 0.0f;
                    double filteredY = 0.0f;
                    double filteredZ = 0.0f;
//                filteredX = mKalmanAccX.update(sensorEvent.values[0]);
//                filteredY = mKalmanAccY.update(sensorEvent.values[1]);
//                filteredZ = mKalmanAccZ.update(sensorEvent.values[2]);

                    filteredY = sensorEvent.values[1];
                    filteredZ = sensorEvent.values[2];
                    filteredX = sensorEvent.values[0];

                    double accVal = Math.sqrt(filteredX*filteredX + filteredY*filteredY + filteredZ*filteredZ);
//                    double accVec = Math.abs(filteredX) + Math.abs(filteredY) + Math.abs(filteredZ);
//                    String acc_result = String.format("%.1f", accVal);
//                    String vec_result = String.format("%.1f", accVec);
//                    acc.setText(acc_result+" m/s2");
//                    result.setText(vec_result);

                    if (accVal > 0.5) move_cnt++;
                    else stop_cnt++;
                    acc_cnt++;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            Location temp = new Location("temp");
            temp.setLongitude(longitude);
            temp.setLatitude(latitude);
            float distance = location208.distanceTo(temp);
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            tv.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : "  + accuracy + "\n208관과의 거리 : " + distance);
            Log.d(".SensorTest", "208관과의 거리 : " + String.valueOf(distance));
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (name == null) {
                    return;
                }
                else if (name.equals("nsl")) {
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    Log.d("Sibal", name + " rssi : " + String.valueOf(rssi));
                    mBluetoothAdapter.cancelDiscovery();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBluetoothAdapter.startDiscovery();
                }

            }
        }
    };

    private Boolean chkPermissions() {
        Boolean rtn = true;

        for (String perm : permissions) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(SensorTest.this, perm)
                    != PackageManager.PERMISSION_GRANTED) {

                rtn = false;
            }
        }

        return rtn;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(receiver);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            lm.unregisterGnssStatusCallback(mGnssStatusCallback);
//        }
    }

    public void startHandlerThread() {
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what) {
                    case 1:
                        Log.d("hing", "msg 1");
                        mHandler.sendEmptyMessage(2);
                        break;
                    case 2:
                        Log.d("hing", "msg 2");
                        mHandler.sendEmptyMessage(1);
                        break;
                    default:
                        break;
                }
            }
        };
    }
}

