package com.eoss.hft.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eoss.hft.EventLog;
import com.eoss.hft.datastore.EventLogDAO;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class EventLogServlet extends HttpServlet {
	
	private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private EventLogDAO eventLogDAO;
	
	private Locale locale = new Locale("TH");
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		eventLogDAO = new EventLogDAO(datastore);
		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
				
		PrintWriter out = resp.getWriter();
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		
		String start = req.getParameter("start");
		
		if (start!=null && start.equals("clear")) {
			eventLogDAO.clear(null);
			out.print("Cleared");
		}
		
		Date startTime;
		
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", locale);
		sf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
		try {
			startTime = sf.parse(start);
		} catch (Exception e) {
			startTime = null;
		}
		
		List<EventLog> eventLogList = eventLogDAO.get(startTime);		
		
		out.println("<center>");
		for (EventLog eventLog:eventLogList) {
			out.println(eventLog);
			out.println("<hr>");
		}
		out.println("</center>");
				
	}
	

}
