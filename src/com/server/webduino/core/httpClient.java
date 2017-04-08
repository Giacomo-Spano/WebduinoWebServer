package com.server.webduino.core;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 11/11/2016.
 */
public class httpClient {

    private static Logger LOGGER = Logger.getLogger(httpClient.class.getName());

    public class Result {
        boolean res;
        String response = "";
    }

    protected Result callGet(String param, String path, URL url) {

        InputStreamReader responseInputStream;
        Result result = new Result();
        try {
            URL jsonurl = new URL(url.toString() + path);

            HttpURLConnection mConnection = (HttpURLConnection) jsonurl.openConnection();

            mConnection.setDoOutput(false);
            mConnection.setRequestProperty("Content-Type", "application/json");
            mConnection.setRequestMethod("GET");
            mConnection.setConnectTimeout(2000); //set timeout to 10 seconds

            int res = mConnection.getResponseCode();

            if (res == HttpURLConnection.HTTP_OK) {


                responseInputStream = new InputStreamReader(
                        mConnection.getInputStream());
                BufferedReader rd = new BufferedReader(responseInputStream);
                String line = "";

                result.response = "";
                while ((line = rd.readLine()) != null) {
                    result.response += line;
                }
                responseInputStream.close();

                result.res = true;
                return result;


            } else {
                // Server returned HTTP error code.
                LOGGER.severe("Server returned HTTP error code" + res);
                result.res = false;
                return result;
            }

        } catch (MalformedURLException e) {
            LOGGER.severe("error: MalformedURLException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        } catch (NoRouteToHostException e) {
            LOGGER.severe("error: NoRouteToHostException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        } catch (SocketTimeoutException e) {
            LOGGER.severe("error: SocketTimeoutException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        } catch (Exception e) {
            LOGGER.severe("error: Exception" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        }
        //LOGGER.info("jsonResultSring = " + jsonResultSring);
        //return jsonResultSring;

    }

    protected Result callPost(String path, String urlParameters, URL url) {

        LOGGER.info("path: " + path + "urlParameters: " + urlParameters+ "url: " + url.toString());


        Result result = new Result();
        try {
            URL jsonurl = new URL(url.toString() + path);

            HttpURLConnection connection = (HttpURLConnection) jsonurl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000); //set timeout to 5 seconds
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
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
                return result;
            } else {
                // Server returned HTTP error code.
                LOGGER.severe("Server returned HTTP error code" + res);
                result.res = false;
                return result;
            }

        } catch (MalformedURLException e) {
            LOGGER.severe("error: MalformedURLException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        } catch (NoRouteToHostException e) {
            LOGGER.severe("error: NoRouteToHostException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        } catch (SocketTimeoutException e) {
            LOGGER.severe("error: SocketTimeoutException" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        } catch (Exception e) {
            LOGGER.severe("error: Exception" + e.toString());
            //e.printStackTrace();
            result.res = false;
            return result;
        }
    }

}