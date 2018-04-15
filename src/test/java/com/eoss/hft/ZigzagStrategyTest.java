package com.eoss.hft;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.eoss.hft.Exchange;
import com.eoss.hft.Fall;
import com.eoss.hft.Pair;
import com.eoss.hft.Route;
import com.eoss.hft.datastore.FallMemoryDAO;
import com.eoss.hft.exchange.abu.DummyABUCoinsExchange;
import com.eoss.hft.exchange.binance.DummyBinanceExchange;
import com.eoss.hft.exchange.bx.DummyBXExchange;
import com.eoss.hft.exchange.hitbtc.DummyHitBTCExchange;
import com.eoss.hft.strategy.ZigzagStrategy;

public class ZigzagStrategyTest {

	public static void main(String[]args) {
		
		//final Exchange ex = new DummyBXExchange();
		//final Exchange ex = new DummyABUCoinsExchange();
		final Exchange ex = new DummyHitBTCExchange();
		//final Exchange ex = new DummyCEXExchange();
		//final Exchange ex = new DummyGDAXExchange();
		//final Exchange ex = new DummyBinanceExchange();
		
		ZigzagStrategy strategy = new ZigzagStrategy(ex, 50, new FallMemoryDAO(), ex.pairMap(), Arrays.asList("BCH/BTC")) {

			@Override
			protected void onTradeSuccess(Fall fall, double amount, double rate) {
				// TODO Auto-generated method stub
				Toolkit.getDefaultToolkit().beep();
				System.out.println(fall+":"+amount+":"+rate+":Hoorey!!!"+":"+ex.getAvailableBalance(fall.target.getName()));
				
			}
			
		};
		
		while (true) {
			
			try {
				
				strategy.process();
				
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
	}

}
