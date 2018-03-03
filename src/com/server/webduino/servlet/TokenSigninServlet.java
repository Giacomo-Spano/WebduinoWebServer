package com.server.webduino.servlet;

import com.google.api.client.json.gson.GsonFactory;
import com.server.webduino.core.httpClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.server.webduino.core.httpClientResult;


/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class TokenSigninServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TokenSigninServlet.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public void doffPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String responseVal = processToken(request);
        response.getWriter().write(responseVal);

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        StringBuffer jb = new StringBuffer();
        String line = null;

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();


        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String idTokenString = jb.toString();
        httpClient client = new httpClient();
        httpClientResult res = client.callGet("","/oauth2/v3/tokeninfo?id_token=" + idTokenString,new URL("https://www.googleapis.com") );

        out.print("prova");
    }

    private String processToken(HttpServletRequest request) {
        String returnVal = "";
        String idTokenString = request.getParameter("id_token");
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = new GsonFactory();

        if (idTokenString != null && !idTokenString.equals("")) {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Arrays.asList("967167304447-5vdlnoa7b1sf0eeasp5bs1uglvq5ov5p.apps.googleusercontent.com"/*ENTER_YOUR_SERVER_CLIENT_ID_HERE*/))
                    // To learn about getting a Server Client ID, see this link
                    // https://developers.google.com/identity/sign-in/android/start
                    // And follow step 4
                    .setIssuer("https://accounts.google.com").build();

            try {
                GoogleIdToken idToken = verifier.verify(idTokenString);
               /* if (idToken != null) {
                    Payload payload = idToken.getPayload();
                    returnVal = "User ID: " ;//+ payload.getSubject();
                    // You can also access the following properties of the payload in index
                    // for other attributes of the user. Note that these fields are only
                    // available if the user has granted the 'profile' and 'email' OAuth
                    // scopes when requested.
                    // String email = payload.getEmail();
                    // boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                    // String name = (String) payload.get("name");
                    // String pictureUrl = (String) payload.get("picture");
                    // String locale = (String) payload.get("locale");
                    // String familyName = (String) payload.get("family_name");
                    // String givenName = (String) payload.get("given_name");
                } else {
                    returnVal = "Invalid ID token.";
                }*/
            } catch (Exception ex) {
                returnVal = ex.getMessage();
            }
        } else {
            returnVal = "Bad Token Passed In";
        }

        return returnVal;
    }
}
