package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.*;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.DoorSensorCommand;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.webduinosystem.scenario.Scenarios;
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

            if (json.has("event")) { // DA ELIMINARE FINO A ELSE
                if (json.getString("event").equals("register")) { // receive status update
                    if (json.has("shield")) {
                        JSONObject jsonShield = json.getJSONObject("shield");
                        jsonResponse = handleRegisterEvent(jsonShield);
                        out.print(jsonResponse.toString());
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    return;
                } else if (json.getString("event").equals("loadsettings")) { // receive status update
                    String MACAddress = json.getString("MAC");
                    jsonResponse = handleLoadSettingEvent(MACAddress);
                    out.print(jsonResponse.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                } else if (json.getString("event").equals("restart")) { // receive status update
                    jsonResponse = handleRestartEvent();
                    out.print(jsonResponse.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

            } else if (json.has("command")) {

                if (json.getString("command").equals("saveshieldsettings")) {
                    handleSaveSettingEvent(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                } /*else if (json.getString("command").equals("reboot") && json.has("shieldid")) {
                    ShieldCommand cmd = new ShieldCommand(json);
                    Command.CommandResult result = cmd.send();
                    if (result.success) {
                        out.print(result);
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        out.print("errore");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }

                } */else if (json.getString("command").equals("reboot")) {

                    if (json.has("shieldid")) {
                        int id = json.getInt("shieldid");
                        Shield shield = Core.getShieldFromId(id);
                        shield.requestReboot();
                        out.print("command sent");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.getString("command").equals("teststart") || json.getString("command").equals("teststop")
                        || json.getString("command").equals("testopen") || json.getString("command").equals("testclose")) {

                    if (json.has("actuatorid")) {
                        int id = json.getInt("actuatorid");
                        SensorBase actuator = Core.getSensorFromId(id);

                        //DoorSensorCommand cmd = new DoorSensorCommand(json);
                        DoorSensorCommand cmd = new DoorSensorCommand(json.getString("command"), actuator.getShieldId(), id, "close");
                        Command.CommandResult result = cmd.send();
                        if (result.success) {
                            out.print(result);
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            out.print("errore");
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        }
                        return;
                    }
                } else if (json.getString("command").equals("updatesensorstatus")) {

                    if (json.has("shieldid")) {
                        int id = json.getInt("shieldid");
                        Shield shield = Core.getShieldFromId(id);
                        shield.requestSensorStatusUpdate();
                        /*ShieldCommand cmd = new ShieldCommand(json);
                        Command.CommandResult result = cmd.send();
                        if (result.success) {
                            out.print(result);
                            response.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            out.print("errore");
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        }*/
                        out.print("command sent");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.getString("command").equals("manual") || json.getString("command").equals("off")) {

                    if (json.has("actuatorid")) {
                        int id = json.getInt("actuatorid");
                        SensorBase actuator = Core.getSensorFromId(id);
                        if (actuator instanceof HeaterActuator) {
                            try {

                                HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);

                                Date startDate = Core.getDate();
                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                cmd.date = df.format(startDate);

                                if (json.getString("command").equals("manual") && json.has("duration")) {
                                    int duration = json.getInt("duration");
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(startDate);
                                    cal.add(Calendar.SECOND,duration);
                                    cmd.enddate = df.format(cal.getTime());

                                    Zone zone = Core.getZoneFromId(cmd.zone);
                                    if (zone != null) {
                                        cmd.temperature = zone.getTemperature();

                                    } else {
                                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                        out.print("Invalid zone " + cmd.zone);
                                        return;
                                    }
                                }


                                Command.CommandResult result = cmd.send();
                                //SensorBase sensor = Core.getSensorFromId(cmd.actuatorid);
                                if (result.success /*&& sensor != null*/) {
                                    //out.print(sensor.toJson().toString());
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    //PrintWriter out = response.getWriter();
                                    out.print(result.result);
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
                        out.print("Invalid actuatorid");
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

    private JSONObject handleRegisterEvent(JSONObject jsonObj) {

        JSONObject jsonResponse;//create Json Response Object
        jsonResponse = new JSONObject();

        try {
            //JSONObject jsonObj = new JSONObject(jb.toString());

            LOGGER.info("SensorServlet:doPost" + jsonObj.toString());

            int id = registerShield(jsonObj);
            // put some value pairs into the JSON object .
            try {
                jsonResponse.put("result", "success");
                jsonResponse.put("id", id);

                Date date = Core.getDate();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jsonResponse.put("date", df.format(date));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int tzOffsetSec = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000);
                jsonResponse.put("timesec", date.getTime() / 1000 + tzOffsetSec);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            try {
                jsonResponse.put("result", "error");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                jsonResponse.put("result", "error");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return jsonResponse;
    }

    private int registerShield(JSONObject jsonObj) throws Exception {

        Shield shield = new Shield(jsonObj);

        int shieldid = Core.registerShield(shield);
        return shieldid;
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

    private boolean handleSaveSettingEvent(JSONObject json) {

        return saveShieldSettings(json);
    }

    private boolean sendRestartCommand(JSONObject json) {
        return Core.sendRestartCommand(json);
    }

    private JSONObject loadShieldSettings(String MACAddress) {

        return Core.loadShieldSettings(MACAddress);
    }

    private boolean saveShieldSettings(JSONObject json) {

        return Core.saveShieldSettings(json);
    }

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

        } else if (command != null && id != null) { // CHIAMTA CON ATTESA RITORNO

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
