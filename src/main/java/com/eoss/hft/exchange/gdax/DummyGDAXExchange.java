package com.eoss.hft.exchange.gdax;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyGDAXExchange extends DummyExchange {

	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONArray array = new JSONArray(get("https://api.gdax.com/products"));
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			
			Double balance;
			for (int i=0; i<pairs.length; i++) {
				jsonPair = array.getJSONObject(i);
				pairs[i] = new GDAXPair(jsonPair.getString("id"), 
						jsonPair.getString("base_currency"), 
						jsonPair.getString("quote_currency"), 
						0.0025, 
						Double.parseDouble(jsonPair.getString("base_min_size")),
						0,
						0);
				
				balance = wallets.get(pairs[i].base);
				
				if (balance==null)
					balance = 1000.0;
				
				wallets.put(pairs[i].base, balance);
				
				balance = wallets.get(pairs[i].counter);
				
				if (balance==null)
					balance = 1000.0;
				
				wallets.put(pairs[i].counter, balance);
				
			}
			
			return pairMap(pairs);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		try {
			
			JSONObject object = new JSONObject(get("https://api.gdax.com/products/" + paringId + "/book?level=1"));
			
			JSONArray jsonAsks = object.getJSONArray("asks");
			
			JSONArray jsonBids = object.getJSONArray("bids");
			
			Order [] orders = new Order[1];
			
			double bid, bidAmount;
			double ask, askAmount;
			
			JSONArray jsonBid, jsonAsk;
			for (int i=0;i<1;i++) {
				
				jsonAsk = jsonAsks.getJSONArray(i);
				jsonBid = jsonBids.getJSONArray(i);
				
				ask = jsonAsk.getDouble(0);
				askAmount = jsonAsk.getDouble(1);
				
				bid = jsonBid.getDouble(0);
				bidAmount = jsonBid.getDouble(1);
								
				orders[i] = new Order(new Order.Book(Order.Type.Bid, bid, bidAmount), 
						new Order.Book(Order.Type.Ask, ask, askAmount));
			}
			
			return orders;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
}
