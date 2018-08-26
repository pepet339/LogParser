package com.logparser.parser.entity;

public class LogEvent {

	private String id;
	private long duration;
	private String host;
	private String type;
	private boolean alert;
		
	public LogEvent(String id, String host, String type, long duration, boolean alert) {
		this.id = id;
		this.host = host;
		this.type = type;
		this.duration = duration;
		this.alert = alert;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isAlert() {
		return alert;
	}
	public void setAlert(boolean alert) {
		this.alert = alert;
	}

}
