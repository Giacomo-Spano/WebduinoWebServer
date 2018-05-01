package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.httpClientResult;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Logger;

/**
 * Created by giaco on 17/05/2017.
 */
public class VoIPCallScenarioProgramInstruction extends ScenarioProgramInstruction {

    private static final Logger LOGGER = Logger.getLogger(VoIPCallScenarioProgramInstruction.class.getName());

    private boolean alarmActive = false;

    public VoIPCallScenarioProgramInstruction(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                              int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid,  name, description, priority, enabled);

    }

    @Override
    public void start() {
        super.start();

        send();

    }

    public httpClientResult send() {


        String stringUrl = "https://maker.ifttt.com/trigger/makecall/with/key/dtRN0oc58j9ynAm1JUPlUq";

        String parameters = "";

        httpClientResult result = new httpClientResult();
        try {
            URL jsonurl = new URL(stringUrl.toString());

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(true);
            //connection.setRequestProperty("Content-Type", "application/json");
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
            } else {
                // Server returned HTTP error code.
                LOGGER.severe("Server returned HTTP error code" + res);
                result.res = false;
            }

        } catch (MalformedURLException e) {
            LOGGER.severe("error: MalformedURLException" + e.toString());
            result.res = false;
        } catch (NoRouteToHostException e) {
            LOGGER.severe("error: NoRouteToHostException" + e.toString());
            result.res = false;
        } catch (SocketTimeoutException e) {
            LOGGER.severe("error: SocketTimeoutException" + e.toString());
            result.res = false;
        } catch (Exception e) {
            LOGGER.severe("error: Exception" + e.toString());
            result.res = false;
        }

        return null;
    }

}
