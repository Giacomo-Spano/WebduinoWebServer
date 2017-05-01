package com.server.webduino.core;

import com.server.webduino.core.sensors.*;

/**
 * Created by giaco on 21/04/2017.
 */
public class SensorFactory {

    static public SensorBase createSensor(String type, String name, String subaddress, int id, int shieldid, String pin, boolean enabled) {

        SensorBase sensor;
        if (type.equals("temperature")) {
            sensor = (SensorBase) new TemperatureSensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("onewiresensor")) {
            sensor = (SensorBase) new OnewireSensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("currentsensor")) {
            sensor = (SensorBase) new CurrentSensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("doorsensor")) {
            sensor = (SensorBase) new DoorSensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("heatersensor")) {
            sensor = (SensorBase) new HeaterActuator(id,name,subaddress,shieldid,pin,enabled);
            //sensor.startPrograms();
        } else if (type.equals("humiditysensor")) {
            sensor = (SensorBase) new HumiditySensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("pirsensor")) {
            sensor = (SensorBase) new PIRSensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("pressuresensor")) {
            sensor = (SensorBase) new PressureSensor(id,name,subaddress,shieldid,pin,enabled);
        } else if (type.equals("relesensor")) {
            sensor = (SensorBase) new ReleActuator(id,name,subaddress,shieldid,pin,enabled);
        } else {
            return null;
        }
        sensor.init();
        /*sensor.type = type;
        sensor.name = name;
        sensor.subaddress = subaddress;
        sensor.id = id;
        sensor.shieldid = shieldid;*/
        return sensor;

    }

}
