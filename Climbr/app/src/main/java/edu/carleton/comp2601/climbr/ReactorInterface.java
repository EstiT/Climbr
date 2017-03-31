package edu.carleton.comp2601.climbr;

public interface ReactorInterface {
	public void register(String type, EventHandler event);
	public void deregister(String type);
	public void dispatch(Event event) throws NoEventHandler;
}
