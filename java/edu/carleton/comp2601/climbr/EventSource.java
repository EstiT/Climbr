package edu.carleton.comp2601.climbr;

import java.io.IOException;

public interface EventSource {
	public Event getEvent() throws IOException, ClassNotFoundException;
}
