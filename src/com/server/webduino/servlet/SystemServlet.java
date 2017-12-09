package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.*;
import com.server.webduino.core.sensors.SensorFactory;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.scenario.*;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramActionFactory;
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

        try {
            JSONObject json = new JSONObject(jb.toString());
            if (data != null && data.equals("scenario")) {
                try {
                    if (param != null && param.equals("delete")) {
                        JSONArray jarray = Core.removeScenario(json);
                        if (jarray != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(jarray.toString());
                            return;
                        }
                    } else {
                        Scenario scenario = Core.saveScenario(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(scenario.toJson());
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(e.toString());
                    return;
                }

            } else if (data != null && data.equals("scenariotrigger")) {
                try {
                    if (param != null && param.equals("delete")) {
                        Scenario scenario = Core.removeScenarioTrigger(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(scenario.toJson());
                        return;
                    } else {
                        ScenarioTrigger trigger = Core.saveScenarioTrigger(json);
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

            } else if (data != null && data.equals("triggers")) {
                try {
                    if (param != null && param.equals("delete")) {
                        Triggers triggers = Core.removeTriggers(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(triggers.toJson());
                        return;
                    } else {
                        Triggers triggers = Core.saveTriggers(json);
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
                    if (param != null && param.equals("delete")) {
                        Scenario scenario = Core.removeScenarioTimeinterval(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(scenario.toJson());
                        return;
                    } else {
                        ScenarioTimeInterval timeInterval = Core.saveScenarioTimeinterval(json);
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
                    if (param != null && param.equals("delete")) {
                        Scenario scenario = Core.removeScenarioProgram(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(scenario.toJson());
                        return;
                    } else {
                        ScenarioProgram program = Core.saveScenarioProgram(json);
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
                    if (param != null && param.equals("delete")) {
                        ScenarioProgram program = Core.removeScenarioProgramTimeRange(json);
                        if (program != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(program.toJson());
                            return;
                        }
                    } else {
                        ScenarioProgramTimeRange timerange = Core.saveScenarioProgramTimeRange(json);
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
            } else if (data != null && data.equals("trigger")) {

                try {
                    int id = json.getInt("id");
                    if (json.getString("status").equals("enabled")) {
                        Core.enableTrigger(id, true);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print("trigger enabled");
                        return;
                    } else if (json.getString("status").equals("disabled")) {
                        Core.enableTrigger(id, false);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print("trigger disabled");
                        return;
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
            } else if (data != null && data.equals("instruction")) {

                try {
                    if (param != null && param.equals("delete")) {
                        ScenarioProgramTimeRange timeRange = Core.removeScenarioProgramTimeRangeInstruction(json);
                        if (timeRange != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(timeRange.toJson());
                            return;
                        }
                    } else {
                        ProgramAction instruction = Core.saveScenarioProgramTimeRangeInstruction(json);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(instruction.toJson());
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
                    if (param != null && param.equals("delete")) {

                        Zone zone = Core.removeZone(json);
                        if (zone != null) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(zone.toJson());
                            return;
                        }
                    } else {
                        Zone zone = Core.saveZone(json);
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


        } catch (
                JSONException e)

        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.severe("BAD REQUEST");
            return;
        }

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

        if (requestCommand != null && requestCommand.equals("sensors")) {
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

        } else if (requestCommand != null && requestCommand.equals("sensortypes")) {

            JSONArray jarray = SensorFactory.getSensorTypesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("instructiontypes")) {

            JSONArray jarray = ProgramActionFactory.getProgramIntructionTypesJSONArray();
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

        } else if (requestCommand != null && requestCommand.equals("triggertypes")) {

            JSONArray jarray = ScenarioTrigger.getTriggerTypesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("triggers")) {

            JSONArray jarray = Core.getTriggersJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } else if (requestCommand != null && requestCommand.equals("zones")) {

            JSONArray jarray = Core.getZonesJSONArray();
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        } /*else if (requestCommand != null && requestCommand.equals("temperaturesensors")) {


            List<SensorBase> list = Shields.getTemperatureSensorList();
            JSONArray jarray = new JSONArray();
            for(SensorBase sensor : list) {
                jarray.put(sensor.toJson());
            }
            if (jarray != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(jarray.toString());
                return;
            }

        }*/ else if (requestCommand != null && requestCommand.equals("zone") && id != null) {
            int zoneid = Integer.parseInt(id);
            Zone zone = Core.getZoneFromId(zoneid);
            if (zone != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(zone.toJson());
                return;
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
        } else if (requestCommand != null && requestCommand.equals("scenario") && id != null) {
            int scenarioid = Integer.parseInt(id);
            Scenario scenario = Scenarios.getScenarioFromId(scenarioid);
            if (scenario != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(scenario.toJson());
                return;
            }
        } else if (requestCommand != null && requestCommand.equals("sensor") && id != null) {
            int sensorid = Integer.parseInt(id);
            SensorBase sensor = Shields.getSensorFromId(sensorid);
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
        } else if (requestCommand != null && requestCommand.equals("instructions") && id != null) {
            String scenarioid = request.getParameter("scenarioid");
            JSONArray jsonArray = new JSONArray();
            if (scenarioid != null) {
                int sid = Integer.parseInt(scenarioid);
            }
            if (jsonArray != null)
                out.print(jsonArray.toString());
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
