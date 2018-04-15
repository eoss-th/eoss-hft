package com.eoss.hft.exchange.bx;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyBXExchange extends DummyExchange {

	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONObject object = new JSONObject(get("https://bx.in.th/api/pairing/"));
			
			String [] names = JSONObject.getNames(object);
			
			Pair [] pairs = new Pair[names.length];
			
			Double balance;
			
			JSONObject jsonPair;
			
			String name;
			for (int i=0; i<names.length; i++) {
				name = names[i];
				jsonPair = object.getJSONObject(name);
				pairs[i] = new BXPair("" + jsonPair.getInt("pairing_id"), 
						jsonPair.getString("secondary_currency"), 
						jsonPair.getString("primary_currency"), 
						0.0025,
						Double.parseDouble(jsonPair.getString("secondary_min")),
						Double.parseDouble(jsonPair.getString("primary_min")),
						8
						);
				
				balance = wallets.get(pairs[i].base);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].base, balance);
				
				balance = wallets.get(pairs[i].counter);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].counter, balance);
				
			}			
			
			wallets.put("THB", 1000.0);
			
			return pairMap(pairs);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		try {
			
			JSONObject object = new JSONObject(get("https://bx.in.th/api/orderbook/?_="+System.currentTimeMillis()+"&pairing=" + paringId));
			
			JSONArray jsonBids = object.getJSONArray("bids");
			
			JSONArray jsonAsks = object.getJSONArray("asks");
			
			int len = 1;
			
			Order [] orders = new Order[len];
			
			double bid, bidAmount;
			double ask, askAmount;
			
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
