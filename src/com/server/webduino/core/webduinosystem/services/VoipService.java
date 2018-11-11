package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.core.httpClientResult;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.logging.Logger;

/**
 * Created by giaco on 10/03/2018.
 */
public class VoipService extends Service {
    private static final Logger LOGGER = Logger.getLogger(VoipService.class.getName());

    public VoipService(int id, String name, String type, String param) {
        super(id, name, type, param);
        ActionCommand cmd = new ActionCommand("voipcall","Chiamata VoIP");
        cmd.addParam("Numero telefono",10);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    httpClientResult res = send();
                    if (res != null)
                        return res.res;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return false;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);


    }

    public httpClientResult send() {


        //String stringUrl = "https://maker.ifttt.com/trigger/makecall/with/key/dF2YNsgRCU5bWMZXuNqBt6";
        String stringUrl = param;

        String parameters = "";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("value1", "valore 1");
            jsonObject.put("value2", "valore 2");
            jsonObject.put("value3", "valore 3");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        httpClientResult result = new httpClientResult();
        try {
            URL jsonurl = new URL(stringUrl.toString());

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(true);
            //connection.setRequestProperty("Content-Type", "application/json");
            //connection.setRequestProperty("Authorization", regkey);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            String input = "{ \"value1\":\"soggiorno\", \"value2\" : \"porta aperta\", \"value3\" : \"\" }";

            connection.setConnectTimeout(5000); //set timeout to 5 seconds
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(input.getBytes());
            os.flush();


            /*DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();*/

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
