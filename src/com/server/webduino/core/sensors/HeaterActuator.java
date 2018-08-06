package com.server.webduino.core.sensors;

import com.server.webduino.core.*;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.Status;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class HeaterActuator extends Actuator /*implements /*SensorBase.SensorListener/*, Zone.WebduinoZoneListener*/ {

    //public static final String STATUS_IDLE = "idle";
    //public static final String STATUS_AUTOPROGRAM = "program";
    //static final String STATUS_MANUALPROGRAM = "manualprogram";
    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_KEEPTEMPERATURE = "keeptemperature";
    public static final String STATUS_OFF = "off";

    public static final String STATUS_DESCRIPTION_MANUAL = "Manuale";
    public static final String STATUS_DESCRIPTION_KEEPTEMPERATURE = "Mantieni temperature";
    public static final String STATUS_DESCRIPTION_OFF = "Off";

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
    private int remoteSensorId;

    private SensorListener remoteSensorListener = new SensorListener() {

        @Override
        public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus) {

        }

        @Override
        public void onChangeValue(SensorBase sensor, double value) {
            if (value != temperature)
                sendTemperature(value);
            temperature = value;
        }
    };

    public HeaterActuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "heatersensor";

        ActionCommand cmd = new ActionCommand("keeptemperature", "Mantieni Temperatura");
        cmd.addTarget("Temperatura", 0, 30);
        cmd.addZone("Zona", TemperatureSensor.temperaturesensortype);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    int seconds = 1800;
                    /*if (json.has("seconds"))
                        seconds = json.getInt("seconds");*/
                    double targetvalue = 0;
                    if (json.has("targetvalue"))
                        targetvalue = json.getDouble("targetvalue");
                    int zoneid = 0;
                    if (json.has("zoneid")) {
                        zoneid = json.getInt("zoneid");
                        Zone zone = Core.getZoneFromId(zoneid);
                        if (zone != null) {
                            if (json.has("zonesensorid")) {
                                int zonesensorid = json.getInt("zonesensorid");
                                ZoneSensor zoneSensor = zone.zoneSensorFromId(zonesensorid);
                                int remotesensorid = zoneSensor.getSensorId();
                                sendKeepTemperatureCommand(targetvalue, seconds, remotesensorid);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return  true;
            }

            @Override
            public void end() {
                sendSwitchOffCommand();
            }
        });
        actionCommandList.add(cmd);

        cmd = new ActionCommand("switchon", "Accendi");
        cmd.addTarget("Temperatura", 0, 30);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    int seconds = 1800;
                    /*if (json.has("seconds"))
                        seconds = json.getInt("seconds");*/
                    double targetvalue = 0;
                    if (json.has("targetvalue"))
                        targetvalue = json.getDouble("targetvalue");
                    sendSwitchOnCommand(targetvalue,seconds);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            public void end() {
                sendSwitchOffCommand();
            }
        });
        actionCommandList.add(cmd);

        // switch off command
        cmd = new ActionCommand("switchoff", "Spengi");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                return sendSwitchOffCommand();
            }
            @Override
            public void end() {
                sendSwitchOffCommand();
            }
        });
        actionCommandList.add(cmd);


        command = new HeaterActuatorCommand(shieldid, id);
        datalog = new HeaterDataLog(id);
    }

    public void init() {
        startPrograms();
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        Status status = new Status(STATUS_MANUAL,STATUS_DESCRIPTION_MANUAL);
        statusList.add(status);
        status = new Status(STATUS_KEEPTEMPERATURE,STATUS_DESCRIPTION_KEEPTEMPERATURE);
        statusList.add(status);
        status = new Status(STATUS_OFF,STATUS_DESCRIPTION_OFF);
        statusList.add(status);
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
    }

    public int getTimeRangeId() {
        return timeRange;
    }

    public int getZoneId() {
        return zoneId;
    }

    protected void setSensorId(int sensorId) {

        if (sensorId == this.remoteSensorId)
            return;

        SensorBase sensorBase = Core.getSensorFromId(sensorId);
        if (sensorBase != null) {
            sensorBase.removeListener(remoteSensorListener);
        }
        this.remoteSensorId = sensorId;
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

    /*public Boolean sendCommand(String command, long duration, double targetTemperature, int scenario, int timeInterval, int zone, double temperature) {

        return false;
    }*/

    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        return null;
    }

    @Override
    public Boolean sendCommand(String cmd, JSONObject json) {

        for (ActionCommand actionCommand : actionCommandList) {
            if (cmd.equals(actionCommand.command))
                actionCommand.commandMethod.execute(json);
        }
        return true;
    }

    @Override
    public Boolean endCommand() {

        sendSwitchOffCommand();
        return true;
    }



    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date, json);

        boolean oldReleStatus = this.releStatus;
        //int oldScenario = scenario;
        //int oldTimeInterval = timeInterval;
        //String oldStatus = getStatus();

        try {
            LOGGER.info("received jsonResultSring=" + json.toString());

            /*if (json.has("status")) {
                String status = json.getString("status");
                setStatus(status);
            }*/

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

            /*if (json.has("zoneid"))
                setZoneId(json.getInt("zoneid"));*/
            if (json.has("relestatus"))
                setReleStatus(json.getBoolean("relestatus"));
            if (json.has("duration"))
                setDuration(json.getInt("duration"));
            if (json.has("remaining"))
                setRemaining(json.getInt("remaining"));
            if (json.has("target"))
                setTargetTemperature(json.getDouble("target"));
            if (json.has("actionid"))
                setActionId(json.getInt("actionid"));
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
        /*if (!getStatus().equals(oldStatus)) {
            // notifica Schedule che è cambiato lo stato ed invia una notifica alle app
            //sensorSchedule.checkProgram();
            String description = "Status changed from " + oldStatus + " to " + getStatus();
            Core.sendPushNotification(SendPushMessages.notification_statuschange, "Status", description, "0", getId());
        }*/

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
            cal.set(Calendar.HOUR_OF_DAY, duration / 3600);
            cal.set(Calendar.MINUTE, duration % 3600 / 60);
            cal.set(Calendar.SECOND, duration % 60);
            String str = timeFormat.format(cal.getTime());
            json.put("duration", str);

            cal.set(Calendar.HOUR_OF_DAY, remaining / 3600);
            cal.set(Calendar.MINUTE, remaining % 3600 / 60);
            cal.set(Calendar.SECOND, remaining % 60);
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

    /*@Override
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
    public void onChangeValue(double value) {
        if (value != temperature)
            sendTemperature(value);
        temperature = value;

    }*/

    /*@Override
    public void programChanged(ActiveProgram newProgram) {

    }*/

    @Override
    public boolean setStatus(String status) {
        //String oldStatus = status;
        boolean res = super.setStatus(status);
        if (res) {
            if (!oldStatus.status.equals(status) && status.equals(STATUS_MANUAL)) {
                // se lo stato diventa manual si mettein ascolto sulla
                // zona della temperatura manuale e manda una richiesta di aggiornamento temperatura alla zona
                SensorBase sensor = Core.getSensorFromId(remoteSensorId);
                if (sensor != null) {
                    sensor.removeListener(remoteSensorListener);
                    sensor.addListener(remoteSensorListener);

                    temperature = 0;
                    sensor.requestAsyncSensorStatusUpdate();
                }
            } else if (!status.equals(STATUS_MANUAL)) {
                SensorBase sensorBase = Core.getSensorFromId(remoteSensorId);
                if (sensorBase != null) {
                    sensorBase.removeListener(remoteSensorListener);
                }
            }
        }
        return res;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String sendKeepTemperatureCommand(double target, int duration, int remoteSensorId) {

        this.duration = duration;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, duration / 3600);
        cal.add(Calendar.MINUTE, duration % 3600 / 60);
        cal.add(Calendar.SECOND, duration % 60);
        setEndDate(cal.getTime());

        JSONObject json = new JSONObject();
        try {
            json.put("actuatorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
            json.put("duration", this.duration);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("enddate", df.format(endDate));
            json.put("target", target);
            json.put("actionid", /*id*/0);
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            cmd.send();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.remoteSensorId != remoteSensorId) {

            SensorBase remoteSensor = Core.getSensorFromId(this.remoteSensorId);
            if (remoteSensor != null)
                remoteSensor.removeListener(remoteSensorListener);

            this.remoteSensorId = remoteSensorId;
            remoteSensor = Core.getSensorFromId(this.remoteSensorId);
            if (remoteSensor != null) {
                remoteSensor.addListener(remoteSensorListener);
            }
        }

        return "";
    }

    public String sendSwitchOnCommand(double target, int duration) {

        this.duration = duration;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, duration / 3600);
        cal.add(Calendar.MINUTE, duration % 3600 / 60);
        cal.add(Calendar.SECOND, duration % 60);
        setEndDate(cal.getTime());

        JSONObject json = new JSONObject();
        try {
            json.put("actuatorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
            json.put("duration", this.duration);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("enddate", df.format(endDate));
            json.put("target", target);
            json.put("actionid", /*id*/0);
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            cmd.send();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean sendSwitchOffCommand() {

        JSONObject json = new JSONObject();
        try {
            json.put("actuatorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_Off);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("actionid", /*id*/0);
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            cmd.send();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /*@Override
    public void onUpdateTemperature(int zoneId, double newTemperature, double oldTemperature) {
        if (newTemperature != temperature)
            sendTemperature(newTemperature);
        temperature = newTemperature;
    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

    }*/
}
