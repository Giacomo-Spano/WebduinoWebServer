package com.server.webduino.core;

import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.DBObject;
import com.server.webduino.core.webduinosystem.scenario.ScenarioProgramTimeRange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
//import java.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class IRCodeSequence {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioTimeIntervalQuartzJob.class.getName());


    public int ircommandidid;

    //public int index;
    public List<IRCode> sequence = new ArrayList<>();


    public IRCodeSequence(int ircommandidid) throws Exception {
        this.ircommandidid = ircommandidid;
        readIRCodeSequence(ircommandidid);
    }

    private void readIRCodeSequence(int ircommandid) throws Exception {

        this.ircommandidid = ircommandid;
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM ircodesequences WHERE ircommandid=" + ircommandid + " ORDER BY ircodesequences.index ASC;";
        ResultSet resultSet = stmt.executeQuery(sql);
        while (resultSet.next()) {
            int ircodeid = resultSet.getInt("ircodeid");
            try {
                IRCode irCode = new IRCode(ircodeid);
                sequence.add(irCode);
            } catch (Exception e) {
                ;
            }
        }
        resultSet.close();
        stmt.close();
        conn.close();
    }
}
