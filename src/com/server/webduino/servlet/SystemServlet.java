package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.*;
import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.sensors.SensorFactory;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.*;
import com.server.webduino.core.webduinosystem.scenario.*;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import com.server.webduino.core.webduinosystem.scenario.actions.Condition;
//import com.server.webduino.core.webduinosystem.scenario.actions.ScenarioProgramInstructionFactory;
import com.server.webduino.core.webduinosystem.services.Service;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */


public class SystemServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SystemServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("SystemServlet:doPost");

        String data = request.getParameter("data");
        String param = request.getParameter("param");

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


        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);


        if (data != null && data.equals("googleassistant")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (json.has("device")) {
                    String device = json.getString("device");
                    String commandparam = json.getString("param");

                    GoogleAssistantParser parser = new GoogleAssistantParser();
                    parser.parseCommand(core, commandparam);

                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("");
                } else if (json.has("zone")) {
                    String zonename = json.getString("zone");
                    GoogleAssistantParser parser = new GoogleAssistantParser();
                    parser.parseZone(core, zonename);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("");
                } else if (json.has("triggerid")) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("dialogflow")) {

            try {
                JSONObject jsonrequest = new JSONObject(jb.toString());

                Dialogflow dialogflow = new Dialogflow();
                JSONObject jsonresult = dialogflow.webhook(jsonrequest);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jsonresult);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }


        } else if (data != null && data.equals("command")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (json.has("webduinosystemid")) {
                    int webduinosystemid = json.getInt("webduinosystemid");
                    WebduinoSystem webduinoSystem = core.getWebduinoSystemFromId(webduinosystemid);
                    if (webduinoSystem != null) {
                        webduinoSystem.sendCommand(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.has("sensorid")) {
                    int actuatorid = json.getInt("sensorid");
                    SensorBase sensor = core.getSensorFromId(actuatorid);
                    if (sensor != null) {
                        boolean res = sensor.sendCommand(json);
                        if (res)
                            out.print(sensor.toJson());
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else if (json.has("triggerid")) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("webduinosystemscenario")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    if (json.has("id")) {
                        JSONArray jarray = core.removeScenario(json);
                        if (jarray != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(jarray.toString());
                        }
                    }
                } else {
                    WebduinoSystemScenario scenario = core.saveScenario(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(scenario.toJson());
                }
                // questo server per aggiornare la lista di scenari nei webduinosystem
                //core.readWebduinoSystems();
                core.initScenarios();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                //response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.toString());
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("scenariotrigger")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    WebduinoSystemScenario scenario = core.removeScenarioTrigger(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(scenario.toJson());
                    return;
                } else {
                    ScenarioTrigger trigger = core.saveScenarioTrigger(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(trigger.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("webduinosystemservice")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    WebduinoSystem webduinoSystem = core.removeWebduinoSystemService(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystem.toJson());
                    return;
                } else {
                    WebduinoSystemService webduinoSystemService = core.saveWebduinoSystemService(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystemService.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("webduinosystemactuator")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    WebduinoSystem webduinoSystem = core.removeWebduinoSystemActuator(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystem.toJson());
                    return;
                } else {
                    WebduinoSystemActuator webduinoSystemActuator = core.saveWebduinoSystemActuator(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystemActuator.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("webduinosystemzone")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    WebduinoSystem webduinoSystem = core.removeWebduinoSystemZone(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystem.toJson());
                    return;
                } else {
                    WebduinoSystemZone webduinoSystemZone = core.saveWebduinoSystemZone(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystemZone.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("webduinosystem")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    WebduinoSystem webduinoSystem = core.removeWebduinoSystem(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystem.toJson());
                    return;
                } else {
                    WebduinoSystem webduinoSystem = core.saveWebduinoSystem(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(webduinoSystem.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("triggers")) {
            try {
                JSONObject json = new JSONObject(jb.toString());

                if (param != null && param.equals("delete")) {
                    Triggers triggers = core.removeTriggers(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(triggers.toJson());
                    return;
                } else {
                    Triggers triggers = core.saveTriggers(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(triggers.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("timeinterval")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    WebduinoSystemScenario scenario = core.removeScenarioTimeinterval(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(scenario.toJson());
                    return;
                } else {
                    ScenarioTimeInterval timeInterval = core.saveScenarioTimeinterval(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(timeInterval.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("program")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    if (json.has("id")) {
                        int programid = json.getInt("id");
                        WebduinoSystemScenario scenario = core.removeScenarioProgram(programid);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(scenario.toJson());
                        return;
                    }
                } else {
                    ScenarioProgram program = core.saveScenarioProgram(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(program.toJson());
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("timerange")) {

            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    ScenarioProgram program = core.removeScenarioProgramTimeRange(json);
                    if (program != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(program.toJson());
                        return;
                    }
                } else {
                    ScenarioProgramTimeRange timerange = core.saveScenarioProgramTimeRange(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(timerange.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }
        } else if (data != null && data.equals("trigger")) { // questo è diverso da scenariotrigger

            try {
                JSONObject json = new JSONObject(jb.toString());
                int id = json.getInt("id");
                if (json.has("status")) {
                    String status = json.getString("status");
                    Trigger trigger = core.getTriggerFromId(id);
                    if (trigger != null && trigger.setStatus(status)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print("trigger " + status);
                        return;
                    }
                }
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("bad command");
                return;

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }
        } else if (data != null && data.equals("condition")) {

            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    Condition condition = core.removeCondition(json);
                    if (condition != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(condition.toJson());
                        return;
                    }
                } else {
                    Condition condition = core.saveCondition(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(condition.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }
        } else if (data != null && data.equals("action")) {

            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    Action action = core.removeAction(json);
                    if (action != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(action.toJson());
                        return;
                    }
                } else if (param != null && param.equals("stop")) {
                    int id = json.getInt("id");
                    Action action = core.getActionFromId(id);
                    if (action != null) {
                        action.stop();
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                } else { // save
                    Action action = core.saveAction(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(action.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }
        } else if (data != null && data.equals("zone")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {

                    Zone zone = core.removeZone(json);
                    if (zone != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(zone.toJson());
                        return;
                    }
                } else {
                    Zone zone = core.saveZone(json);
                    if (zone != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(zone.toJson());
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }

        } else if (data != null && data.equals("sensor")) {

            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    Shield shield = removeSensor(json);
                    if (shield != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(shield.toJson());
                        return;
                    }
                } else {
                    SensorBase sensor = saveSensor(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(sensor.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }
        } else if (data != null && data.equals("shield")) {
            try {
                JSONObject json = new JSONObject(jb.toString());
                if (param != null && param.equals("delete")) {
                    Shield shield = removeShield(json);
                    if (shield != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(shield.toJson());
                        return;
                    }
                } else {
                    Shield shield = saveShield(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(shield.toJson());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(e.toString());
                return;
            }
        }

        boolean res = false;


        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        LOGGER.severe("BAD REQUEST");
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");
        String requestCommand = request.getParameter("requestcommand");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);

        if (requestCommand != null && requestCommand.equals("checkhealth")) {

            JSONObject json = new JSONObject();
            try {
                json.put("date", Core.getDate().toString());
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(json.toString());
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (requestCommand != null && requestCommand.equals("sensors")) {
            int shieldid = 0;
            if (id != null)
                shieldid = Integer.parseInt(id);

            String type = request.getParameter("type");

            JSONArray jarray = Core.getSensorsJSONArray(shieldid, type);
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("scenarios")) {

            JSONArray jarray = Scenarios.getScenariosJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("webduinosystems")) {

            try {
                JSONArray jarray = core.getWebduinoSystemJSONArray();
                if (jarray != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setHeader("Access-Control-Allow-Origin", "*");
                    /*header("Access-Control-Allow-Origin:*");
                    header('Access-Control-Allow-Methods: GET, POST, OPTIONS, DELETE, PUT');
                    header('Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization, X-Request-With, X-CLIENT-ID, X-CLIENT-SECRET');
                    header('Access-Control-Allow-Credentials: true');*/
                    out.print(jarray.toString());
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (requestCommand != null && requestCommand.equals("webduinosystem") && id != null) {
            int webduinosystemid = Integer.parseInt(id);

            WebduinoSystem system = core.getWebduinoSystemFromId(webduinosystemid);
            if (system != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Access-Control-Allow-Origin", "*");
                try {
                    out.print(system.toJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("services")) {

            JSONArray jarray = Core.getServicesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("devices")) {

            JSONArray jarray = null;
            try {
                jarray = Core.getDevicesJSONArray();
                if (jarray != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(jarray.toString());
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else if (requestCommand != null && requestCommand.equals("service") && id != null) {
            int serviceid = Integer.parseInt(id);
            Service service = core.getServiceFromId(serviceid);
            if (service != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                //try {
                out.print(service.toJson());
                /*} catch (JSONException e) {
                    e.printStackTrace();
                }*/
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("sensortypes")) {

            JSONArray jarray = SensorFactory.getSensorTypesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else /*if (requestCommand != null && requestCommand.equals("instructiontypes")) {

            JSONArray jarray = ScenarioProgramInstructionFactory.getProgramIntructionTypesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else */if (requestCommand != null && requestCommand.equals("webduinosystemtypes")) {

            JSONArray jarray = WebduinoSystemFactory.getWebduinoSystemTypesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("swversions")) {

            JSONArray jarray = SWVersion.getSWVersionJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("pins")) {

            JSONArray jarray = SensorFactory.getPinJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } /*else if (requestCommand != null && requestCommand.equals("triggertypes")) {

            JSONArray jarray = ScenarioTrigger.getTriggerTypesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } */ else if (requestCommand != null && requestCommand.equals("triggers")) {

            JSONArray jarray = Core.getTriggersJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("trigger") && id != null) {
            int triggerid = Integer.parseInt(id);
            Trigger trigger = core.getTriggerFromId(triggerid);
            if (trigger != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                try {
                    out.print(trigger.toJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("zones")) {

            JSONArray jarray = core.getZonesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("zone") && id != null) {
            int zoneid = Integer.parseInt(id);
            Zone zone = core.getZoneFromId(zoneid);
            if (zone != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(zone.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("zonesensor")) {
            String zoneidstr = request.getParameter("zoneid");
            String zonesensoridstr = request.getParameter("zonesensorid");
            if (zoneidstr != null && zonesensoridstr != null) {
                int zoneid = Integer.parseInt(zoneidstr);
                int zonesensorid = Integer.parseInt(zonesensoridstr);
                Zone zone = core.getZoneFromId(zoneid);
                if (zone != null) {
                    ZoneSensor zoneSensor = zone.zoneSensorFromId(zonesensorid);
                    if (zoneSensor != null) {

                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(zoneSensor.toJson());
                        return;

                        /*SensorBase sensor = Core.getSensorFromId(zoneSensor.getSensorId());
                        if (sensor != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(sensor.toJson());
                            return;
                        }*/
                    }
                }
            }
        } else if (requestCommand != null && requestCommand.equals("shields")) {
            List<Shield> list = core.getShields();
            //create Json Object
            JSONArray jsonarray = new JSONArray();
            Iterator<Shield> iterator = list.iterator();
            while (iterator.hasNext()) {
                Shield shield = iterator.next();
                JSONObject json = shield.toJson();
                jsonarray.put(json);
            }
            if (jsonarray != null) {
                out.print(jsonarray.toString());
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("shield") && id != null) {
            int shieldid = Integer.parseInt(id);
            Shield shield = Core.getShieldFromId(shieldid);
            if (shield != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(shield.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("webduinosystemactuator") && id != null) {
            int sensorid = Integer.parseInt(id);
            WebduinoSystemActuator webduinoSystemActuator = core.getWebduinoSystemActuatorFromId(sensorid);
            if (webduinoSystemActuator != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                try {
                    out.print(webduinoSystemActuator.toJson());
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCommand != null && requestCommand.equals("webduinosystemzone") && id != null) {
            int zoneid = Integer.parseInt(id);
            WebduinoSystemZone webduinoSystemZone = core.getWebduinoSystemZoneFromId(zoneid);
            if (webduinoSystemZone != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                try {
                    out.print(webduinoSystemZone.toJson());
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCommand != null && requestCommand.equals("webduinosystemservice") && id != null) {
            int serviceid = Integer.parseInt(id);
            WebduinoSystemService webduinoSystemService = core.getWebduinoSystemServiceFromId(serviceid);
            if (webduinoSystemService != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                try {
                    out.print(webduinoSystemService.toJson());
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCommand != null && requestCommand.equals("webduinosystemscenario") && id != null) {
            int scenarioid = Integer.parseInt(id);
            WebduinoSystemScenario scenario = Scenarios.getScenarioFromId(scenarioid);
            if (scenario != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Access-Control-Allow-Origin", "*");
                out.print(scenario.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("sensor") && id != null) {
            int sensorid = Integer.parseInt(id);

            SensorBase sensor = core.getSensorFromId(sensorid);
            if (sensor != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(sensor.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("program") && id != null) {
            int programid = Integer.parseInt(id);
            ScenarioProgram program = Scenarios.getScenarioProgramFromId(programid);
            if (program != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(program.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("timerange") && id != null) {
            int timerangeid = Integer.parseInt(id);
            ScenarioProgramTimeRange timerange = Scenarios.getScenarioProgramTimeRangeFromId(timerangeid);
            if (timerange != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(timerange.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("nextprograms") && id != null) {
            List<NextTimeRangeAction> list = core.getNextTimeRangeActions(/*scenarioprogramid*/);
            if (list != null) {
                JSONArray jarray = new JSONArray();
                for (NextTimeRangeAction timeRange : list) {
                    jarray.put(timeRange.toJson());
                }
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("commandlog")) {
            if (id != null) {
                JSONArray jarray = new JSONArray();
                int actuatorid = Integer.parseInt(id);
                List<DataLog> list = core.getCommandDatalogs(actuatorid, null, null);
                if (list != null) {
                    for (DataLog dataLog : list) {
                        try {
                            jarray.put(dataLog.toJson());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(jarray.toString());
                    return;
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    public SensorBase saveSensor(JSONObject json) throws Exception {
        SensorFactory factory = new SensorFactory();
        SensorBase sensor = factory.fromJson(json);
        sensor.save();
        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);
        core.init();
        return sensor;
    }

    public Shield removeSensor(JSONObject json) throws Exception {
        SensorFactory factory = new SensorFactory();
        SensorBase sensor = factory.fromJson(json);
        int sensorid = sensor.getId();
        sensor.remove();
        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);
        core.init();
        Shield shield = core.getShieldFromId(sensorid);
        return shield;
    }

    public Shield saveShield(JSONObject json) throws Exception {
        Shield shield = new Shield(json);
        shield.save();
        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);
        core.init();
        return shield;
    }

    public Shield removeShield(JSONObject json) throws Exception {
        SensorFactory factory = new SensorFactory();
        SensorBase sensor = factory.fromJson(json);
        int sensorid = sensor.getId();
        sensor.remove();
        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);
        core.init();
        Shield shield = core.getShieldFromId(sensorid);
        return shield;
    }
}
