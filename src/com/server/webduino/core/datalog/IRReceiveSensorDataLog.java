package com.server.webduino.core.datalog;

import com.server.webduino.core.sensors.IRReceiverSensor;
import com.server.webduino.core.sensors.IRSensor;

public class IRReceiveSensorDataLog extends DataLog {

    public boolean open = false;
    public String tableName = "irreceivedatalog";
    private int sensorid;

    public IRReceiveSensorDataLog(int sensorid) {
        super();
        this.sensorid = sensorid;
    }

    @Override
    public String getSQLInsert(String event, Object object) {

        IRReceiverSensor rfidSensor = (IRReceiverSensor) object;
        String sql;
        sql = "INSERT INTO " + tableName + " (id, sensorid, date, status) VALUES ("
                + rfidSensor.getId() + "," + rfidSensor.getId() + ","  + getStrDate() + ",\"" + rfidSensor.getStatus().status + "\");";
        return sql;
    }

}
