package com.tealeaf;

import java.util.Queue;

import com.tealeaf.event.Event;

public class EventQueue {

	private static Queue<String> events = new java.util.concurrent.ConcurrentLinkedQueue<String>();

	private static Object lock = new Object();

	public static void pushEvent(Event e) {
		synchronized (lock) {
			events.add(e.pack());
		}
	}

	protected static String popEvent() {
		return events.poll();
	}

	private static String[] getEvents() {
		String[] ret;
		synchronized (lock) {
			ret = new String[events.size()];
			events.toArray(ret);
			events.clear();
		}
		return ret;
	}

	public static void dispatchEvents() {
		String[] e = getEvents();
		NativeShim.dispatchEvents(e);
	}
}
