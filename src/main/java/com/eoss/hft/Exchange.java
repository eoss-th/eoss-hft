package com.eoss.hft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.eoss.hft.exception.ExchangeException;

public abstract class Exchange {
	
	protected String key;
	protected String secret;
	
	protected long invokedTime;
	
	public void setKey(String key) {
		this.key = key;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public long invokedTime() {
		return invokedTime;
	}

	public abstract Map<String, Pair> pairMap();
	
	public abstract Order [] fetchOrders(String pairingId);

	public abstract Currency getAvailableBalance(String name);
	
	public abstract long buy(Pair pair, double rate, Currency amount) throws ExchangeException;
	
	public abstract long sell(Pair pair, double rate, Currency amount) throws ExchangeException;
	
	public abstract boolean cancel(Pair pair, long orderId) throws ExchangeException;
	
	protected final Map<String, Pair> pairMap(Pair[]pairs) {
		Map<String, Pair> pairMap = new HashMap<>();
		if (pairs!=null) {
			for (Pair pair:pairs) {
				pairMap.put(pair.toString(), pair);
			}
		}
		return pairMap;
	}
	
	protected final String get(String apiURL) {		
		try {
			URL url = new URL(apiURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.addRequestProperty("User-Agent", "Mozilla/4.0");
			
			invokedTime = System.currentTimeMillis();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer json = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null) {
			  json.append(line);
			}
			reader.close();		
			
			return json.toString();			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected final String get(String apiURL, Map<String, String> headerMap) {		
		try {
			URL url = new URL(apiURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();		
			for (Map.Entry<String, String> entry:headerMap.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());			
			}		
			conn.setUseCaches(false);
			conn.addRequestProperty("User-Agent", "Mozilla/4.0");
			
			invokedTime = System.currentTimeMillis();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer json = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null) {
			  json.append(line);
			}
			reader.close();		
			
			return json.toString();			
		} catch (Exception e) {
			throw new RuntimeException(e);			
		}
	}
	
	protected final String post(String apiURL, Map<String, String> headerMap, String body) {
		try {
			URL url = new URL(apiURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			
			if (headerMap!=null) {
				for (Map.Entry<String, String> entry:headerMap.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());			
				}
			}

			conn.getOutputStream().write(body.getBytes("UTF-8"));
			/*
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(body);
			writer.close();
			*/

			invokedTime = System.currentTimeMillis();
			int respCode = conn.getResponseCode();  // New items get NOT_FOUND on PUT
			StringBuffer response = new StringBuffer();
			String line;

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		    reader.close();
		    
		    if (respCode==200)
		    		return response.toString();
		    
		    throw new RuntimeException("Error:" + respCode + "\n" + headerMap + "\n" + body);
		    
		} catch (Exception e) {
			throw new RuntimeException(e);			
		}
	}
	
	protected final String queryString(Map<String, String> map) { 
	    StringBuilder sb = new StringBuilder(); 
	    for (Map.Entry<String, String> entry : map.entrySet()) { 
	      sb.append(entry.getKey()); 
	      sb.append('='); 
	      sb.append(entry.getValue()); 
	      sb.append('&'); 
	    } 
	    if (sb.length() > 0) { 
	      sb.setLength(sb.length() - 1); // Remove the trailing &. 
	    } 
	    return sb.toString(); 
	}	
	
	protected final String jsonString(Map<String, String> map) {
		return new JSONObject(map).toString();
	}
	
	protected final byte [] hash(String text) {		
		try {			
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(text.getBytes(StandardCharsets.UTF_8));			
		} catch (Exception e) {
			throw new RuntimeException(e);			
		}
	}
	
	protected final String bytesToHex(byte[] hash) {
	    StringBuffer hexString = new StringBuffer();
	    for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xff & hash[i]);
	    if(hex.length() == 1) hexString.append('0');
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}		
}
