package com.server.webduino.servlet;

import com.server.webduino.core.Core;
import com.server.webduino.core.SWVersion;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_OK;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class OTAServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OTAServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /*response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "filename=\"hoge.txt\"");
        File srcFile = new File("/src_directory_path/hoge.txt");
        FileUtils.copyFile(srcFile, response.getOutputStream());*/

        /*
        [HTTP_USER_AGENT] => ESP8266-http-Update
        [HTTP_X_ESP8266_STA_MAC] => 18:FE:AA:AA:AA:AA
        [HTTP_X_ESP8266_AP_MAC] => 1A:FE:AA:AA:AA:AA
        [HTTP_X_ESP8266_FREE_SPACE] => 671744
        [HTTP_X_ESP8266_SKETCH_SIZE] => 373940
        [HTTP_X_ESP8266_SKETCH_MD5] => a56f8ef78a0bebd812f62067daf1408a
        [HTTP_X_ESP8266_CHIP_SIZE] => 4194304
        [HTTP_X_ESP8266_SDK_VERSION] => 1.3.0
        [HTTP_X_ESP8266_VERSION] => DOOR-7-g14f53a19
         */
        LOGGER.severe("OTA servlet - doGet");

        SWVersion latestVersion = SWVersion.getLatestVersion();
        LOGGER.severe("latestVersion:" + latestVersion.name);
        String swversion = request.getHeader("x-esp8266-version");
        LOGGER.severe("swversion:" + swversion);

        String[] split1 = swversion.split("\\.");
        LOGGER.severe("split1[0]:" + split1[0]);
        LOGGER.severe("split1[1]:" + split1[1]);
        String[] split2 = latestVersion.version.split("\\.");
        LOGGER.severe("split2[0]:" + split2[0]);
        LOGGER.severe("split2[1]:" + split2[1]);




        if ((Integer.parseInt(split2[0]) == Integer.parseInt(split1[0]) &&
                Integer.parseInt(split2[1]) > Integer.parseInt(split1[1])) ||

                (Integer.parseInt(split2[0]) > Integer.parseInt(split2[0]))) {


            LOGGER.info("update software");
            /*String path = System.getProperty("java.io.tmpdir");
            if (!Core.isProduction())
                path = System.getenv("tmp");
            else
                path = System.getProperty("java.io.tmpdir");*/
            String path = latestVersion.path;


            //String fileName = "ESP8266Webduino.ino.bin";
            String fileName = latestVersion.filename;
            String fileType = ".bin";

            response.setContentType(fileType);

            // Make sure to show the download dialog
            //response.setHeader("Content-disposition", "attachment; filename=ESP8266Webduino.ino");
            response.setHeader("Content-disposition", "attachment; filename="+fileName);
            response.setStatus(SC_OK);

            File my_file = new File(path + "/" + fileName);

            LOGGER.info("my_file:" + path + "/" + fileName);

            response.setContentLength((int) my_file.length());

            // This should send the file to browser
            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(my_file);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.flush();

        } else {
            LOGGER.info("update not needed");
            response.setStatus(304);

        }


    }
}
