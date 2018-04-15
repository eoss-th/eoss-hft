package com.eoss.hft.exchange.hitbtc;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyHitBTCExchange extends DummyExchange {

	double fee = 0.001;
		
	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONArray array = new JSONArray(get("https://api.hitbtc.com/api/2/public/symbol"));
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			Double balance;
			for (int i=0; i<array.length(); i++) {
				jsonPair = array.getJSONObject(i);
				pairs[i] = new HitBTCPair(jsonPair.getString("id"), 
						jsonPair.getString("baseCurrency"), 
						jsonPair.getString("quoteCurrency"), 
						fee, 
						Double.parseDouble(jsonPair.getString("quantityIncrement")),
						Double.parseDouble(jsonPair.getString("tickSize")),
						Pair.getNumberOfDecimalPlaces(Double.parseDouble(jsonPair.getString("quantityIncrement"))),
						Pair.getNumberOfDecimalPlaces(Double.parseDouble(jsonPair.getString("tickSize"))));
				
				balance = wallets.get(pairs[i].base);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].base, balance);
				
				balance = wallets.get(pairs[i].counter);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].counter, balance);
				
			}
			
			wallets.put("BTC", 0.02);
			
			return pairMap(pairs);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		try {
			
			JSONObject object = new JSONObject(get("https://api.hitbtc.com/api/2/public/orderbook/" + paringId + "?_="+System.currentTimeMillis() + "&limit=1"));
			
			JSONArray jsonAsks = object.getJSONArray("ask");
			
			JSONArray jsonBids = object.getJSONArray("bid");			
			
			int len = 1;
			
			Order [] orders = new Order[len];
			
			double ask, askAmount;
			double bid, bidAmount;
			
			JSONObject jsonBid, jsonAsk;
			
			for (int i=0;i<orders.length;i++) {
				
				jsonAsk = jsonAsks.getJSONObject(i);
				jsonBid = jsonBids.getJSONObject(i);
				
				ask = jsonAsk.getDouble("price");
				askAmount = jsonAsk.getDouble("size");
				
				bid = jsonBid.getDouble("price");
				bidAmount = jsonBid.getDouble("size");
				
				orders[i] = new Order(new Order.Book(Order.Type.Bid, bid, bidAmount), 
						new Order.Book(Order.Type.Ask, ask, askAmount));
			}
			
			return orders;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}
		
}
