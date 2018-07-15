package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.IRSensor;
import com.server.webduino.core.sensors.RFIDSensor;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class IRSensorDataLog extends DataLog {

    public boolean open = false;
    public String tableName = "irdatalog";
    private int sensorid;

    public IRSensorDataLog(int sensorid) {
        super();
        this.sensorid = sensorid;
    }

    @Override
    public String getSQLInsert(String event, Object object) {

        IRSensor rfidSensor = (IRSensor) object;
        String sql;
        sql = "INSERT INTO " + tableName + " (id, sensorid, date, status) VALUES ("
                + rfidSensor.getId() + "," + rfidSensor.getId() + ","  + getStrDate() + ",\"" + rfidSensor.getStatus().status + "\");";
        return sql;
    }

}
