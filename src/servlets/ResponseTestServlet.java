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


public class ResponseTestServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		StringBuffer theForm =new StringBuffer();

		theForm.append("<html><head><title>Testing</title></head><body>");
		theForm.append("<h1>Testing Response</h1>");
		theForm.append("<form method=\"POST\" action=\"/testResponse\">");
		theForm.append("<input type=\"text\" name=\"A\" id=\"A\" value=\"B\">");
		theForm.append("<input type=\"submit\" value=\"Submit Form\"/>");
		theForm.append("</form>");

		theForm.append("</body></html>");

		out.println(theForm.toString());
		out.flush();
		//out.close();
	}



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try{

			
			response.setContentLength(524);

			// test setContentType
			response.setContentType(request.getContentType());
			
			if (request.getContentType() != response.getContentType()) {
				out.println("FAILED: getContentType() - Content type incorrect, should be "
						+ request.getContentType()+ ", is " 
						+ response.getContentType());
			} else {
				out.println("PASSED: getContentType()");
			}

			if (!request.getParameter("A").equals("B")) {
				out.println("FAILED: getParameter() - Parameter incorrect, should be B is "
						+ request.getParameter("A"));
			} else {
				out.println("PASSED: getParameter()");
			}
			if(request.getContentLength()==3)
				out.println("PASSED: getContentLength() - ContentLength = "+request.getContentLength()+"");
			else
				out.println("FAILED: getContentLength() - ContentLength = "+request.getContentLength()+"");
			out.flush();
			// getWriter is tested via using it... if nothing works, getWriter is broken


		} catch (NullPointerException e){
			out.println("Response: Null Pointer Exception in Servlet");
			out.flush();
		}
	}
}



