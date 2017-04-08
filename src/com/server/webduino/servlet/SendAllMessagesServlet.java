/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.server.webduino.servlet;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Servlet that adds a new message to all registered devices.
 * <p/>
 * This servlet is used just by the browser (i.e., not device).
 */

public class SendAllMessagesServlet extends BaseServlet {

    //private static final String PARAMETER_RELESTATUS = "relestatus";
    private static final String PARAMETER_MESSAGETITLE = "title";
    private static final String PARAMETER_MESSAGETEXT = "message";
    //private static final String PARAMETER_APPNAME = "appname";
    private static final String PARAMETER_NOTIFICATIONTYPE = "notificationtype";
    private static final String PARAMETER_COLLAPSEKEY = "collapsekey";

    // String host = System.getenv("OPENSHIFT_MYSQL_DB_HOST");

//	private static final int MULTICAST_SIZE = 1000;


    //private SendPushMessages data = new SendPushMessages();

    private static final Executor threadPool = Executors.newFixedThreadPool(5);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //data.sender = newSender(config);
    }

    /**
     * Creates the {@link Sender} based on the servlet settings.
     */
    /*protected Sender newSender(ServletConfig config) {
		
		 // String key = (String) config.getServletContext().getAttribute(
		 // ApiKeyInitializer.ATTRIBUTE_ACCESS_KEY);
		 

		String key = "AIzaSyCVaM1D21srrg8-0gzPC8e_4EpznChipW4";
		return new Sender(key);
	}*/

    /**
     * Processes the request to add a new message.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String status;

        String relestatus = "";// = getParameter(req, PARAMETER_RELESTATUS);
        String title = "", messagetxt = "", collapsekey = "", notificationtype = "";

        try {

            title = getParameter(req, PARAMETER_MESSAGETITLE);

        } catch (ServletException e) {
            System.out.println("title nbot found");
        }
        try {

            messagetxt = getParameter(req, PARAMETER_MESSAGETEXT);
        } catch (ServletException e) {
            System.out.println("Message not found");
        }
        /*try {

            appname = getParameter(req, PARAMETER_APPNAME);
        } catch (ServletException e) {
            System.out.println("Appname not found");
        }*/
        try {

            notificationtype = getParameter(req, PARAMETER_NOTIFICATIONTYPE);
        } catch (ServletException e) {
            System.out.println("Notificationtype not found");
        }
        try {

            collapsekey = getParameter(req, PARAMETER_COLLAPSEKEY);

        } catch (ServletException e) {
            System.out.println("colapsekey not found");
        }

        Message message = new Message.Builder()
                // .collapseKey(collapsekey) // se c'� gi� un messaggio con lo
                // stesso collapskey e red id allora l'ultimo sostituir� il
                // precedente
                // .timeToLive(3).delayWhileIdle(true) // numero di secondi per
                // i quali il messagio rimane in coda (default 4 week)
                .addData("title", title + "(" + "hostgoogle" + ")")
                .addData("message", messagetxt)
                //.addData("notificationtype", notificationtype)
                .build();


        //ControlPanel.sendPushMessage.send(title, messagetxt);

        /*SendPushMessages sp = new SendPushMessages();
        sp.init();
        sp.send(message);*/

    }


}
