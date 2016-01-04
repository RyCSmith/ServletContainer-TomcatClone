package servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestTestServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try{
			request.setAttribute("Frodo","Baggins");
			if(request.getAttribute("Frodo")!=null&&request.getAttribute("Frodo").equals("Baggins"))
				out.println("PASSED: getAttribute()/setAttribute() - Attribute <Frodo> = "+request.getAttribute("Frodo") +"");
			else
				out.println("FAILED: getAttribute()/setAttribute() - Attribute <Frodo> = "+request.getAttribute("Frodo") +"");

			request.removeAttribute("Frodo");

			if (request.getAttribute("Frodo")==null){
				out.println("PASSED: removeAttribute()");
			}
			else {
				out.println("FAILED: removeAttribute()");
			}

			if (request.getHeader("Host")!=null)
				out.println("PASSED: getHeader() - HostHeader = " + request.getHeader("Host") +"");
			else
				out.println("FAILED: getHeader() - HostHeader = " + request.getHeader("Host") +"");

			if(request.getMethod().equalsIgnoreCase("get"))
				out.println("PASSED: getMethod() - Method = " +request.getMethod() + "");
			else
				out.println("FAILED: getMethod() - Method = "+request.getMethod()+"");

			boolean queryStringflag=false;
			if(request.getQueryString()!=null && ( request.getQueryString().equals("?Sam=Gamgee") || request.getQueryString().equals("Sam=Gamgee") )){
				out.println("PASSED: getQueryString() - QueryString = "+request.getQueryString()+"");
				queryStringflag=true;
			}
			else
				out.println("FAILED: getQueryString() - QueryString is null");

			if(queryStringflag && request.getParameter("Sam").equals("Gamgee"))
				out.println("PASSED: getParameter()fromQueryString - Parameter from QS <Sam> = "+request.getParameter("Sam") +"");
			else
				out.println("FAILED: getParameter()fromQueryString - Parameter from QS <Sam> = "+request.getParameter("Sam") +"");


			// test add header
			response.addHeader("headerName555", "37");

			// test contains header
			if (response.containsHeader("headerName555")) {
				out.println("PASSED: containsHeader()");
				out.println("PASSED: addHeader()");
			} else {
				out.println("FAILED: containsHeader()");
				out.println("FAILED: addHeader()");
			}




			out.flush();
		}catch (NullPointerException e){
			out.println("Response: NUll Pointer Exception in Servlet");
			out.flush();
		}

	}



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		try{
			request.setAttribute("Frodo","Baggins");
			if(request.getAttribute("Frodo")!=null&&request.getAttribute("Frodo").equals("Baggins"))
				out.println("PASSED: getAttribute()/setAttribute() - Attribute <Frodo> = "+request.getAttribute("Frodo") +"");
			else
				out.println("FAILED: getAttribute()/setAttribute() - Attribute <Frodo> = "+request.getAttribute("Frodo") +"");

			request.removeAttribute("Frodo");

			if (request.getAttribute("Frodo")==null){
				out.println("PASSED: removeAttribute()");
			}
			else {
				out.println("FAILED: removeAttribute()");
			}
			if(request.getContentLength()==13)
				out.println("PASSED: getContentLength() - ContentLength = "+request.getContentLength()+"");
			else
				out.println("FAILED: getContentLength() - ContentLength = "+request.getContentLength()+"");

			if(request.getContentType()!=null)
				out.println("PASSED: getContentType() - ContentType = " +request.getContentType()+"");
			else
				out.println("FAILED: getContentType() - ContentType = " +request.getContentType()+"");

			if (request.getHeader("Date")!=null)
				out.println("PASSED: getHeader() - DateHeader = " + request.getHeader("Date") +"");
			else
				out.println("FAILED: getHeader() - DateHeader = " + request.getHeader("Date") +"");

			if(request.getMethod().equalsIgnoreCase("post"))
				out.println("PASSED: getMethod() - Method = " +request.getMethod() + "");
			else
				out.println("FAILED: getMethod() - Method = "+request.getMethod()+"");

			if(request.getParameter("Peregrin").equals("Took"))
				out.println("PASSED: getParameter() - Parameter <Peregrin> = " + request.getParameter("Peregrin")+"");
			else
				out.println("FAILED: getParameter() - Parameter <Peregrin> = " + request.getParameter("Peregrin")+"");

			boolean queryStringflag=false;
			if(request.getQueryString()!=null && ( request.getQueryString().equals("?Sam=Gamgee") || request.getQueryString().equals("Sam=Gamgee") )){
				out.println("PASSED: getQueryString() - QueryString = "+request.getQueryString()+"");
				queryStringflag=true;
			}
			else
				out.println("FAILED: getQueryString() - QueryString is null");

			if(queryStringflag && request.getParameter("Sam").equals("Gamgee"))
				out.println("PASSED: getParameter()fromQueryString - Parameter from QS <Sam> = "+request.getParameter("Sam") +"");
			else
				out.println("FAILED: getParameter()fromQueryString - Parameter from QS <Sam> = "+request.getParameter("Sam") +"");

			out.flush();
		}catch (NullPointerException e){
			out.println("Response: NUll Pointer Exception in Servlet");
			out.flush();
		}

	}
}
