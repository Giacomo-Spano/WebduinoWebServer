package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.scenario.Conflict;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepTemperatureProgramAction extends ProgramAction /*implements SensorBase.SensorListener*/ /*implements Command.CommandListener*/ {

    private double targetTemperature;
    private long duration = 1000;
    private double temperature;
    private Date lastSent = null;
    private String lastSentResult = "";

    private HeaterListenerClass heaterListener = new HeaterListenerClass();

    public KeepTemperatureProgramAction(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                        int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid, type, name, description, priority, actuatorid, targevalue, thresholdvalue,
                zoneId, seconds, enabled);

        targetTemperature = targetvalue;
    }

    @Override
    public void start() {
        super.start();
        requestZoneStatusUpdate();

        SensorBase sensor = Core.getSensorFromId(actuatorid);
        if (sensor != null) {
            if (sensor instanceof HeaterActuator) {
                HeaterActuator heater = (HeaterActuator) sensor;
                heater.addListener(heaterListener);
            }
        } else {

        }

    }

    private void requestZoneStatusUpdate() {
        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            zone.addListener(this);
            zone.requestSensorStatusUpdate();
        }
    }

    @Override
    public void stop() {
        super.stop();
        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            zone.removeListener(this);
        }

        SensorBase sensor = Core.getSensorFromId(actuatorid);
        if (sensor != null) {
            sensor.deleteListener(heaterListener);
        }
    }

    @Override
    public void addConflict(Conflict newconflict) {

        // controlla che non ci sia già nella lista altrimenti
        for (Conflict conflict: conflictList) {
            if (conflict.action.id == newconflict.action.id) {
                return;
            }
        }

        // se la action ha lo stesso actuator aggiunge il conflitto
        if (newconflict.action.actuatorid == this.actuatorid) {
            if (newconflict.action instanceof KeepTemperatureProgramAction || newconflict.action instanceof KeepOffProgramActions ) {
                conflictList.add(newconflict);
            }
        }
    }

    @Override
    public void finalize() {
        stop();
        System.out.println("Book instance is getting destroyed");
    }

    @Override
    public String getStatus() {
        String status = "";
        status += "id:" + id + " ";

        status += " enabled:";
        if (enabled)
            status += "true ";
        else
            status += "false ";

        status += " active:";
        if (active) {
            if (conflictList.size() > 0)
                status += "conflict ";
            else
                status += "true ";
        } else {
            status += "false ";
        }

        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            status += "Zona: " + zoneId + "." + zone.getName() + " - Temperatura: " + zone.getTemperature() + " °C";
            status += " - Target: " + targetTemperature + " °C";
        } else {
            status += zone + "not found";
        }

        status += " - Actuator: ";
        SensorBase sensor = Core.getSensorFromId(actuatorid);
        if (sensor != null && sensor instanceof HeaterActuator) {

            status += sensor.getId() + "." + sensor.getName();
            status += " - Status: " + ((HeaterActuator) sensor).getStatus();

            status += " - Relè: ";
            if (((HeaterActuator)sensor).getReleStatus())
                status += "acceso";
            else
                status += "spento";
        } else {
            status += actuatorid + " not found";
        }

        if (lastSent != null) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            status += " - lastSent: " + df.format(lastSent) + " result: " + lastSentResult;
        } else {
            status += " - lastSent: invalid";
        }

        status += " - conflicts: ";
        if (conflictList.size() > 0) {
            for (Conflict conflict: conflictList) {
                status += conflict.action.id + "." + conflict.action.description;
            }
        }

        return status;
    }

    @Override
    public void onUpdateTemperature(int zoneId, double newTemperature, double oldTemperature) {

        if (!active)
            return;

        if (newTemperature == temperature)
            return;
        temperature = newTemperature;


        if (conflictList.size() == 0) {

            duration = (endDate.getTime() - Core.getDate().getTime()) / 1000;  // durata in secondi
            if (duration <= 0)
                return;

            // crea un thread separato per inviare il comando Command_KeepTemperature
            // altrrimenti non si riceve la risposta
            sendCommandThread commandThread = new sendCommandThread();
            commandThread.start();
        }
    }

    public class sendCommandThread extends Thread {

        public void run(){
            System.out.println("sendCommandThread running");

            try {
                Command.CommandResult result;
                JSONObject json = new JSONObject();
                json.put("actuatorid", actuatorid);

                SensorBase sensor = Core.getSensorFromId(actuatorid);
                if (sensor != null)
                    json.put("shieldid", sensor.getShieldId());
                json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
                json.put("duration", duration);
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                json.put("enddate", df.format(endDate));
                json.put("target", targetTemperature);
                json.put("actionid", id);
                json.put("date", df.format(Core.getDate()));
                json.put("zone", zoneId);
                json.put("temperature", temperature);


                HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
                result = cmd.send();
                SensorBase s = Core.getSensorFromId(cmd.actuatorid);
                if (result.success && sensor != null) {
                    System.out.println(s.toJson().toString());

                    lastSent = Core.getDate();
                    lastSentResult = "successful";


                    JSONObject jsonObject = new JSONObject(result.result);
                    if (jsonObject.has("shieldid"))
                        Core.updateShieldStatus(jsonObject.getInt("shieldid"), jsonObject);


                    return;
                } else {
                    System.out.println("errore");lastSent = Core.getDate();
                    lastSentResult = "error";
                    return;
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class HeaterListenerClass implements SensorBase.SensorListener {

        @Override
        public void changeOnlineStatus(boolean online) {

        }

        @Override
        public void changeOnlineStatus(int sensorId, boolean online) {

        }

        @Override
        public void onChangeStatus(String newStatus, String oldStatus) {
            if (newStatus.equals(HeaterActuator.STATUS_MANUAL) || newStatus.equals(HeaterActuator.STATUS_KEEPTEMPERATURE)) {
                requestZoneStatusUpdate();
            }
        }

        @Override
        public void changeDoorStatus(int sensorId, boolean open, boolean oldOpen) {

        }
    }
}
