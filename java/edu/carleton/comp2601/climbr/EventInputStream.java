package edu.carleton.comp2601.climbr;

import java.io.IOException;

public interface EventInputStream {
	public Event getEvent() throws IOException, ClassNotFoundException;
}
