package com.eoss.hft;

import java.awt.Toolkit;
import java.util.Set;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Pair;
import com.eoss.hft.Route;
import com.eoss.hft.exchange.bx.BXExchange;
import com.eoss.hft.exchange.bx.DummyBXExchange;
import com.eoss.hft.exchange.hitbtc.DummyHitBTCExchange;

public class RouteRateTester {

	public static void main(String[]args) {
		
		Exchange ex = new DummyHitBTCExchange();
		Route [] routes = Route.load(ex.pairMap());
		
		for (Route r:routes) {
			System.out.println(r);
		}
		
		Set<Pair> pairSet = Route.pairSet(ex, routes);
		
		double rate;
		
		while (true) {
			
			for (Route route:routes) {
								
				if (route.fetch(ex)==null) continue;
				
				rate = route.test(new Currency(route.targetCurrencyName, 1)).getAmount();
				if (rate>=0.98) {
					if (rate>1) {
						Toolkit.getDefaultToolkit().beep();
					}
					System.out.println(route+"\t"+rate);					
				}
				
			}
			System.out.println();
			
			try {
				Thread.sleep(5*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
