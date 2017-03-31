package edu.carleton.comp2601.climbr;


public interface EventStream extends EventInputStream, EventOutputStream {
	public void close();
}
