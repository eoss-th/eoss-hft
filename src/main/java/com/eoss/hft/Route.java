package com.eoss.hft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Route {
	
	public final String targetCurrencyName;
	
	public final List<Pair> pairs;
	
	public Route(String targetCurrencyName, List<Pair> pairs) {
		this.targetCurrencyName = targetCurrencyName;
		this.pairs = pairs;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder(targetCurrencyName);
		sb.append("->");
		
		for (int i=0;i<pairs.size();i++) {
			sb.append(pairs.get(i));
			if (i==pairs.size()-1) break;
			sb.append("->");
		}
		
		return sb.toString();
	}
			
	public static Route [] load (Map<String, Pair> pairMap) {
		
		/**
		 * BTC/THB
		 * ETC/THB
		 * ETC/BTC
		 * 
		 * LTC/THB
		 * LTC/BTC
		 * BTC/THB
		 */
		
		Map<String, Set<Pair>> counterMap = new HashMap<>();
		
		Set<Pair> baseSet;
		for (Pair pair:pairMap.values()) {
			baseSet = counterMap.get(pair.counter);
			if (baseSet==null) {
				baseSet = new HashSet<>();
			}
			baseSet.add(pair);
			counterMap.put(pair.counter, baseSet);
			
		}
		
		List<Route> routeList = new ArrayList<>();
		
		String name;
		
		for (Map.Entry<String, Set<Pair>> entry:counterMap.entrySet()) {
			Set<Pair> set = new HashSet<>();
			for (Pair p1:entry.getValue()) {
				for (Pair p2:entry.getValue()) {
					name = p1.base+"/"+p2.base;
					if (pairMap.get(name)!=null)
						set.add(pairMap.get(name));
				}
			}
			
			List<Pair> pairList;
			for (Pair p:set) {
				
				/**
				 * Route 1
				 */
				pairList = new ArrayList<Pair>();				
				pairList.add(pairMap.get(p.base+"/"+entry.getKey()));
				pairList.add(p);
				pairList.add(pairMap.get(p.counter+"/"+entry.getKey()));
				routeList.add(new Route(entry.getKey(), pairList));
				
				pairList = new ArrayList<Pair>();				
				pairList.add(pairMap.get(p.counter+"/"+entry.getKey()));
				pairList.add(p);
				pairList.add(pairMap.get(p.base+"/"+entry.getKey()));
				routeList.add(new Route(entry.getKey(), pairList));
				
				/**
				 * Route 2
				 */
				pairList = new ArrayList<Pair>();				
				pairList.add(p);
				pairList.add(pairMap.get(p.counter+"/"+entry.getKey()));
				pairList.add(pairMap.get(p.base+"/"+entry.getKey()));
				routeList.add(new Route(p.base, pairList));
				
				pairList = new ArrayList<Pair>();				
				pairList.add(pairMap.get(p.base+"/"+entry.getKey()));
				pairList.add(pairMap.get(p.counter+"/"+entry.getKey()));
				pairList.add(p);
				routeList.add(new Route(p.base, pairList));
				
				/**
				 * Route 3
				 */
				pairList = new ArrayList<Pair>();				
				pairList.add(p);
				pairList.add(pairMap.get(p.base+"/"+entry.getKey()));
				pairList.add(pairMap.get(p.counter+"/"+entry.getKey()));
				routeList.add(new Route(p.counter, pairList));
				

				pairList = new ArrayList<Pair>();				
				pairList.add(pairMap.get(p.counter+"/"+entry.getKey()));
				pairList.add(pairMap.get(p.base+"/"+entry.getKey()));
				pairList.add(p);
				routeList.add(new Route(p.counter, pairList));
			}
		}
		
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public static Route [] filterMinAmountPlaces(Route [] routes, int minAmountPlaces) {
		
		List<Route> routeList = new ArrayList<>();
		
		for (Route route:routes) {
			if (route.minAmountPlaces()>=minAmountPlaces) {
				routeList.add(route);
			}
		}			
		
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public static Route [] filterMinPricePlaces(Route [] routes, int minPricePlaces) {
		
		List<Route> routeList = new ArrayList<>();
		
		for (Route route:routes) {
			if (route.minPricePlaces()>=minPricePlaces) {
				routeList.add(route);
			}
		}			
		
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public static Route [] filterCurrencies(Route [] routes, List<String> onlyCurrencyList) {
		
		List<Route> routeList = new ArrayList<>();
		
		for (Route route:routes) {
			if (onlyCurrencyList.contains(route.targetCurrencyName)) {
				routeList.add(route);					
			}
		}			
		
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public static Route [] filterPairs(Route [] routes, List<String> onlyPairList) {
		
		List<Route> routeList = new ArrayList<>();
		
		for (Route route:routes) {
			if (onlyPairList.contains(route.pairs.get(0).toString()) ||
					onlyPairList.contains(route.pairs.get(2).toString())) {
				routeList.add(route);				
			}
		}		
		
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public static Route [] filterBridges(Route [] routes, List<String> onlyBridgeList) {
		
		List<Route> routeList = new ArrayList<>();
		
		for (Route route:routes) {
			if (onlyBridgeList.contains(route.pairs.get(1).toString())) {
				routeList.add(route);					
			}
		}			
		
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public static Set<Pair> pairSet(Exchange ex, Route[]routes) {
		
		Set<Pair> pairSet = new HashSet<>();
		for (Route r:routes) {
			for (Pair p:r.pairs) {
				pairSet.add(p);
			}
		}
		
		return pairSet;
	}
	
	public static void fetch(Exchange ex, Set<Pair> pairSet) {
		for (Pair pair:pairSet) {
			pair.fetch(ex);
		}		
	}
	
	public Route fetch(Exchange ex) {
		
		for (Pair pair:pairs) {
			if (pair.fetch(ex)==null) return null;
		}
		
		return this;
	}
	
	public Currency test(Currency currency) {
		
		Currency out = currency;
		
		for (Pair pair:pairs) {
			out = pair.forward(out);
			if (out.getAmount()==0) break;
		}
		
		return out;
	}
		
	public int position(Pair pair) {
		for (int i=0; i<pairs.size(); i++) {
			if (pair.toString().equals(pairs.get(i).toString())) return i;
		}
		return -1;
	}
	
	public int minAmountPlaces() {
		
		int minAmountPlaces = Integer.MAX_VALUE;
		for (Pair pair:pairs) {
			if (pair.amountPlaces < minAmountPlaces) {
				minAmountPlaces = pair.amountPlaces;
			}
		}
		
		return minAmountPlaces;
	}
	
	public int minPricePlaces() {
		
		int minPricePlaces = Integer.MAX_VALUE;
		for (Pair pair:pairs) {
			if (pair.pricePlaces < minPricePlaces) {
				minPricePlaces = pair.pricePlaces;
			}
		}
		
		return minPricePlaces;
	}
	
	private Currency maximum (String currencyName, Pair...pairs) {
		
		if (pairs.length==1) {
			if (currencyName.equals(pairs[pairs.length-1].counter))
				return pairs[pairs.length-1].counterMax();
			return pairs[pairs.length-1].baseMax();
		}
		
		Currency max;
		
		/**
		 * Y/?<-?ask/Y
		 * ?/Y<-?ask/Y
		 */
		if (pairs[pairs.length-1].counter.equals(pairs[pairs.length-2].base) || 
				pairs[pairs.length-1].counter.equals(pairs[pairs.length-2].counter)) {			
			max = pairs[pairs.length-1].counterMax();
		} 
		
		/**
		 * ?/Y<-Y/?bid
		 * Y/?<-Y/?bid
		 */
		else {
			max = pairs[pairs.length-1].baseMax();
		}
		
		/**
		 * BTC/THB -> ETH/BTC - Correct!
		 * BTC/THB -> ETH/BTC -> ETH/THB - Correct!
		 */
		
		for (int i=pairs.length-2;i>=0;i--) {
			max = pairs[i].backward(max);
		}
		
		return max;
	}
	
	public Currency calculateMaximum () {
		
		Currency hop1 = maximum(targetCurrencyName, new Pair[] {pairs.get(0)});
		
		Currency hop2 = maximum(targetCurrencyName, new Pair[] {pairs.get(0), pairs.get(1)});		
		
		Currency hop3 = maximum(targetCurrencyName, new Pair[] {pairs.get(0), pairs.get(1), pairs.get(2)});
		
		double min;
		
		if (hop1.getAmount()<hop2.getAmount())
			min = hop1.getAmount();
		else
			min = hop2.getAmount();
		
		if (hop3.getAmount()<min)
			min = hop3.getAmount();
		
		return new Currency(targetCurrencyName, min);
	}
	
	private Currency minimum (String currencyName, Pair...pairs) {
		
		if (pairs.length==1) {
			if (currencyName.equals(pairs[pairs.length-1].counter))
				return pairs[pairs.length-1].counterMin();
			return pairs[pairs.length-1].baseMin();
		}
		
		Currency min;
		
		/**
		 * Y/?<-?ask/Y
		 * ?/Y<-?ask/Y
		 */
		if (pairs[pairs.length-1].counter.equals(pairs[pairs.length-2].base) || 
				pairs[pairs.length-1].counter.equals(pairs[pairs.length-2].counter)) {			
			min = pairs[pairs.length-1].counterMin();
		} 
		
		/**
		 * ?/Y<-Y/?bid
		 * Y/?<-Y/?bid
		 */
		else {
			min = pairs[pairs.length-1].baseMin();
		}
		
		/**
		 * BTC/THB -> ETH/BTC - Correct!
		 * BTC/THB -> ETH/BTC -> ETH/THB - Correct!
		 */
		
		for (int i=pairs.length-2;i>=0;i--) {
			min = pairs[i].backward(min);
		}
				
		return min;
	}
	
	public Currency calculateMinimum () {
		
		Currency hop1 = minimum(targetCurrencyName, new Pair[] {pairs.get(0)});
		
		Currency hop2 = minimum(targetCurrencyName, new Pair[] {pairs.get(0), pairs.get(1)});		
		
		Currency hop3 = minimum(targetCurrencyName, new Pair[] {pairs.get(0), pairs.get(1), pairs.get(2)});
		
		double max;
		
		if (hop1.getAmount()>hop2.getAmount())
			max = hop1.getAmount();
		else
			max = hop2.getAmount();
		
		if (hop3.getAmount()>max)
			max = hop3.getAmount();
		
		return new Currency(targetCurrencyName, max);
	}
	
}
