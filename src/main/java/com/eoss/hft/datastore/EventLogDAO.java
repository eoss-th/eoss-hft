package com.eoss.hft.datastore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.eoss.hft.EventLog;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

public class EventLogDAO {

	private final DatastoreService datastore;
	
	public EventLogDAO(DatastoreService datastore) {
		this.datastore = datastore;
	}
	
	public List<EventLog> get(Date startTime) {
		
		List<EventLog> eventLogList = new ArrayList<>();
		
		Query eventLogQuery = new Query("EVENTLOG");
		
		if (startTime!=null) {
			
			Filter propertyFilter = new FilterPredicate("timestamp", FilterOperator.GREATER_THAN_OR_EQUAL, startTime.getTime());
			
			eventLogQuery.setFilter(propertyFilter);			
		} 
		
		eventLogQuery.addSort("date", SortDirection.DESCENDING);		
		
		List<Entity> entities = datastore.prepare(eventLogQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			eventLogList.add(create(entity));
		}
		
		return eventLogList;
	}
	
	private EventLog create(Entity eventLogEntity) {
		
		Date date = (Date) eventLogEntity.getProperty("date");
		String title = (String) eventLogEntity.getProperty("title");
		String message = (String) eventLogEntity.getProperty("message");
		
	    return new EventLog(date, title, message);		
	}
	
	public void put(EventLog eventLog) {
		
		Entity eventLogEntity = new Entity("EVENTLOG");
		eventLogEntity.setProperty("timestamp", eventLog.date.getTime());
		eventLogEntity.setProperty("date", eventLog.date);
		eventLogEntity.setProperty("title", eventLog.title);
		eventLogEntity.setProperty("message", eventLog.message);
		
		datastore.put(eventLogEntity);
	}

	public void clear(Date endTime) {
		
		Query eventLogQuery = new Query("EVENTLOG");
		
		if (endTime!=null) {
			
			Filter propertyFilter = new FilterPredicate("timestamp", FilterOperator.LESS_THAN, endTime.getTime());
			
			eventLogQuery.setFilter(propertyFilter);			
		} 
		
		List<Entity> entities = datastore.prepare(eventLogQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			datastore.delete(entity.getKey());
		}
		
		Query txLogQuery = new Query("TXLOG");
		entities = datastore.prepare(txLogQuery).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity:entities) {
			datastore.delete(entity.getKey());
		}
		
	}
	
}
