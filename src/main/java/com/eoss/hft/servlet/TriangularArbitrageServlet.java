package com.eoss.hft.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.eoss.hft.Route;
import com.eoss.hft.datastore.EventLogDAO;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;
import com.eoss.hft.exception.InstantTradeException;
import com.eoss.hft.strategy.FallbackStrategy;
import com.eoss.hft.strategy.TriangularArbitrageStrategy;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import googleSendgridJava.Sendgrid;

public class TriangularArbitrageServlet extends HttpServlet {

	private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private FallDAO fallDAO;	
	private EventLogDAO eventLogDAO;
	private Exchange ex;	
	private Map<String, Pair> pairMap;
	private TriangularArbitrageStrategy triangularStrategy;
	private FallbackStrategy fallbackStrategy;
	
	private String key;
	private String secret;
	private String mailToList;
	private String counterCurrency;			
	private int delay;
	private int interval;
	private int processDelay;
	private double candidatedScore;
	private String pairFilters;
	private String currencyFilters;
	
	private boolean debug;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
				
		fallDAO = new FallDAO(datastore);		
		eventLogDAO = new EventLogDAO(datastore);
		
		key = config.getInitParameter("key");
		secret = config.getInitParameter("secret");
		mailToList = config.getInitParameter("mailTo");
		counterCurrency = config.getInitParameter("counterCurrency");
		
		try {
			delay = Integer.parseInt(config.getInitParameter("delay"));
		} catch (Exception e) {
			delay = 2000;
		}
				
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
			candidatedScore = Double.parseDouble(config.getInitParameter("candidatedScore"));
		} catch (Exception e) {
			candidatedScore = 0.99;
		}
		
		try {
			
			ex = (Exchange) Class.forName(config.getInitParameter("exchange")).newInstance();
			
			if ( key!=null && secret!=null ) {
				ex.setKey(key);
				ex.setSecret(secret);
			}
			
			pairMap = ex.pairMap();
			
			Route [] routes = Route.load(pairMap);
			
			try { routes = Route.filterMinAmountPlaces(routes, Integer.parseInt(config.getInitParameter("minAmountPlaces"))); } catch (Exception e) {}
			
			try { routes = Route.filterMinPricePlaces(routes, Integer.parseInt(config.getInitParameter("minPricePlaces"))); } catch (Exception e) {}
			
			pairFilters = config.getInitParameter("pairFilters");
			
			if (pairFilters!=null) {
				
				List<String> onlyPairList = new ArrayList<>();
				String [] pairs = pairFilters.split(",");
				for (String pair:pairs) {
					onlyPairList.add(pair.trim());
				}
				
				routes = Route.filterPairs(routes, onlyPairList);
				
			} 
			
			currencyFilters = config.getInitParameter("currencyFilters");
			
			if (currencyFilters!=null) {
				
				List<String> onlyCurrencyList = new ArrayList<>();
				String [] currencies = currencyFilters.split(",");
				for (String currency:currencies) {
					onlyCurrencyList.add(currency.trim());
				}
				
				routes = Route.filterCurrencies(routes, onlyCurrencyList);
				
			} 
									
			triangularStrategy = new TriangularArbitrageStrategy(ex, routes, delay, fallDAO, candidatedScore) {

				@Override
				protected void onTradeSuccess(Route bestRoute, Currency startBalance, Currency amount, Currency endBalance) {
															
					logSuccessTx(bestRoute.toString(), startBalance.getName(), endBalance.getAmount()-startBalance.getAmount(), roundHalfUp((endBalance.getAmount()-startBalance.getAmount())*100.0/startBalance.getAmount(), 2));

					String subject = ex.getClass().getName().replace("com.eoss.hft.exchange.", "") + ": You got " + new Currency(startBalance.getName(), endBalance.getAmount() - startBalance.getAmount());
					String title = bestRoute.toString();
					String body = "Rate:" + (endBalance.getAmount() / startBalance.getAmount()-1) * 100 + "%<br>" + "Start:" + amount + "<br>" + "Succeed:" + startBalance+"->"+endBalance;

					eventLogDAO.put(new EventLog(new Date(), subject, title + "<br>" + body));
					
					sendGridAlertMail(subject, title, body);
					
				}

				@Override
				protected void onTradeFail(ExchangeException e) {
					
					if (e instanceof InstantTradeException) {
						
						InstantTradeException i = ((InstantTradeException)e);
						
						String subject = ex.getClass().getName().replace("com.eoss.hft.exchange.", "") + ": Coin Moved to " + i.amount.getName();
						String title = "Coin Moved" + i.amount;
						String body = i.getMessage().replace(System.lineSeparator(), "<br>") + "Current Rate=" + i.rate;
								
						eventLogDAO.put(new EventLog(new Date(), subject, title + "<br>" + body));
						
						sendGridAlertMail(subject, title, body);						
						return;
					}
					
					if (e instanceof OrderException) {
						
						OrderException o = ((OrderException)e);
						
						String subject = ex.getClass().getName().replace("com.eoss.hft.exchange.", "") + ": Order Error! Please check Tx Log";
						String title = o.type + ":" + o.pair + ":" + o.rate + ":" + o.amount;
						String body = o.getMessage().replace(System.lineSeparator(), "<br>");;
						
						eventLogDAO.put(new EventLog(new Date(), subject, title + "<br>" + body));
						
						sendGridAlertMail(subject, title, body);
						return;
					}
					
				}

			};
			
			fallbackStrategy = new FallbackStrategy(ex, delay, fallDAO, pairMap) {

				@Override
				protected void onTradeSuccess(Fall fall, double amount, double rate) {
					
					String subject = ex.getClass().getName().replace("com.eoss.hft.exchange.", "") + ":Fallback! You got " + new Currency(fall.target.getName(), amount);
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
		
		String cmd = req.getParameter("cmd");
		
		if (cmd!=null) {
			
			resp.setContentType("text/html");
			resp.setCharacterEncoding("UTF-8");
			
			PrintWriter out = resp.getWriter();
			out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
			
			if (cmd.equals("sell")) {
				
				String pairString = req.getParameter("pair");
				
				Pair pair = pairMap.get(pairString);
				
				pair.fetch(ex);
				
				double amount = ex.getAvailableBalance(pair.base).getAmount();	
				
				if (pair.bidAmount < amount)
					amount = pair.bidAmount;
				
				try {
					ex.sell(pair, pair.bid, new Currency(pair.base, amount));
					resp.getWriter().println("Succeed");
				} catch (ExchangeException e) {
					resp.getWriter().println(e.getMessage()+":"+String.format("%.8f", pair.bid)+":"+String.format("%.8f", amount));
				}			
				
			} else if (cmd.equals("debug")) {
				
				resp.getWriter().println(EventLog.sf.format(new Date(TriangularArbitrageStrategy.maxTime))+"<br>"+TriangularArbitrageStrategy.maxRoute+":"+TriangularArbitrageStrategy.maxRate);
				resp.getWriter().println("<hr>");
				
				resp.getWriter().println("<b>Candidated Routes (Rate >= " + candidatedScore + ")</b><br>");
				for (Map.Entry<Route, Double> entry:triangularStrategy.candidatedRoutes.entrySet()) {
					resp.getWriter().println(entry.getKey()+":"+entry.getValue()+"<br>");	
				}
				resp.getWriter().println("<hr>");
				
				if (triangularStrategy.candidatedRoutes.isEmpty())
					resp.getWriter().println("<b>Reselection Routes</b> (" + triangularStrategy.suspendedRoutes.size() +") <br>");
				else
					resp.getWriter().println("<b>Suspended Routes</b> (" + triangularStrategy.suspendedRoutes.size() +") <br>");
					
				for (Map.Entry<Route, Double> entry:triangularStrategy.suspendedRoutes.entrySet()) {
					resp.getWriter().println(entry.getKey()+":"+entry.getValue()+"<br>");	
				}
			}
			
			return;
		}
		
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime < interval * 1000) {
			try {
				
				triangularStrategy.process();				
				Thread.sleep(processDelay);
				
			} catch (Exception e) {
			} 
		}
		
		try { Thread.sleep(delay); } catch (InterruptedException e) {}
		
		fallbackStrategy.process();
	}
	
	private void logRate(Route route, double rate) {
		
		Entity arb = new Entity("RATELOG");
		arb.setProperty("Date", new Date());
		arb.setProperty("ROUTE", route.toString());
		arb.setProperty("Rate", rate);
		
		datastore.put(arb);
		
	}	
		
	private void logSuccessTx(String route, String currencyName, double earn, double rate) {
		
		Entity arb = new Entity("TXLOG");
		arb.setProperty("Date", new Date());
		arb.setProperty("ROUTE", route);
		arb.setProperty("Success", true);
		arb.setProperty("Currency", currencyName);
		arb.setProperty("Earn", earn);
		arb.setProperty("Rate", rate);
		
		datastore.put(arb);
		
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
	
	private static double roundHalfUp(double value, int places) {
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();	    
	}
}