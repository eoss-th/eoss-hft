package com.eoss.hft;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Pair;
import com.eoss.hft.Route;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.InstantTradeException;
import com.eoss.hft.exchange.bx.BXExchange;
import com.eoss.hft.exchange.bx.DummyBXExchange;

public class PairFetchTester {
	
	private static Currency trade(int i) {
		
		if (i==0) {
			
			throw new RuntimeException();
						
		} else if (i==1) {
			
			throw new RuntimeException();
		}
	
		throw new IllegalArgumentException();
	}
	
	public static void main(String[] args) {
		
		Exchange ex = new DummyBXExchange();
		
		Route [] routes = Route.load(ex.pairMap());
		
		trade(0);
		
		/*

		double fee = 0.0025;
		
		Pair btc_thb = new Pair("1", "BTC", "THB", fee);
		
		Pair eth_thb = new Pair("21", "ETH", "THB", fee);
		
		Pair eth_btc = new Pair("20", "ETH", "BTC", fee);
		
		Pair [][] routes1 = { 
				{btc_thb, eth_btc, eth_thb},
				{eth_thb, eth_btc, btc_thb}
		};
						
		Pair [][] routes2 = { 
				{btc_thb, eth_thb, eth_btc},
				{eth_btc, eth_thb, btc_thb},
		};
						
		Pair [][] routes3 = { 
				{eth_thb, btc_thb, eth_btc},
				{eth_btc, btc_thb, eth_thb}
		};
		
		Currency money;
		while (true) {
			btc_thb.fetch(ex);
			eth_thb.fetch(ex);
			eth_btc.fetch(ex);
			
			System.out.println("THB");
			for (Pair[]route:routes1) {
				
				money = new Currency("THB", 1);
				for (Pair pair:route) {
					money = Pair.forward(money, pair);
				}
				System.out.print("\t");
				System.out.println(money);
				
			}
			
			System.out.println("BTC");
			for (Pair[]route:routes2) {
				
				money = new Currency("BTC", 1);
				for (Pair pair:route) {
					money = Pair.forward(money, pair);
				}
				
				System.out.print("\t");
				System.out.println(money);
				
			}
			
			System.out.println("ETH");
			for (Pair[]route:routes3) {
				
				money = new Currency("ETH", 1);
				for (Pair pair:route) {
					money = Pair.forward(money, pair);
				}
				
				System.out.print("\t");
				System.out.println(money);				
			}
			
			System.out.println("======");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}

}
