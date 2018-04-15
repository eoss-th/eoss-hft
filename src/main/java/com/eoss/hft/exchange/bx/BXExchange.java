package com.eoss.hft.exchange.bx;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;

public class BXExchange extends Exchange {

	private final Map<String, String> headers;
	
	public BXExchange() {
		headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");		
	}
	
	@Override
	public Map<String, Pair> pairMap() {		
		JSONObject object = new JSONObject(get("https://bx.in.th/api/pairing/"));
		
		String [] names = JSONObject.getNames(object);
		
		Pair [] pairs = new Pair[names.length];
		
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
					8);
		}
		
		return pairMap(pairs);
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
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
	}
	
	public Currency getAvailableBalance(String name) {
		
		long nonce = System.currentTimeMillis();			
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(key+nonce+secret)));
					
		JSONObject object = new JSONObject(post("https://bx.in.th/api/balance/", headers, queryString(params)));	
		
		if (object.getBoolean("success")) {
			JSONObject balance = object.getJSONObject("balance");
			JSONObject wallet = balance.getJSONObject(name);
			String available = wallet.getString("available");
			return new Currency(name, Double.parseDouble(available));
		}
					
		throw new RuntimeException(object.toString());
		
	}
	
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		long nonce = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(key+nonce+secret)));
		params.put("pairing", pair.id);
		params.put("type", "buy");
		params.put("amount", String.format("%.8f", amount.getAmount()));
		params.put("rate", String.format("%.8f", rate));
		
		JSONObject object = new JSONObject(post("https://bx.in.th/api/order/", headers, queryString(params)));
		
		if (object.getBoolean("success")) {
			return object.getInt("order_id");
		}
					
		throw new OrderException(object.toString(), "buy", pair, rate, amount);
		
	}
	
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		long nonce = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(key+nonce+secret)));
		params.put("pairing", pair.id);
		params.put("type", "sell");
		params.put("amount", String.format("%.8f", amount.getAmount()));
		params.put("rate", String.format("%.8f", rate));
		
		JSONObject object = new JSONObject(post("https://bx.in.th/api/order/", headers, queryString(params)));
		
		if (object.getBoolean("success")) {
			return object.getInt("order_id");
		}
					
		throw new OrderException(object.toString(), "sell", pair, rate, amount);
		
	}
	
	public boolean cancel(Pair pair, long orderId)  throws ExchangeException {
		
		long nonce = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("key", key);
		params.put("nonce", "" + nonce);
		params.put("signature", bytesToHex(hash(key+nonce+secret)));
		params.put("pairing", pair.id);
		params.put("order_id", "" + orderId);
		
		JSONObject object = new JSONObject(post("https://bx.in.th/api/cancel/", headers, queryString(params)));
		
		if (object.getBoolean("success")) {
			return true;
		}
		
		throw new CancelException(object.toString());		
	}

}
