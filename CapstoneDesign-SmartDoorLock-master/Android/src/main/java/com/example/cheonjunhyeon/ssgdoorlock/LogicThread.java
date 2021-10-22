package com.example.cheonjunhyeon.ssgdoorlock;

public class LogicThread extends Thread {

    private AccelerometerThread accThr = null;
    private GeomagnetismThread magThr = null;
    private GetRssiThread rssiThr = null;
    private SatelliteCountThread satThr = null;

    public LogicThread(AccelerometerThread accThr, GeomagnetismThread magThr, GetRssiThread rssiThr,
                       SatelliteCountThread satThr) {
        this.accThr = accThr;
        this.magThr = magThr;
        this.rssiThr = rssiThr;
        this.satThr = satThr;
    }

    public void run() {

    }
}
