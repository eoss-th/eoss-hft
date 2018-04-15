package com.eoss.hft;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Pair {
	
	public final String id;
	public final String base;
	public final String counter;
	public final double fee;
	public final double baseMin;
	public final double counterMin;
	public final int amountPlaces;
	public final int pricePlaces;
	
	public double bid;
	public double bidAmount;
	
	public double ask;
	public double askAmount;
	
	public Pair(String id, String base, String counter, double fee, double baseMin, double counterMin, int amountPlaces) {
		this(id, base, counter, fee, baseMin, counterMin, amountPlaces, 0);
	}
	
	public Pair(String id, String base, String counter, double fee, double baseMin, double counterMin, int amountPlaces, int pricePlaces) {
		this.id = id;
		this.base = base;
		this.counter = counter;
		this.fee = fee;
		this.baseMin = baseMin;
		this.counterMin = counterMin;
		this.amountPlaces = amountPlaces;
		this.pricePlaces = pricePlaces;
	}
	
	public double bid() {
		return bid;
	}
	
	public double bidAmount() {
		return bidAmount;
	}
	
	public double ask() {
		return ask;
	}
	
	public double askAmount() {
		return askAmount;
	}
	
	public double totalBid() {
		return bid * bidAmount;
	}
	
	public double totalAsk() {
		return ask * askAmount;
	}
	
	public Pair fetch(Exchange exchange) {
		
		try {
			Order [] orders = exchange.fetchOrders(id);
			
			Order lastOrder = orders[0];
			
			bid = lastOrder.bid.rate;
			bidAmount = lastOrder.bid.amount;
			
			ask = lastOrder.ask.rate;
			askAmount = lastOrder.ask.amount;
			
			return this;			
		} catch (Exception e) {			
		}
		
		return null;
	}
			
	public Currency forward(Currency currency) {
		
		if (currency.getName().equals(base)) {
			
			return sell(currency);
						
		} else if (currency.getName().equals(counter)) {
			
			return buy(currency);
			
		}
	
		throw new IllegalArgumentException(this +" is not support for " + currency.getName());
	}
	
	protected Currency buyCapacity(Currency baseCurrency) {
		
		return new Currency(counter, baseCurrency.getAmount() * (1-fee) * ask );
	}
	
	protected Currency sellCapacity(Currency counterCurrency) {
		
		return new Currency(base, counterCurrency.getAmount() * (1 - fee) / bid );
	}
	
	/**
	 * USD/THB<-USD = 32.6THB
	 * USD/THB<-THB = 1/32.6USD
	 * 
	 * @param ratio
	 * @param prevPair
	 * @return
	 */
	public Currency backward(Currency currency) {
		
		if (currency.getName().equals(base)) {
			
			return buyCapacity(currency);
						
		} else if (currency.getName().equals(counter)) {
			
			return sellCapacity(currency);
			
		}
				
		throw new IllegalArgumentException(this +" is not support for " + currency.getName());
	}
	
	protected Currency counterMax() {
		return new Currency(counter, totalAsk());		
	}
	
	protected Currency baseMax() {
		return new Currency(base, bidAmount);					
	}
	
	protected Currency counterMin() {
		return new Currency(counter, counterMin);		
	}
	
	protected Currency baseMin() {
		return new Currency(base, baseMin);					
	}
	
	public String toString() {
		return base + "/" + counter;
	}
	
	public static int getNumberOfDecimalPlaces(double amount) {
	    String string = "" + amount;
	    if (string.endsWith(".0")) return 0;
	    int index = string.indexOf(".");
	    return index < 0 ? 0 : string.length() - index - 1;
	}

	public static double round(double value, int places) {
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_DOWN);
	    return bd.doubleValue();
	}
	
	public static double floor(double value, int places) {
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.FLOOR);
	    return bd.doubleValue();
	}
	
	public static void main(String[]args) {
		
		//ETH
		double bid = 0.090164;
		double amount = 0.106;
		
		double x = amount * bid * 0.999;
		System.out.println(x);
		double btc = 0.00954771;
		System.out.println(btc/x);
		
		
		System.out.println(getNumberOfDecimalPlaces(1.000000));
	}

	public abstract Currency buy(Currency counterCurrency);

	public abstract Currency sell(Currency baseCurrency);
}
