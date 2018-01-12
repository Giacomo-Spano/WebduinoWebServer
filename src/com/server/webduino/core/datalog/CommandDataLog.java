package com.server.webduino.core.datalog;

import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.datalog.DataLog;

public class CommandDataLog extends DataLog {

    public String tableName = "commanddatalog";

    @Override
    public String getSQLInsert(String event, Object object) {
    //public String getSQLInsert(String event, Command command) {
        Command command = (Command) object;
        String sql = "INSERT INTO " + tableName + " (date, shieldid, actuatorid, uuid) VALUES ("
                + "'" + getStrDate() + "'"
                + ",'" + command.command + "'"
                + "," + command.shieldid
                + "," + command.actuatorid

                + ");";
        return sql;
    }
}
