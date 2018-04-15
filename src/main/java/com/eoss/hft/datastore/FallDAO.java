package com.eoss.hft.datastore;

import java.util.ArrayList;
import java.util.List;

import com.eoss.hft.Currency;
import com.eoss.hft.Fall;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class FallDAO {

	private final DatastoreService datastore;
	
	public FallDAO(DatastoreService datastore) {
		this.datastore = datastore;
	}
	
	public List<Fall> get() {
		
		List<Fall> fallList = new ArrayList<>();
		Query fallQuery = new Query("Fall");
		
		List<Entity> entities = datastore.prepare(fallQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			fallList.add(create(entity));
		}
		
		return fallList;
	}
	
	public List<Fall> get(String startCurrency) {
		
		List<Fall> fallList = new ArrayList<>();
		Query fallQuery = new Query("Fall");
		
		List<Entity> entities = datastore.prepare(fallQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			if (entity.getProperty("startCurrency").equals(startCurrency))
				fallList.add(create(entity));
		}
		
		return fallList;
	}
	
	public Fall get(String startCurrency, String targetCurrency) {
		
		Key key = KeyFactory.createKey("Fall", startCurrency + ">" + targetCurrency);
	    try {
	    	
	      return create(datastore.get(key));
	      
	    } catch (EntityNotFoundException e) {
	    		e.printStackTrace();
	    }
		return null;
	}
	
	private Fall create(Entity fallEntity) {
		
		String startCurrency = (String) fallEntity.getProperty("startCurrency");
	    double startAmount = (Double) fallEntity.getProperty("startAmount");
	    
	    String targetCurrency = (String) fallEntity.getProperty("targetCurrency");
	    double targetAmount = (Double) fallEntity.getProperty("targetAmount");
	      
	    return new Fall(new Currency(startCurrency, startAmount), new Currency(targetCurrency, targetAmount));		
	}
	
	public void put(Fall fall) {
		
		Entity fallEntity = new Entity("Fall", fall.start.getName() + ">" + fall.target.getName());
		fallEntity.setProperty("startCurrency", fall.start.getName());
		fallEntity.setProperty("startAmount", fall.start.getAmount());
		fallEntity.setProperty("targetCurrency", fall.target.getName());
		fallEntity.setProperty("targetAmount", fall.target.getAmount());
		
		if (fall.start.getAmount()<=0 || fall.target.getAmount()<=0) {
			datastore.delete(fallEntity.getKey());
			return;
		}
		
		datastore.put(fallEntity);
	}

	public void clear(String name) {
		
		Query fallQuery = new Query("Fall");
		
		List<Entity> entities = datastore.prepare(fallQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			if (entity.getProperty("startCurrency").equals(name))
				datastore.delete(entity.getKey());
		}
		
	}
	
	public void clearMax(String name) {
		
		Query fallQuery = new Query("Fall");
		
		List<Entity> entities = datastore.prepare(fallQuery).asList(FetchOptions.Builder.withDefaults());
		Double maxAmount = Double.MIN_VALUE;
		Key maxKey = null;
		double startAmount;
		for (Entity entity:entities) {
			if (entity.getProperty("startCurrency").equals(name)) {
				startAmount = ((Double)entity.getProperty("startAmount"));
				if (startAmount > maxAmount) {
					maxAmount = startAmount; 
					maxKey = entity.getKey();
				}
			}
		}
		
		if (maxKey!=null)
			datastore.delete(maxKey);
	}	
	
	public void clearMin(String name) {
		
		Query fallQuery = new Query("Fall");
		
		List<Entity> entities = datastore.prepare(fallQuery).asList(FetchOptions.Builder.withDefaults());
		Double minAmount = Double.MAX_VALUE;
		Key minKey = null;
		double startAmount;
		for (Entity entity:entities) {
			if (entity.getProperty("startCurrency").equals(name)) {
				startAmount = ((Double)entity.getProperty("startAmount"));
				if (startAmount < minAmount) {
					minAmount = startAmount; 
					minKey = entity.getKey();
				}
			}
		}
		
		if (minKey!=null)
			datastore.delete(minKey);
	}
	
	public void clear() {
		
		Query fallQuery = new Query("Fall");
		
		List<Entity> entities = datastore.prepare(fallQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			datastore.delete(entity.getKey());
		}
		
	}
	
}
