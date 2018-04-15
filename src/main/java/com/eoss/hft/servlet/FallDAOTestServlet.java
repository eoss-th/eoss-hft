package com.eoss.hft.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eoss.hft.Currency;
import com.eoss.hft.Fall;
import com.eoss.hft.datastore.FallDAO;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class FallDAOTestServlet extends HttpServlet {

	private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		FallDAO fallDAO = new FallDAO(datastore);
		
		String start = req.getParameter("start");
		
		String target = req.getParameter("target");
		
		if (start!=null && target!=null) {
			String [] startTokens = start.split(" ");
			String [] targetTokens = target.split(" ");
			Currency startCurrency = new Currency(startTokens[1], Double.parseDouble(startTokens[0]));
			Currency targetCurrency = new Currency(targetTokens[1], Double.parseDouble(targetTokens[0]));
			
			Fall fall = fallDAO.get(startCurrency.getName(), targetCurrency.getName());
			if (fall==null) {
				fall = new Fall(startCurrency, targetCurrency);
			} else {
				fall = fall.add(new Fall(startCurrency, targetCurrency));
			}
			
			fallDAO.put(fall);
		}
		
		String startCurrency = req.getParameter("startCurrency");
		
		List<Fall> fallList;
		if (startCurrency==null) {
			fallList = fallDAO.get();
		} else {
			fallList = fallDAO.get(startCurrency);
		}
		
		PrintWriter out = resp.getWriter();
		
		for (Fall fall:fallList) {
			out.println(fall + "<br>");
		}
		
		out.println("Total:" + Fall.totalAmount(fallList));
	}
	

}
