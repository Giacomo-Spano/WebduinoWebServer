package com.server.webduino.core.sensors;

import com.quartz.KeepTemperatureQuartzJob;
import com.server.webduino.core.*;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.Status;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.logging.Logger;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import static com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand.*;
import static org.quartz.JobBuilder.newJob;

public class HeaterActuator extends Actuator {

    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_KEEPTEMPERATURE = "keeptemperature";
    public static final String STATUS_KEEPTEMPERATURE_RELEOFF = "keeptemperaturereleoff";
    public static final String STATUS_OFF = "off";
    public static final String STATUS_ERROR = "error";

    public static final String STATUS_DESCRIPTION_MANUAL = "Manuale";
    public static final String STATUS_DESCRIPTION_KEEPTEMPERATURE = "Mantieni temperature";
    public static final String STATUS_DESCRIPTION_KEEPTEMPERATURE_RELEOFF = "Mantieni temperatura relè spento";
    public static final String STATUS_DESCRIPTION_OFF = "Off";
    public static final String STATUS_DESCRIPTION_ERROR = "Error - remote device";

    private static final Logger LOGGER = Logger.getLogger(HeaterActuator.class.getName());

    private volatile boolean stopCommand = false; // variabile di sincronizzazione
    private List<Thread> commandThreadList = new ArrayList<>(); // è la lista di tutti i thread dei comandi attivi

    protected boolean releStatus;
    protected double temperature;
    protected long duration;
    protected int remaining;
    protected double targetTemperature;
    protected Date lastTemperatureUpdateReceived;
    protected Date lastCommandDate;
    protected Date endDate;
    // valori letti dal sensore (ricevutri da updatefromJson
    // possono potenzialemte essere diversi da quelli salvati ActiveProgram
    public int actionId;
    public int timeRange;
    protected int zoneId; //  questo valore non è letto dal sensore ma rimane solo sul server
    private int remoteSensorId;

    protected Date lastTemperatureCommandUpdateSent;

    Status status_manual, status_keeptemperature, status_keeptemperature_releoff, status_off, status_error;

    //protected Date lastTemperatureCommandUpdateSentError;

    public interface HeaterActuatorListener extends SensorListener {
        public void onChangeRemoteSensor(int id);

        public void onChangeRemoteSensorOnlineStatus(Status newStatus, Status oldStatus);

        public void onChangeTargetTemperature(double value);

        public void onChangeCurrentTemperature(double value);
    }

    private SensorListener remoteSensorListener = new SensorListener() {

        @Override
        public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus) {

        }

        @Override
        public void onChangeOnlineStatus(Status newStatus, Status oldStatus) {

        }

        @Override
        public void onChangeValue(SensorBase sensor, double value) {

            //if (value != temperature)
            sendTemperature(value); // invia la temperatura al sensore webduino via
            setTemperature(value);

            if (listeners != null) {  // invia la temperatura ai listener (i.e. HeaterSystem)
                for (SensorListener listener : listeners) {
                    if (listener instanceof HeaterActuatorListener)
                        ((HeaterActuatorListener) listener).onChangeCurrentTemperature(value);
                }
            }
        }
    };

    public HeaterActuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "heatersensor";

        command = new HeaterActuatorCommand(shieldid, id);
        datalog = new HeaterDataLog(id);
    }

    protected void initCommandList() {
        super.initCommandList();
        ActionCommand cmd = new ActionCommand(ACTIONCOMMAND_KEEPTEMPERATURE, ActionCommand.ACTIONCOMMAND_KEEPTEMPERATURE_DESCRIPTION);
        cmd.addTarget("Temperatura", 0, 30, "°C");
        cmd.addZone("Zona", TemperatureSensor.temperaturesensortype);
        cmd.addCommand(new KeepTemperatureCommand());
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ACTIONCOMMAND_STOP_KEEPTEMPERATURE, ActionCommand.ACTIONCOMMAND_STOP_KEEPTEMPERATURE_DESCRIPTION);
        cmd.addCommand(new StopKeeptemperatureCommand());
        actionCommandList.add(cmd);
    }


    public void init() {
        startPrograms();
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        status_manual = new Status(STATUS_MANUAL, STATUS_DESCRIPTION_MANUAL);
        statusList.add(status_manual);
        status_keeptemperature = new Status(STATUS_KEEPTEMPERATURE, STATUS_DESCRIPTION_KEEPTEMPERATURE);
        statusList.add(status_keeptemperature);
        status_keeptemperature_releoff = new Status(STATUS_KEEPTEMPERATURE_RELEOFF, STATUS_DESCRIPTION_KEEPTEMPERATURE_RELEOFF);
        statusList.add(status_keeptemperature_releoff);
        status_off = new Status(STATUS_OFF, STATUS_DESCRIPTION_OFF);
        statusList.add(status_off);
        status_error = new Status(STATUS_ERROR, STATUS_DESCRIPTION_ERROR);
        statusList.add(status_error);
    }

    public long getDuration() {
        return duration;
    }

    public Date getEndDate() {
        return endDate;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
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

    protected boolean setRemoteSensor(int remotesensorId) {

        SensorBase remotesensor = Core.getSensorFromId(remotesensorId);
        if (remotesensor != null) {

            setTemperature(remotesensor.getValue());
            remotesensor.removeListener(remoteSensorListener);
            this.remoteSensorId = remotesensorId;
            remotesensor.addListener(remoteSensorListener);

            if (remotesensor.getStatus().status.equals(Status.STATUS_OFFLINE))
                setStatus(HeaterActuator.STATUS_ERROR);

            if (listeners != null) {
                for (SensorListener listener : listeners) {
                    if (listener instanceof HeaterActuatorListener)
                        ((HeaterActuatorListener) listener).onChangeRemoteSensor(remotesensorId);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public int getRemoteSensorId() {
        return remoteSensorId;
    }

    protected void removeRemoteSensor() {
        SensorBase remotesensor = Core.getSensorFromId(this.remoteSensorId);
        if (remotesensor != null) {
            remotesensor.removeListener(remoteSensorListener);
        }
        this.remoteSensorId = 0;

        if (listeners != null) {
            for (SensorListener listener : listeners) {
                if (listener instanceof HeaterActuatorListener)
                    ((HeaterActuatorListener) listener).onChangeRemoteSensor(this.remoteSensorId);
            }
        }
    }

    protected double getRemoteSensorTemperature() {
        System.out.println("getRemoteSensorTemperature");
        System.out.println("remoteSensorId=" + this.remoteSensorId);
        SensorBase remotesensor = Core.getSensorFromId(this.remoteSensorId);
        if (remotesensor != null && remotesensor instanceof TemperatureSensor) {
            return ((TemperatureSensor) remotesensor).getTemperature();
        }
        System.out.println("remoteSensorId not found");
        return -100;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    protected void setTargetTemperature(double targetTemperature) {
        this.targetTemperature = targetTemperature;

        //invia aggiornamento temperatura target ai listener (i.e. HeaterSystem)
        //remoteSensorListener.onChangeTargetTemperature(targetTemperature);
        if (listeners != null) {
            for (SensorListener listener : listeners) {
                if (listener instanceof HeaterActuatorListener)
                    ((HeaterActuatorListener) listener).onChangeTargetTemperature(targetTemperature);
            }
        }
    }

    public boolean getReleStatus() {
        return releStatus;
    }

    protected void setReleStatus(boolean releStatus) {

        this.releStatus = releStatus;
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    public double getTemperature() {
        return temperature;
    }

    protected void setTemperature(double temperature) {
        this.temperature = temperature;
        //remoteSensorListener.onChangeCurrentTemperature(temperature);
        if (listeners != null) {
            for (SensorListener listener : listeners) {
                if (listener instanceof HeaterActuatorListener)
                    ((HeaterActuatorListener) listener).onChangeCurrentTemperature(temperature);
            }
        }

    }

    protected void setLastTemperatureUpdateReceived(Date date) {
        this.lastTemperatureUpdateReceived = date;
    }

    protected void setLastCommandUpdate(Date date) {
        this.lastCommandDate = date;
    }

    protected void setEndDate(Date date) {
        this.endDate = date;
    }

    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        return null;
    }

    @Override
    public Boolean endCommand() {

        //sendSwitchOff();
        sendIdle();
        // rimuovi eventuali comandi pending
        removeActiveCommandThreads();

        return true;
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date, json);
        //boolean oldReleStatus = this.releStatus;
        try {
            LOGGER.info("received jsonResultSring=" + json.toString());

            if (json.has("remotetemp")) {
                setTemperature(json.getDouble("remotetemp"));
                setStatus(getStatus().status); // questo serve per aggiornare la descrizione di keeptemperature
            }
            if (json.has("lasttemp") && !json.getString("lasttemp").equals("")) {
                String str = json.getString("lasttemp");
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                try {
                    Date lasttemperatureupdate = df.parse(str);
                    setLastTemperatureUpdateReceived(lasttemperatureupdate);
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

            /*JSONObject jsonstatus = new JSONObject();
            try {
                jsonstatus.put("sensorid", id);
                jsonstatus.put("shieldid", shieldid);
                jsonstatus.put("name", name);
                jsonstatus.put("description", description);
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                jsonstatus.put("date", df.format(Core.getDate()));
                jsonstatus.put("releStatus", releStatus);
                jsonstatus.put("temperature", temperature);
                jsonstatus.put("duration", duration);
                jsonstatus.put("remaining", remaining);
                df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                jsonstatus.put("lastTemperatureUpdateReceived", df.format(lastTemperatureUpdateReceived));
                jsonstatus.put("lastCommandDate", df.format(lastCommandDate));
                jsonstatus.put("endDate", df.format(endDate));
                jsonstatus.put("actionId", actionId);
                jsonstatus.put("timeRange", timeRange);
                jsonstatus.put("zoneId", zoneId);
                jsonstatus.put("remoteSensorId", remoteSensorId);
                jsonstatus.put("lastTemperatureCommandUpdateSent", df.format(lastTemperatureCommandUpdateSent));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            // Attributes
            //String message = "{\"Attributes\":" + json.toString() + "}";
            //Core.updateHomeAssistant("homeassistant/sensor/" + id + "/attributes", message);   //send

            // current temperature and target
            String message = "{\"temperature\":" + temperature + ",\"target\":" + targetTemperature + "}";
            Core.updateHomeAssistant("homeassistant/sensor/" + id + "/temperature", message);   //send


            // mode
            if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE) || this.getStatus().status.equals(STATUS_KEEPTEMPERATURE_RELEOFF)) {
                //if (this.getStatus().equals(status_keeptemperature) || this.getStatus().equals(status_keeptemperature_releoff)) {
                if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE) || this.getStatus().status.equals(STATUS_KEEPTEMPERATURE_RELEOFF)) {
                    Core.updateHomeAssistant("homeassistant/sensor/" + id + "/mode", "{\"mode\": \"auto\"}");   //send
                } else {
                    Core.updateHomeAssistant("homeassistant/sensor/" + id + "/mode", "{\"mode\": \"off\"}");
                }

                // action
                if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE)) {
                    // if (this.getStatus().equals(status_keeptemperature)) {
                }
                Core.updateHomeAssistant("homeassistant/sensor/" + id + "/action", "{\"action\": \"heating\"}");   //send
            }

            if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE_RELEOFF)) {
                //if (this.getStatus().equals(status_keeptemperature_releoff)) {
                Core.updateHomeAssistant("homeassistant/sensor/" + id + "/action", "{\"action\": \"idle\"}");   //send
            }

        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("error");
        }
        writeDataLog("update");
    }

    @Override
    public void getJSONField(JSONObject json) {
        try {
            json.put("temperature", temperature);
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, (int) duration / 3600);
            cal.set(Calendar.MINUTE, (int) duration % 3600 / 60);
            cal.set(Calendar.SECOND, (int) duration % 60);
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
            if (lastTemperatureUpdateReceived != null)
                json.put("lasttemperatureupdate", df.format(lastTemperatureUpdateReceived));
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
    public boolean setStatus(String status) {

        if (this.getStatus().status.equals(STATUS_OFF)) {
            /*SensorBase remotesensor = Core.getSensorFromId(getRemoteSensorId());
            if (remotesensor.getStatus().status.equals(Status.STATUS_OFFLINE))
                return false;*/
        }

        boolean res = super.setStatus(status);

        this.getStatus().description = "";
        if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE) || this.getStatus().status.equals(STATUS_KEEPTEMPERATURE_RELEOFF)) {

            if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE))
                this.getStatus().description = STATUS_DESCRIPTION_KEEPTEMPERATURE + " " + targetTemperature;
            else if (this.getStatus().status.equals(STATUS_KEEPTEMPERATURE_RELEOFF))
                this.getStatus().description = STATUS_DESCRIPTION_KEEPTEMPERATURE_RELEOFF + " " + targetTemperature;

            Zone zone = Core.getZoneFromId(zoneId);
            if (zone != null)
                this.getStatus().description += " Zona: [" + zone.id + "]." + zone.getName();
            SensorBase remotesensor = Core.getSensorFromId(remoteSensorId);
            if (remotesensor != null)
                this.getStatus().description += " Sensore attivo: [" + remotesensor.id + "]." + remotesensor.name + remotesensor.getValue() + remotesensor.valueunit;
            this.getStatus().description += " Durata: " + duration;
            this.getStatus().description += " Tempo rimanente: " + remaining;
            this.getStatus().description += " Ultimo aggiornamento: " + lastTemperatureUpdateReceived;
            this.getStatus().description += " Ultimo comando ricevuto: " + lastCommandDate;
            this.getStatus().description += " Data fine: " + endDate;
        }

        if (res) {

            this.getStatus().description += " Rele ";
            if (releStatus)
                this.getStatus().description += " ACCESO";
            else
                this.getStatus().description += " SPENTO";
        }
        return res;
    }

    public boolean sendTemperature(double temperature) {
        JSONObject json = new JSONObject();
        try {
            json.put("sensorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_SendTemperature);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            json.put("zoneid", zoneId);
            json.put("zonesensorid", remoteSensorId);

            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            boolean res = cmd.send();
            if (!res) {
                return false;
            }
            JSONObject jsonresult = new JSONObject(cmd.result);
            updateFromJson(Core.getDate(), jsonresult);
            lastTemperatureCommandUpdateSent = Core.getDate();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean sendKeepTemperature(double target, long duration, int remoteSensorId, int actionid) {

        this.duration = duration;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, (int) duration / 3600);
        cal.add(Calendar.MINUTE, (int) duration % 3600 / 60);
        cal.add(Calendar.SECOND, (int) duration % 60);
        setEndDate(cal.getTime());

        JSONObject json = new JSONObject();
        try {
            json.put("sensorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
            json.put("duration", this.duration);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("enddate", df.format(endDate));
            json.put("target", target);
            json.put("actionid", actionid);
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            json.put("zoneid", zoneId);
            json.put("zonesensorid", remoteSensorId);

            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            boolean res = cmd.send();
            if (!res) {
                return false;
            }
            JSONObject jsonresult = new JSONObject(cmd.result);

            ////
            double oldTarget = getTargetTemperature();
            int oldRemoteSensorId = getRemoteSensorId();
            Date oldDate = getEndDate();
            //

            updateFromJson(Core.getDate(), jsonresult);
            lastTemperatureCommandUpdateSent = Core.getDate();

            boolean sendupdate = false;
            if (getOldStatus() != getStatus()) {
                sendupdate = true;
            } else if (getTargetTemperature() != oldTarget ||
                    getRemoteSensorId() != oldRemoteSensorId ||
                    getEndDate() != oldDate) {
                sendupdate = true;
            }

            if (sendupdate) {
                String message = "Riscaldamento acceso.";


                message += "Temperatura impostata a " + getTargetTemperature() + "gradi.  Temperatura attuale " + getTemperature() + "gradi.";

                //Core.updateHomeAssistant("tts/play/media_player.studio", message);   //send


                Calendar calendar = Calendar.getInstance();
                cal.setTime(getEndDate());

                message += " Fine programma alle ore ";
                message += (calendar.get(Calendar.HOUR_OF_DAY) + 1) + " e " + calendar.get(Calendar.MINUTE) + " minuti";
                //Core.updateHomeAssistant("tts/play/media_player.studio", message);   //send
                Core.updateHomeAssistant("fromServer/homeassistant/playheaterstatus", "");   //send
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendStopKeepTemperature() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, (int) duration / 3600);
        cal.add(Calendar.MINUTE, (int) duration % 3600 / 60);
        cal.add(Calendar.SECOND, (int) duration % 60);
        setEndDate(cal.getTime());
        JSONObject json = new JSONObject();
        try {
            json.put("sensorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_Stop_KeepTemperature);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("actionid", /*id*/0);
            json.put("date", df.format(Core.getDate()));
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            boolean res = cmd.send();
            if (!res) {
                return false;
            }
            JSONObject jsonresult = new JSONObject(cmd.result);
            updateFromJson(Core.getDate(), jsonresult);
            lastTemperatureCommandUpdateSent = Core.getDate();

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected String sendSwitchOn(double target, int duration) {

        this.duration = duration;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, duration / 3600);
        cal.add(Calendar.MINUTE, duration % 3600 / 60);
        cal.add(Calendar.SECOND, duration % 60);
        setEndDate(cal.getTime());

        JSONObject json = new JSONObject();
        try {
            json.put("sensorid", id);
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

    protected boolean sendSwitchOff() {

        JSONObject json = new JSONObject();
        try {
            json.put("sensorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_Off);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("actionid", /*id*/0);
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            return cmd.send();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    protected boolean sendIdle() {

        JSONObject json = new JSONObject();
        try {
            json.put("sensorid", id);
            json.put("shieldid", shieldid);
            json.put("command", HeaterActuatorCommand.Command_Idle);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            json.put("actionid", 0);
            json.put("date", df.format(Core.getDate()));
            json.put("temperature", temperature);
            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            return cmd.send();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private class StopKeeptemperatureCommand implements Command {
        @Override
        public boolean execute(JSONObject json) {
            removeActiveCommandThreads();
            boolean res = sendStopKeepTemperature();
            if (res) {
                end();
                return res;
            }
            return false;
        }

        @Override
        public void end() {
            //sendSwitchOff();
            // rimuovi eventuali comandi pending
            removeActiveCommandThreads();
        }

        @Override
        public JSONObject getResult() {
            return null;
        }
    }

    private class KeepTemperatureCommand implements Command {

        CommandThread commandThread;
        double targetvalue = 0;
        long duration = 0;
        int commandremotesensorid = 0;
        Date endtime;

        @Override
        public boolean execute(JSONObject json) {
            try {
                System.out.println("CKeepTemperatureCommand::execute");
                // rimuovi eventuali comandi pending
                removeActiveCommandThreads();

                if (json.has("duration"))
                    duration = json.getInt("duration");
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Core.getDate());
                calendar.add(Calendar.SECOND, (int) duration);
                endtime = calendar.getTime();
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
                            if (zoneSensor != null) {
                                if (json.has("actionid"))
                                    actionId = json.getInt("actionid");
                                commandremotesensorid = zoneSensor.getSensorId();

                                // imposta il sensore remoto
                                boolean res = setRemoteSensor(commandremotesensorid);
                                if (!res) // se fallisce interrompi il thread perchè il comando non può esserre eseguito
                                    return false;
                                long diffInMillies = Math.abs(endtime.getTime() - Core.getDate().getTime());
                                long duration = diffInMillies / 1000;
                                res = sendKeepTemperature(targetvalue, duration, commandremotesensorid, actionId);
                                // avvia un threa della durata di duration secondi
                                long timeout = duration * 1000; // 1in millisecondi
                                commandThread = new CommandThread();
                                //commandThread.start();
                                Thread thread = new Thread(commandThread, "keeptemperaturecommandThread");
                                thread.start();
                                commandThreadList.add(commandThread);
                                try {
                                    thread.join(timeout); // timeout è il timeout di attesa fine thread in millisecondi
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    end();
                                }
                                //commandThread.count = 0;
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        public void end() {
            System.out.println("CommandThread keeptemperature end");
            sendStopKeepTemperature();
            removeRemoteSensor();
            commandThread.interrupt();
        }

        @Override
        public JSONObject getResult() {
            return null;
        }

        public class CommandThread extends Thread implements Runnable {

            private TimerTask timertask;
            private int count = 0;

            public CommandThread() {
            }

            @Override
            public void run() {
                Thread t = Thread.currentThread();
                System.out.println("CommandThread keeptemperature started");
                try {
                    scheduleKeeptemperatureJob(endtime, targetvalue, commandremotesensorid, actionId);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void interrupt() {
                System.out.println("interrupt -  stop timer");
                deleteKeeptemperatureJob();
                super.interrupt();
            }
        }

        ;
    }

    private void removeActiveCommandThreads() {
        Iterator<Thread> it = commandThreadList.iterator();
        System.out.println("removeActiveCommandThreads n=" + commandThreadList.size());
        while (it.hasNext()) {
            Thread thread = it.next();
            thread.interrupt();
            it.remove();
            System.out.println("remove");
        }
        System.out.println("removeActiveCommandThreads n=" + commandThreadList.size());
    }

    private JobKey jobKey;
    private Scheduler scheduler = null;

    private void scheduleKeeptemperatureJob(Date endtime, double targetvalue, int commandremotesensorid, int actionid) throws SchedulerException {

        scheduler = new StdSchedulerFactory().getScheduler();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("heater", this);
        jobDataMap.put("endtime", endtime);
        jobDataMap.put("target", targetvalue);
        jobDataMap.put("commandremotesensorid", commandremotesensorid);
        jobDataMap.put("actionid", actionid);
        // define the job and tie it to our job's class
        jobKey = JobKey.jobKey("KeepTemperatureQuartzJob", "Group");
        JobDetail job = newJob(KeepTemperatureQuartzJob.class)
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build();
        // Trigger the job to run now, and then every 60 seconds
        Trigger trigger = newTrigger()
                .withIdentity("KeeptemperatureTriggerName", "Group")
                //.startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(1 * 60)   // interroga ogni 1 minuti
                        .repeatForever())
                .build();
        // Setup the Job and WebduinoTrigger with Scheduler & schedule jobs
        scheduler.scheduleJob(job, trigger);
    }

    private void deleteKeeptemperatureJob() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            printJobsAndTriggers(scheduler);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void printJobsAndTriggers(Scheduler scheduler) throws SchedulerException {
        LOGGER.info("Quartz Scheduler: {}" + scheduler.getSchedulerName());
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                LOGGER.info("Found job identified by {} " + jobKey);
            }
        }
        for (String group : scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(group))) {
                LOGGER.info("Found trigger identified by {} " + triggerKey);
            }
        }
    }
}
