package com.eoss.hft.strategy;

import java.util.List;
import java.util.Map;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Fall;
import com.eoss.hft.Pair;
import com.eoss.hft.Strategy;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;

public abstract class FallbackStrategy implements Strategy {
	
	private final Exchange ex;
	
	private final long delay;
	
	private final FallDAO fallDAO;
	
	private final Map<String, Pair> pairMap;	
	
	public FallbackStrategy(Exchange ex, long delay, FallDAO fallDAO, Map<String, Pair> pairMap) {
		this.ex = ex;
		this.delay = delay;
		this.fallDAO = fallDAO;
		this.pairMap = pairMap;
	}
	
	public boolean process() {
		
		List<Fall> fallList = fallDAO.get();
		
		for (Fall fall:fallList) {
			
			try { Thread.sleep(delay); } catch (InterruptedException e) {}
			
			Pair pair = pairMap.get(fall.start.getName() + "/" + fall.target.getName());
			if (pair==null) {
				pair = pairMap.get(fall.target.getName() + "/" + fall.start.getName());
			}
			
			if (pair!=null) {
				pair.fetch(ex);
				Currency end = pair.forward(fall.start);
				double rate = end.getAmount() / fall.target.getAmount();
				if (rate >= 1.003) {
					
					Currency balance = ex.getAvailableBalance(fall.start.getName());
					
					try {
						
						if (fall.start.getName().equals(pair.base)) {
						
							double amount = balance.getAmount() <= pair.bidAmount ? balance.getAmount() : pair.bidAmount;
							
							long orderId = ex.sell(pair, pair.bid, new Currency(fall.start.getName(), amount));
							if (orderId==0) {
								
								fallDAO.put(fall.reduct(amount));								
								onTradeSuccess(fall, (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
								
							} else {
								try {
									ex.cancel(pair, orderId);
								} catch (CancelException e) {
									
									fallDAO.put(fall.reduct(amount));
									onTradeSuccess(fall, (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
									
								}
							}
														
						
						} else if (fall.start.getName().equals(pair.counter)) {
						
							double amount = balance.getAmount() <= pair.totalAsk() ? balance.getAmount() : pair.totalAsk();
							
							long orderId = ex.buy(pair, pair.ask, new Currency(fall.start.getName(), amount));
							if (orderId==0) {
								
								fallDAO.put(fall.reduct(amount));
								onTradeSuccess(fall,  (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
								
							} else {
								try {
									ex.cancel(pair, orderId);
								} catch (CancelException e) {
									
									fallDAO.put(fall.reduct(amount));
									onTradeSuccess(fall,  (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
								}
							}
						}	
					
					} catch (ExchangeException e) {
						e.printStackTrace();
					}
				}
				
			}			
			
		}
		
		return true;
	}

	protected abstract void onTradeSuccess(Fall fall, double amount, double rate);
}
