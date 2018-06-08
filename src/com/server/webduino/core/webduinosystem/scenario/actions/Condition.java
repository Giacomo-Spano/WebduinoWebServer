package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Core;
import com.server.webduino.core.Trigger;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.SensorListenerClass;
import com.server.webduino.core.webduinosystem.Status;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giaco on 17/05/2017.
 */
public class Condition extends SensorListenerClass {

    public int id = 0;
    public int timerangeid = 0;
    public int zoneid;
    public int zonesensorid;
    public int triggerid = 0;
    public String sensorstatus = "";
    public String triggerstatus = "";
    public String type;
    public String valueoperator;
    public double value;
    public String[] valueoperators = {">", "<", "="};

    private boolean active = false;

    Zone zone = null;
    ZoneSensor zoneSensor = null;
    SensorBase sensor = null;
    Trigger trigger = null;

    protected List<ConditionListener> listeners = new ArrayList<>();

    public interface ConditionListener {
        void onActiveChange(boolean active);
    }

    public void addListener(ConditionListener toAdd) {
        listeners.add(toAdd);
    }

    public void deleteListener(ConditionListener toRemove) {
        listeners.remove(toRemove);
    }

    public void start() {
        active = false;

        if (type.equals("zonesensorvalue")) {
            handleZoneSensorValue();
        } else if (type.equals("zonesensorstatus")) {
            handleZoneSensorStatus();
        } else if (type.equals("triggerstatus")) {
            handleTriggerStatus();
        }
    }

    public void stop() {

        if (sensor != null)
            sensor.removeListener(this);
        active = false;
    }

    public String getStatus() {
        if (active)
            return "active";
        return "not active";
    }


    private void handleTriggerStatus() {
        trigger = Core.getTriggerFromId(triggerid);
        if (trigger != null) {
            trigger.addListener(new Trigger.TriggerListener() {
                @Override
                public void onChangeStatus(boolean status) {

                }
            });
        }
    }

    private void handleZoneSensorStatus() {
        zone = Core.getZoneFromId(zoneid);
        if (zone != null) {

            zoneSensor = zone.zoneSensorFromId(zonesensorid);
            if (zoneSensor != null) {
                sensor = Core.getSensorFromId(zoneSensor.getSensorId());
                if (sensor != null) {
                    sensor.addListener(new SensorBase.SensorListener() {

                        @Override
                        public void onChangeValue(SensorBase sensor, double val) {

                        }

                        @Override
                        public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus) {

                            boolean oldactive = active;
                            zone = Core.getZoneFromId(zoneid);
                            if (zone != null) {
                                zoneSensor = zone.zoneSensorFromId(zonesensorid);
                                if (zoneSensor != null) {
                                    sensor = Core.getSensorFromId(zoneSensor.getSensorId());
                                    if (sensor != null && sensor.getId() == zoneSensor.getSensorId()) {
                                        if (newStatus.status.equals(sensorstatus))
                                            active = true;
                                        else
                                            active = false;
                                    }
                                }
                            }
                            if (oldactive != active) {
                                for (ConditionListener listener : listeners) {
                                    listener.onActiveChange(active);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void handleZoneSensorValue() {
        zone = Core.getZoneFromId(zoneid);
        if (zone != null) {
            zoneSensor = zone.zoneSensorFromId(zonesensorid);
            if (zoneSensor != null) {
                sensor = Core.getSensorFromId(zoneSensor.getSensorId());
                if (sensor != null) {
                    sensor.addListener(new SensorBase.SensorListener() {

                        @Override
                        public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus) {
                        }

                        @Override
                        public void onChangeValue(SensorBase sensor, double val) {
                            boolean oldactive = active;

                            if (valueoperator.equals(">")) {
                                if (val > value)
                                    active = true;
                                else
                                    active = false;
                            } else if (valueoperator.equals("<")) {
                                if (val < value)
                                    active = true;
                                else
                                    active = false;
                            } else if (valueoperator.equals("=")) {
                                if (val == value)
                                    active = true;
                                else
                                    active = false;
                            } else {
                                active = false;
                            }

                            if (oldactive != active) {
                                for (ConditionListener listener : listeners) {
                                    listener.onActiveChange(active);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onChangeValue(SensorBase sensor, double value) {

    }


    public boolean isActive() {
        return active;
    }


    public Condition(JSONObject json) throws Exception {
        fromJson(json);
    }

    public Condition(Connection conn, ResultSet resultSet) throws Exception {
        fromResultSet(conn, resultSet);
    }

    public void fromJson(JSONObject json) throws Exception {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("timerangeid")) timerangeid = json.getInt("timerangeid");
        if (json.has("zoneid")) zoneid = json.getInt("zoneid");
        if (json.has("zonesensorid")) zonesensorid = json.getInt("zonesensorid");
        if (json.has("triggerid")) triggerid = json.getInt("triggerid");
        if (json.has("sensorstatus")) sensorstatus = json.getString("sensorstatus");
        if (json.has("triggerstatus")) triggerstatus = json.getString("triggerstatus");
        if (json.has("type")) type = json.getString("type");
        if (json.has("value")) value = json.getInt("value");
        if (json.has("valueoperator")) valueoperator = json.getString("valueoperator");
    }

    public void fromResultSet(Connection conn, ResultSet resultSet) throws Exception {
        id = resultSet.getInt("id");
        timerangeid = resultSet.getInt("timerangeid");
        zoneid = resultSet.getInt("zoneid");
        zonesensorid = resultSet.getInt("zonesensorid");
        triggerid = resultSet.getInt("triggerid");
        sensorstatus = resultSet.getString("sensorstatus");
        triggerstatus = resultSet.getString("triggerstatus");
        type = resultSet.getString("type");
        value = resultSet.getDouble("value");
        valueoperator = resultSet.getString("valueoperator");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("zoneid", zoneid);
            json.put("timerangeid", timerangeid);
            json.put("zonesensorid", zonesensorid);
            json.put("triggerid", triggerid);
            json.put("sensorstatus", sensorstatus);
            json.put("triggerstatus", triggerstatus);
            json.put("type", type);
            json.put("value", value);
            json.put("valueoperator", valueoperator);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < valueoperators.length; i++) {
                jsonArray.put(valueoperators[i]);
            }
            json.put("valueoperators", jsonArray);

            json.put("status", getStatus());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void save() throws Exception {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            delete(stmt);
            write(conn);
            stmt.close();
            conn.commit();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        }
    }

    public void remove() throws Exception {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            delete(stmt);
            stmt.close();
            conn.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        }
    }


    public void delete() throws Exception {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            delete(stmt);
            stmt.close();
            conn.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        }
    }

    public void write(Connection conn) throws SQLException {
        String zoneidstr = "null";
        if (zoneid > 0)
            zoneidstr = "" + zoneid;
        String zonesensoridstr = "null";
        if (zonesensorid > 0)
            zonesensoridstr = "" + zonesensorid;
        String triggerstr = "null";
        if (triggerid > 0)
            triggerstr = "" + triggerid;

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sql = "INSERT INTO scenarios_conditions (id, timerangeid, zoneid, zonesensorid, triggerid, sensorstatus,triggerstatus,type,value,valueoperator)" +
                " VALUES ("
                + id + ","
                + timerangeid + ","
                + zoneidstr + ","
                + zonesensoridstr + ","
                + triggerstr + ","
                + "\"" + sensorstatus + "\","
                + "\"" + triggerstatus + "\","
                + "\"" + type + "\","
                + value + ","
                + "\"" + valueoperator + "\""
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "timerangeid=" + timerangeid + ","
                + "zoneid=" + zoneidstr + ","
                + "zonesensorid=" + zonesensoridstr + ","
                + "triggerid=" + triggerstr + ","
                + "sensorstatus=\"" + sensorstatus + "\","
                + "triggerstatus=\"" + triggerstatus + "\","
                + "type=\"" + type + "\","
                + "value=" + value + ","
                + "valueoperator=\"" + valueoperator + "\""
                + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
    }

    public void delete(Statement stmt) throws SQLException {

        String sql = "DELETE FROM scenarios_conditions WHERE id=" + id;
        stmt.executeUpdate(sql);

    }
}
