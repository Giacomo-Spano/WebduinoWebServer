package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import com.server.webduino.core.webduinosystem.scenario.actions.Condition;

import java.text.SimpleDateFormat;

public class ConditionDataLog extends DataLog {

    public int id;
    private String table = "conditiondatalog";

    @Override
    public String getSQLInsert(String event, Object object) {

        Condition condition = (Condition) object;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sql = "";

        sql = "INSERT INTO " + table + " (date,event,conditionid)" +
                " VALUES ("
                + "'" + df.format(Core.getDate()) + "',"
                + "'" + event + "',"
                + condition.id + ");";

        return sql;
    }

    @Override
    public int writelog(String event, Object object) {
        // salva id perchè è riutilizzato nella chiamata successiva
        id = super.writelog(event, object);
        return id;
    }
}
