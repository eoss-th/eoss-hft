package com.eoss.hft.exchange.abu;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyABUCoinsExchange extends DummyExchange {

	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONArray array = new JSONArray(get("https://api.abucoins.com/products"));
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			
			Double balance;
			for (int i=0; i<pairs.length; i++) {
				jsonPair = array.getJSONObject(i);
				pairs[i] = new ABUPair(jsonPair.getString("id"), 
						jsonPair.getString("base_currency"), 
						jsonPair.getString("quote_currency"), 
						jsonPair.getString("quote_currency").equals("BTC")?0.001:0.0025, 
						Double.parseDouble(jsonPair.getString("base_min_size")),
						0,
						8);
				
				balance = wallets.get(pairs[i].base);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].base, balance);
				
				balance = wallets.get(pairs[i].counter);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].counter, balance);
				
			}
			
			wallets.put("BTC", 0.01);
			
			return pairMap(pairs);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		try {
			
			JSONObject object = new JSONObject(get("https://api.abucoins.com/products/" + paringId + "/book?_="+System.currentTimeMillis()+"&level=1"));
			
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
