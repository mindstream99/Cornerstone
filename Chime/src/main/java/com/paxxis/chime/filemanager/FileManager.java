/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.chime.filemanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.paxxis.chime.json.JSONObject;

import eu.medsea.mimeutil.MimeUtil;

/**
 *
 * @author Robert Englander
 */
public class FileManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static class FileItemResponse {
        FileItem item;
        boolean isImage;

        FileItemResponse(FileItem item, boolean isImage) {
            this.item = item;
            this.isImage = isImage;
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        String fileId = request.getParameter("id");

        OutputStream out = response.getOutputStream();
        try {
            FileInputStream fis = new FileInputStream("./filestore/" + fileId);
            byte[] buffer = new byte[2048];
            int cnt;
            while ((cnt = fis.read(buffer)) != -1) {
                out.write(buffer, 0, cnt);
            }

            @SuppressWarnings("unchecked")
            Collection mimeTypes = MimeUtil.getMimeTypes("./filestore/" + fileId);
            String mimeType = mimeTypes.toArray()[0].toString();

            response.setContentType(mimeType);

        } finally { 
            out.close();
        }
    } 

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html");

        // get the json payload that has the message content
        FileItemResponse uploadItem = getFileItem(request);
        if(uploadItem == null) {
            response.getWriter().write("NO-DATA");
            return;
        }

        long hashCode = System.currentTimeMillis();
        String token = Long.toHexString(hashCode);
        if (!uploadItem.isImage) {
            token += ".file";
        }

        FileOutputStream fos = new FileOutputStream("./filestore/" + token.toUpperCase());
        fos.write(uploadItem.item.get());
        fos.close();

        JSONObject json = new JSONObject();
        json.put("fileId", token.toUpperCase());

        String name = uploadItem.item.getName();
        long size = uploadItem.item.getSize();
        json.put("size", size);

        int idx = name.lastIndexOf(".");
        if (idx != -1 && idx < (name.length() - 1)) {

            String fileExtension = name.substring(idx + 1);
            json.put("extension", fileExtension);
        }

        try {
            @SuppressWarnings("unchecked")
            Collection mimeTypes = MimeUtil.getMimeTypes("./filestore/" + token.toUpperCase());
            if (mimeTypes.size() > 0) {
                String mimeType = mimeTypes.toArray()[0].toString();
                json.put("mimeType", mimeType);
            }

        } catch (Throwable t) {
            json.put("mimeType", t.getMessage());
        }

        response.getWriter().write(json.toString());

    }

    private FileItemResponse getFileItem(HttpServletRequest req) {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            @SuppressWarnings("unchecked")
            List items = upload.parseRequest(req);

            @SuppressWarnings("unchecked")
            Iterator it = items.iterator();

            while(it.hasNext()) {
                FileItem item = (FileItem) it.next();
                if(!item.isFormField()) {
                    String fieldName = item.getFieldName();
                    if ("uploaded.File".equals(fieldName)) {
                        return new FileItemResponse(item, false);
                    } else if ("uploaded.Image".equals(fieldName)) {
                        return new FileItemResponse(item, true);
                    }
                }
            }
        }
        catch(FileUploadException e){
            return null;
        }

        return null;
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "The Chime file upload manager";
    }

}
