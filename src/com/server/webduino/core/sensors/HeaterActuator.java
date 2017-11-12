package com.server.webduino.core.sensors;

import com.server.webduino.core.*;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class HeaterActuator extends Actuator implements SensorBase.SensorListener, Schedule.ProgramsListener, Zone.WebduinoZoneListener {

    public static final String STATUS_IDLE = "idle";
    public static final String STATUS_AUTOPROGRAM = "program";
    //static final String STATUS_MANUALPROGRAM = "manualprogram";
    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_MANUALOFF = "manualoff";
    public static final String STATUS_DISABLED = "disabled";

    private static final Logger LOGGER = Logger.getLogger(HeaterActuator.class.getName());

    protected boolean releStatus;
    protected double temperature;
    protected int duration;
    protected int remaining;
    protected double targetTemperature;
    // valori letti dal sensore (ricevutri da updatefromJson
    // possono potenzialemte essere diversi da quelli salvati ActiveProgram
    public int scenario;
    public int timeInterval;
    protected int zone; //  questo valore non è letto dal sensore ma rimane solo sul server

    public interface TemperatureSensorListener extends SensorBase.SensorListener {
        public String TemperatureEvents = "temperature event";
        void onUpdateTemperature(int sensorId, double temperature, double oldtemperature);
        void changeAvTemperature(int sensorId, double avTemperature);
    }

    public HeaterActuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "heatersensor";
    }

    public void init() {
        startPrograms();
    }


    public boolean receiveEvent(String eventtype) {
        return true;
    }

    public int getDuration() {
        return duration;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
    }

    public long getRemaining() {

        return remaining;
    }

    protected void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public int getScenarioId() {
        return scenario;
    }

    protected void setScenarioId(int scenaarioId) { //  questo valore non è letto dal sensore ma rimane solo sul server
        this.scenario = scenaarioId;
    }

    public int getTimeIntervalId() {
        return timeInterval;
    }

    protected void setTimeIntervalId(int timeIntervalId) { //  questo valore non è letto dal sensore ma rimane solo sul server
        this.timeInterval = timeIntervalId;
    }

    public int getZoneId() {
        return zone;
    }

    protected void setZoneId(int zoneId) {

        if (this.getZoneId() != zoneId) {
            Zone zone = Core.getZoneFromId(getZoneId());
            if (zone != null) {
                zone.removeListener(this);
                this.zone = zoneId;
                zone = Core.getZoneFromId(getZoneId());
                if (zone != null) {
                    zone.addListener(this);
                }
            }
        }
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    protected void setTargetTemperature(double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public boolean getReleStatus() {
        return releStatus;
    }

    protected void setReleStatus(boolean releStatus) {

        this.releStatus = releStatus;
    }

    @Override
    public void writeDataLog(String event) {
        HeaterDataLog dl = new HeaterDataLog();
        dl.writelog(event, this);
    }

    public double getTemperature() {
        return temperature;
    }

    protected void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Boolean sendCommand(String command, long duration, double targetTemperature, int scenario, int timeInterval, int zone, double temperature) {

        return false;
    }

    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        return null;
    }

    @Override
    public Boolean sendCommand(ActuatorCommand cmd) {

        return true;
    }


    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date, json);

        boolean oldReleStatus = this.releStatus;
        int oldScenario = scenario;
        int oldTimeInterval = timeInterval;
        int oldZoneId = zone;
        double oldTargetId = targetTemperature;
        String oldStatus = getStatus();

        lastUpdate = date;
        online = true;
        try {
            LOGGER.info("received jsonResultSring=" + json.toString());

            if (json.has("remotetemperature")) {
                setTemperature(json.getDouble("remotetemperature"));
            }
            if (json.has("zoneid"))
                setZoneId(json.getInt("zoneid"));
            if (json.has("relestatus"))
                setReleStatus(json.getBoolean("relestatus"));
            if (json.has("status")) { // attenzione. Lostatus deve essere impostato do zoneid altrimenti in caso di manual non funziona
                String status = json.getString("status");
                setStatus(status);
            }
            if (json.has("duration"))
                setDuration(duration = json.getInt("duration"));
            if (json.has("remaining"))
                setRemaining(remaining = json.getInt("remaining"));
            if (json.has("target"))
                setTargetTemperature(targetTemperature = json.getDouble("target"));
            if (json.has("scenario"))
                setScenarioId(json.getInt("scenario"));
            if (json.has("timerange"))
                setTimeIntervalId(json.getInt("timerange"));
        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("error");
        }

        if (releStatus != oldReleStatus) {
            if (releStatus == true)
                Core.sendPushNotification(SendPushMessages.notification_relestatuschange, "titolo", "stato rele", "acceso", getId());
            else
                Core.sendPushNotification(SendPushMessages.notification_relestatuschange, "titolo", "rele", "spento", getId());
        }
        if (scenario != oldScenario || timeInterval != oldTimeInterval) {
            //sensorSchedule.heaterProgramChange(this, activeProgramID, oldProgramId, activeTimeRangeID, oldTimerangeId);

            String description = "New scenario " + scenario + "." + timeInterval + "." + " zone " + zone;
            Core.sendPushNotification(SendPushMessages.notification_programchange, "Program", description, "0", getId());
        }
        if (!getStatus().equals(oldStatus)) {
            // notifica Schedule che è cambiato lo stato ed invia una notifica alle app
            //sensorSchedule.checkProgram();
            String description = "Status changed from " + oldStatus + " to " + getStatus();
            Core.sendPushNotification(SendPushMessages.notification_statuschange, "Status", description, "0", getId());
        }

        writeDataLog("update");
        //LOGGER.info("updateFromJson HeaterActuator old=" + oldRemoteTemperature + "new " + getRemoteTemperature());
    }

    @Override
    public void getJSONField(JSONObject json) {
        try {
            json.put("temperature", temperature);
            json.put("status", getStatus());
            json.put("duration", duration);
            json.put("remaining", getRemaining());
            json.put("relestatus", getReleStatus());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (lastUpdate != null)
                json.put("lastupdate", df.format(lastUpdate));
            json.put("target", targetTemperature);
            json.put("scenario", getScenarioId());
            json.put("timeInterval", getTimeIntervalId());
            json.put("zone", zone);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changeOnlineStatus(boolean online) {
    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }

    @Override
    public void onChangeStatus(String newStatus, String oldStatus) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open, boolean oldOpen) {

    }

    @Override
    public void programChanged(ActiveProgram newProgram) {

    }

    @Override
    public void setStatus(String status) {
        //String oldStatus = status;
        super.setStatus(status);
        if (!oldStatus.equals(status)) {
            if (status.equals(STATUS_MANUAL)) {
                Zone zone = Core.getZoneFromId(getZoneId());
                if (zone != null) {
                    zone.addListener(this/*new Zone.WebduinoZoneListener() {
                        @Override
                        public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {
                            sendTemperature(newTemperature);
                        }

                        @Override
                        public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

                        }
                    }*/);
                }
            } else if (oldStatus.equals(STATUS_MANUAL)) {
                Zone zone = Core.getZoneFromId(getZoneId());
                if (zone != null) {
                    zone.removeListener(this);
                }
            }
        }
    }

    public String sendTemperature(double temperature) {
        JSONObject json = new JSONObject();
        try {
            json.put("actuatorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_SendTemperature);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            Core.postCommand(cmd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {
        sendTemperature(newTemperature);
    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

    }
}
