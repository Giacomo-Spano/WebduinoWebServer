package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.TemperatureSensor;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import static com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand.*;
import static java.nio.charset.StandardCharsets.*;


//import static com.server.webduino.core.sensors.HeaterActuator.*;

/**
 * Created by giaco on 12/05/2017.
 */
public class HeaterSystem extends com.server.webduino.core.webduinosystem.WebduinoSystem {

    private static final Logger LOGGER = Logger.getLogger(HeaterSystem.class.getName());

    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_AUTO = "auto";
    public static final String STATUS_OFF = "off";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_OUT_OF_HOME = "outofhome";

    public static final String STATUS_DESCRIPTION_MANUAL = "Modalità manuale";
    public static final String STATUS_DESCRIPTION_AUTO = "Modalità automatica";
    public static final String STATUS_DESCRIPTION_OFF = "Off";
    public static final String STATUS_DESCRIPTION_ERROR = "error - actuator or sensor offline";
    public static final String STATUS_DESCRIPTION_OUT_OF_HOME = "Out of home";

    Status status_auto, status_manual, status_off, status_error, status_out_of_home;

    HeaterActuator heaterActuator = null;
    HeaterActuator.HeaterActuatorListener heaterActuatorChangeListener;

    public HeaterSystem(int id, String name, String type, boolean enabled, String status) {
        super(id, name, type, enabled, status);

        heaterActuatorChangeListener = new HeaterActuator.HeaterActuatorListener() {
            @Override
            public void onChangeRemoteSensor(int id) {
                updateHA();// invia il remote sensor nuovo come attribute a HomeAssistant
            }

            @Override
            public void onChangeRemoteSensorOnlineStatus(Status newStatus, Status oldStatus) {
                // se il remote sensor di HeaterActuator è offline metti in stato di errore HeaterSystem
                /*if (!status.equals(status_off) && newStatus.equals(Status.STATUS_OFFLINE)) {
                    try {
                        setStatus(status_error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/
            }

            @Override
            public void onChangeTargetTemperature(double value) {
                if (heaterActuator != null) {
                    //Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/temp", "{\"target\": " + heaterActuator.getTargetTemperature() + ", \"temperature\": " + heaterActuator.getTemperature() + "}");
                    updateHA();
                }
            }

            @Override
            public void onChangeCurrentTemperature(double value) {
//                Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/temperature", "{\"temperature\" :" + value +"}");
                if (heaterActuator != null) {
                    //Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/temp", "{\"target\": " + heaterActuator.getTargetTemperature() + ", \"temperature\": " + heaterActuator.getTemperature() + "}");
                    updateHA();
                }
            }

            @Override
            public void onChangeOnlineStatus(Status newStatus, Status oldStatus) {

                // se HeaterActuator è offline metti in stato di errore HeaterSystem
                if (newStatus.status.equals(Status.STATUS_OFFLINE)) {
                    try {
                        setStatus(status_error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus) {

                //if (heaterActuator.getStatus().status.equals(HeaterActuator.STATUS_KEEPTEMPERATURE))

                updateHA();

                // ripristina stato auto se fine errore
                if (status.equals(status_error)) {
                    if (!newStatus.status.equals(HeaterActuator.STATUS_ERROR))
                        try {
                            setStatus(status_auto);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }

                // metti in stato di erro re se actuator è in errore
                if (newStatus.status.equals(HeaterActuator.STATUS_ERROR)) {
                    try {
                        setStatus(status_error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                // se il sensore heateractuator è offline allora metti in stato offline anche il system HeaterSystem
                if (newStatus.status.equals(Status.STATUS_OFFLINE)) {
                    try {
                        setStatus(status_offline);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            @Override
            public void onChangeValue(SensorBase sensor, double value) {
                //Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/mode", "{\"mode\": \"off\"}");
            }
        };
    }

    @Override
    public void init() {

        super.init();

        initHeaterActuator();

        if (getEnabled()) {

            if (getInitialStatus() != null) {
                if (getInitialStatus().equals("manual")) {
                    double target = 21;
                    SendManualCommand(target);
                } else if (getInitialStatus().equals("auto")) {
                    SendAutoCommand();
                } else if (getInitialStatus().equals("off")) {
                    SendOffCommand();
                } else if (getInitialStatus().equals("outofhome")) {
                    SendOutOfHomeCommand();
                } else {
                    SendAutoCommand();
                }
            } else {
                SendAutoCommand();
            }
        }



        initHA(); // noin serve a nula
    }

    @Override
    public void onChangeOutOfHomeStatus(boolean newStatus, boolean oldStatus) {
        if (newStatus && !status.equals(status_off)) {
            SendOutOfHomeCommand();
        } else {
            SendAutoCommand();
        }
    }

    @Override
    public void destroy() {

        super.destroy();
        if (heaterActuator != null) {
            heaterActuator.removeListener(heaterActuatorChangeListener);
        }
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        status_manual = new Status(STATUS_MANUAL, STATUS_DESCRIPTION_MANUAL);
        statusList.add(status_manual);
        status_auto = new Status(STATUS_AUTO, STATUS_DESCRIPTION_AUTO);
        statusList.add(status_auto);
        status_off = new Status(STATUS_OFF, STATUS_DESCRIPTION_OFF);
        statusList.add(status_off);
        status_error = new Status(STATUS_ERROR, STATUS_DESCRIPTION_ERROR);
        statusList.add(status_error);
        status_out_of_home = new Status(STATUS_OUT_OF_HOME, STATUS_DESCRIPTION_OUT_OF_HOME);
        statusList.add(status_out_of_home);


        /*try {
            setStatus(status_auto);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void initCommandList() {
        super.initCommandList();
        ActionCommand cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_AUTO, ActionCommand.ACTIONCOMMAND_AUTO_DESCRIPTION);
        cmd.addCommand(new AutoCommand());
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_MANUAL, ActionCommand.ACTIONCOMMAND_MANUAL_DESCRIPTION);
        cmd.addTarget("Temperatura", 0, 30, "°C");
        cmd.addZone("Zona", TemperatureSensor.temperaturesensortype);
        cmd.addDuration("Durata");
        cmd.addCommand(new ManualCommand());
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_SWITCHOFF, ActionCommand.ACTIONCOMMAND_SWITCHOFF_DESCRIPTION);
        cmd.addCommand(new SwitchOffCommand());
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_OUT_OF_HOME, ActionCommand.ACTIONCOMMAND_OUT_OF_HOME_DESCRIPTION);
        cmd.addCommand(new OutOfHomeCommand());
        actionCommandList.add(cmd);
    }

    @Override
    public void getJSONField(JSONObject json) {
        try {
            json.put("heatersensorid", getHeaterActuator().getId());
            json.put("heaterrelestatus", getHeaterActuator().getReleStatus());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HeaterActuator getHeaterActuator() {
        return heaterActuator;
    }

    public void initHeaterActuator() {
        heaterActuator = null;
        for (WebduinoSystemActuator actuator : actuators) {
            SensorBase sensor = Core.getSensorFromId(actuator.sensorid);
            if (sensor instanceof HeaterActuator) {
                /*HeaterActuator */
                heaterActuator = (HeaterActuator) sensor;
            }
        }
        if (heaterActuator == null || heaterActuator.getStatus().equals(Status.STATUS_OFFLINE)) {
            try {
                setStatus(status_error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            heaterActuator.addListener(heaterActuatorChangeListener);
        }
    }

    @Override
    public boolean setStatus(Status status) throws Exception {
        LOGGER.info("setStatus: " + status.status);
        boolean ret = super.setStatus(status);



        /*if (status.equals(status_auto)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/mode", "{\"mode\": \"auto\"}");
        } else if (status.equals(status_manual)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/mode", "{\"mode\": \"heat\"}");
        } else if (status.equals(status_off)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/mode", "{\"mode\": \"off\"}");
        }

        if (status.equals(status_auto) || status.equals(status_manual)) {
            if (heaterActuator != null) {
                if (heaterActuator.getStatus().equals(HeaterActuator.STATUS_KEEPTEMPERATURE)) {
                    Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/action", "{\"action\" : \"heating\"}");
                } else {
                    Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/action", "{\"action\" : \"idle\"}");
                }
                Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/temp", "{\"target\": " + heaterActuator.getTargetTemperature() + ", \"temperature\": " + heaterActuator.getTemperature() + "}");
            }

        } else if (status.equals(status_off)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/mode", "{\"mode\": \"off\"}");
        }

        if (true) {
            sendAttributes();
        }*/
        updateHA();
        return ret;
    }

    private void initHA() {
        /*Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/mode", "{\"mode\": \"auto\"}");
        try {
            JSONObject json = new JSONObject();
            json.put("target", 33);
            json.put("temperature", 15);
            Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/temp",  json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void updateHA() {
        boolean active = false;
        if (status.equals(status_auto)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/mode", "auto");

        } else if (status.equals(status_manual)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/mode", "heat");
            active = true;
        } else if (status.equals(status_off)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/mode", "off");
        } else if (status.equals(status_out_of_home)) {
            Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/mode", "off");
        }

        if (heaterActuator != null) {
            if (heaterActuator.getStatus().status.equals(HeaterActuator.STATUS_KEEPTEMPERATURE)) {
                Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/action", "{\"action\" : \"heating\"}");
            } else if (heaterActuator.getStatus().status.equals(HeaterActuator.STATUS_KEEPTEMPERATURE_RELEOFF)) {
                Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/action", "{\"action\" : \"idle\"}");
            } else if (heaterActuator.getStatus().status.equals(HeaterActuator.STATUS_OFF)) {
                Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/action", "{\"action\" : \"off\"}");
            } else {
                ;
            }

            JSONObject json = new JSONObject();
            try {
                if (status.equals(status_off) ||status.equals(status_out_of_home)) {
                    json.put("target", "--");
                } else {
                    json.put("target", heaterActuator.getTargetTemperature());
                }
                json.put("temperature", heaterActuator.getTemperature());
                Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/temp", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        sendAttributes();
    }

    public void sendAttributes() {

        JSONObject json = new JSONObject();
        try {
            json.put("systemid", getId());
            json.put("name", getName());
            json.put("description", getType());
            json.put("status", status.status);
            json.put("statussescription", status.description);

            if (heaterActuator != null) {
                json.put("heaterstatus", heaterActuator.getStatus().status);
                json.put("heaterstatusdescription", "yyy"/*heaterActuator.getStatus().description.substring(0,254)*/);
                if (heaterActuator.getReleStatus())
                    json.put("rele", "on");
                else
                    json.put("rele", "off");
                json.put("heattemperature", heaterActuator.getTemperature());
                json.put("heattarget", heaterActuator.getTargetTemperature());
                json.put("duration", heaterActuator.getDuration());
                //jsonstatus.put("remaining", heaterActuator.get());
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                if (heaterActuator.getLastUpdate() != null)
                    json.put("lastTemperatureUpdateReceived", df.format(heaterActuator.getLastUpdate()));
                else
                    json.put("lastTemperatureUpdateReceived", "unavailable");
                //jsonstatus.put("lastCommandDate", df.format(lastCommandDate));
                json.put("endDate", df.format(heaterActuator.getEndDate()));
                json.put("actionId", heaterActuator.getActionId());
                json.put("timeRange", heaterActuator.getTimeRangeId());
                json.put("zoneId", heaterActuator.getZoneId());
                int remoteSensorId = heaterActuator.getRemoteSensorId();
                json.put("remoteSensorId", remoteSensorId);
                SensorBase remotesensor = Core.getSensorFromId(remoteSensorId);
                if (remotesensor != null) {
                    json.put("remoteSensorName", remotesensor.getName());
                } else {
                    json.put("remoteSensorName", "unavailable");
                }
                json.put("remoteSensorId", heaterActuator.getRemoteSensorId());

            } else {
                json.put("heaterstatus", "unavailable");
                json.put("heaterstatusdescription", "unavailable");
                json.put("rele", "unavailable");
                json.put("temperature", "unavailable");
                json.put("target", "unavailable");
                json.put("duration", "unavailable");
                json.put("lastTemperatureUpdateReceived", "unavailable");
                json.put("endDate", "unavailable");
                json.put("actionId", "unavailable");
                json.put("timeRange", "unavailable");
                json.put("zoneId", "unavailable");
                json.put("remoteSensorId", "unavailable");
                json.put("remoteSensorName", "unavailable");
            }
            String message = "{\"Attributes\":" + json.toString() + "}";
            //message = "{\"Attributes\":" + message + "}";
            //byte[] ptext = message.getBytes(ISO_8859_1);
            //message = new String(ptext, UTF_8);

            Core.updateHomeAssistant("homeassistant/webduinosystem/" + id + "/attributes", message);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*String jsonstr = "{";
        jsonstr += "\"systemid\": " + getId();
        jsonstr += ", \"name\": \"" + getName() + "\"";;
        jsonstr += ", \"type\": \"" + getType() + "\"";;
        jsonstr += ", \"status\": \"" + status.status + "\"";;
        jsonstr += ", \"statussdescription\": \"" + status.description + "\"";

        if (heaterActuator != null) {
            jsonstr += ", \"heaterstatus\": \"" + heaterActuator.getStatus().status + "\"";
            jsonstr += ", \"heaterstatusdescription\": \"" + heaterActuator.getStatus().description + "\"";
            if (heaterActuator.getReleStatus())
                jsonstr += ", \"rele\": \"" + "on" + "\"";
            else
                jsonstr += ", \"rele\": \"" + "off" + "\"";
            jsonstr += ", \"temperature\": " + heaterActuator.getTemperature();
            jsonstr += ", \"target\": " + heaterActuator.getTargetTemperature();
            jsonstr += ", \"duration\": " + heaterActuator.getDuration();

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            jsonstr += ", \"lastTemperatureUpdateReceived\": \"" + df.format(heaterActuator.getLastUpdate()) + "\"";
            //jsonstatus.put("lastCommandDate", df.format(lastCommandDate));
            jsonstr += ", \"endDate\": \"" + heaterActuator.getEndDate() + "\"";
            jsonstr += ", \"actionId\": \"" + heaterActuator.getActionId() + "\"";
            jsonstr += ", \"timeRange\": \"" + heaterActuator.getTimeRangeId() + "\"";
            jsonstr += ", \"zoneId\": \"" + heaterActuator.getZoneId() + "\"";
            int remoteSensorId = heaterActuator.getRemoteSensorId();
            jsonstr += ", \"remoteSensorId\": \"" + heaterActuator.getRemoteSensorId() + "\"";
        } else {
            jsonstr += ", \"heaterstatus\": \"unknown\"";
            jsonstr += ", \"heaterstatusdescription\": \"unknown\"";
            jsonstr += ", \"rele\": \"unknown\"";
            jsonstr += ", \"temperature\": \"unknown\"";
            jsonstr += ", \"target\": \"unknown\"";
            jsonstr += ", \"duration\": \"unknown\"";
            jsonstr += ", \"lastTemperatureUpdateReceived\": \"unknown\"";
            //jsonstatus.put("lastCommandDate", df.format(lastCommandDate));
            jsonstr += ", \"endDate\": \"unknown\"";
            jsonstr += ", \"actionId\": \"unknown\"";
            jsonstr += ", \"timeRange\": \"unknown\"";
            jsonstr += ", \"zoneId\": \"unknown\"";
            jsonstr += ", \"remoteSensorId\": \"unknown\"";
        }

        jsonstr += "}";

        byte[] ptext = jsonstr.getBytes(ISO_8859_1);
        String value = new String(ptext, UTF_8);
        // Attributes
        String message = "{\"Attributes\":" + value + "}";
        Core.updateHomeAssistant("homeassistant/webduinosystem/"+ id + "/attributes", message);   //send*/
    }

    public void receiveHomeAssistantCommand(String command, String message) {

        if (command.equals("temperature")) {
            double target = Double.parseDouble(message);
            SendManualCommand(target);
        } else if (command.equals("mode")) {
            if (message.equals("heat")) {
                SendManualCommand(21);
            } else if (message.equals("auto")) {

                SendAutoCommand();
                // aggiorna lo stato dello scenario ed eventualmente avvia o ferma i programmui
                for (WebduinoSystemScenario scenario : scenarios) {
                    //scenario.updateStatus();
                    scenario.stop();
                    scenario.start();
                }


            } /*else if (message.equals("off")) {
                SendOffCommand();

            }*/
        } else if (command.equals("power")) {
            if (message.equals("OFF")) {
                SendOffCommand();
            }
        } else if (command.equals("outofhome")) {
            if (status.equals(status_out_of_home.status)) {
                SendAutoCommand();
            } else {
                SendOutOfHomeCommand();
            }
        }





    }

    private void SendAutoCommand() {
        try {
            JSONObject json = new JSONObject();
            json.put("command", ACTIONCOMMAND_AUTO);
            json.put("duration", 1800);
            json.put("webduinosystemid", this.id);
            json.put("zoneid", 19);
            json.put("zonesensorid", 2);

            this.sendCommand(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void SendManualCommand(double target) {

        try {
            JSONObject json = new JSONObject();
            json.put("command", ACTIONCOMMAND_MANUAL);
            json.put("duration", 1800);
            json.put("webduinosystemid", this.id);
            json.put("zoneid", 19);
            json.put("zonesensorid", 2);
            //double target = Double.parseDouble(message);
            json.put("targetvalue", target);

            this.sendCommand(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void SendOffCommand() {
        try {
            JSONObject json = new JSONObject();
            json.put("command", ACTIONCOMMAND_SWITCHOFF);
            json.put("webduinosystemid", this.id);

            this.sendCommand(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void SendOutOfHomeCommand() {
        try {
            JSONObject json = new JSONObject();
            json.put("command", ACTIONCOMMAND_OUT_OF_HOME);
            json.put("webduinosystemid", this.id);

            this.sendCommand(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class SwitchOffCommand implements ActionCommand.Command {
        @Override
        public boolean execute(JSONObject json) {

            LOGGER.info("SwitchCommand::execute" + json.toString());
            HeaterActuator heaterActuator = getHeaterActuator();
            if (heaterActuator == null)
                return false;
            JSONObject commandjson = new JSONObject();
            try {
                commandjson.put("command", ACTIONCOMMAND_STOP_KEEPTEMPERATURE);
                //ActionCommand.Command actioncommand = heaterActuator.sendCommand(commandjson);
                if (heaterActuator.sendCommand(commandjson)) {
                    setStatus(status_off);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        public void end() {

        }

        @Override
        public JSONObject getResult() {
            return null;
        }
    }

    public class OutOfHomeCommand implements ActionCommand.Command {
        @Override
        public boolean execute(JSONObject json) {

            LOGGER.info("OutOfHomeCommand::execute" + json.toString());
            HeaterActuator heaterActuator = getHeaterActuator();
            if (heaterActuator == null)
                return false;
            JSONObject commandjson = new JSONObject();
            try {
                commandjson.put("command", ACTIONCOMMAND_STOP_KEEPTEMPERATURE);
                //ActionCommand.Command actioncommand = heaterActuator.sendCommand(commandjson);
                if (heaterActuator.sendCommand(commandjson)) {
                    setStatus(status_out_of_home);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        public void end() {

        }

        @Override
        public JSONObject getResult() {
            return null;
        }
    }


    public class AutoCommand implements ActionCommand.Command {
        @Override
        public boolean execute(JSONObject json) {

            LOGGER.info("AutoCommand::execute" + json.toString());

            HeaterActuator heaterActuator = getHeaterActuator();
            if (heaterActuator == null)
                return false;
            JSONObject commandjson = new JSONObject();
            try {
                commandjson.put("command", ACTIONCOMMAND_STOP_KEEPTEMPERATURE);
                //ActionCommand.Command actioncommand = heaterActuator.sendCommand(commandjson);
                if (heaterActuator.sendCommand(commandjson)) {
                    setStatus(status_auto);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        public void end() {

        }

        @Override
        public JSONObject getResult() {
            return null;
        }
    }

    private class ManualCommand implements ActionCommand.Command {
        JSONObject result;

        @Override
        public boolean execute(JSONObject json) {

            LOGGER.info("ManualCommand::execute" + json.toString());

            try {
                HeaterActuator heaterActuator = getHeaterActuator();
                if (heaterActuator == null)
                    return false;
                int duration = 0;
                if (json.has("duration"))
                    duration = json.getInt("duration");
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
                            JSONObject commandjson = new JSONObject();
                            try {
                                commandjson.put("command", ACTIONCOMMAND_KEEPTEMPERATURE);
                                commandjson.put("duration", duration);
                                commandjson.put("targetvalue", targetvalue);
                                commandjson.put("zoneid", zoneid);
                                commandjson.put("zonesensorid", zonesensorid);
                                //commandjson.put("mode", "manual");////
                                //ActionCommand.Command actioncommand = heaterActuator.sendCommand(commandjson);
                                if (heaterActuator.sendCommand(commandjson)) {
                                    setStatus(status_manual);
                                    //result = toJson(); //a cosa servee??? da cancellare
                                    return true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        public void end() {

        }

        @Override
        public JSONObject getResult() {
            return result;
        }
    }
}
