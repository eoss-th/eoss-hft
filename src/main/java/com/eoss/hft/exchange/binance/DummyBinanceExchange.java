package com.eoss.hft.exchange.binance;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.DummyExchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;

public class DummyBinanceExchange extends DummyExchange {

	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONObject object = new JSONObject(get("https://api.binance.com/api/v1/exchangeInfo"));
			JSONArray array = object.getJSONArray("symbols");
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			
			Double balance;
			
			JSONArray filters;
			double minNominal, minQty;
			int amountPlaces, pricePlaces;
			for (int i=0; i<pairs.length; i++) {
				jsonPair = array.getJSONObject(i);
				
				filters = jsonPair.getJSONArray("filters");
				minQty = Double.parseDouble(filters.getJSONObject(1).getString("minQty"));
				amountPlaces = Pair.getNumberOfDecimalPlaces(Double.parseDouble(filters.getJSONObject(1).getString("stepSize")));
				pricePlaces = Pair.getNumberOfDecimalPlaces(Double.parseDouble(filters.getJSONObject(0).getString("tickSize")));
				minNominal = Double.parseDouble(filters.getJSONObject(2).getString("minNotional"));
				
				pairs[i] = new BinancePair(jsonPair.getString("symbol"), 
						jsonPair.getString("baseAsset"), 
						jsonPair.getString("quoteAsset"), 
						0.001, 
						minQty,
						minNominal,
						amountPlaces,
						pricePlaces);
				
				balance = wallets.get(pairs[i].base);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].base, balance);
				
				balance = wallets.get(pairs[i].counter);
				
				if (balance==null)
					balance = 0.0;
				
				wallets.put(pairs[i].counter, balance);
			}
			
			wallets.put("BTC", 0.007);
			
			return pairMap(pairs);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		try {
			
			JSONObject object = new JSONObject(get("https://api.binance.com/api/v1/depth?symbol=" + paringId + "&limit=5"));
			
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
