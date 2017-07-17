package com.server.webduino.servlet;

/**
 * Created by giaco on 09/05/2017.
 */


import com.server.webduino.core.Core;
import com.server.webduino.core.SWVersion;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

//@WebServlet("/FileUploadServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 205242440643911308L;

    /**
     * Directory where uploaded files will be saved, its relative to
     * the web application directory.
     */
    private static final String UPLOAD_DIR = "uploads";

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // gets absolute path of the web application
        //String applicationPath = request.getServletContext().getRealPath("");
        String version = request.getParameter("version");
        //String fileName = request.getParameter("fileName");
        String name = request.getParameter("name");

        String tmpDir = "";
        if (Core.isProduction())
            tmpDir = "/home/pi/Downloads";
        else
            tmpDir = "c:/scratch";

        /*if(!Core.isProduction())
            tmpDir = System.getenv("tmp");
        else
            tmpDir = System.getProperty("java.io.tmpdir");*/

        // constructs path of the directory to save uploaded file
        String uploadFilePath = tmpDir/* + File.separator + UPLOAD_DIR*/;

        // creates the save directory if it does not exists
        //String fileName = "version_" + fileName;
        File fileSaveDir = new File(uploadFilePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdirs();
        }
        System.out.println("Upload File Directory=" + fileSaveDir.getAbsolutePath());

        String fileName = "";
        //File file = new File(fileSaveDir, fileName);
       // File file = new File(uploads, "somefilename.ext");
        //Get all the parts from request and write it to the file on server
        for (Part part : request.getParts()) {


            if(part.getName().equals("fileName")) {
                fileName = getFileName(part);
                File file = new File(fileSaveDir, fileName);
                try (InputStream input = part.getInputStream()) {
                    Files.copy(input, file.toPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            /*fileName = getFileName(part);

            part.write(uploadFilePath + File.separator + fileName);*/
        }

        SWVersion swVersion = new SWVersion(0,name,version,uploadFilePath,fileName);
        swVersion.write();

        request.setAttribute("message", version + " File uploaded successfully!");
        getServletContext().getRequestDispatcher("/response.jsp").forward(
                request, response);
    }

    /**
     * Utility method to get file name from HTTP header content-disposition
     */
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println("content-disposition header= " + contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
