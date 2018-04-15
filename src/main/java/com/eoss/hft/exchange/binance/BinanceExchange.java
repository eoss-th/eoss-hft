package com.eoss.hft.exchange.binance;

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
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Hex;

public class BinanceExchange extends Exchange {

	private static final String contentType = "application/x-www-form-urlencoded";
	
	@Override
	public Map<String, Pair> pairMap() {
		
		JSONObject object = new JSONObject(get("https://api.binance.com/api/v1/exchangeInfo"));
		JSONArray array = object.getJSONArray("symbols");
		
		Pair [] pairs = new Pair[array.length()];
		
		JSONObject jsonPair;
		
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
			
		}
		
		return pairMap(pairs);
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
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
	}
	
    private  String sign(String queryParameters, String bodyParamsters) {
    		try {
    	        String queryArgs = queryParameters + bodyParamsters;        
    	        Mac shaMac = Mac.getInstance("HmacSHA256");
    	        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes("ASCII"), "RAW");
    	        shaMac.init(keySpec);
    	        final byte[] macData = shaMac.doFinal(queryArgs.getBytes());
    	        return Hex.encodeHexString(macData);    			
    		} catch (Exception e) {
    			throw new RuntimeException(e);
    		}
   }
    
	public Currency getAvailableBalance(String name) {
		
		Map<String, String> headers = new HashMap<>();
		headers.put("X-MBX-APIKEY", key);
		
        long timestamp = System.currentTimeMillis();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("timestamp", "" + timestamp);	        
        String queryParams = queryString(queryMap);	        
        String signature = sign(queryParams, "");
        
        String response = get("https://api.binance.com/api/v3/account?" + queryParams + "&signature=" + signature, headers);
		JSONObject object = new JSONObject(response);
					
		JSONArray array = object.getJSONArray("balances");
		
		JSONObject balance;
		String asset;
		for (int i=0;i<array.length();i++) {
			balance = array.getJSONObject(i);
			asset = balance.getString("asset");
			if (asset.equals(name)) {					
				return new Currency(name, Double.parseDouble(balance.getString("free")));	
			}
		}
		
		throw new RuntimeException(response);
	}
	
	@Override
	public boolean cancel(Pair pair, long orderId) throws ExchangeException {
		return true;
	}

	@Override
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("X-MBX-APIKEY", key);
		
        long timestamp = System.currentTimeMillis();
        
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("timestamp", "" + timestamp);	        
        String queryParams = queryString(queryMap);
        
		Map<String, String> params = new HashMap<>();
		params.put("symbol", pair.id);
		params.put("side", "BUY");
		params.put("type", "LIMIT");
		params.put("timeInForce", "IOC");
		params.put("quantity", String.format("%.8f", Pair.floor(amount.getAmount() * (1-pair.fee) / rate, pair.amountPlaces)));
		params.put("price", String.format("%.8f", rate));
        
		String bodyParams = queryString(params);			
        String signature = sign(queryParams, bodyParams);
        
        JSONObject object = new JSONObject(post("https://api.binance.com/api/v3/order?" + queryParams + "&signature=" + signature, headers, bodyParams));
		
        if (object.getString("timeInForce").equals("IOC")) {
        		return object.getString("status").equals("FILLED")?0:-1;
        }
		
		throw new OrderException(object.toString(), "buy", pair, rate, amount);		
	}

	@Override
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("X-MBX-APIKEY", key);
		
        long timestamp = System.currentTimeMillis();
        
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("timestamp", "" + timestamp);	        
        String queryParams = queryString(queryMap);
        
		Map<String, String> params = new HashMap<>();
		params.put("symbol", pair.id);
		params.put("side", "SELL");
		params.put("type", "LIMIT");
		params.put("timeInForce", "IOC");
		params.put("quantity", String.format("%.8f", Pair.floor(amount.getAmount(), pair.amountPlaces)));
		params.put("price", String.format("%.8f", rate));
        
		String bodyParams = queryString(params);
        String signature = sign(queryParams, bodyParams);
        
        JSONObject object = new JSONObject(post("https://api.binance.com/api/v3/order?" + queryParams + "&signature=" + signature, headers, bodyParams));
		
        if (object.getString("timeInForce").equals("IOC")) {
        		return object.getString("status").equals("FILLED")?0:-1;
        }
		
		throw new OrderException(object.toString(), "sell", pair, rate, amount);
	}
	
	public static void main(String[]args) {
		int amountPlaces = Pair.getNumberOfDecimalPlaces(Double.parseDouble("1.10000000"));
		System.out.println(amountPlaces);
		System.out.println(String.format("%.8f", Pair.round(78.49100000, amountPlaces)));
	}
	
}
