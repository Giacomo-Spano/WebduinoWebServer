package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.scenario.ScenarioProgramTimeRange;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * Created by gs163400 on 30/12/2017.
 */
public class NextScenario {

    Scenario scenario;

    List<NextProgram> nextPrograms;

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
/*        try {
            json.put("starttime", start.toString());
            json.put("endtime", end.toString());
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            if (date != null)
                json.put("nextjobdate", df.format(date));

            json.put("timerange", timeRange.toJson());


        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        return json;
    }}
