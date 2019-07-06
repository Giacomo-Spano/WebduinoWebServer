package com.server.webduino.core;

import com.mysql.fabric.xmlrpc.base.Data;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.webduinosystem.HeaterSystem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by giaco on 02/09/2018.
 */
public class Dialogflow {

    public JSONObject webhook(JSONObject jsonrequest) {
        try {
            JSONObject jsonresult = new JSONObject();

            // read session
            String session = null;
            if (jsonrequest.has("session")) {
                session = jsonrequest.getString("session");
            }

            // read parameters, action and outputContexts
            JSONObject queryResultJson = null;
            JSONObject parametersJson = null;
            JSONArray outputContextsJsonarray = null;
            String action = "";
            String languageCode;

            JSONObject intent;
            String intentDisplayname = "", intentName = "";

            if (jsonrequest.has("queryResult")) {
                queryResultJson = jsonrequest.getJSONObject("queryResult");
                if (queryResultJson.has("intent")) {
                    parametersJson = queryResultJson.getJSONObject("parameters");
                }

                if (queryResultJson.has("parameters")) {
                    intent = queryResultJson.getJSONObject("intent");
                    if (intent != null && intent.has("displayName"))
                        intentDisplayname = intent.getString("displayName");
                    if (intent != null && intent.has("name"))
                        intentName = intent.getString("name");
                }
                if (queryResultJson.has("action")) {
                    action = queryResultJson.getString("action");
                }
                if (queryResultJson.has("outputContexts")) {
                    outputContextsJsonarray = queryResultJson.getJSONArray("outputContexts");
                }
                if (queryResultJson.has("languageCode")) {
                    languageCode = queryResultJson.getString("languageCode");
                }

            }

            //jsonresult.put("fulfillmentText", "risposta");
            //JSONObject followupEventInputJson = new JSONObject();
            //followupEventInputJson.put("name", "commandcompletedevent");
            //jsonresult.put("followupEventInput", followupEventInputJson);
            if (intentDisplayname.equalsIgnoreCase("riscaldamento")) {
                return handleRiscaldamentoIntent(session, parametersJson, outputContextsJsonarray,action);
                /*if (action.equalsIgnoreCase("status")) {

                }*/

            } else {
                if (action.equalsIgnoreCase("controldevice")) {

                    return handleControlDeviceIntent(session, parametersJson, outputContextsJsonarray);

                } else if (action.equalsIgnoreCase("commandtelevisione")) {

                    return handleCommandTelevisioneIntent(session, parametersJson, outputContextsJsonarray);

                } else if (action.equalsIgnoreCase("commandariacondizionata")) {

                    return handleCommandAriaCondizionataIntent(session, parametersJson, outputContextsJsonarray);
                }
            }


            return jsonresult;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    JSONObject handleRiscaldamentoIntent(String session, JSONObject parametersJson, JSONArray outputContextsJsonarray, String action) throws JSONException {

        GoogleAssistantParser parser = new GoogleAssistantParser();
        HeaterSystem heatersystem = parser.getHeater();
        HeaterActuator heater = heatersystem.getHeaterActuator();

        if (action != null && action.equalsIgnoreCase("status")) {


            parametersJson.put("temperature", heater.getTemperature());
            parametersJson.put("target", heater.getTargetTemperature());
            if (heater.getReleStatus())
                parametersJson.put("relestatus", "Acceso");
            else
                parametersJson.put("relestatus", "Spento");

            Date endDate = heater.getEndDate();
            SimpleDateFormat df = new SimpleDateFormat(/*"yyyy-MM-dd HH:mm:ss"*/"HH:mm");
            String endtime = df.format(endDate);
            parametersJson.put("endtime", endtime);


            JSONObject followupEventInputJson = new JSONObject();
            followupEventInputJson.put("name", "riscaldamento-event");
            followupEventInputJson.put("parameters", parametersJson);
            JSONObject jsonresult = new JSONObject();
            jsonresult.put("followupEventInput", followupEventInputJson);



            JSONObject devicecontexJson = new JSONObject();
            devicecontexJson.put("name", session + "/contexts/" + "riscaldamento-followup");
            devicecontexJson.put("lifespanCount", 5);
            devicecontexJson.put("parameters", parametersJson);
            if (outputContextsJsonarray == null) {
                outputContextsJsonarray = new JSONArray();
            }
            outputContextsJsonarray.put(devicecontexJson);
            jsonresult.put("outputContexts", outputContextsJsonarray);
            return jsonresult;
        } else {
            JSONObject jsonresult = new JSONObject();
            jsonresult.put("fulfillmentText", "comando riuscaldamento non riconosciuto");
            return jsonresult;
        }
        //return null;
    }

    JSONObject handleControlDeviceIntent(String session, JSONObject parametersJson, JSONArray outputContextsJsonarray) throws JSONException {

        if (parametersJson != null && parametersJson.has("device")
                && parametersJson.has("location")) {

            String device = parametersJson.getString("device");
            String location = parametersJson.getString("location");
            GoogleAssistantParser parser = new GoogleAssistantParser();
            IRDevice irdevice = parser.IRDeviceFromNameAndZone(device, location);

            if (irdevice != null) {
                JSONObject followupEventInputJson = new JSONObject();
                followupEventInputJson.put("name", device + "commandevent");
                followupEventInputJson.put("parameters", parametersJson);
                JSONObject jsonresult = new JSONObject();
                jsonresult.put("followupEventInput", followupEventInputJson);

                JSONObject devicecontexJson = new JSONObject();
                devicecontexJson.put("name", session + "/contexts/" + device);
                devicecontexJson.put("lifespanCount", 5);
                devicecontexJson.put("parameters", parametersJson);
                if (outputContextsJsonarray == null) {
                    outputContextsJsonarray = new JSONArray();
                }
                outputContextsJsonarray.put(devicecontexJson);
                jsonresult.put("outputContexts", outputContextsJsonarray);
                return jsonresult;
            } else {
                JSONObject jsonresult = new JSONObject();
                jsonresult.put("fulfillmentText", "Dispositivo non riconosciuto");
            }
        }
        return null;
    }

    JSONObject handleCommandTelevisioneIntent(String session, JSONObject parametersJson, JSONArray outputContextsJsonarray) throws JSONException {

        JSONObject televisioneContextParameter = null;
        for (int i = 0; i < outputContextsJsonarray.length(); i++) {
            JSONObject jsonObject = outputContextsJsonarray.getJSONObject(i);
            String name = jsonObject.getString("name");
            if (name.equalsIgnoreCase(session + "/contexts/televisione"))
                televisioneContextParameter = jsonObject.getJSONObject("parameters");
        }

        JSONObject jsonresult = new JSONObject();

        if (parametersJson.has("channel") && televisioneContextParameter != null
                && televisioneContextParameter.has("device")
                && televisioneContextParameter.has("location")) {


            String device = televisioneContextParameter.getString("device");
            String location = televisioneContextParameter.getString("location");
            GoogleAssistantParser parser = new GoogleAssistantParser();
            IRDevice irdevice = parser.IRDeviceFromNameAndZone(device, location);
            String channel = parametersJson.getString("channel");
            if (irdevice != null && !channel.equalsIgnoreCase("")) {

                JSONObject json = new JSONObject();
                json.put("command", "changechannel");
                json.put("channel", channel);
                irdevice.sendCommand(json);

                JSONObject followupEventInputJson = new JSONObject();
                followupEventInputJson.put("name", "channelchanged");
                followupEventInputJson.put("parameters", parametersJson);
                jsonresult.put("followupEventInput", followupEventInputJson);
                return jsonresult;
            }
        } else if (parametersJson.has("channelnumber")) {
            int channelnumber = parametersJson.getInt("channelnumber");
            if (channelnumber >= 0) {
                JSONObject followupEventInputJson = new JSONObject();
                followupEventInputJson.put("name", "channelchanged");
                jsonresult.put("followupEventInput", followupEventInputJson);
                return jsonresult;
            }
        } else if (parametersJson.has("command")) {
            String command = parametersJson.getString("command");
            if (!command.equalsIgnoreCase("")) {
                JSONObject followupEventInputJson = new JSONObject();
                followupEventInputJson.put("name", "channelchanged");
                jsonresult.put("followupEventInput", followupEventInputJson);
                return jsonresult;
            }
        }

                    /*JSONObject followupEventInputJson = new JSONObject();
                    followupEventInputJson.put("name", "commandcompletedevent");
                    jsonresult.put("followupEventInput", followupEventInputJson);*/


        return null;
    }

    JSONObject handleCommandAriaCondizionataIntent(String session, JSONObject parametersJson, JSONArray outputContextsJsonarray) throws JSONException {


        return null;
    }
}
