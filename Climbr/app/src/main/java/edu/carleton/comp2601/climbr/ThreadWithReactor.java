package edu.carleton.comp2601.climbr;

import java.io.IOException;

/*
 * Simple Implementation of a Reactor pattern which runs in its own 
 * thread. It is possible to start the reactor with an event which
 * is useful for client-side usage where a 'first request' is sent 
 * to a server.
 */

public class ThreadWithReactor extends Thread implements ReactorInterface {
	private boolean running;
	private EventStream source;
	private Reactor reactor;
	private Event startEvent;

	/* 
	 * Reactor will be for this thread only
	 */
	public ThreadWithReactor(EventStream source) {
		this.source = source;
		this.running = false;
		this.reactor = new Reactor();
	}

	/*
	 * Allows a reactor instance to be shared
	 */
	public ThreadWithReactor(EventStream source, Reactor reactor) {
		this.source = source;
		this.running = false;
		this.reactor = reactor;
	}

	/*
	 * End it!
	 */
	public void quit() {
		running = false;
	}

	/*
	 * Save the event and send it in the run method
	 */
	public void start(Event e) {
		startEvent = e;
		start();
	}

	public void run() {
		running = source != null;
		// Only try the even if we are running
		if (startEvent != null && running)
			try {
				source.putEvent(startEvent);
			} catch (ClassNotFoundException | IOException e2) {
				quit();
			} finally {
				startEvent = null;
			}
		/*
		 * Main processing loop 
		 */
		while (running) {
			Event event;
			try {
				event = source.getEvent();
				if (event != null) {
					try {
						dispatch(event);
					} catch (NoEventHandler e) {
						quit();
					}
				} else
					quit();
			} catch (ClassNotFoundException | IOException e1) {
				quit();
			}
		}
	}

	@Override
	public void register(String type, EventHandler event) {
		reactor.register(type, event);
	}

	@Override
	public void deregister(String type) {
		reactor.deregister(type);
	}

	@Override
	public void dispatch(Event event) throws NoEventHandler {
		reactor.dispatch(event);
	}

	public EventStream getEventSource() {
		return source;
	}
}
