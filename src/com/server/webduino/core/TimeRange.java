package com.server.webduino.core;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramAction;

import java.sql.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class TimeRange {

    public int ID;
    public String name;
    //Time startDateTime;
    public Time endTime;
    public Double temperature;

    public ProgramAction programActions;
    public int sensorId = 0;
    public int programID;
    public int priority;

    public TimeRange() {
    }

    public TimeRange(int ID, String name, Time endTime, Double temperature, int shieldId, int programID) {
        this.ID = ID;
        this.name = name;
        //this.startDateTime = startDateTime;
        this.endTime = endTime;
        this.temperature = temperature;
        this.sensorId = shieldId;
        this.programID = programID;
    }

    public TimeRange(TimeRange tr) {
        this.ID = tr.ID;
        this.name = tr.name;
        this.endTime = tr.endTime;
        this.temperature = tr.temperature;
        this.sensorId = tr.sensorId;
        this.programID = tr.programID;
    }
}
