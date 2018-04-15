package com.eoss.hft.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.eoss.hft.Currency;
import com.eoss.hft.EventLog;
import com.eoss.hft.Exchange;
import com.eoss.hft.Fall;
import com.eoss.hft.Pair;
import com.eoss.hft.datastore.EventLogDAO;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.strategy.ZigzagStrategy;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import googleSendgridJava.Sendgrid;

public class ZigzagServlet extends HttpServlet {

	private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private FallDAO fallDAO;	
	private EventLogDAO eventLogDAO;
	private Exchange ex;	
	private Map<String, Pair> pairMap;
	private ZigzagStrategy zigZagStrategy;
	
	private String key;
	private String secret;
	private String mailToList;
	private int interval;
	private int processDelay;
	private String pairFilters;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
				
		fallDAO = new FallDAO(datastore);		
		eventLogDAO = new EventLogDAO(datastore);
		
		key = config.getInitParameter("key");
		secret = config.getInitParameter("secret");
		mailToList = config.getInitParameter("mailTo");
				
		try {
			interval = Integer.parseInt(config.getInitParameter("interval"));
		} catch (Exception e) {
			interval = 60;
		}
				
		try {
			processDelay = Integer.parseInt(config.getInitParameter("processDelay"));
		} catch (Exception e) {
			processDelay = 1000;
		}
		
		try {
			
			ex = (Exchange) Class.forName(config.getInitParameter("exchange")).newInstance();
			
			if ( key!=null && secret!=null ) {
				ex.setKey(key);
				ex.setSecret(secret);
			}
			
			pairMap = ex.pairMap();
			
			pairFilters = config.getInitParameter("pairFilters");
			
			List<String> onlyPairList = new ArrayList<>();
			
			if (pairFilters!=null) {
				
				String [] pairNames = pairFilters.split(",");
				for (String pair:pairNames) {
					onlyPairList.add(pair.trim());
				}
				
			} 
			
			zigZagStrategy = new ZigzagStrategy(ex, 50, fallDAO, pairMap, onlyPairList) {

				@Override
				protected void onTradeSuccess(Fall fall, double amount, double rate) {
					String subject = ex.getClass().getName().replace("com.eoss.hft.exchange.", "") + ":Zigzag! You got " + new Currency(fall.target.getName(), amount);
					String title = fall.toString();
					String body = (rate-1)*100 + "%" ;
					
					eventLogDAO.put(new EventLog(new Date(), subject, title + "<br>" + body));
					
					sendGridAlertMail(subject, title, body);
				}
				
			};
									
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
					
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime < interval * 1000) {
			try {
				zigZagStrategy.process();			
				Thread.sleep(processDelay);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
	}
	
	private void sendGridAlertMail(String subject, String title, String body) {
		
		if (mailToList==null) return;

		Sendgrid mail = new Sendgrid("wizarud","y7Ybo2YE");
		
		// set email data
		mail
			.setFrom("contact@eoss-th.com")
			.setFromName("Your Arbitrage Bot")
			.setSubject(subject)
			.setText(title)
			.setHtml("<b>" + title + "</b><br>" + body);
		
		String [] emails = mailToList.split(",");
		
		for (String email:emails) {
			mail.addTo(email.trim());
		}

		// send your message
		try {
			mail.send();
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		
	}
	
}