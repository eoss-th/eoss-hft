package com.eoss.hft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EventLog {
	
	public static final SimpleDateFormat sf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSSSSS", new Locale("th", "TH"));
	
	static {
		sf.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
	}
	
	public final Date date;
	
	public final String title;
	
	public final String message;

	public EventLog(Date date, String title, String message) {
		this.date = date;
		this.title = title;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return sf.format(date) + " " + title + " " + message;
	}
	
}
