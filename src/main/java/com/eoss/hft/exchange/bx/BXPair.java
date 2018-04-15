package com.eoss.hft.exchange.bx;

import com.eoss.hft.Currency;
import com.eoss.hft.Pair;

public class BXPair extends Pair {

	public BXPair(String id, String base, String counter, double fee, double baseMin, double counterMin,
			int amountPlaces) {
		super(id, base, counter, fee, baseMin, counterMin, amountPlaces);
	}

	@Override
	public Currency buy(Currency counterCurrency) {
		
		if (floor(counterCurrency.getAmount(), amountPlaces) < counterMin) return new Currency(base, 0);
		
		return new Currency(base, floor(counterCurrency.getAmount() * (1-fee) / ask, amountPlaces) - 0.0000001);
	}
	
	@Override
	public Currency sell(Currency baseCurrency) {
		
		if (floor(baseCurrency.getAmount(), amountPlaces) < baseMin) return new Currency(counter, 0);
		
		return new Currency(counter, floor(baseCurrency.getAmount() * (1-fee) * bid, amountPlaces) - 0.0000001);
	}
	
}
