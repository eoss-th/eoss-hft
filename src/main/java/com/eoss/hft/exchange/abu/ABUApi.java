package com.eoss.hft.exchange.abu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import com.google.appengine.repackaged.com.google.api.client.util.Base64;

public class ABUApi {

    private final String URL = "https://api.abucoins.com";
    private final String accessKey = "10530894-QNYDHZ40FMBX1VUTG8PIEROALK539W6C";
    private final String secret = "VFthfVVuI0suamlWOVF3eGB5V0dILTReSU8sZXJYITJCRlA6MWgkbHQ3PyVmQVk1";
    
    private final String passphrase = "amou6a5nk";
    private final String USER_AGENT = "Mozilla/5.0";

    private String sendRequest(String method,String path, String body) throws Exception {
        String localUrl;
        if (method.equals("GET"))
            localUrl = URL+path+body;
        else
            localUrl = URL+path;

        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        String sign = createSign(timestamp,method,path,body);

        URL url = new URL(localUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("AC-ACCESS-KEY", accessKey);
        con.setRequestProperty("AC-ACCESS-SIGN", sign);
        con.setRequestProperty("AC-ACCESS-PASSPHRASE", passphrase);
        con.setRequestProperty("AC-ACCESS-TIMESTAMP", timestamp);

        if (method.equals("POST")){
            con.setRequestProperty("Content-type", "application/json");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();
        }
        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private String createSign(String timestamp, String method, String path, String queryParameters) throws NoSuchAlgorithmException, InvalidKeyException{
        String queryArgs = timestamp + method + path + queryParameters;
        Mac shaMac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(Base64.decodeBase64(secret), "HmacSHA256");
        shaMac.init(keySpec);
        final byte[] macData = shaMac.doFinal(queryArgs.getBytes());
        return Base64.encodeBase64String(macData);
    }
    
    public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
    		ABUApi api = new ABUApi();
    		String response;
    		response = api.sendRequest("GET", "/accounts/10530894-PLN", "");
    		System.out.println(response);
    		
    		/*
		Map<String, String> params = new HashMap<>();
		params.put("product_id", "ETH-BTC");
		params.put("side", "buy");
		params.put("size", "" + 0.01);
		params.put("price", "" + 20);
		params.put("type", "limit");
		params.put("time_in_force", "IOC");
		
		System.out.println(new JSONObject(params).toString());
    		*/
	}

}
