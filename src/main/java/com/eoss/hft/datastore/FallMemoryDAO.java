package com.eoss.hft.datastore;

import java.util.ArrayList;
import java.util.List;

import com.eoss.hft.Fall;

public class FallMemoryDAO extends FallDAO {

	public FallMemoryDAO() {
		super(null);
	}

	private final List<Fall> fallList = new ArrayList<>();
	
	public List<Fall> get() {
		
		return fallList;
	}
	
	public List<Fall> get(String startCurrency) {
		
		List<Fall> newFallList = new ArrayList<>();
		
		for (Fall fall:fallList) {
			if (fall.start.getName().equals(startCurrency)) {
				newFallList.add(fall);
			}
		}
		
		return newFallList;
	}
	
	public Fall get(String startCurrency, String targetCurrency) {
		
		for (Fall fall:fallList) {
			if (fall.start.getName().equals(startCurrency) && fall.target.getName().equals(targetCurrency)) {
				return fall;
			}
		}
		
		return null;
	}
	
	public void put(Fall fall) {
		
		Fall lastFall = get(fall.start.getName(), fall.target.getName());
		
		if (lastFall!=null) {
			fallList.remove(lastFall);			
		}
				
		if (fall.start.getAmount()<=0 || fall.target.getAmount()<=0) {
			return;
		}
		
		fallList.add(fall);
	}

	public void clear(String name) {
		
		List<Fall> lastFallList = get(name);
		
		fallList.removeAll(lastFallList);
		
	}
	
	public void clear() {
		
		fallList.clear();
		
	}
	
}
