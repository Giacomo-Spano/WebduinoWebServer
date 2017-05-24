package com.server.webduino.core.webduinosystem.programinstruction;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.WebduinoTrigger;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.WebduinoZone;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepTemperatureProgramInstructions extends ProgramInstructions {

    private double targetTemperature;
    private int duration = 1000;
    private int scenario;
    private int timeInterval;
    private double temperature;

    public KeepTemperatureProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId) {
        super(id, type, actuatorid, targetValue, zoneId);

        targetTemperature = targetValue;

        WebduinoZone zone = Core.getZoneFromId(zoneId);
        zone.addListener(this);
    }

    @Override
    public void onTrigger(WebduinoTrigger trigger) {
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {

        try {

            JSONObject json = new JSONObject();
            json.put("actuatorid", actuatorid);

            SensorBase sensor = Core.getSensorFromId(actuatorid);
            if (sensor != null)
                json.put("shieldid", sensor.getShieldId());
            json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
            json.put("duration", duration);
            json.put("target", targetTemperature);
            json.put("scenario", scenario);
            json.put("timeinterval", timeInterval);
            json.put("zone", zoneId);
            json.put("temperature", temperature);

            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);


            Core.postCommand(cmd);

            } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
}
