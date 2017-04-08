package com.server.webduino.core;

import com.server.webduino.servlet.NotificationServlet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 20/11/2016.
 */
public class SendNotification extends httpClient {

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    Result send(String title, String description, String notificationType, int id) {
        //result = callPost(param, "/fcm/send", "http://fcm.googleapis.com");

        Devices devices = new Devices();
        devices.read();
        List<Device> list = devices.getList();

        if (list == null || list.size() == 0)
            return null;

        for (Device device : list) {
            String to = device.tokenId;


            String stringUrl = "http://fcm.googleapis.com/fcm/send";
            String regkey = "key=AIzaSyAUxSqvqoXlTHTt1GgniBMLsd4nOe2goDI";

            String parameters = "{\n" +
                    "  \"to\": \"" + to + "\",\n" +
                    "  \"data\": {\n" +
                    "    \"hello\": \"This is a Firebase Cloud Messaging Device Group Message!\",\n" +
                    "    \"type\": \""+ notificationType + "\",\n" +
                    "    \"id\": \"" + id + "\",\n" +
                    "   }\n" +
                    "   \"notification\" : {\n" +
                    "      \"body\" : \"" + description + "\",\n" +
                    "      \"title\" : \"" + title + "\",\n" +
                    "      \"icon\" : \"myicon\"\n" +

                    "    }\n" +
                    "}";

            Result result = new Result();
            try {
                URL jsonurl = new URL(stringUrl.toString());

                HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", regkey);
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
        }
        return null;
    }
}

