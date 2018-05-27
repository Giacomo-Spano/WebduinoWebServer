package com.server.webduino.core.sensors;

import com.server.webduino.core.webduinosystem.Status;

/**
 * Created by giaco on 24/04/2018.
 */
public class SensorListenerClass implements SensorBase.SensorListener {

    @Override
    public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus) {

    }

    @Override
    public void onChangeValue(SensorBase sensor, double value) {

    }
}