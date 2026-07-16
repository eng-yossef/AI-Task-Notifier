//package main.webapp.controller;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//@WebServlet(urlPatterns = "/home")
//public class home extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        System.out.println("home");
//        PrintWriter pr=resp.getWriter();
//        String name=req.getParameter("name");
//        String email=req.getParameter("email");
//        String password=req.getParameter("password");
//        pr.println("<html><body>");
//        pr.println("<h2 class='title' >Test</h2>");
//        pr.println("<h2>Test Two</h2>");
//        pr.println("name: "+name  );
//        pr.println("<br>");
//        pr.println("Email:"+email  );
//        pr.println("<br>");
//        pr.println("Password:  "+password  );
//        pr.println("</body></html>");
//        System.out.println("in Home");
//
//    }
//}
