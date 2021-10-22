package com.example.cheonjunhyeon.ssgdoorlock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GeomagnetismThread extends Thread implements SensorEventListener {

    private SensorManager mSensorManager;
    public double[] magVal;
    public int magCnt;
    public int magSampleCnt = 20;
    public int magVarCnt;
    public int magVarSampleCnt = 50;
    public int magIndoorCnt;
    public int magOutdoorCnt;
    public final int threshold = 5;

    public int isIndoor; //NONE - 0, TRUE - 1, FALSE - 2

    private Handler magHandler;

    public GeomagnetismThread(SensorManager sensorService) {
        mSensorManager = (SensorManager) sensorService;
        magVal = new double[magSampleCnt];
        magCnt = 0;
        magVarCnt = 0;
        magOutdoorCnt = 0;
        magIndoorCnt = 0;

        isIndoor = 0;
    }

    public void run() {
        Looper.prepare();
        magHandler = new Handler();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI, magHandler);
        Looper.loop();
    }

    public void finish() {
        if(magHandler != null) {
            magHandler.getLooper().quit();
        }
        mSensorManager.unregisterListener(this);
    }

    public int isIndoor() {
        return isIndoor;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;
        int sensorType = sensorEvent.sensor.getType();

        if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            if (magCnt == magSampleCnt) {
                for (int i = 0; i < magSampleCnt -1; i++) {
                    magVal[i] = magVal[i+1];
                }
                magVal[magSampleCnt -1] = Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
            }
            else {
                magVal[magCnt++] = Math.sqrt(values[0]*values[0] + values[1]*values[1] + values[2]*values[2]);
            }

            double magAvg = 0;
            double magVar = 0;
            int d = 0;
            if (magCnt == 1) {
                d = magCnt;
            }
            else d = magCnt;
            for (int i = 0; i < magCnt; i++) {
                magAvg += magVal[i];
            }

            magAvg /= d;

            for (int i = 0; i < magCnt; i++) {
                magVar = (magVal[i] - magAvg) * (magVal[i] - magAvg);
            }
            magVar /= d;
            if(magVarCnt == magVarSampleCnt) {
                if(magIndoorCnt > magOutdoorCnt) {
                    isIndoor = 1;
                }
                else {
                    isIndoor = 2;
                }
                magVarCnt = 0;
                magIndoorCnt = 0;
                magOutdoorCnt = 0;

                if(magVar > threshold) {
                    magIndoorCnt++;
                    magVarCnt++;
                }
                else {
                    magOutdoorCnt++;
                    magVarCnt++;
                }
            }
            else {
                if(magVar > threshold) {
                    magIndoorCnt++;
                    magVarCnt++;
                }
                else {
                    magOutdoorCnt++;
                    magVarCnt++;
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
