package servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InitParamServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		ServletConfig config = getServletConfig();
		ServletContext context = config.getServletContext();
		out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
		out.println("<P>The value of 'webmaster' is: " + 
				context.getInitParameter("webmaster") + "</P>");
		out.println("<P>The value of 'TestParam' is: " + 
				config.getInitParameter("TestParam") + "</P>");
		out.println("</BODY></HTML>");		
	}
}
