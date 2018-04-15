package com.eoss.hft.exchange.abu;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;

public class ABUCoinsExchange extends Exchange {

	private String contentType = "application/json";
	
    private final String API_URL = "https://api.abucoins.com";
    
    private final String profileId = "10530894";
    private final String passphrase = "amou6a5nk";
    	
	@Override
	public Map<String, Pair> pairMap() {
		
		JSONArray array = new JSONArray(get(API_URL + "/products"));
		
		Pair [] pairs = new Pair[array.length()];
		
		JSONObject jsonPair;
		
		for (int i=0; i<pairs.length; i++) {
			jsonPair = array.getJSONObject(i);
			pairs[i] = new ABUPair(jsonPair.getString("id"), 
					jsonPair.getString("base_currency"), 
					jsonPair.getString("quote_currency"), 
					jsonPair.getString("quote_currency").equals("BTC")?0.001:0.0025, 
					Double.parseDouble(jsonPair.getString("base_min_size")),
					0,
					8);
			
		}
		
		return pairMap(pairs);
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		JSONObject object = new JSONObject(get(API_URL + "/products/" + paringId + "/book?_="+System.currentTimeMillis()+"&level=1"));
		
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
	}
	
    private String sign(String timestamp, String method, String path, String queryParameters) {
    		try {
        		String queryArgs = timestamp + method + path + queryParameters;
            Mac shaMac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(Base64.decodeBase64(secret), "HmacSHA256");
            shaMac.init(keySpec);
            byte[] macData = shaMac.doFinal(queryArgs.getBytes());
            return Base64.encodeBase64String(macData);    			
    		} catch (Exception e) {
    			throw new RuntimeException(e);
    		}
    }
    
	public Currency getAvailableBalance(String name) {
		
		String method = "GET";
		String path = "/accounts/" + profileId + "-" + name;
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        
		Map<String, String> headers = new HashMap<>();
		headers.put("AC-ACCESS-KEY", key);
		headers.put("AC-ACCESS-SIGN", sign(timestamp, method, path, ""));
		headers.put("AC-ACCESS-PASSPHRASE", passphrase);
		headers.put("AC-ACCESS-TIMESTAMP", timestamp);
		
		JSONObject object = new JSONObject(get(API_URL + path, headers));
		
		String available = object.getString("available");
		
		return new Currency(name, Double.parseDouble(available));			
	}
	
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		String method = "POST";
		String path = "/orders";
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        	        
		Map<String, String> params = new HashMap<>();
		params.put("product_id", pair.id);
		params.put("side", "buy");
		params.put("size", String.format("%.8f", Pair.floor(amount.getAmount() / rate, pair.amountPlaces)));
		params.put("price", String.format("%.8f", rate));
		params.put("time_in_force", "IOC");
		
		String body = new JSONObject(params).toString();
		
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("AC-ACCESS-KEY", key);
		headers.put("AC-ACCESS-SIGN", sign(timestamp, method, path, body));
		headers.put("AC-ACCESS-PASSPHRASE", passphrase);
		headers.put("AC-ACCESS-TIMESTAMP", timestamp);
		
		JSONObject object = new JSONObject(post(API_URL + path, headers, body));
		
		if (object.getString("status").equals("done")) {
			return object.getBoolean("settled")?0:-1;
		}
					
		throw new OrderException(object.toString(), "buy", pair, rate, amount);		
	}	
	
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		String method = "POST";
		String path = "/orders";
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        	        
		Map<String, String> params = new HashMap<>();
		params.put("product_id", pair.id);
		params.put("side", "sell");
		params.put("size", String.format("%.8f", Pair.floor(amount.getAmount(), pair.amountPlaces)));
		params.put("price", String.format("%.8f", rate));
		params.put("time_in_force", "IOC");
		
		String body = new JSONObject(params).toString();
		
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("AC-ACCESS-KEY", key);
		headers.put("AC-ACCESS-SIGN", sign(timestamp, method, path, body));
		headers.put("AC-ACCESS-PASSPHRASE", passphrase);
		headers.put("AC-ACCESS-TIMESTAMP", timestamp);
		
		JSONObject object = new JSONObject(post(API_URL + path, headers, body));
		
		if (object.getString("status").equals("done")) {
			return object.getBoolean("settled")?0:-1;
		}
					
		throw new OrderException(object.toString(), "sell", pair, rate, amount);	}	
	
	@Override
	public boolean cancel(Pair pair, long orderId) throws ExchangeException {
		return true;
	}
	
}
