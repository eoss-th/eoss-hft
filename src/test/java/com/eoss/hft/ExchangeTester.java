package com.eoss.hft;

import com.eoss.hft.exchange.live.LiveCoinExchange;

public class ExchangeTester {

	public static void main(String[] args) throws Exception {
	
		Exchange ex = new LiveCoinExchange();
		
		ex.setKey("4sKS7g6De4ceu15ETJHGCeH32wENgvhV");
		ex.setSecret("mQjf2TQMqxm8SQFBdxYmyxaE6U7qxmGv");
		
		Currency balance = ex.getAvailableBalance("BTC");
		System.out.println(balance);
		
		Pair pair = ex.pairMap().get("WIC/BTC");
		
		pair.fetch(ex);
		
		double amount = balance.getAmount();
		
		//2272.53672727
//		System.out.println("buy :" + Pair.floor(amount * (1-0.002) / pair.ask, pair.amountPlaces));
//		long orderId = ex.buy(pair, pair.ask, new Currency("BTC", amount));
		
//		long orderId = ex.sell(pair, pair.bid, new Currency("WIC", amount));
		
//		if (orderId!=0) ex.cancel(pair, orderId);
	}

}
