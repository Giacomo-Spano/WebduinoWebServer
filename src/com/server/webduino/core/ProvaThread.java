package com.server.webduino.core;

import com.quartz.QuartzListener;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class ProvaThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ProvaThread.class.getName());
    private CallBack callBack;
    public abstract class CallBack {
        public abstract void onRequestComplete(String response);
    }

    public ProvaThread(CallBack callBack) {
        super("str");

        this.callBack = callBack;

    }

    public void run() {

         callBack.onRequestComplete("fine");
    }
}

