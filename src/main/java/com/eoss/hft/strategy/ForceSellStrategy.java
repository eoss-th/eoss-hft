package com.eoss.hft.strategy;

import java.util.Map;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Fall;
import com.eoss.hft.Pair;
import com.eoss.hft.Strategy;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;

public abstract class ForceSellStrategy implements Strategy {
	
	private final Exchange ex;
	
	private final long delay;
	
	private final long processDelay;
	
	private final FallDAO fallDAO;
	
	private final Map<String, Pair> pairMap;	
	
	public ForceSellStrategy(Exchange ex, long delay, long processDelay, FallDAO fallDAO, Map<String, Pair> pairMap) {
		this.ex = ex;
		this.delay = delay;
		this.processDelay = processDelay;
		this.fallDAO = fallDAO;
		this.pairMap = pairMap;
	}
	
	public boolean process() {
		
		Pair pair;
		Currency balance;
		double amount;
		
		Fall fall;
		for (Map.Entry<String, Pair> entry:pairMap.entrySet()) {
			pair = entry.getValue();
			
			while (true) {
				
				fall = fallDAO.get(pair.base, pair.counter);
				
				balance = ex.getAvailableBalance(pair.base);
				if (balance.getAmount() < pair.baseMin) break;
				
				amount = balance.getAmount();
				
				pair = pair.fetch(ex);
				if (pair!=null) {
					if (pair.bidAmount < amount) {
						amount = pair.bidAmount;
					}
					
					try {
						long orderId = ex.sell(pair, pair.bid, new Currency(pair.base, amount));
						
						if (orderId==0) {
							
							if (fall!=null)
								fallDAO.put(fall.reduct(amount));
							
						} else {
							
							try {
								ex.cancel(pair, orderId);
								
							} catch (CancelException e) {
								e.printStackTrace();
							}
							
						}
						
					} catch (ExchangeException e) {
						e.printStackTrace();
					}
				}
				try { Thread.sleep(delay); } catch (InterruptedException e) {}
			}
			
			try { Thread.sleep(processDelay); } catch (InterruptedException e) {}					
		}
		
		onTradeSuccess();
		
		return true;
	}

	protected abstract void onTradeSuccess();
}
