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

public abstract class ZigzagStrategy implements Strategy {
	
	private final Exchange ex;
	
	private final long delay;
	
	private final FallDAO fallDAO;
	
	public final Map<String, Pair> pairMap;	
	
	public final List<String> onlyPairList;
	
	public ZigzagStrategy(Exchange ex, long delay, FallDAO fallDAO, Map<String, Pair> pairMap, List<String> onlyPairList) {
		this.ex = ex;
		this.delay = delay;
		this.fallDAO = fallDAO;
		this.pairMap = pairMap;
		this.onlyPairList = onlyPairList;		
	}
	
	public boolean process() {
		
		List<Fall> fallList = fallDAO.get();
		
		if (fallList.isEmpty()) {
			
			Currency balance, target;
			
			Pair pair;
			for (String pairId:onlyPairList) {
				
				pair = pairMap.get(pairId);
				
				pair.fetch(ex);
				
				balance = ex.getAvailableBalance(pair.base);
				target = pair.forward(balance);
				
				if (target.getAmount() > 0)
					fallDAO.put(new Fall(balance, target));
				
				balance = ex.getAvailableBalance(pair.counter);
				target = pair.forward(balance);
				
				if (target.getAmount() > 0)
					fallDAO.put(new Fall(balance, target));
				
			}
		}
		
		for (Fall fall:fallList) {
			
			Pair pair = pairMap.get(fall.start.getName() + "/" + fall.target.getName());
			if (pair==null) {
				pair = pairMap.get(fall.target.getName() + "/" + fall.start.getName());
			}
			if (pair!=null) {
				pair.fetch(ex);
				Currency end = pair.forward(fall.start);
				double rate = end.getAmount() / fall.target.getAmount();
				System.out.println(fall+":"+rate);
				//if (rate > 1.002) {
				if (rate > 1) {
					
					try {
						
						if (fall.start.getName().equals(pair.base)) {
						
							double amount = fall.start.getAmount();
							if (amount > pair.bidAmount) continue;
							
							long orderId = ex.sell(pair, pair.bid, new Currency(fall.start.getName(), amount));
							if (orderId==0) {
								
								fallDAO.put(fall.reduct(amount));	
								fallDAO.put(new Fall(end, fall.start));
								
								onTradeSuccess(fall, (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
								
							} else {
								try {
									ex.cancel(pair, orderId);
								} catch (CancelException e) {
									
									fallDAO.put(fall.reduct(amount));								
									fallDAO.put(new Fall(end, fall.start));

									onTradeSuccess(fall, (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
									
								}
							}
																				
						} else if (fall.start.getName().equals(pair.counter)) {
						
							double amount = fall.start.getAmount();
							if (amount > pair.totalAsk()) continue;
							
							long orderId = ex.buy(pair, pair.ask, new Currency(fall.start.getName(), amount));
							if (orderId==0) {
								
								fallDAO.put(fall.reduct(amount));
								fallDAO.put(new Fall(end, fall.start));
								
								onTradeSuccess(fall,  (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
								
							} else {
								try {
									ex.cancel(pair, orderId);
								} catch (CancelException e) {
									
									fallDAO.put(fall.reduct(amount));
									fallDAO.put(new Fall(end, fall.start));
									onTradeSuccess(fall,  (end.getAmount() - fall.target.getAmount()) * (amount/fall.start.getAmount()), end.getAmount() / fall.target.getAmount());
								}
							}
						}	
					
					} catch (ExchangeException e) {
						e.printStackTrace();
					}
				}
				
			}			
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}

	protected abstract void onTradeSuccess(Fall fall, double amount, double rate);
}
