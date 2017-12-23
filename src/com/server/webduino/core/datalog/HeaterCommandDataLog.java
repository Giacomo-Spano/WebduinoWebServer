package com.server.webduino.core.datalog;

import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;

public class HeaterCommandDataLog extends DataLog {


    public String tableName = "heatercommanddatalog";

    @Override
    public String getSQLInsert(String event, Command command) {

        /*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = "", strEndDate = "";*/

        HeaterActuatorCommand heaterCommand = (HeaterActuatorCommand) command;

        /*if (heaterCommand.date != null)
            strDate = "'" + df.format(heaterCommand.date) + "'";
        if (heaterCommand.enddate != null)
            strEndDate = "'" + df.format(heaterCommand.enddate) + "'";*/

        String sql;
        sql = "INSERT INTO " + tableName + " (date, command, shieldid, actuatorid, uuid, duration, target, scenario, zone, temperature, actionid, enddate, success, result) VALUES ("
                + "'" + heaterCommand.date + "'"
                + ",'" + heaterCommand.command + "'"
                + "," + command.shieldid
                + "," + heaterCommand.actuatorid
                + ",'" + heaterCommand.uuid + "'"
                + "," + heaterCommand.duration
                + "," + heaterCommand.targetTemperature
                + "," + heaterCommand.scenario
                + "," + heaterCommand.zone
                + "," + heaterCommand.temperature
                + "," + heaterCommand.actionid
                + ",'" + heaterCommand.enddate + "'"
                + "," + heaterCommand.result.success
                + ",'" + heaterCommand.result.result + "'"
                + ");";
        return sql;
    }

}
