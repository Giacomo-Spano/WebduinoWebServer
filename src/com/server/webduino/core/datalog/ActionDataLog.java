package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.Program;
import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;

import java.text.SimpleDateFormat;

public class ActionDataLog extends DataLog {

    public int id;

    @Override
    public String getSQLInsert(String event, Object object) {

        ProgramAction action = (ProgramAction) object;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sql = "";
        if (event.equals("start")) {
            sql = "INSERT INTO actiondatalog (id,start,actionid)" +
                    " VALUES ("
                    + id + ","
                    + "'" + df.format(Core.getDate()) + "',"
                    + action.id + ");";
        } else {
            sql = "UPDATE actiondatalog set "
                    + "end='" + df.format(Core.getDate()) + "' "
                    + " WHERE id=" + id
                    + ";";
        }
        return sql;
    }

    @Override
    public int writelog(String event, Object object) {
        // salva id perchè è riutilizzato nella chiamata successiva
        id = super.writelog(event,object);
        return id;
    }
}
