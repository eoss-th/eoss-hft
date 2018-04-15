package com.eoss.hft.exchange.cex;

import com.eoss.hft.Currency;
import com.eoss.hft.Pair;

public class CEXPair extends Pair {

	public CEXPair(String id, String base, String counter, double fee, double baseMin, double counterMin,
			int amountPlaces) {
		super(id, base, counter, fee, baseMin, counterMin, amountPlaces);
	}

	@Override
	public Currency buy(Currency counterCurrency) {
		
		double amount = round(counterCurrency.getAmount(), amountPlaces);
		if (amount < counterMin) 
			amount = 0;
		
		return new Currency(base, (amount / ask) * (1-fee));
	}
	
	@Override
	public Currency sell(Currency baseCurrency) {
		
		double amount = round(baseCurrency.getAmount() , amountPlaces);
		if (amount < baseMin)
			amount = 0;
		
		return new Currency(counter, (amount * bid) * (1-fee));
	}
}
