package com.server.webduino.core.datalog;

import com.server.webduino.core.sensors.commands.Command;

public class CommandDataLog extends DataLog {

    public String tableName = "commanddatalog";
    public CommandDataLog commandDataLog = null;
    public String result;
    public boolean success;

    public CommandDataLog(String commanddatalog) {
        this.tableName = commanddatalog;
    }
    @Override
    public String getSQLInsert(String event, Object object) {
    //public String getSQLInsert(String event, Command command) {
        Command command = (Command) object;
        String sql = "INSERT INTO " + tableName + " (date, command, shieldid, sensorid, uuid, success, result) VALUES ("
                + getStrDate()
                + ",'" + command.command + "'"
                + "," + command.shieldid
                + "," + command.actuatorid
                + ",'" + command.uuid + "'"
                + "," + success
                + ",'" + result + "'"
                + ");";
        return sql;
    }
}
