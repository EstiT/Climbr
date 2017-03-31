package edu.carleton.comp2601.climbr;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EventStreamImpl implements EventStream {

	ObjectInputStream ois;
	ObjectOutputStream oos;
	Socket socket;
	
	/*
	 * Allows streams to be created: input followed by output
	 */
	public EventStreamImpl(InputStream is, OutputStream os) throws IOException {
		ois = new ObjectInputStream(is);
		oos = new ObjectOutputStream(os);
	}
	
	/*
	 * Allows streams to be created: output followed by input
	 */
	public EventStreamImpl(OutputStream os, InputStream is) throws IOException {
		oos = new ObjectOutputStream(os);
		ois = new ObjectInputStream(is);
	}
	/*
	 * Designed for server-side usage when a socket has been accepted
	 */
	public EventStreamImpl(Socket s) throws IOException {
		this(s.getInputStream(), s.getOutputStream());
		this.socket = s;
	}
	
	@Override
	public Event getEvent() throws IOException, ClassNotFoundException {

		Event e = (Event)ois.readObject();
		e.es = this;
		return e;

	}
	
	@Override
	public void putEvent(Event e) throws IOException {
		//System.out.println("writing object to outputstream: " + oos);
		oos.writeObject(e);
		oos.flush();

	}

	@Override
	public void close() {
		try {
			if (socket != null) 
				socket.close();
			if (oos != null)
				oos.close();
			if (ois != null) 
				ois.close();
			socket = null;
			oos = null;
			ois = null;
		} catch (IOException e) {
			// Fail quietly
		}
	}	
}
