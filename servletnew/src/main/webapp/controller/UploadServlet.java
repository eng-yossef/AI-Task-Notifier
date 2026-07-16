package main.webapp.controller;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
@WebServlet(urlPatterns = "/UploadServlet")

public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the uploaded file
        Part filePart = request.getPart("file");

        // Process the file (save it to a directory, etc.)
        // For simplicity, let's just get the input stream
        InputStream fileContent = filePart.getInputStream();

        // Pass the input stream to the JSP page
        request.setAttribute("imageStream", fileContent);

        // Forward the request to the JSP page
        RequestDispatcher dispatcher = request.getRequestDispatcher("view.jsp");
        dispatcher.forward(request, response);
    }
}
