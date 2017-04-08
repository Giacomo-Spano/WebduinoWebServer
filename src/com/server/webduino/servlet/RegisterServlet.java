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

import com.server.webduino.core.Core;
import com.server.webduino.core.Device;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RegisterServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {

        Device device = new Device();
        device.id= 0;
        device.tokenId = getParameter(req, "tokenid");
        device.name = getParameter(req, "name");
        device.date = Core.getDate();

        int id = Core.mDevices.insert(device);

        setSuccess(resp);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = null;
        try {
            out = resp.getWriter();
            //create Json Response Object
            JSONObject jsonResponse = new JSONObject();
            // put some value pairs into the JSON object .
            try {
                jsonResponse.put("result", "success");
                jsonResponse.put("id", "" + id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // finally output the json string
            out.print(jsonResponse.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
