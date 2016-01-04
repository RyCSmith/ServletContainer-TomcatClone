package servlets;


import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigTestServlet extends HttpServlet {
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		try{
			ServletConfig config  = getServletConfig();
			PrintWriter pw = response.getWriter();
			String param = config.getInitParameter("TestParam");
			if(param!=null && param.equalsIgnoreCase("1776")){
				pw.println("PASSED: getInitParameter()");
			}else{
				pw.println("FAILED: getInitParameter()");
			}
			if(config.getServletName().equalsIgnoreCase("testConfig")){
			//	pw.println("ServletName:"+config.getServletName());
				pw.println("PASSED: getServletName() - ServletName: "+config.getServletName());
			}else{
			//	pw.println("ServletName:"+config.getServletName());
				pw.println("FAILED: getServletName() - ServletName: "+config.getServletName());
			}
			if(config.getServletContext()!=null){
				pw.println("PASSED: getServletContext()");
			}else{
				pw.println("FAILED: getServletContext()");
			}
			pw.flush();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		try{
			ServletConfig config  = getServletConfig();
			PrintWriter pw = response.getWriter();
			String param = config.getInitParameter("TestParam");
			if(param!=null && param.equalsIgnoreCase("1776")){
				pw.println("PASSED: getInitParameter()");
			}else{
				pw.println("FAILED: getInitParameter()");
			}
			if(config.getServletName().equalsIgnoreCase("testConfig")){
			//	pw.println("ServletName:"+config.getServletName());
				pw.println("PASSED: getServletName() - ServletName: "+config.getServletName());
			}else{
			//	pw.println("ServletName:"+config.getServletName());
				pw.println("FAILED: getServletName() - ServletName: "+config.getServletName());
			}
			if(config.getServletContext()!=null){
				pw.println("PASSED: getServletContext()");
			}else{
				pw.println("FAILED: getServletContext()");
			}
			pw.flush();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
