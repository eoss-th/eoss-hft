package com.eoss.hft.exchange.live;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyLiveCoinExchange extends DummyExchange {

	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONObject object = new JSONObject(get("https://api.livecoin.net/exchange/restrictions"));
			JSONArray array = object.getJSONArray("restrictions");
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			
			Double balance;
			String id;
			String [] names;
			for (int i=0; i<pairs.length; i++) {
				jsonPair = array.getJSONObject(i);
				id = jsonPair.getString("currencyPair");
				names = id.split("/");
				pairs[i] = new LiveCoinPair(id, 
						names[0], 
						names[1], 
						0.0018, 
						jsonPair.getDouble("minLimitQuantity"),
						0,
						8,
						jsonPair.getInt("priceScale"));
				
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
			
			JSONObject object = new JSONObject(get("https://api.livecoin.net/exchange/order_book?currencyPair=" + paringId + "&_="+System.currentTimeMillis()+"&depth=1"));
			
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
