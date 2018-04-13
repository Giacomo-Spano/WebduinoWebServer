package com.server.webduino.core.sensors;

import com.server.webduino.core.*;
import com.server.webduino.core.datalog.HeaterCommandDataLog;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.scenario.Scenario;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class HeaterActuator extends Actuator implements SensorBase.SensorListener, Schedule.ProgramsListener, Zone.WebduinoZoneListener {

    public static final String STATUS_IDLE = "idle";
    public static final String STATUS_AUTOPROGRAM = "program";
    //static final String STATUS_MANUALPROGRAM = "manualprogram";
    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_KEEPTEMPERATURE = "keeptemperature";
    public static final String STATUS_MANUALOFF = "manualoff";
    public static final String STATUS_DISABLED = "disabled";

    private static final Logger LOGGER = Logger.getLogger(HeaterActuator.class.getName());

    protected boolean releStatus;
    protected double temperature;
    protected int duration;
    protected int remaining;
    protected double targetTemperature;
    protected Date lastTemperatureUpdate;
    protected Date lastCommandDate;
    protected Date endDate;
    // valori letti dal sensore (ricevutri da updatefromJson
    // possono potenzialemte essere diversi da quelli salvati ActiveProgram
    public int actionId;
    public int timeRange;
    protected int zoneId; //  questo valore non è letto dal sensore ma rimane solo sul server

    public interface TemperatureSensorListener extends SensorBase.SensorListener {
        public String TemperatureEvents = "temperature event";

        void onUpdateTemperature(int sensorId, double temperature, double oldtemperature);

        void changeAvTemperature(int sensorId, double avTemperature);
    }

    public HeaterActuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "heatersensor";

        ActionCommand cmd = new ActionCommand("keeptemperature","Temperatura target");
        cmd.addTarget("Temperatura",0,30);
        cmd.addZone("Zona", TemperatureSensor.temperaturesensortype);
        actionCommandList.add(cmd);

        command = new HeaterActuatorCommand(shieldid,id);
        datalog = new HeaterDataLog(id);
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

    public int getActionId() {
        return actionId;
    }

    protected void setActionId(int actionId) { //  questo valore non è letto dal sensore ma rimane solo sul server

        this.actionId = actionId;
        //Scenario scenario = Scenario.scenarioFromProgramTimeRange()
    }

    public int getTimeRangeId() {
        return timeRange;
    }

    /*protected void setTimeRangeId(int timeRangeId) { //  questo valore non è letto dal sensore ma rimane solo sul server
        this.timeRange = timeRangeId;
    }*/

    public int getZoneId() {
        return zoneId;
    }

    protected void setZoneId(int zoneId) {

        if (zoneId == this.zoneId)
            return;

        Zone zone = Core.getZoneFromId(getZoneId());
        if (zone != null) {
            zone.removeListener(this);
        }

        this.zoneId = zoneId;

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
        //HeaterDataLog dl = new HeaterDataLog();
        datalog.writelog(event, this);
    }

    public double getTemperature() {
        return temperature;
    }

    protected void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    protected void setLastTemperatureUpdate(Date date) {
        this.lastTemperatureUpdate = date;
    }

    protected void setLastCommandUpdate(Date date) {
        this.lastCommandDate = date;
    }

    protected void setEndDate(Date date) {
        this.endDate = date;
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
        //int oldScenario = scenario;
        //int oldTimeInterval = timeInterval;
        String oldStatus = getStatus();


        try {
            LOGGER.info("received jsonResultSring=" + json.toString());

            if (json.has("remotetemp")) {
                setTemperature(json.getDouble("remotetemp"));
            }
            if (json.has("lasttemp") && !json.getString("lasttemp").equals("")) {
                String str = json.getString("lasttemp");
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                try {
                    Date lasttemperatureupdate = df.parse(str);
                    setLastTemperatureUpdate(lasttemperatureupdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            if (json.has("lastcmnd") && !json.getString("lastcmnd").equals("")) {
                String str = json.getString("lastcmnd");
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                try {
                    Date lastcommanddate = df.parse(str);
                    setLastCommandUpdate(lastcommanddate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

            if (json.has("enddate") && !json.getString("enddate").equals("")) {
                String str = json.getString("enddate");
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                try {
                    Date enddate = df.parse(str);
                    setEndDate(enddate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

            if (json.has("zoneid"))
                setZoneId(json.getInt("zoneid"));
            if (json.has("relestatus"))
                setReleStatus(json.getBoolean("relestatus"));
            /*if (json.has("status")) { // attenzione. Lostatus deve essere impostato do zoneid altrimenti in caso di manual non funziona
                String status = json.getString("status");
                setStatus(status);
            }*/
            if (json.has("duration"))
                setDuration(json.getInt("duration"));
            if (json.has("remaining"))
                setRemaining(json.getInt("remaining"));
            if (json.has("target"))
                setTargetTemperature(json.getDouble("target"));
            if (json.has("actionid"))
                setActionId(json.getInt("actionid"));
            /*if (json.has("timerange"))
                setTimeIntervalId(json.getInt("timerange"));*/
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
        /*if (scenario != oldScenario || timeInterval != oldTimeInterval) {
            //sensorSchedule.heaterProgramChange(this, activeProgramID, oldProgramId, activeTimeRangeID, oldTimerangeId);

            String description = "New scenario " + scenario + "." + timeInterval + "." + " zone " + zoneId;
            Core.sendPushNotification(SendPushMessages.notification_programchange, "Program", description, "0", getId());
        }*/
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
            //json.put("status", getStatus());

            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY,duration/3600);
            cal.set(Calendar.MINUTE,duration%3600/60);
            cal.set(Calendar.SECOND,duration%60);
            String str = timeFormat.format(cal.getTime());
            json.put("duration", str);

            cal.set(Calendar.HOUR_OF_DAY,remaining/3600);
            cal.set(Calendar.MINUTE,remaining%3600/60);
            cal.set(Calendar.SECOND,remaining%60);
            str = timeFormat.format(cal.getTime());
            json.put("remaining", str);

            json.put("relestatus", getReleStatus());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (lastUpdate != null)
                json.put("lastupdate", df.format(lastUpdate));
            if (lastTemperatureUpdate != null)
                json.put("lasttemperatureupdate", df.format(lastTemperatureUpdate));
            if (lastCommandDate != null)
                json.put("lastcommanddate", df.format(lastCommandDate));
            if (endDate != null)
                json.put("enddate", df.format(endDate));
            json.put("target", targetTemperature);
            json.put("action", actionId);
            //json.put("scenario", getScenarioId());
            //json.put("program", getActiveProgram());
            //json.put("timerange", getTimeRangeId());
            json.put("zone", zoneId);

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
        if (!oldStatus.equals(status) && status.equals(STATUS_MANUAL)) {
            // se lo stato diventa manual si mettein ascolto sulla
            // zona della temperatura manuale e manda una richiesta di aggiornamento temperatura alla zona
            Zone zone = Core.getZoneFromId(getZoneId());
            if (zone != null) {
                zone.removeListener(this);
                zone.addListener(this);

                temperature = 0;
                zone.requestSensorStatusUpdate();

            }
        } else if (!status.equals(STATUS_MANUAL)) {
            Zone zone = Core.getZoneFromId(getZoneId());
            if (zone != null) {
                zone.removeListener(this);
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
            //Core.postCommand(cmd);
            boolean res = cmd.send();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onUpdateTemperature(int zoneId, double newTemperature, double oldTemperature) {
        if (newTemperature != temperature)
            sendTemperature(newTemperature);
        temperature = newTemperature;
    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

    }
}
