package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.IRActuatorCommand;
import com.server.webduino.core.sensors.commands.SensorCommand;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.server.webduino.core.webduinosystem.Status.STATUS_OFFLINE;

/**
 * Created by giaco on 18/08/2018.
 */
public class IRDevice {
    int id;
    String device;
    String description;
    //String type;
    int zoneid;
    int zonesensorid;

    protected List<ActionCommand> actionCommandList = new ArrayList<ActionCommand>();

    public IRDevice(String name, int zoneid) throws Exception {
        //this.id = id;
        //readIRDevice(name, zoneid);

        ActionCommand cmd = new ActionCommand("sendcode","Cambia canale");
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

            @Override
            public org.json.JSONObject getResult() {
                return null;
            }
        });
        actionCommandList.add(cmd);
    }

    public Boolean sendCommand(org.json.JSONObject json) {
        for (ActionCommand actionCommand : actionCommandList) {
            String cmd = null;
            try {
                cmd = json.getString("command");
                if (cmd.equals(actionCommand.command))
                    actionCommand.commandMethod.execute(json);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        json.put("device", device);
        json.put("description", description);
        //json.put("type", type);
        json.put("zoneid", zoneid);
        json.put("zonesensor", zonesensorid);

        return json;
    }

    /*
    private void readIRDevice(String aliasname, int zoneid) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
        String sql;
        Statement stmt = conn.createStatement();
        //sql = "SELECT * FROM irdevices WHERE name=\"" + name + "\" AND zoneid=" + zoneid + ";";

        sql = "SELECT *\n" +
                "FROM irdevices\n" +
                "INNER JOIN irdevicealias ON irdevices.id = irdevicealias.irdeviceid\n" +
                "WHERE irdevicealias.alias='" + aliasname + "';";

        ResultSet resultSet = stmt.executeQuery(sql);
        if (resultSet.next()) {
            this.name = resultSet.getString("name");
            this.description = resultSet.getString("description");
            this.type = resultSet.getString("type");
            this.zoneid = resultSet.getInt("zoneid");
            this.zonesensorid = resultSet.getInt("zonesensorid");
            resultSet.close();
            stmt.close();
            conn.close();
        } else {
            resultSet.close();
            stmt.close();
            conn.close();
            throw new Exception("not found");
        }
        resultSet.close();
        stmt.close();
        conn.close();
    }
    */
}
