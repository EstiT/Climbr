package edu.carleton.comp2601.climbr;

import java.io.IOException;

public interface EventOutputStream {
	public void putEvent(Event e) throws IOException, ClassNotFoundException;
}
