package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.IRActuatorCommand;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONException;

/**
 * Created by giaco on 02/09/2018.
 */
public class TVIRDevice extends IRDevice{

    public TVIRDevice(String name, int zoneid) throws Exception {
        super(name, zoneid);

        ActionCommand cmd = new ActionCommand("changechannel","Cambia canale");
        cmd.addParam("Canale",10);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(org.json.JSONObject json) {

                Zone zone = Core.getZoneFromId(zoneid);
                if (zone != null) {
                    ZoneSensor zoneSensor = zone.zoneSensorFromId(zonesensorid);
                    if (zoneSensor != null) {
                        SensorBase sensor = Core.getSensorFromId(zoneSensor.getSensorId());
                        if (sensor != null) {

                            if (json.has("channel")) {
                                try {
                                    String channel = json.getString("channel");
                                    IRCommand ircommand = new IRCommand(channel);
                                    IRActuatorCommand cmd = new IRActuatorCommand("send", sensor.getShieldId(), sensor.getId(), ircommand.irsequence);
                                    boolean res = cmd.send();
                                    return res;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                return false;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);
    }
}
