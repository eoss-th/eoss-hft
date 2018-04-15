package com.eoss.hft;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Route;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;
import com.eoss.hft.exception.InstantTradeException;
import com.eoss.hft.exchange.abu.DummyABUCoinsExchange;
import com.eoss.hft.exchange.binance.DummyBinanceExchange;
import com.eoss.hft.exchange.bx.DummyBXExchange;
import com.eoss.hft.exchange.cex.DummyCEXExchange;
import com.eoss.hft.exchange.gdax.DummyGDAXExchange;
import com.eoss.hft.exchange.hitbtc.DummyHitBTCExchange;
import com.eoss.hft.exchange.live.DummyLiveCoinExchange;
import com.eoss.hft.strategy.TriangularArbitrageStrategy;

public class TriangularArbitrageStrategyTest {

	public static void main(String[]args) {
		
		//Exchange ex = new DummyBXExchange();
		//Exchange ex = new DummyABUCoinsExchange();
		//Exchange ex = new DummyHitBTCExchange();
		Exchange ex = new DummyCEXExchange();
		//Exchange ex = new DummyGDAXExchange();
		//Exchange ex = new DummyBinanceExchange();
		//Exchange ex = new DummyLiveCoinExchange();
		
		Route [] routes = Route.load(ex.pairMap());
		
		//routes = Route.filterMinAmountPlaces(routes, 2);
		
		//routes = Route.filterMinPricePlaces(routes, 8);
		
		routes = Route.filterCurrencies(routes, Arrays.asList("BTC"));

		TriangularArbitrageStrategy strategy = new TriangularArbitrageStrategy(ex, routes, 200, null, 0.99) {

			@Override
			protected void onTradeSuccess(Route bestRoute, Currency startBalance, Currency amount, Currency endBalance) {
				// TODO Auto-generated method stub
				Toolkit.getDefaultToolkit().beep();
				System.out.println(bestRoute+":"+startBalance+":"+endBalance+":"+"Hoorey!!!!");
			}

			@Override
			protected void onTradeFail(ExchangeException e) {
				
				if (e instanceof OrderException) {
					OrderException orderException = ((OrderException)e);
					System.out.println(orderException.getMessage());
					return;
				}
				
				if (e instanceof InstantTradeException) {
					InstantTradeException tradeException = ((InstantTradeException)e);
					System.out.println(tradeException.getMessage() +":"+tradeException.orderId+":"+tradeException.amount+":"+tradeException.pair);
				}
			}

		};
		
		//System.out.println(strategy.pairMap.get("BTC/PLN").baseMin);
		
		int interval = 60;
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("There are " + strategy.routes.size() + " routes"); 
		for (Route r:strategy.routes) {
			System.out.println(r+":"+r.minAmountPlaces()+":"+r.minPricePlaces());
		}
		
		//int i=0;
		//while (System.currentTimeMillis() - startTime < interval * 1000) {			
		for (int i=0;/*i<strategy.routes.size() * 100*/; i++) {
			
			try {
				
				strategy.process();
				
				if (i % strategy.routes.size() == 0) {
					System.out.println("MAX: " + TriangularArbitrageStrategy.maxRoute + ":" + TriangularArbitrageStrategy.maxRate);
					System.out.println();
				}
				
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//i++;
			
		}
		
		
	}

}
