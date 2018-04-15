package com.eoss.hft;

import java.util.HashMap;
import java.util.Map;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Order;
import com.eoss.hft.Pair;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;

public abstract class DummyExchange extends Exchange {

	protected final Map<String, Double> wallets;
	
	public DummyExchange() {	
		this.wallets = new HashMap<>();
	}
	
	private boolean transfer(String fromWallet, double fromAmount, String toWallet, double toAmount) {
		
		if (wallets.get(fromWallet) < fromAmount) return false;
		
		wallets.put(fromWallet, wallets.get(fromWallet) - fromAmount);
		
		wallets.put(toWallet, wallets.get(toWallet) + toAmount);
		
		return true;
	}
	
	public Currency getAvailableBalance(String name) {
		
		try {			
			return new Currency(name, wallets.get(name));			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public long buy(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		Order [] orders = fetchOrders(pair.id);
		Order lastOrder = orders[0];
		
		if (lastOrder.ask.rate==rate && amount.getAmount()<=lastOrder.ask.total()) {
			
			Currency c = pair.buy(amount);
			if (!transfer(pair.counter, amount.getAmount(), pair.base, c.getAmount())) throw new OrderException("Insufficient balance from " + pair.counter + ", Balance = " + getAvailableBalance(pair.counter), "buy",  pair, rate, amount);
			
			return 0;
		}
		
		return (lastOrder.ask.rate!=rate) ? 1001:1002;
		
	}
	
	public long sell(Pair pair, double rate, Currency amount) throws ExchangeException {
		
		Order [] orders = fetchOrders(pair.id);
		Order lastOrder = orders[0];
		
		if (lastOrder.bid.rate==rate && amount.getAmount()<=lastOrder.bid.amount) {
			
			Currency c = pair.sell(amount);
			if (!transfer(pair.base, amount.getAmount(), pair.counter, c.getAmount())) throw new OrderException("Insufficient balance from " + pair.base + ":" + amount.getAmount() + " , Balance = " + getAvailableBalance(pair.base), "sell",  pair, rate, amount);
			
			return 0;			
		}
		
		return (lastOrder.bid.rate!=rate) ? 2001:2002;
	}
	
	public boolean cancel(Pair pair, long orderId)  throws ExchangeException {
		
		if (orderId==1000 || orderId==1001) {
			
			System.out.println("Cancel buy at " + pair + " caused by " + orderId);
			
		} else if (orderId==2000 || orderId==2001) {
			
			System.out.println("Cancel sell at " + pair + " caused by " + orderId);
			
		} else {
			
			throw new CancelException("Invalid orderId");
		}		
					
		return true;			
	}
		
}
