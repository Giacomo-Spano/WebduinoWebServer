package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.*;
import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.datalog.TemperatureSensorDataLog;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.DoorSensorCommand;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.sensors.commands.SensorCommand;
import com.server.webduino.core.webduinosystem.scenario.NextTimeRangeAction;
import com.server.webduino.core.webduinosystem.scenario.Scenarios;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */

//@WebServlet(name = "SensorServlet")
public class ShieldServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ShieldServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("SensorServlet:doPost");

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject jsonResponse;

        StringBuffer jb = new StringBuffer();
        String line;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        try {
            JSONObject json = new JSONObject(jb.toString());

            //boolean res = false;
            String error = "";

            if (json.has("event")) {
                if (json.getString("event").equals("loadsettings")) { // chiato all'avvio della shield
                    String MACAddress = json.getString("MAC");
                    jsonResponse = handleLoadSettingEvent(MACAddress);
                    out.print(jsonResponse.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                } else if (json.getString("event").equals("restart")) { // receive zonesensorstatus update
                    jsonResponse = handleRestartEvent();
                    out.print(jsonResponse.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

            } else if (json.has("command")) {

                if (json.getString("command").equals("reboot")) {
                    if (json.has("shieldid")) {
                        int id = json.getInt("shieldid");
                        Shield shield = Core.getShieldFromId(id);
                        shield.requestReboot();
                        out.print("reboot command sent");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.getString("command").equals("reset")) {
                    if (json.has("shieldid")) {
                        int id = json.getInt("shieldid");
                        Shield shield = Core.getShieldFromId(id);
                        shield.requestResetSettings();
                        out.print("reset command sent");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.getString("command").equals("updatesensorstatus")) {

                    if (json.has("shieldid")) {
                        int id = json.getInt("shieldid");
                        Shield shield = Core.getShieldFromId(id);
                        shield.requestAsyncAllSensorStatusUpdate();
                        out.print("command sent");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.getString("command").equals("send")) {

                    if (json.has("id")) {
                        int id = json.getInt("id");
                        SensorBase sensor = Core.getSensorFromId(id);
                        if (sensor != null) {


                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("command", json.getString("command"));
                                jsonObject.put("code", "aa");
                                ActionCommand.Command command = sensor.sendCommand(json.getString("command"), jsonObject);
                                if (command != null) {
                                    out.print("command sent");
                                    response.setStatus(HttpServletResponse.SC_OK);
                                } else {
                                    out.print("errore");
                                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (json.getString("command").equals("updatesensor")) {

                    if (json.has("id")) {
                        int id = json.getInt("id");
                        SensorBase sensor = Core.getSensorFromId(id);
                        if (sensor != null) {

                            //sensor.requestAsyncSensorStatusUpdate();

                            //SensorCommand cmd = new SensorCommand(SensorCommand.Command_RequestSensorStatusUpdate, sensor.getShieldId(), id);
                            //boolean res = cmd.send();
                            boolean res = sensor.requestStatusUpdate();

                            if (res) {
                                out.print("command sent");
                                response.setStatus(HttpServletResponse.SC_OK);
                            } else {
                                out.print("errore");
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                return;
                            }
                            return;
                        }
                    }
                } else if (json.getString("command").equals("manual") || json.getString("command").equals("off")) {

                    if (json.has("sensorid")) {
                        int id = json.getInt("sensorid");
                        SensorBase actuator = Core.getSensorFromId(id);
                        if (actuator instanceof HeaterActuator) {
                            try {

                                HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);

                                Date startDate = Core.getDate();
                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                cmd.date = startDate;

                                if (json.getString("command").equals("manual")) {
                                    if (json.has("duration")) {
                                        int duration = json.getInt("duration");
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(startDate);
                                        cal.add(Calendar.SECOND,duration);
                                        cmd.enddate = cal.getTime();
                                    } else if (json.has("nexttimerange") && json.getBoolean("nexttimerange") == true) {
                                        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);
                                        NextTimeRangeAction nextTimeRangeAction = core.getNextActuatorProgramTimeRange(cmd.actuatorid);
                                        if (nextTimeRangeAction != null) {
                                            Calendar cal = Calendar.getInstance();
                                            if (nextTimeRangeAction.start.isBefore(Core.getTime())) {

                                                cal.setTime(Core.getDate());
                                                cal.set(Calendar.HOUR_OF_DAY,nextTimeRangeAction.end.getHour());
                                                cal.set(Calendar.MINUTE,nextTimeRangeAction.end.getMinute());
                                                cal.set(Calendar.SECOND,nextTimeRangeAction.end.getSecond());
                                                cmd.enddate = cal.getTime();
                                            } else {
                                                //Calendar cal = Calendar.getInstance();
                                                cal.setTime(Core.getDate());
                                                cal.set(Calendar.HOUR_OF_DAY,nextTimeRangeAction.start.getHour());
                                                cal.set(Calendar.MINUTE,nextTimeRangeAction.start.getMinute());
                                                cal.set(Calendar.SECOND,nextTimeRangeAction.start.getSecond());
                                                cmd.enddate = cal.getTime();
                                            }
                                            long diffInMillies = Math.abs(cal.getTime().getTime() - Core.getDate().getTime());
                                            cmd.duration = diffInMillies/1000;
                                        } else {
                                            response.setStatus(HttpServletResponse.SC_OK);
                                            out.print("Invalid timerange");
                                            return;
                                        }

                                    } else if (json.has("endtime")) {
                                        SimpleDateFormat tf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                        String str = json.getString("endtime");
                                        Date enddate = tf.parse(str);
                                        cmd.enddate = enddate;
                                        long diffInMillies = Math.abs(enddate.getTime() - Core.getDate().getTime());
                                        cmd.duration = diffInMillies/1000;
                                    } else {
                                        response.setStatus(HttpServletResponse.SC_OK);
                                        out.print("Invalid endtime");
                                        return;
                                    }


                                    Zone zone = Core.getZoneFromId(cmd.zoneid);
                                    if (zone != null) {
                                        //cmd.temperature = zone.getTemperature();

                                    } else {
                                        //response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                        out.print("Invalid zone " + cmd.zoneid);
                                        //response.send
                                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "custom message");
                                        //response.se(HttpServletResponse.SC_BAD_REQUEST, "custom message");
                                        return;
                                    }
                                }

                                boolean res = cmd.send();
                                //SensorBase sensor = Core.getSensorFromId(cmd.sensorid);
                                if (res) {
                                    //out.print(sensor.toJson().toString());
                                    // aggiorna lòo stato del sensore in base alla risposta al comando
                                    //JSONObject jresult = new JSONObject(cmd.getResult());
                                    //actuator.updateFromJson(Core.getDate(),jresult);



                                    response.setStatus(HttpServletResponse.SC_OK);
                                    //PrintWriter out = response.getWriter();
                                    //out.print(jresult);
                                    return;
                                } else {
                                    out.print("errore");
                                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                    return;
                                }


                            } catch (Exception e) {
                                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                out.print(e.toString());
                                return;
                            }
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("Invalid sensorid");
                        return;
                    }


                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("command '" + json.getString("command") + "' not found");
                    return;
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.severe("BAD REQUEST");
            return;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JSONObject json = new JSONObject();
        try {
            json.put("result", "error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // finally output the json string
        out.print(json.toString());
    }

    private JSONObject handleErrorEvent(StringBuffer jb) {
        return null;
    }

    private JSONObject handleRestartEvent() {
        String description = "Shield restart";
        Core.sendPushNotification(SendPushMessages.notification_restarted, "Restart", description, "0", 0);
        return null;
    }


    private JSONObject handleLoadSettingEvent(String MACAddress) {

        JSONObject jsonResponse;//create Json Response Object


        try {
            LOGGER.info("SensorServlet:doPost" + MACAddress);

            if (MACAddress == null || MACAddress.equals("")) {
                jsonResponse = new JSONObject();
                jsonResponse.put("result", "error");
                return jsonResponse;
            }

            jsonResponse = loadShieldSettings(MACAddress);

        } catch (JSONException e) {
            return null;
        }
        return jsonResponse;
    }

    /*private boolean handleSaveSettingEvent(JSONObject json) {

        return saveShieldSettings(json);
    }*/

    private JSONObject loadShieldSettings(String MACAddress) {

        return Core.loadShieldSettings(MACAddress);
    }

    /*private boolean saveShieldSettings(JSONObject json) {

        return Core.saveShieldSettings(json);
    }*/

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");
        String command = request.getParameter("command");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);

        if (command.equals("settings")) {
            if (id != null) {
                int shieldId = Integer.parseInt(id);
                JSONObject json = Core.getShieldFromId(shieldId).toJson();
                out.print(json.toString());
            }
        } else if (command.equals("scenarios")) {

            JSONArray jarray = Scenarios.getScenariosJSONArray();
            out.print(jarray.toString());

        } else if (command.equals("nextactions")) {
            if (id != null) {
                JSONArray jarray = new JSONArray();
                int actuatorid = Integer.parseInt(id);
                List<NextTimeRangeAction> list = core.getNextActuatorProgramTimeRangeActionList(actuatorid);
                if (list != null) {
                    for (NextTimeRangeAction nextaction:list) {
                        jarray.put(nextaction.toJson());
                    }
                    out.print(jarray.toString());
                }
            }
        } else if (command.equals("sensorlog")) {
            if (id != null) {
                JSONArray jarray = new JSONArray();
                int sensorid = Integer.parseInt(id);
                SensorBase sensor = core.getSensorFromId(sensorid);

                Date enddate = Core.getDate();
                Calendar cal = Calendar.getInstance();
                cal.setTime(enddate);
                cal.add(Calendar.HOUR,-24);
                Date startdate = cal.getTime();
                //sensor.datalog.getDataLogValue(startdate,enddate);

                DataLog.DataLogValues values = core.getSensorDataLogList(sensorid,startdate,enddate);
                if (values != null) {

                    try {
                        out.print(values.toJson().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }  else if (command != null && id != null) { // CHIAMTA CON ATTESA RITORNO

            String json = "";
            json = handleGetJson(Integer.parseInt(id), command);
            out.print(json.toString());

        } else if (command.equals("shields")) {
            List<Shield> list = core.getShields();
            //create Json Object
            JSONArray jsonarray = new JSONArray();
            Iterator<Shield> iterator = list.iterator();
            while (iterator.hasNext()) {
                Shield shield = iterator.next();
                JSONObject json = shield.toJson();
                jsonarray.put(json);
            }
            out.print(jsonarray.toString());
        }

    }

    private final String updateSettingStatusRequest = "updatesettingstatusrequest";
    private final String updateSensorStatusRequest = "updatesensorstatusrequest";

    // questa classe fa una chiamata alla scheda esp tramite mqtt. Dopo aver fatto la chiamata avvia un thread di attesa
    // che periodicamente controlla se è stato ricevuto il risultato
    // se il risultato è ricevuto tempina il thread
    private String handleGetJson(int shieldid, String command) {

        WebduinoRequest webduinoRequest = new WebduinoRequest(shieldid, command);

        Thread thread = new Thread(webduinoRequest, "t1");
        thread.start();

        // il thread esegue la chiamata alla shield webduinoed aspetta una risposta (join) dal thead per x secondi
        try {
            thread.join(1000000); // 100000 è il timeout di attesa fien thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // recupera il risulktato della chiamamata che a questo punto è disponibile
        // forse bisiognerebbe metttere syncronized
        String json = webduinoRequest.getResultJson();
        return json;
    }

    class WebduinoRequest implements Runnable {

        private int shieldid;
        private String command;
        private volatile boolean execute; // variabile di sincronizzazione
        private String resultJson = "";

        public WebduinoRequest(int shieldid, String command) {
            this.shieldid = shieldid;
            this.command = command;
        }

        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("Thread started: " + t.getName());

            if (command.equals(updateSettingStatusRequest)) {
                Core.requestShieldSettingsUpdate(shieldid);
            } else if (command.equals(updateSensorStatusRequest)) {
                Core.requestShieldSensorsUpdate(shieldid);
            }
            // il thread si mette in attesa di aggiornamento
            // AGGIUNGERE TIMEOU
            this.execute = true;
            while (this.execute) {
                try {
                    checkStatusUpdate();
                    Thread.sleep((long) 1000);
                } catch (InterruptedException e) {
                    this.execute = false;
                }
            }
            // aggiornamento ricevuto
        }

        public String getResultJson() {

            //JSONObject result;
            if (command.equals(updateSettingStatusRequest)) {
                JSONObject result = Core.getShieldSettingJson(shieldid);
                return result.toString();
            } else if (command.equals(updateSensorStatusRequest)) {
                JSONObject result = Core.getShieldSensorsJson(shieldid);
                return result.toString();
            }
            return null;
        }

        public void checkStatusUpdate() {

            String status = "";
            if (command.equals(updateSettingStatusRequest)) {
                status = Core.getShieldSettingStatus(shieldid);
            } else if (command.equals(updateSensorStatusRequest)) {
                status = Core.getShieldSensorsStatus(shieldid);
            }
            if (status == Shield.updateStatus_updated)
                this.execute = false;
        }
    }

}
