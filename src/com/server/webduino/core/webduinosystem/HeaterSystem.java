package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.TemperatureSensor;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONException;
import org.json.JSONObject;

import static com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand.ACTIONCOMMAND_KEEPTEMPERATURE;
import static com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand.ACTIONCOMMAND_STOP_KEEPTEMPERATURE;

//import static com.server.webduino.core.sensors.HeaterActuator.*;

/**
 * Created by giaco on 12/05/2017.
 */
public class HeaterSystem extends com.server.webduino.core.webduinosystem.WebduinoSystem {

    public static final String STATUS_MANUAL = "manual";
    public static final String STATUS_AUTO = "auto";
    public static final String STATUS_OFF = "off";

    public static final String STATUS_DESCRIPTION_MANUAL = "Modalità manuale";
    public static final String STATUS_DESCRIPTION_AUTO = "Modalità automatica";
    public static final String STATUS_DESCRIPTION_OFF = "Off";

    Status status_auto, status_manual, status_off;

    public HeaterSystem(int id, String name, String type, boolean enabled) {
        super(id, name, type, enabled);
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
    }

    @Override
    protected void initCommandList() {
        super.initCommandList();
        ActionCommand cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_AUTO, ActionCommand.ACTIONCOMMAND_AUTO_DESCRIPTION);

        //cmd.addStatus("Stato");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {

                HeaterActuator heaterActuator = getHeaterActuator();
                if (heaterActuator == null)
                    return false;
                JSONObject commandjson = new JSONObject();
                try {
                    commandjson.put("command", ACTIONCOMMAND_STOP_KEEPTEMPERATURE);
                    boolean res = heaterActuator.sendCommand(commandjson);
                    if (res) {
                        setStatus(status_auto);
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_MANUAL, ActionCommand.ACTIONCOMMAND_MANUAL_DESCRIPTION);
        cmd.addTarget("Temperatura", 0, 30, "°C");
        cmd.addZone("Zona", TemperatureSensor.temperaturesensortype);
        cmd.addDuration("Durata");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
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
                                    boolean res = heaterActuator.sendCommand(commandjson);
                                    if (res) {
                                        setStatus(status_manual);
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
        });
        actionCommandList.add(cmd);

    }

    public HeaterActuator getHeaterActuator() {
        for (WebduinoSystemActuator actuator : actuators) {
            SensorBase sensor = Core.getSensorFromId(actuator.sensorid);
            if (sensor instanceof HeaterActuator) {
                HeaterActuator heaterActuator = (HeaterActuator) sensor;
                HeaterActuatorCommand command;
                return heaterActuator;
            }
        }
        return null;
    }
}
