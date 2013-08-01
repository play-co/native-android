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

		if (e.length > 256) {
			String[] batch256 = new String[256];

			int ii, len = e.length;
			for (ii = 0; ii < len; ii += 256) {
				int batchLength = len - ii;
				if (batchLength < 256) {
					break;
				}

				System.arraycopy(e, ii, batch256, 0, 256);
				NativeShim.dispatchEvents(batch256);
			}

			if ((len & 255) > 0) {
				int batchLength = len & 255;
				String[] batch = new String[batchLength];

				System.arraycopy(e, len & ~255, batch, 0, batchLength);
				NativeShim.dispatchEvents(batch);
			}
		} else {
			NativeShim.dispatchEvents(e);
		}
	}
}
