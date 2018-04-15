package com.eoss.hft.exchange.abu;

import com.eoss.hft.Currency;
import com.eoss.hft.Pair;

public class ABUPair extends Pair {

	public ABUPair(String id, String base, String counter, double fee, double baseMin, double counterMin,
			int amountPlaces) {
		super(id, base, counter, fee, baseMin, counterMin, amountPlaces);
	}

	@Override
	public Currency buy(Currency counterCurrency) {
		
		if (floor(counterCurrency.getAmount() / ask, amountPlaces) < baseMin) return new Currency(base, 0);
		
		return new Currency(base, floor(counterCurrency.getAmount() * (1-fee) / ask, amountPlaces));
	}
	
	@Override
	public Currency sell(Currency baseCurrency) {
		
		if (floor(baseCurrency.getAmount(), amountPlaces) < baseMin) return new Currency(counter, 0);
		
		return new Currency(counter, floor(baseCurrency.getAmount() * (1-fee) * bid, amountPlaces));
	}
}
