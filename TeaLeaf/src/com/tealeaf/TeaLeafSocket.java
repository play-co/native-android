/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.

 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
package com.tealeaf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Queue;

import com.tealeaf.event.SocketErrorEvent;
import com.tealeaf.event.SocketOpenEvent;
import com.tealeaf.event.SocketCloseEvent;
import com.tealeaf.event.SocketReadEvent;

//TODO get rid of the reference in NativeShim when one of these goes out of scope
public class TeaLeafSocket implements Runnable{
	private String address;
	private int port;
	private Socket socket;
	private InputStream is;
	private BufferedReader in;
	private OutputStreamWriter out;
	private int id;
	private boolean connected = false;
	private char[] cData = new char[512];

	private Thread writeThread = new Thread(new Runnable() {
		public void run() {
			while(connected) {
				try {
					String[] results = null;
					 while(writeQueue.size() > 0) {
						results = new String[writeQueue.size()];
						writeQueue.toArray(results);
						writeQueue.clear();
						for(String result : results) {
							out.write(result);
							out.flush();
						}
					}
					synchronized(writeMonitor) {
						writeMonitor.wait();
					}
				} catch(Exception e) {
					logger.log(e);
					return;
				}
			}
			logger.log("{socket} Write thread terminating");
		}
	});
	private Object writeMonitor = new Object();
	private Queue<String> writeQueue = new java.util.concurrent.ConcurrentLinkedQueue<String>();

	public TeaLeafSocket(String address, int port, int id) {
		this.address = address;
		this.port = port;
		this.id = id;
	}

	public void connect() {
		boolean error = false;
		try {
			logger.log("{socket} Created for", address, ":", port);
			socket = new Socket(address, port);
			socket.setSoTimeout(3000);
		} catch (Exception e) {
			error(e.toString());
			error = true;
		}
		if (socket != null) {
			try {
				is = socket.getInputStream();
				in = new BufferedReader(new InputStreamReader(is));
				out = new OutputStreamWriter(socket.getOutputStream());
			} catch (IOException e) {
				error(e.toString());
				error = true;
			}
		}
		if (!error) {
			this.connected = true;
			EventQueue.pushEvent(new SocketOpenEvent(this.id));
		}
	}

	public int getID() {
		return id;
	}

	public void error(String message) {
		EventQueue.pushEvent(new SocketErrorEvent(this.id, message));
		close();
	}

	public void write(String data) {
		synchronized(writeMonitor) {
			writeQueue.add(data);
			writeMonitor.notifyAll();
		}
	}

	public synchronized void read() {
		if (in == null) { return; }
		int cLen = 0;
		try {
			cLen = in.read(cData);
		} catch (SocketTimeoutException e) {
			// do nothing
		} catch (IOException e) {
			error("read error: " + e.toString());
		}
		if (cLen == -1) {
			close();
		} else if (cLen > 0) {
			String data = new String(Arrays.copyOfRange(cData, 0, cLen));
			EventQueue.pushEvent(new SocketReadEvent(this.id, data));
		}
	}

	public void close() {
		connected = false;
		EventQueue.pushEvent(new SocketCloseEvent(this.id));
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		}
	}

	public void run() {
		connect();
		writeThread.start();
		while (connected) {
			read();
		}
	}

}
