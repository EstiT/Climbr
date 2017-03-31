package edu.carleton.comp2601.climbr;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Event implements EventStream, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String type;
	public transient EventStream es;
	private HashMap<String, Serializable> map;
	
	public HashMap<String, Serializable> getMap(){
		return map;
	}
	
	public Event(String type) {
		this.type = type;
		this.es = null;
		this.map = new HashMap<String, Serializable>();
	}

	public Event(String type, EventStream es) {
		this.type = type;
		this.es = es;
		this.map = new HashMap<String, Serializable>();
	}
	
	public Event(String type, EventStream es, HashMap<String, Serializable> map) {
		this.type = type;
		this.es = es;
		this.map = map;
	}
	
	public void put(String key, Serializable value) {
		map.put(key, value);
	}
	
	public Serializable get(String key) {
		return map.get(key);
	}
	
	public void putEvent() throws ClassNotFoundException, IOException {
		putEvent(this);
	}
	
	public void putEvent(Event e) throws IOException, ClassNotFoundException {
		if (es != null)
			es.putEvent(e);
		else
			throw new IOException("No event stream defined");
	}
	
	public Event getEvent() throws ClassNotFoundException, IOException {
		if (es != null) {
			return es.getEvent();
		} else 
			throw new IOException("No event stream defined");
	}

	public void close() {
		if (es != null) {
			es.close();
		}
	}
}
