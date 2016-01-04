package servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class SessionTestServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		StringBuffer theForm =new StringBuffer();

		theForm.append("<html><head><title>Testing</title></head><body>");
		theForm.append("<h1>Testing Session</h1>");
		theForm.append("<form method=\"POST\" action=\"/testSession\">");
		theForm.append("<input type=\"submit\" value=\"Submit Form\"/>");
		theForm.append("</form>");

		theForm.append("</body></html>");

		out.println(theForm.toString());
		out.flush();
		out.close();
	}



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try{
			
			// create a session
			HttpSession session = request.getSession(true);
			
			out.println("PASSED: getSession()");

			// test get/setAttribute
			session.setAttribute("sessionAttributeTest", "value555");
			if ("value555".equals( (String) session.getAttribute("sessionAttributeTest")) ) {
				out.println("PASSED: setAttribute() and getAttribute()");
			} else {
				out.println("FAILED: setAttribute() and/or getAttribute()");
			}

			// test get/setMaxInactiveInterval
			session.setMaxInactiveInterval(40);
			if (session.getMaxInactiveInterval() == 40) {
				out.println("PASSED: setMaxInactiveInterval() and getMaxInactiveInterval()");
			} else {
				out.println("FAILED: setMaxInactiveInterval() and/or getMaxInactiveInterval()");
			}

			// test removeAttribute
			session.removeAttribute("attributeToBeRemoved");

			if (session.getAttribute("attributeToBeRemoved") == null) {
				out.println("PASSED: removeAttribute()");
			} else {
				out.println("FAILED: removeAttribute()");
			}

			// test getId
			String id = session.getId();
			if (id.equals("")) {
				out.println("FAILED: getId() - getId returned empty string");
			} else {
				out.println("PASSED: getId()");
			}

		} catch (NullPointerException e){
			out.println("Response: Null Pointer Exception in Servlet\r\n");
			out.flush();
		}
	}
}


