package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

public class HeaterActuator extends Actuator /*implements TemperatureSensor.TemperatureSensorListener*/ {
    static final protected int local_sensor = 0;
    static final String STATUS_IDLE = "idle";
    static final String STATUS_AUTOPROGRAM = "program";
    //static final String STATUS_MANUALPROGRAM = "manualprogram";
    static final String STATUS_MANUAL = "manual";
    static final String STATUS_MANUALOFF = "manualoff";
    static final String STATUS_DISABLED = "disabled";
    private static final Logger LOGGER = Logger.getLogger(HeaterActuator.class.getName());
    protected boolean releStatus;
    protected double avTemperature;
    protected double temperature;
    protected int duration;
    protected int remaining;
    protected boolean localSensor;
    protected double targetTemperature;
    protected int activeProgramID;
    protected int activeTimeRangeID;
    protected int activeSensorID; //  questo valore non è letto dal sensore ma rimane solo sul server
    private double remoteTemperature;

    //protected String activeSensorName; //  questo valore non è letto dal sensore ma rimane solo sul server

    public HeaterActuator() {
        super();
        type = "heater";
        statusUpdatePath = "/heaterstatus";
    }

    /*public HeaterActuator(int id, int shieldid, String subaddress, String name) {
        super(id, shieldid, subaddress, name);

        //listeners = new ArrayList<HeaterActuatorListener>();
    }*/

    @Override
    public void addListener(ActuatorListener toAdd) {
        listeners.add((HeaterActuatorListener) toAdd);
    }

    public int getDuration() {
        return duration;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
    }

    public long getRemaining() {

        /*if (getStatus().equals(Actuator.STATUS_MANUALMODE) && lastUpdate != null) {

            Date currentDate = Core.getDate();
            long diff = currentDate.getTime() - lastUpdate.getTime();//as given
            long secondDiff = TimeUnit.MILLISECONDS.toSeconds(diff);
            //long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return remaining - secondDiff;
        } else {
            return 0;
        }*/
        return remaining;
    }

    protected void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public boolean isLocalSensor() {
        return localSensor;
    }

    protected void setLocalSensor(boolean localSensor) {
        this.localSensor = localSensor;
    }

    public int getActiveSensorID() {
        return activeSensorID;
    }

    protected void setActiveSensorID(int activeSensorID) { //  questo valore non è letto dal sensore ma rimane solo sul server
        this.activeSensorID = activeSensorID;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    /*public String getActiveSensorName() {
        return activeSensorName;
    }
    protected void setActiveSensorID(String activeSensorName) { //  questo valore non è letto dal sensore ma rimane solo sul server
        this.activeSensorName = activeSensorName;
    }*/

    protected void setTargetTemperature(double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public int getActiveProgramID() {
        return activeProgramID;
    }

    protected void setActiveProgramID(int activeProgramID) {
        this.activeProgramID = activeProgramID;
    }

    public double getRemoteTemperature() {
        return remoteTemperature;
    }

    protected void setRemoteTemperature(double remoteTemperature) {
        LOGGER.info("SetRemoteTemperature old:" + this.remoteTemperature + " new " + remoteTemperature);
        this.remoteTemperature = remoteTemperature;
    }

    public int getActiveTimeRangeID() {
        return activeTimeRangeID;
    }

    protected void setActiveTimeRangeID(int activeTimeRangeID) {
        this.activeTimeRangeID = activeTimeRangeID;
    }

    public boolean getReleStatus() {
        return releStatus;
    }

    protected void setReleStatus(boolean releStatus) {

        //boolean oldReleStatus = this.releStatus;
        this.releStatus = releStatus;

        /*if (releStatus != oldReleStatus) {
            // Notify everybody that may be interested.
            for (ActuatorListener hl : listeners)
                hl.changeReleStatus(releStatus, oldReleStatus);
        }*/
    }

    @Override
    public void writeDataLog(String event) {
        HeaterDataLog dl = new HeaterDataLog();
        dl.writelog(event, this);
    }

    public double getAvTemperature() {
        return avTemperature;
    }

    protected void setAvTemperature(double temperature) {
        this.avTemperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    protected void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Boolean sendCommand(String command, long duration, double targetTemperature, boolean localSensor, int activeProgramID, int activeTimeRangeID, int activeSensorID, double activeSensorTemperature) {

        HeaterActuatorCommand cmd = new HeaterActuatorCommand();
        cmd.command = command;
        cmd.duration = duration;
        cmd.targetTemperature = targetTemperature;
        cmd.remoteSensor = !localSensor;
        cmd.activeProgramID = activeProgramID;
        cmd.activeTimeRangeID = activeTimeRangeID;
        cmd.activeSensorID = activeSensorID;
        cmd.activeSensorTemperature = remoteTemperature;
        return sendCommand(cmd);
    }

    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        HeaterActuatorCommand command = new HeaterActuatorCommand();
        if (command.fromJson(json))
            return command;
        else
            return null;
    }

    @Override
    public Boolean sendCommand(ActuatorCommand cmd) {
        // sendcommand è usata anche da actuatorservlet per mandare i command dalle app
        HeaterActuatorCommand heaterActuatorCommand = (HeaterActuatorCommand) cmd;

        LOGGER.info("sendCommand " +
                "\ncommand = " + heaterActuatorCommand.command +
                "\nduration = " + heaterActuatorCommand.duration +
                "\ntargetTemperature = " + heaterActuatorCommand.targetTemperature +
                "\nremoteSensor = " + heaterActuatorCommand.remoteSensor +
                "\nactiveProgramID = " + heaterActuatorCommand.activeProgramID +
                "\nactiveTimeRangeID = " + heaterActuatorCommand.activeTimeRangeID +
                "\nactiveSensorID = " + heaterActuatorCommand.activeSensorID +
                "\nactiveSensorTemperature = " + heaterActuatorCommand.activeSensorTemperature);

        if (isUpdated()) {// controlla se l'actuator è in stato updated (cioè non è offline)

            // se non sono cambiati i parametri del programma già attivo non inviare
            if (noProgramDataChanges(heaterActuatorCommand) &&
                    (heaterActuatorCommand.command == HeaterActuatorCommand.Command_Program_ReleOn ||
                            heaterActuatorCommand.command == HeaterActuatorCommand.Command_Program_ReleOff)) {
                LOGGER.info("skip send command " + heaterActuatorCommand.command + "no program changes");
                //writeDataLog("skip send command " + heaterActuatorCommand.command + "no changes" );
                return false;
            }
        }
        setActiveSensorID(heaterActuatorCommand.activeSensorID);
        writeDataLog("Sending command" + heaterActuatorCommand.command);

        String postParam = "";
        String path = "";
        String strEvent = "event";

        LOGGER.info("sendCommand command=" + heaterActuatorCommand.command + ",duration=" + heaterActuatorCommand.duration + ",targetTemperature=" + heaterActuatorCommand.targetTemperature + ",remoteSensor=" + heaterActuatorCommand.remoteSensor +
                ",activeProgramID=" + heaterActuatorCommand.activeProgramID + ",activeTimeRangeID=" + heaterActuatorCommand.activeTimeRangeID + ",activeSensorID=" + heaterActuatorCommand.activeSensorID + "activeSensorTemperature=" + heaterActuatorCommand.activeSensorTemperature);

        return heaterActuatorCommand.send(this);

/*
        if (heaterActuatorCommand.command.equals(HeaterActuatorCommand.Command_Program_ReleOn)) {
            strEvent = "Command_Program_ReleOn";
            path = "/rele";
            postParam = "status=" + HeaterActuatorCommand.Command_Program_ReleOn;
            postParam += "&duration=" + heaterActuatorCommand.duration;
            postParam += "&target=" + heaterActuatorCommand.targetTemperature;
            if (!heaterActuatorCommand.remoteSensor) {
                postParam += "&localsensor=1";
            } else {
                postParam += "&localsensor=0";
            }
            postParam += "&sensor=" + heaterActuatorCommand.activeSensorID;
            postParam += "&program=" + heaterActuatorCommand.activeProgramID;
            postParam += "&timerange=" + heaterActuatorCommand.activeTimeRangeID;
            postParam += "&temperature=" + heaterActuatorCommand.activeSensorTemperature;
            postParam += "&json=1";

        } else if (heaterActuatorCommand.command.equals(HeaterActuatorCommand.Command_Program_ReleOff)) {
            strEvent = "Command_Program_ReleOff";
            path = "/rele";
            postParam = "status=" + HeaterActuatorCommand.Command_Program_ReleOff;
            postParam += "&duration=" + heaterActuatorCommand.duration;
            postParam += "&target=" + heaterActuatorCommand.targetTemperature;
            if (!heaterActuatorCommand.remoteSensor) {
                postParam += "&localsensor=1";
            } else {
                postParam += "&localsensor=0";
            }
            postParam += "&sensor=" + heaterActuatorCommand.activeSensorID;
            postParam += "&program=" + heaterActuatorCommand.activeProgramID;
            postParam += "&timerange=" + heaterActuatorCommand.activeTimeRangeID;
            postParam += "&temperature=" + heaterActuatorCommand.activeSensorTemperature;
            postParam += "&json=1";

        } else if (heaterActuatorCommand.command.equals(HeaterActuatorCommand.Command_Manual_Auto)) {
            strEvent = "Command_Manual_Auto";
            path = "/rele";
            postParam = "status=" + HeaterActuatorCommand.Command_Manual_Auto;
            postParam += "&duration=" + heaterActuatorCommand.duration;
            if (!heaterActuatorCommand.remoteSensor) {
                postParam += "&localsensor=1";
            } else {
                postParam += "&localsensor=0";
            }
            postParam += "&sensor=" + heaterActuatorCommand.activeSensorID;
            postParam += "&manual=1";
            postParam += "&temperature=" + heaterActuatorCommand.activeSensorTemperature;
            postParam += "&json=1";

        } else if (heaterActuatorCommand.command.equals(HeaterActuatorCommand.Command_Manual_Off)) {
            strEvent = "Command_Manual_Off";
            path = "/rele";
            postParam = "status=" + HeaterActuatorCommand.Command_Manual_Off;
            postParam += "&duration=" + heaterActuatorCommand.duration;
            postParam += "&manual=2";
            postParam += "&json=1";

        } else if (heaterActuatorCommand.command.equals(HeaterActuatorCommand.Command_Manual_End)) {
            strEvent = "Command_Manual_End";
            path = "/rele";
            postParam = "status=" + HeaterActuatorCommand.Command_Manual_End;
            postParam += "&manual=3";
            postParam += "&temperature=" + heaterActuatorCommand.activeSensorTemperature;
            postParam += "&json=1";

        } else if (heaterActuatorCommand.command.equals(HeaterActuatorCommand.Command_Send_Temperature)) {
            strEvent = "Command_Send_Temperature";
            path = "/temp";
            postParam = "temperature=" + heaterActuatorCommand.activeSensorTemperature;
            if (localSensor == true)
                postParam += "&localsensor=0";
            else
                postParam += "&localsensor=1";
            postParam += "&sensor=" + heaterActuatorCommand.activeSensorID;
        }

        boolean res = postCommand(postParam, path);

        if (res) {
            //writeDataLog(strEvent + " sent");
            LOGGER.info("Command=" + heaterActuatorCommand.command + " sent");
        } else {
            //writeDataLog(strEvent + " FAILED");
            LOGGER.info("Command=" + heaterActuatorCommand.command + " failed");
        }
        return res;
        */
    }

    public boolean noProgramDataChanges(HeaterActuatorCommand heaterActuatorCommand) {
        return heaterActuatorCommand.duration * 60 == duration &&
                heaterActuatorCommand.targetTemperature == targetTemperature &&
                heaterActuatorCommand.remoteSensor == !isLocalSensor() &&
                heaterActuatorCommand.activeProgramID == activeProgramID &&
                heaterActuatorCommand.activeTimeRangeID == activeTimeRangeID &&
                heaterActuatorCommand.activeSensorID == activeSensorID &&
                (heaterActuatorCommand.remoteSensor && heaterActuatorCommand.activeSensorTemperature == remoteTemperature);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        boolean oldReleStatus = this.releStatus;
        int oldProgramId = activeProgramID;
        int oldTimerangeId = activeTimeRangeID;
        int oldsensorId = activeSensorID;
        double oldTargetId = targetTemperature;
        String oldStatus = getStatus();
        double oldRemoteTemperature = -1;

        lastUpdate = date;
        online = true;
        try {
            LOGGER.info("received jsonResultSring=" + json.toString());

            /*if (json.has("temperature"))
                setTemperature(json.getDouble("temperature"));
            if (json.has("avtemperature"))
                setAvTemperature(json.getDouble("avtemperature"));*/
            if (json.has("remotetemperature")) {
                oldRemoteTemperature = getRemoteTemperature();
                setRemoteTemperature(json.getDouble("remotetemperature"));
            }
            if (json.has("relestatus"))
                setReleStatus(json.getBoolean("relestatus"));
            if (json.has("status"))
                setStatus(json.getString("status"));
            if (json.has("name"))
                setName(json.getString("name"));
            if (json.has("sensorid"))
                setId(json.getInt("sensorid"));
            if (json.has("duration"))
                setDuration(duration = json.getInt("duration"));
            if (json.has("remaining"))
                setRemaining(remaining = json.getInt("remaining"));
            if (json.has("localsensor"))
                setLocalSensor(json.getBoolean("localsensor"));
            if (json.has("target"))
                setTargetTemperature(targetTemperature = json.getDouble("target"));
            if (json.has("program"))
                setActiveProgramID(activeProgramID = json.getInt("program"));
            if (json.has("timerange"))
                setActiveTimeRangeID(activeTimeRangeID = json.getInt("timerange"));
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("error");
        }

        if (releStatus != oldReleStatus) {
            // Notify everybody that may be interested.
            for (ActuatorListener hl : listeners) {
                ((HeaterActuatorListener) hl).changeReleStatus(releStatus, oldReleStatus);
            }
        }
        if (activeProgramID != oldProgramId || activeTimeRangeID != oldTimerangeId) {
            // Notify everybody that may be interested.
            for (ActuatorListener hl : listeners)
                ((HeaterActuatorListener) hl).changeProgram(this, activeProgramID, oldProgramId, activeTimeRangeID, oldTimerangeId);
        }
        if (!getStatus().equals(oldStatus)) {
            // Notify everybody that may be interested.
            for (ActuatorListener hl : listeners) {
                // notifica Programs che è cambiato lo stato ed invia una notifica alle app
                ((HeaterActuatorListener) hl).changeStatus(getStatus(), oldStatus);
            }
        }

        writeDataLog("update");
        LOGGER.info("updateFromJson HeaterActuator old=" + oldRemoteTemperature + "new " + getRemoteTemperature());

    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("temperature", temperature);
            json.put("avtemperature", avTemperature);
            json.put("remotetemperature", remoteTemperature);
            json.put("name", name);
            json.put("status", getStatus());
            json.put("duration", duration);
            json.put("remaining", getRemaining());
            json.put("relestatus", getReleStatus());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (lastUpdate != null)
                json.put("lastupdate", df.format(lastUpdate));
            json.put("localsensor", localSensor);

            json.put("target", targetTemperature);


            json.put("program", activeProgramID);
            Program program = Core.getProgramFromId(activeProgramID);
            if (program != null) {
                json.put("programname", program.name);
                json.put("timerange", activeTimeRangeID);

                TimeRange timeRange = program.getTimeRangeFromId(activeTimeRangeID);
                if (timeRange != null)
                    json.put("timerangename", timeRange.name);
            }

            json.put("sensorID", activeSensorID);
            SensorBase sensor = Core.getSensorFromId(activeSensorID);
            if (sensor != null)
                json.put("sensorIDname", sensor.name);

            Date currentDate = Core.getDate();

            Locale.setDefault(Locale.ITALIAN);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy");
            String strDate = sdf.format(currentDate);
            json.put("fulldate", strDate);

            sdf = new SimpleDateFormat("dd-MM-yyyy");
            strDate = sdf.format(currentDate);
            json.put("date", strDate);

            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
            strDate = sdf.format(currentDate);
            json.put("UTCdate", strDate);

            sdf = new SimpleDateFormat("hh:mm:ss");
            String strTime = sdf.format(currentDate);
            json.put("time", strTime);

            json.put("shieldid", shieldid);
            json.put("online", online);

            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    interface HeaterActuatorListener extends ActuatorListener {
        void changeStatus(String newStatus, String oldStatus);

        void changeReleStatus(boolean newReleStatus, boolean oldReleStatus);

        void changeProgram(HeaterActuator heater, int newProgram, int oldProgram, int newTimerange, int oldTimerange);


    }

}
