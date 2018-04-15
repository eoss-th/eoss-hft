package com.eoss.hft.exchange.cex;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyCEXExchange extends DummyExchange {

	double fee = 0.0025;
	
	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONObject object = new JSONObject(get("https://cex.io/api/currency_limits"));
			
			JSONObject data = object.getJSONObject("data");
			
			JSONArray array = data.getJSONArray("pairs");
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			Double balance;
			for (int i=0; i<array.length(); i++) {
				jsonPair = array.getJSONObject(i);
				pairs[i] = new CEXPair(jsonPair.getString("symbol1") + "/" + jsonPair.getString("symbol2"), 
						jsonPair.getString("symbol1"), 
						jsonPair.getString("symbol2"), 
						fee, 
						0.0,
						0.0,
						7);
				
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
			
			JSONObject object = new JSONObject(get("https://cex.io/api/order_book/" + paringId + "/?depth=1"));
			
			JSONArray jsonBids = object.getJSONArray("bids");			
			
			JSONArray jsonAsks = object.getJSONArray("asks");			
			
			int len = 1;
			
			Order [] orders = new Order[len];
			
			double ask, askAmount;
			double bid, bidAmount;
			
			JSONArray jsonBid, jsonAsk;
			
			for (int i=0;i<orders.length;i++) {
				
				jsonBid = jsonBids.getJSONArray(i);
				jsonAsk = jsonAsks.getJSONArray(i);
				
				bid = jsonBid.getDouble(0);
				bidAmount = jsonBid.getDouble(1);
				
				ask = jsonAsk.getDouble(0);
				askAmount = jsonAsk.getDouble(1);				
				
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
