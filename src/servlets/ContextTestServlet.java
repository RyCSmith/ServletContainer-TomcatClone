package servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ContextTestServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try{
			ServletConfig config = getServletConfig();
			ServletContext context = config.getServletContext();

			context.setAttribute("Frodo","Baggins");
			if(context.getAttribute("Frodo").equals("Baggins"))
				out.println("PASSED: setAttribute() - Attribute <Frodo> = "+context.getAttribute("Frodo"));
			else
				out.println("FAILED: setAttribute() - Attribute <Frodo> = "+context.getAttribute("Frodo") );
			context.removeAttribute("Frodo");

			if (context.getAttribute("Frodo")==null){
				out.println("PASSED: removeAttribute()");
			}
			else {
				out.println("FAILED: removeAttribute()");
			}
			if(context.getContext("/testContext")!=null)
				out.println("PASSED: getContext('/testContext') - Context= "+context.getContext("/").toString()+"");
			else
				out.println("FAILED: getContext('/testContext') - Context "+context.getContext("/") +"");

			if(context.getServletContextName()!=null && context.getServletContextName().equals("Test servlets"))
				out.println("PASSED: getServletContextName() - Context Name = " + context.getServletContextName()+"");
			else 
				out.println("FAILED: getServletContextName() - Context Name = " + context.getServletContextName()+"");
			out.flush();

		} catch (NullPointerException e){
			out.println("Response: Null Pointer Exception in Servlet");
			out.flush();
		}
	}



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try{
			ServletConfig config = getServletConfig();
			ServletContext context = config.getServletContext();

			context.setAttribute("Frodo","Baggins");
			if(context.getAttribute("Frodo").equals("Baggins"))
				out.println("PASSED: setAttribute() - Attribute <Frodo> = "+context.getAttribute("Frodo") +"");
			else
				out.println("FAILED: setAttribute() - Attribute <Frodo> = "+context.getAttribute("Frodo") +"");
			context.removeAttribute("Frodo");

			if (context.getAttribute("Frodo")==null){
				out.println("PASSED: removeAttribute()");
			}
			else {
				out.println("FAILED: removeAttribute()");
			}
			if(context.getContext("/testContext")!=null)
				out.println("PASSED: getContext('/testContext') - Context= "+context.getContext("/").toString()+"");
			else
				out.println("FAILED: getContext('/testContext') - Context "+context.getContext("/") +"");

			if(context.getServletContextName()!=null && context.getServletContextName().equals("Test servlets"))
				out.println("PASSED: getServletContextName() - Context Name = " + context.getServletContextName()+"");
			else 
				out.println("FAILED: getServletContextName() - Context Name = " + context.getServletContextName()+"");
			out.flush();

		} catch (NullPointerException e){
			out.println("Response: Null Pointer Exception in Servlet");
			out.flush();
		}
	}
}


