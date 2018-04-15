package com.eoss.hft.exchange.lakebtc;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;
 
class LakebtcAuthentication{
 
	private static final String ACCESS_KEY = "Your Email";
	private static final String PRIVATE_KEY = "Your Private_key";
	private static final String LAKE_URL = "https://api.lakebtc.com/api_v2/";
	private static final String H_SHA1 = "HmacSHA1";
	
	public static void main(String args[]) throws Exception{
		//Public API
		System.out.println(ticker());
		System.out.println(bcorderbook());
		System.out.println(bcorderbook_cny());
		
		//Private API
		//System.out.println(getAccountInfo());
		//System.out.println(buyOrder("500,0.001,USD"));
		//System.out.println(sellOrder("300,0.001,USD"));
		//System.out.println(getOrders());
		//System.out.println(getTrades("1403078138"));
	}
	
	private static String prepare_params(String str){
		String arr[] = str.split(",");
		String res = "";
		for (String a : arr)
			res += ("\"" + a.trim() + "\"" + ",");
		return str == "" ? "" : res.substring(0, res.lastIndexOf(","));
	}
	
	public static String makeSign(String data, String key) throws Exception {
		SecretKeySpec sign = new SecretKeySpec(key.getBytes(), H_SHA1);
		Mac mac = Mac.getInstance(H_SHA1);
		mac.init(sign);
		byte[] rawHmac = mac.doFinal(data.getBytes());
		 
		return arrayHex(rawHmac);
	}
	
	private static String get_data(String mymethod) throws Exception{
		HttpsURLConnection con=(HttpsURLConnection)new URL(LAKE_URL + '/' + mymethod).openConnection();
		con.setConnectTimeout(5000);
		con.setRequestMethod("GET");
		con.connect();
		BufferedReader in = new BufferedReader(
			new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}
	
	public static String post_data(String mymethod, String str) throws Exception{
		String tonce = ""+(System.currentTimeMillis() * 1000);
		String params = "tonce="+tonce.toString()+"&accesskey="+ACCESS_KEY+"&requestmethod=post&id=1&method=" + mymethod +"&params=" + str;
		String hash = makeSign(params, PRIVATE_KEY);
		URL obj = new URL(LAKE_URL);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		String userpass = ACCESS_KEY + ":" + hash;
		String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		 
		//Header
		con.setRequestMethod("POST");
		con.setRequestProperty("Json-Rpc-Tonce", tonce.toString());
		con.setRequestProperty ("Authorization", basicAuth);
		String postdata = "{\"method\": \"" + mymethod + "\", \"params\": [" + prepare_params(str) + "], \"id\": 1}";
		 
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(postdata);
		wr.flush();
		wr.close();
		 
		int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(
			new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}
	
	private static String arrayHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for(byte b: a)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
	}
	
	private static String ticker() throws Exception{
		return get_data("ticker");
	}
	
	public static String bcorderbook() throws Exception{
		return get_data("bcorderbook");
	}
	
	private static String bcorderbook_cny() throws Exception{
		return get_data("bcorderbook_cny");
	}
	
	private static String getAccountInfo() throws Exception{
		return post_data("getAccountInfo", "");
	}
	
	private static String buyOrder(String str) throws Exception{
		return post_data("buyOrder", str);
	}
	
	private static String sellOrder(String str) throws Exception{
		return post_data("sellOrder", str);
	}
	
	private static String getOrders() throws Exception{
		return post_data("getOrders", "");
	}
	
	private static String cancelOrder(String str) throws Exception{
		return post_data("cancelOrder", str);
	}
	
	private static String getTrades(String str) throws Exception{
		return post_data("getTrades", str);
	}
	
	private static String getTrades() throws Exception{
		return post_data("getTrades", "");
	}
}