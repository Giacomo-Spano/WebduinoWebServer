package com.server.webduino.core;

import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.core.Core;
import com.server.webduino.core.IRCode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
//import java.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class IRDeviceAlias {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioTimeIntervalQuartzJob.class.getName());


    public int irdeviceid;

    public List<String> aliasList = new ArrayList<>();


    public IRDeviceAlias(int irdeviceid) throws Exception {
        this.irdeviceid = irdeviceid;
        readIRDeviceAliasList(irdeviceid);
    }

    private void readIRDeviceAliasList(int irdeviceid) throws Exception {

        this.irdeviceid = irdeviceid;
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM irdevicealias WHERE irdeviceid=" + irdeviceid + " ;";
        ResultSet resultSet = stmt.executeQuery(sql);
        while (resultSet.next()) {
            String alias = resultSet.getString("alias");
            try {
                aliasList.add(alias);
            } catch (Exception e) {
                ;
            }
        }
        resultSet.close();
        stmt.close();
        conn.close();
    }
}
