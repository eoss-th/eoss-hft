package com.eoss.hft.exchange.hitbtc;

import com.eoss.hft.Currency;
import com.eoss.hft.Pair;

public class HitBTCPair extends Pair {

	public HitBTCPair(String id, String base, String counter, double fee, double baseMin, double counterMin,
			int amountPlaces, int pricePlaces) {
		super(id, base, counter, fee, baseMin, counterMin, amountPlaces, pricePlaces);
	}
	
	@Override
	public Currency buy(Currency counterCurrency) {
		
		if (floor(counterCurrency.getAmount() * (1-fee), amountPlaces) < counterMin) return new Currency(base, 0);
		
		return new Currency(base, floor(counterCurrency.getAmount() * (1-fee) / ask, amountPlaces));
	}
	
	@Override
	public Currency sell(Currency baseCurrency) {
		
		if (floor(baseCurrency.getAmount() * (1-fee), amountPlaces) < baseMin) return new Currency(counter, 0);
		
		return new Currency(counter, floor(baseCurrency.getAmount(), amountPlaces) * (1-fee) * bid);
	}
	
}
