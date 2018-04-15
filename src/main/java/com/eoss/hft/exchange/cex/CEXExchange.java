package com.eoss.hft.exchange.cex;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;

public class CEXExchange extends Exchange {

	double fee = 0.0025;
	String userid = "";
	
	@Override
	public Map<String, Pair> pairMap() {
		
		try {
			
			JSONObject object = new JSONObject(get("https://cex.io/api/currency_limits"));
			
			JSONObject data = object.getJSONObject("data");
			
			JSONArray array = data.getJSONArray("pairs");
			
			Pair [] pairs = new Pair[array.length()];
			
			JSONObject jsonPair;
			for (int i=0; i<array.length(); i++) {
				jsonPair = array.getJSONObject(i);
				pairs[i] = new CEXPair(jsonPair.getString("symbol1") + "/" + jsonPair.getString("symbol2"), 
						jsonPair.getString("symbol1"), 
						jsonPair.getString("symbol2"), 
						fee, 
						0.0,
						0.0,
						7);				
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
	
	public Currency getAvailableBalance(String name) {
		
		long nonce = System.currentTimeMillis();			
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(nonce+userid+key)));
		
		JSONObject object = new JSONObject(post("https://cex.io/api/balance/", null, jsonString(params)));	
		
		JSONObject wallet = object.getJSONObject(name);
		String available = wallet.getString("available");
		
		return new Currency(name, Double.parseDouble(available));
	}	
		
	@Override
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		long nonce = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(nonce+userid+key)));
		params.put("pairing", pair.id);
		params.put("type", "buy");
		params.put("amount", String.format("%.8f", amount.getAmount()));
		params.put("price", String.format("%.8f", rate));
		
		JSONObject object = new JSONObject(post("https://cex.io/api/place_order/" + pair.id, null, jsonString(params)));
		
		try {
			
			if (!object.getBoolean("complete")) {
				return object.getInt("order_id");
			}
			
		} catch (JSONException e) {
			
			
		} catch (Exception e) {
			throw new OrderException(object.toString(), "buy", pair, rate, amount);			
		}
				
		return 0;
	}
	
	@Override
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		long nonce = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(nonce+userid+key)));
		params.put("pairing", pair.id);
		params.put("type", "sell");
		params.put("amount", String.format("%.8f", amount.getAmount()));
		params.put("price", String.format("%.8f", rate));
		
		JSONObject object = new JSONObject(post("https://cex.io/api/place_order/" + pair.id, null, jsonString(params)));
		
		try {
			
			if (!object.getBoolean("complete")) {
				return object.getInt("order_id");
			}
			
		} catch (JSONException e) {
			
			
		} catch (Exception e) {
			throw new OrderException(object.toString(), "buy", pair, rate, amount);			
		}
				
		return 0;
	}
	
	public boolean cancel(Pair pair, long orderId)  throws ExchangeException {
		
		long nonce = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(nonce+userid+key)));
		params.put("order_id", "" + orderId);
		
		JSONObject object = null;
		try {
			object = new JSONObject(post("https://cex.io/api/cancel_orders/" + pair.id, null, jsonString(params)));			
		} catch (Exception e) {
			throw new CancelException(object.toString());					
		}
		
		return true;		
	}
	
}
