package com.tealeaf;

import com.tealeaf.event.OverlayEvent;

public class BrowserInterface {

	public BrowserInterface() {
	}
	
	public void log(String message) {
		logger.log("{overlay} ", message);
	}
	
	public void sendMessage(String event) {
		logger.log("{overlay} Event: ", event);
		EventQueue.pushEvent(new OverlayEvent(event));
	}
}
