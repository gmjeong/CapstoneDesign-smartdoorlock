package com.example.cheonjunhyeon.ssgdoorlock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AccelerometerThread extends Thread implements SensorEventListener {

    private SensorManager mSensorManager;
    public int moveCnt, stopCnt, accCnt;
    public int accSampleCnt = 100;

    private Handler accHandler;

    private boolean[] moveFlag;
    private int flagCnt;

    private static final int STATE_TRUE = 1;
    private static final int STATE_FALSE = 2;

    public AccelerometerThread(SensorManager sensorService) {
        mSensorManager = (SensorManager) sensorService;

        moveCnt = 0;
        stopCnt = 0;
        accCnt = 0;
        flagCnt = 0;
        moveFlag = new boolean[2];
    }

    public void run() {
        Looper.prepare();
        accHandler = new Handler();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_FASTEST, accHandler);
        Looper.loop();
    }

    public void finish() {
        if(accHandler != null) {
            accHandler.getLooper().quit();
        }
        mSensorManager.unregisterListener(this);
    }

    private void setMoveFlag(boolean bool) {
        if (flagCnt == 2) {
            flagCnt = 0;
            moveFlag[flagCnt++] = bool;
        }
        else {
            moveFlag[flagCnt++] = bool;
        }
    }

    public int isMovement() {
        if (moveFlag[0] || moveFlag[1]) {
            return STATE_TRUE;
        }
        else {
            return STATE_FALSE;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;
        int sensorType = sensorEvent.sensor.getType();

        if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (accCnt == accSampleCnt) {
                Log.d("siba", "move : " + String.valueOf(moveCnt) + " stop : " + String.valueOf(stopCnt));
                if(moveCnt > stopCnt) {
                    setMoveFlag(true);
                }
                else {
                    setMoveFlag(false);
                }
                accCnt = 0;
                moveCnt = 0;
                stopCnt = 0;
            }
            else {
                float filteredX = 0.0f;
                float filteredY = 0.0f;
                float filteredZ = 0.0f;

                filteredX = values[0];
                filteredY = values[1];
                filteredZ = values[2];

                double accVal = Math.sqrt(filteredX*filteredX + filteredY*filteredY + filteredZ*filteredZ);

                if (accVal > 1.0) moveCnt++;
                else stopCnt++;
                accCnt++;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
