package com.eoss.hft.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.eoss.hft.Currency;
import com.eoss.hft.EventLog;
import com.eoss.hft.Exchange;
import com.eoss.hft.Pair;
import com.eoss.hft.Route;
import com.eoss.hft.datastore.EventLogDAO;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.strategy.ForceSellStrategy;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import googleSendgridJava.Sendgrid;

public class ForceSellServlet extends HttpServlet {

	private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private FallDAO fallDAO;
	private EventLogDAO eventLogDAO;
	private Exchange ex;	
	private Map<String, Pair> pairMap;
	private ForceSellStrategy forceSellStrategy;
	
	private String key;
	private String secret;
	private String mailToList;
	private int delay;
	private int processDelay;
	private String counterCurrency;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
				
		fallDAO = new FallDAO(datastore);		
		eventLogDAO = new EventLogDAO(datastore);
		
		key = config.getInitParameter("key");
		secret = config.getInitParameter("secret");
		mailToList = config.getInitParameter("mailTo");
				
		try {
			delay = Integer.parseInt(config.getInitParameter("delay"));
		} catch (Exception e) {
			delay = 2000;
		}
		
		try {
			processDelay = Integer.parseInt(config.getInitParameter("processDelay"));
		} catch (Exception e) {
			processDelay = 1000;
		}
		
		counterCurrency = config.getInitParameter("counterCurrency");
		
		try {
			
			ex = (Exchange) Class.forName(config.getInitParameter("exchange")).newInstance();
			
			if ( key!=null && secret!=null ) {
				ex.setKey(key);
				ex.setSecret(secret);
			}
			
			Route [] routes = Route.load(ex.pairMap());
			
			pairMap = new HashMap<>();
			
			for (Route route:routes) {
				for (Pair pair:route.pairs) {
					if (pair.counter.equals(counterCurrency)) {
						pairMap.put(pair.toString(), pair);
					}					
				}
			}
			
			forceSellStrategy = new ForceSellStrategy(ex, delay, processDelay, fallDAO, pairMap) {

				@Override
				protected void onTradeSuccess() {
					
					Currency balance = ex.getAvailableBalance(counterCurrency);
					String subject = ex.getClass().getName().replace("com.eoss.hft.exchange.", "") + ":ForceSell! You got " + balance;
					String title = "";
					String body = "";
					
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
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		
		PrintWriter out = resp.getWriter();
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		
		Route.fetch(ex,new HashSet<>(pairMap.values()));
		
		Set<String> wallets = new HashSet<>();
		for (Pair pair:pairMap.values()) {
			wallets.add(pair.base);
			wallets.add(pair.counter);
		}
		
		Map<String, Currency> walletMap = new HashMap<>();
		Map<String, Currency> currencyMap = new HashMap<>();
		Currency currency;
		
		Pair pair;
		for (String currencyName:wallets) {
			
			try {
				currency = ex.getAvailableBalance(currencyName);
				walletMap.put(currencyName, currency);
			
				if (counterCurrency!=null) {
					if (currencyName.equals(counterCurrency)) {
						currencyMap.put(currencyName, currency);
					} else {
						pair = pairMap.get(currencyName + "/" + counterCurrency);
						if (pair!=null) {
							currencyMap.put(currencyName, pair.forward(currency));								
						}
					}				
				}
			} catch (Exception e) {
				
			} finally {
				try { Thread.sleep(processDelay); } catch (InterruptedException e) {}					
			}
		}
		
		double totalAmount = 0;
		for (Map.Entry<String, Currency> entry:walletMap.entrySet()) {
			if (counterCurrency!=null) {
				currency = currencyMap.get(entry.getKey());
				if (currency!=null) {
					out.println("<b>" + entry.getKey() + ":</b>" + entry.getValue() + "~" + currency + "<br>");				
					totalAmount += currency.getAmount();							
				}
			} else {
				out.println("<b>" + entry.getKey() + ":</b>" + entry.getValue() + "<br>");								
			}
		}
				
		if (counterCurrency!=null) {
			out.println("Total in " + counterCurrency + " ~ <b>" + new Currency(counterCurrency, floor(totalAmount, 8)) + "</b>");				
		}				
		
		out.println("<center><form action=\"" + req.getRequestURI() +"\" method=\"post\"><input type=\"submit\" value=\"FORCE SELL!\"></form></center>");
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		forceSellStrategy.process();
		resp.sendRedirect(req.getRequestURI());
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
	
	private static double floor(double value, int places) {
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.FLOOR);
	    return bd.doubleValue();	    
	}
}