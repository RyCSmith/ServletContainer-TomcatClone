package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
    response.setContentType("text/html");
    response.setStatus(200);
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Test</title></head><body>");
    out.println("RequestURL: ["+request.getRequestURL()+"]<br>");
    out.println("RequestURI: ["+request.getRequestURI()+"]<br>");
    out.println("PathInfo: ["+request.getPathInfo()+"]<br>");
    out.println("Context path: ["+request.getContextPath()+"]<br>");
    out.println("Header: ["+request.getHeader("Accept-Language")+"]<br>");
    out.println("Servlet Path: ["+request.getServletPath()+"]");
    out.println("</body></html>");
    HttpSession session = request.getSession();
    
    //Cookie cook = new Cookie("JSESSIONID", session.getId());
    request.getLocalName();
    //response.addCookie(cook);
    //response.sendRedirect("test.gif");
    BufferedReader reader = request.getReader();
    String line;
    if (reader!=null){
	    while((line = reader.readLine()) != "") {
	    	System.out.println(line);
	    }
    }
    //response.flushBuffer();
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
	       throws java.io.IOException
  {
	  response.setContentType("text/html");
	  response.setStatus(200);
	  PrintWriter out = response.getWriter();
	  out.println("<html><head><title>Test</title></head><body>");
  }
}
  
