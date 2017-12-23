package com.server.webduino.core.datalog;

import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.datalog.DataLog;

public class CommandDataLog extends DataLog {

    public String tableName = "commanddatalog";

    @Override
    public String getSQLInsert(String event, Command command) {

        String sql = "INSERT INTO " + tableName + " (date, shieldid, actuatorid, uuid) VALUES ("
                + "'" + getStrDate() + "'"
                + ",'" + command.command + "'"
                + "," + command.shieldid
                + "," + command.actuatorid

                + ");";
        return sql;
    }
}
