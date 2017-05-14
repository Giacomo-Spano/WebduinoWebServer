package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.*;
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

            boolean res = false;

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
                } else {

                    ShieldCommand command = new ShieldCommand(json);
                    Core.postCommand(command.shieldId, command);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.severe("BAD REQUEST");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        JSONObject json = new JSONObject();
        try {
            json.put("result", "success");
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
        }
        return jsonResponse;
    }

    private int registerShield(JSONObject jsonObj) throws JSONException {

        Shield shield = new Shield(/*jsonObj*/);
        if (!shield.FromJson(jsonObj))
            return 0;

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
            // put some value pairs into the JSON object .
            /*try {
                jsonResponse.put("result", "success");
                //date
                Date date = Core.getDate();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jsonResponse.put("date", df.format(date));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int tzOffsetSec = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000);
                jsonResponse.put("timesec", date.getTime() / 1000 + tzOffsetSec);
                // settings
                jsonResponse.put("settings",settings);

            } catch (JSONException e) {
                e.printStackTrace();
            }*/

        } catch (JSONException e) {
            return null;
        }
        return jsonResponse;
    }

    private boolean handleSaveSettingEvent(JSONObject json) {

        return saveShieldSettings(json);
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

        if (id!=null && command.equals("settings")) {
            int shieldId = Integer.parseInt(id);
            JSONObject json = Core.getShieldFromId(shieldId).getJson();
            out.print(json.toString());

        } /*else if (id!=null && command.equals("sensors")) {
            int shieldId = Integer.parseInt(id);
            JSONObject json = Core.getShieldFromId(shieldId).get();
            out.print(json.toString());

        } */else if (command != null && id != null) {

            String json = "";
            json = handleGetJson(Integer.parseInt(id), command);

            /*if (command.equals("updatesettingstatusrequest"))
                json = handleGetJson(request, Integer.parseInt(id));
            if (command.equals("updatesensorstatusrequest"))
                json = handleGetSettings(request, Integer.parseInt(id));*/
            out.print(json.toString());

        } else {

            List<Shield> list = core.getShields();
            //create Json Object
            JSONArray jsonarray = new JSONArray();
            Iterator<Shield> iterator = list.iterator();
            while (iterator.hasNext()) {
                Shield shield = iterator.next();
                JSONObject json = shield.getJson();
                jsonarray.put(json);
            }
            out.print(jsonarray.toString());
        }

    }

    private final String updateSettingStatusRequest = "updatesettingstatusrequest";
    private final String updateSensorStatusRequest = "updatesensorstatusrequest";
    // questa classe fa una chiamata alla sheda esp tramite mqtt. Dopo aver fatto la chiamata avvia un thread di attesa
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
