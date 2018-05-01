package com.server.webduino.core.sensors;

/**
 * Created by giaco on 24/04/2018.
 */
public class SensorListenerClass implements SensorBase.SensorListener {
    @Override
    public void changeOnlineStatus(boolean online) {

    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }

    @Override
    public void onChangeStatus(String newStatus, String oldStatus) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open, boolean oldOpen) {

    }

    @Override
    public void changeValue(double value) {

    }
}