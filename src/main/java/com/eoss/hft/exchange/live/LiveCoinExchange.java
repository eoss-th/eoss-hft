package com.eoss.hft.exchange.live;

import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;

public class LiveCoinExchange extends Exchange {

	@Override
	public Map<String, Pair> pairMap() {
		
		JSONObject object = new JSONObject(get("https://api.livecoin.net/exchange/restrictions"));
		JSONArray array = object.getJSONArray("restrictions");
		
		Pair [] pairs = new Pair[array.length()];
		
		JSONObject jsonPair;
		
		Double balance;
		String id;
		String [] names;
		double fee;
		for (int i=0; i<pairs.length; i++) {
			jsonPair = array.getJSONObject(i);
			id = jsonPair.getString("currencyPair");
			
			if (id.equals("XSPEC/BTC"))
				fee = 0.003;
			else if (id.equals("WIC/BTC"))
				fee = 0.002;
			else
				fee = 0.0018;
			
			names = id.split("/");
			pairs[i] = new LiveCoinPair(id, 
					names[0], 
					names[1], 
					fee, 
					jsonPair.getDouble("minLimitQuantity"),
					0,
					8,
					jsonPair.getInt("priceScale"));				
		}
		
		return pairMap(pairs);
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		
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
	}
	
	private String createSignature(String paramData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(paramData.getBytes());
            return byteArrayToHexString(hmacData).toUpperCase();
        } catch (Exception e) {
        		throw new RuntimeException(e);
        }
    }
	
	private static String buildQueryString(Map<String, String> args) {
        StringBuilder result = new StringBuilder();
        for (String hashKey : args.keySet()) {
            if (result.length() > 0) result.append('&');
            try {
                result.append(URLEncoder.encode(hashKey, "UTF-8"))
                    .append("=").append(URLEncoder.encode(args.get(hashKey), "UTF-8"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    
	}	
	private String byteArrayToHexString(byte[] bytes) {
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }	

	@Override
	public Currency getAvailableBalance(String name) {
        Map<String, String> queryMap = new TreeMap<>();
        queryMap.put("currency", name);	        
        String queryParams = buildQueryString(queryMap);
		
		Map<String, String> headers = new TreeMap<>();
		headers.put("Api-Key", key);
		headers.put("Sign", createSignature(queryParams));
		
		JSONObject object = new JSONObject(get("https://api.livecoin.net/payment/balance?" + queryParams, headers));
					
		return new Currency(name, object.getDouble("value"));	
	}
	
	private boolean isExecuted(long orderId) {
		try {	        	        
	        Map<String, String> queryMap = new TreeMap<>();
	        queryMap.put("orderId", "" + orderId);	        
	        String queryParams = buildQueryString(queryMap);
			
			Map<String, String> headers = new TreeMap<>();
			headers.put("Api-Key", key);
			headers.put("Sign", createSignature(queryParams));
			
			JSONObject object = new JSONObject(get("https://api.livecoin.net/exchange/order?" + queryParams, headers));						
			return object.getString("status").equals("EXECUTED");			
		} catch (Exception e) {
			return false;
		}		
	}
	
	@Override
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		Map<String, String> params = new TreeMap<>();
		params.put("currencyPair", pair.id);
		params.put("price", "" + rate);
		params.put("quantity", "" + Pair.floor(amount.getAmount() * (1-pair.fee) / rate, pair.amountPlaces));
        String bodyParams = buildQueryString(params);
        
		Map<String, String> headers = new TreeMap<>();
		headers.put("Api-Key", key);
		headers.put("Sign", createSignature(bodyParams));
		
		JSONObject object = new JSONObject(post("https://api.livecoin.net/exchange/buylimit", headers, bodyParams));
		
		if (object.getBoolean("success")) {			
			long orderId = object.getLong("orderId");			
			if (isExecuted(orderId)) return 0;			
			return orderId;			
		}
		
        throw new OrderException(object.toString(), "buy", pair, rate, amount);		
	}

	@Override
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		Map<String, String> params = new TreeMap<>();
		params.put("currencyPair", pair.id);
		params.put("price", "" + rate);
		params.put("quantity", "" + Pair.floor(amount.getAmount(), pair.amountPlaces));
        String bodyParams = buildQueryString(params);
        
		Map<String, String> headers = new TreeMap<>();
		headers.put("Api-Key", key);
		headers.put("Sign", createSignature(bodyParams));
		
		JSONObject object = new JSONObject(post("https://api.livecoin.net/exchange/selllimit", headers, bodyParams));
		
		if (object.getBoolean("success")) {			
			long orderId = object.getLong("orderId");			
			if (isExecuted(orderId)) return 0;			
			return orderId;			
		}
		
        throw new OrderException(object.toString(), "sell", pair, rate, amount);		
	}

	@Override
	public boolean cancel(Pair pair, long orderId) throws ExchangeException {
		
		JSONObject object = null;
		try {
			Map<String, String> params = new TreeMap<>();
			params.put("currencyPair", pair.id);
			params.put("orderId", "" + orderId);
	        String bodyParams = buildQueryString(params);
	        
			Map<String, String> headers = new TreeMap<>();
			headers.put("Api-Key", key);
			headers.put("Sign", createSignature(bodyParams));
			
			object = new JSONObject(post("https://api.livecoin.net/exchange/cancellimit", headers, bodyParams));
			
			if (object.getBoolean("cancelled")) {
				return true;
			}			
			
			throw new CancelException(object.toString());
			
		} catch (CancelException e) {
			throw e;
		} catch (Exception e) {
			if (object!=null)
				throw new RuntimeException(object.toString() + pair + ":" + orderId);
			else
				throw new RuntimeException(e);
		}
		
	}
	
}
