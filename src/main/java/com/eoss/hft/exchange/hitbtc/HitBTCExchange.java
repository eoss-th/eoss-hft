package com.eoss.hft.exchange.hitbtc;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Base64;

public class HitBTCExchange extends Exchange {

	double fee = 0.001;
	
	@Override
	public Map<String, Pair> pairMap() {
		
		JSONArray array = new JSONArray(get("https://api.hitbtc.com/api/2/public/symbol"));
		
		Pair [] pairs = new Pair[array.length()];
		
		JSONObject jsonPair;
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
		}
		
		return pairMap(pairs);
	}
	
	@Override
	public Order[] fetchOrders(String paringId) {
		
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
	}
	
	public Currency getAvailableBalance(String name) {
		
		String keySecret = key + ":" + secret;			
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic " + Base64.encodeBase64String((keySecret.getBytes())));
		
		String response = get("https://api.hitbtc.com/api/2/trading/balance", headers);
		JSONArray array = new JSONArray(response);
		
		JSONObject wallet;
		for (int i=0; i<array.length(); i++) {
			wallet = array.getJSONObject(i);
			if (wallet.getString("currency").equals(name)) {
				return new Currency(name, wallet.getDouble("available"));			
			}
		}
		
		throw new RuntimeException(response);
	}
	
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		String keySecret = key + ":" + secret;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic " + Base64.encodeBase64String((keySecret.getBytes())));
		
		Map<String, String> params = new HashMap<>();
		params.put("symbol", pair.id);
		params.put("side", "buy");
		params.put("quantity", String.format("%.8f", Pair.floor(amount.getAmount() * (1-pair.fee) / rate, pair.amountPlaces)));
		params.put("price", String.format("%.8f", rate));
		params.put("type", "limit");
		params.put("timeInForce", "IOC");
		
		JSONObject object = new JSONObject(post("https://api.hitbtc.com/api/2/order", headers, queryString(params)));
		
        if (object.getString("timeInForce").equals("IOC")) {
        		return object.getString("status").equals("filled")?0:-1;
        }
	
        throw new OrderException(object.toString(), "buy", pair, rate, amount);		
	}
	
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		String keySecret = key + ":" + secret;
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic " + Base64.encodeBase64String((keySecret.getBytes())));
		
		Map<String, String> params = new HashMap<>();
		params.put("symbol", pair.id);
		params.put("side", "sell");
		params.put("quantity", String.format("%.8f", Pair.floor(amount.getAmount(), pair.amountPlaces)));
		params.put("price", String.format("%.8f", rate));
		params.put("type", "limit");
		params.put("timeInForce", "IOC");
		
		JSONObject object = new JSONObject(post("https://api.hitbtc.com/api/2/order", headers, queryString(params)));
		
        if (object.getString("timeInForce").equals("IOC")) {
        		return object.getString("status").equals("filled")?0:-1;
        }

        throw new OrderException(object.toString(), "sell", pair, rate, amount);		
	}
	
	public boolean cancel(Pair pair, long orderId)  throws ExchangeException {		
		return true;			
	}
	
	public static void main(String[]args) {
		String json = "{\n" + 
				"        \"id\": 0,\n" + 
				"        \"clientOrderId\": \"d8574207d9e3b16a4a5511753eeef175\",\n" + 
				"        \"symbol\": \"ETHBTC\",\n" + 
				"        \"side\": \"sell\",\n" + 
				"        \"status\": \"new\",\n" + 
				"        \"type\": \"limit\",\n" + 
				"        \"timeInForce\": \"GTC\",\n" + 
				"        \"quantity\": \"0.063\",\n" + 
				"        \"price\": \"0.046016\",\n" + 
				"        \"cumQuantity\": \"0.000\",\n" + 
				"        \"createdAt\": \"2017-05-15T17:01:05.092Z\",\n" + 
				"        \"updatedAt\": \"2017-05-15T17:01:05.092Z\"\n" + 
				"    }";
		JSONObject object = new JSONObject(json);
		System.out.println(object.getString("status2"));
	}
		
}
