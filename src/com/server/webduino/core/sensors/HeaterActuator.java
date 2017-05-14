package com.server.webduino.core.sensors;

import com.server.webduino.core.*;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
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

public class HeaterActuator extends Actuator implements SensorBase.SensorListener, Programs.ProgramsListener {

    public static final int local_sensor = 0;
    public static final String STATUS_IDLE = "idle";
    public static final String STATUS_AUTOPROGRAM = "program";
    //static final String STATUS_MANUALPROGRAM = "manualprogram";
    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_MANUALOFF = "manualoff";
    public static final String STATUS_DISABLED = "disabled";

    private static final Logger LOGGER = Logger.getLogger(HeaterActuator.class.getName());
    protected boolean releStatus;
    public double avTemperature;
    protected double temperature;
    protected int duration;
    protected int remaining;
    protected boolean localSensor;
    protected double targetTemperature;

    // valori letti dal sensore (ricevutri da updatefromJson
    // possono potenzialemte essere diversi da quelli salvati ActiveProgram
    public int activeProgramID;
    public int activeTimeRangeID;
    protected int activeSensorID; //  questo valore non è letto dal sensore ma rimane solo sul server
    private double remoteTemperature;

    //protected ActiveProgram activeProgram;
    protected double activeSensorTemperature;

    public HeaterActuator(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id,name,subaddress,shieldid, pin, enabled);
        type = "heatersensor";
        //startPrograms();
    }

    public void init() {
        startPrograms();
        sensorPrograms.addListener(this);
        sensorPrograms.setHeaterStatus(getStatus());
        sensorPrograms.checkProgram();
    }

    public boolean receiveEvent(String eventtype) {
       /* if (super.receiveEvent(eventtype) || eventtype == TemperatureEvents)
            return true;
        return false;*/
       return true;
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

    public int getActiveSensorID() { return getActiveSensorID(); }

    protected void setActiveSensorID(int activeSensorID) { //  questo valore non è letto dal sensore ma rimane solo sul server
        this.activeSensorID = activeSensorID;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

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
                hl.changedReleStatus(releStatus, oldReleStatus);
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

        super.updateFromJson(date,json);

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

            if (json.has("remotetemperature")) {
                oldRemoteTemperature = getRemoteTemperature();
                setRemoteTemperature(json.getDouble("remotetemperature"));
            }
            if (json.has("relestatus"))
                setReleStatus(json.getBoolean("relestatus"));
            if (json.has("status")) {
                String status = json.getString("status");
                setStatus(status);
                sensorPrograms.setHeaterStatus(status);
            }
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
            if (releStatus == true)
                Core.sendPushNotification(SendPushMessages.notification_relestatuschange, "titolo", "stato rele", "acceso",getId());
            else
                Core.sendPushNotification(SendPushMessages.notification_relestatuschange, "titolo", "rele", "spento",getId());
        }
        if (activeProgramID != oldProgramId || activeTimeRangeID != oldTimerangeId) {
            sensorPrograms.heaterProgramChange(this, activeProgramID, oldProgramId, activeTimeRangeID, oldTimerangeId);

            String description = "New program " + activeProgramID + " " + activeTimeRangeID + " " + " sensors " + activeSensorID;
            Core.sendPushNotification(SendPushMessages.notification_programchange, "Program", description, "0", getId());
        }
        if (!getStatus().equals(oldStatus)) {
            // notifica Programs che è cambiato lo stato ed invia una notifica alle app
            sensorPrograms.checkProgram();
            String description = "Status changed from " + oldStatus + " to " + getStatus();
            Core.sendPushNotification(SendPushMessages.notification_statuschange, "Status", description, "0", getId());
        }

        writeDataLog("update");
        LOGGER.info("updateFromJson HeaterActuator old=" + oldRemoteTemperature + "new " + getRemoteTemperature());
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("shieldid", shieldid);
            json.put("online", online);
            json.put("subaddress", subaddress);
            json.put("type", type);
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
            Program program = sensorPrograms.getProgramFromId(activeProgramID);
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


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void changeTemperature(int sensorId, double temperature) {
        // chiamata quando cambia la temperatura di un sensore qualsiasi

        LOGGER.info("changeTemperature sensorId=" + sensorId + ", temperature = " + temperature);

        double roundedTemperature;
        BigDecimal bd = new BigDecimal(temperature).setScale(1, RoundingMode.HALF_EVEN);
        roundedTemperature = bd.doubleValue();

        if (activeSensorID == sensorId) {
            activeSensorTemperature = roundedTemperature;
            sensorPrograms.setSensorTemperature(roundedTemperature);

            if (getStatus().equals(HeaterActuator.STATUS_MANUAL)) {
                sendCommand(HeaterActuatorCommand.Command_Send_Temperature, 0, 0.0, false, 0, 0, activeSensorID, activeSensorTemperature);
            } else {
                sensorPrograms.checkProgram();
            }
        }
    }

    @Override
    public void changeAvTemperature(int sensorId, double avTemperature) {
    }

    @Override
    public void changeOnlineStatus(boolean online) {
    }

    @Override
    public void programChanged(ActiveProgram newActiveProgram) {

        activeProgram = newActiveProgram;
        // qui se cambia il programma bisoigna caricare la temperatura corrente del nuovo sensore di temperatura
        // attivi e inviarlo ai program
        SensorBase sensor = Core.getSensorFromId(activeProgram.timeRange.sensorId);
        if (sensor != null && sensor instanceof TemperatureSensor) {
            TemperatureSensor temperatureSensor = (TemperatureSensor) sensor;
            sensorPrograms.setSensorTemperature(temperatureSensor.getAvTemperature());
        } else {
            return;
        }

        LOGGER.info("->Active program" + activeProgram.program.id + " " + activeProgram.program.name);
        if (getStatus().equals(HeaterActuator.STATUS_IDLE) || getStatus().equals(HeaterActuator.STATUS_AUTOPROGRAM)) {

            Date currentDate = Core.getDate();
            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String timeStr = timeFormat.format(currentDate);
            Time currentTime = Time.valueOf(timeStr);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            boolean commandSent = false;
            try {
                // calcola la durata del prossimo programma
                Date date1 = format.parse(currentTime.toString());
                Date date2 = format.parse(activeProgram.timeRange.endTime.toString());
                long difference = date2.getTime() - date1.getTime();
                long duration = (difference / 1000 + 59) / 60; // aggiungi 59 secondi per non fare andare l'actuator in idle

                if (activeProgram.timeRange.sensorId == 0) { // active sensors local

                    HeaterActuatorCommand cmd = new HeaterActuatorCommand();
                    cmd.command = HeaterActuatorCommand.Command_Program_ReleOn;
                    cmd.duration = duration;
                    cmd.targetTemperature = activeProgram.timeRange.temperature;
                    cmd.remoteSensor = false;
                    cmd.activeProgramID = activeProgram.program.id;
                    cmd.activeTimeRangeID = activeProgram.timeRange.ID;
                    cmd.activeSensorID = 0;//mActiveProgram.timeRange.sensorId;
                    cmd.activeSensorTemperature = 0;//currentTemperature;
                    commandSent = sendCommand(cmd);
                    if (!commandSent)
                        LOGGER.severe("sendCommand Program on failed: " + activeProgram.program.id + " " + activeProgram.program.name);

                } else { // active sensors remote

                    HeaterActuatorCommand cmd = new HeaterActuatorCommand();
                    //cmd.command = HeaterActuatorCommand.Command_Program_ReleOff;
                    cmd.duration = duration;
                    cmd.targetTemperature = activeProgram.timeRange.temperature;
                    cmd.remoteSensor = true;
                    cmd.activeProgramID = activeProgram.program.id;
                    cmd.activeTimeRangeID = activeProgram.timeRange.ID;
                    cmd.activeSensorID = activeProgram.timeRange.sensorId;
                    cmd.activeSensorTemperature = activeSensorTemperature;

                    if (activeSensorTemperature < activeProgram.timeRange.temperature) { // temperatura bassa

                        cmd.command = HeaterActuatorCommand.Command_Program_ReleOn;

                    } else { // temperatura alta

                        cmd.command = HeaterActuatorCommand.Command_Program_ReleOff;
                    }
                    commandSent = sendCommand(cmd);
                    if (!commandSent) {
                        LOGGER.severe("sendCommand " + cmd.command + " failed: " + activeProgram.program.id + " " + activeProgram.program.name);
                        if (getRemoteTemperature() != activeSensorTemperature) {
                            sendCommand(HeaterActuatorCommand.Command_Send_Temperature, 0, 0.0, false, 0, 0, activeProgram.timeRange.sensorId, activeSensorTemperature);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (getStatus().equals(HeaterActuator.STATUS_MANUAL)) {
            sendCommand(HeaterActuatorCommand.Command_Send_Temperature, 0, 0.0, false, 0, 0, activeProgram.timeRange.sensorId, activeSensorTemperature);
        }



    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open) {

    }
}
