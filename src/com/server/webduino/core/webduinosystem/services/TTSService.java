package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.core.*;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 10/03/2018.
 */
public class TTSService extends Service {
    private static final Logger LOGGER = Logger.getLogger(TTSService.class.getName());

    public TTSService(int id, String name, String type, String param) {
        super(id, name, type, param);
        ActionCommand cmd = new ActionCommand("tts", "Riproduci testo audio");
        cmd.addMediaplayer("Mediaplayer");
        cmd.addParam("Testo da riprodurre", 50);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    if (json.has("mediaplayerid")) {
                        int mediaplayerid = json.getInt("mediaplayerid");
                        String message = "";
                        if (json.has("param"))
                            message = json.getString("param");
                        //httpClientResult ret = send(mediaplayerid, message);
                        boolean ret = sendMQTT(mediaplayerid, message);
                        return ret;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return false;
            }

            @Override
            public void end() {

            }

            @Override
            public JSONObject getResult() {
                return null;
            }
        });
        actionCommandList.add(cmd);
    }

    boolean sendMQTT(int mediaplayerid, String message) {

        LOGGER.info("TTSService::sendMQTT message=" + message);

        SimpleMqttClient smc;
        smc = new SimpleMqttClient("ttsClient");
        if (!smc.runClient(Core.getMQTTUrl(),Core.getMQTTPort(), Core.getMQTTUser(), Core.getMQTTPassword())) {
            LOGGER.severe("cannot open MQTT client");
            return false;
        }
        /*smc.subscribe("toServer/shield/#");
        smc.addListener(new SimpleMqttClient.SimpleMqttClientListener() {
            @Override
            public void messageReceived(String topic, String message) {

            }

            @Override
            public void connectionLost() {

            }
        });*/

        Mediaplayer mediaplayer = Core.getMediaplayersFromId(mediaplayerid);
        boolean ret = false;

        if (mediaplayer != null && smc != null) {
            smc.publish("tts/play/" + mediaplayer.name, message);
        } else {
            LOGGER.severe("invalid media player");
        }

        if (smc.isConnected()) {
            //smc.unsubscribe(responseTopic);
            smc.disconnect();
        }

        return true;
    }

    httpClientResult send(int mediaplayerid, String message) {

        LOGGER.info("message=" + message);
        String stringUrl = "https://giacomocasa.duckdns.org:8123/api/webhook/8b264933b2e1a3cd56f78a4daf6c2f3262f4081a474a5e4210424313e4822bc3";


        httpClientResult result = new httpClientResult();
        try {
            URL jsonurl = new URL(stringUrl.toString());

            String parameters = "{\"message\": \"" + message + "\" }";

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Authorization", regkey);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000); //set timeout to 5 seconds
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader rd = new BufferedReader(reader);
            String line = "";
            result.response = "";
            while ((line = rd.readLine()) != null) {
                result.response += line;
            }

            reader.close();
            int res = connection.getResponseCode();

            if (res == HttpURLConnection.HTTP_OK) {
                LOGGER.info("result: " + result.toString());
                result.res = true;
                //return result;
            } else {
                // Server returned HTTP error code.
                LOGGER.severe("Server returned HTTP error code" + res);
                result.res = false;
                //return result;
            }

        } catch (MalformedURLException e) {
            LOGGER.severe("error: MalformedURLException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            //return result;
        } catch (NoRouteToHostException e) {
            LOGGER.severe("error: NoRouteToHostException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            //return result;
        } catch (SocketTimeoutException e) {
            LOGGER.severe("error: SocketTimeoutException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            //return result;
        } catch (Exception e) {
            LOGGER.severe("error: Exception" + e.toString());
            //e.printStackTrace();
            result.res = false;
            //return result;
        }
        return null;
    }
}
