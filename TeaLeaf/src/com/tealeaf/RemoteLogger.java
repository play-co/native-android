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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.tealeaf.util.Connection;
import com.tealeaf.util.HTTP;
import com.tealeaf.util.ILogger;

import com.google.gson.Gson;

public class RemoteLogger implements ILogger {
	private static final URI url = URI.create("http://track.gameclosure.com");
	private Gson gson = null;
	private Context context;
	private boolean logging = false;
	private String appID = "unknown";
	private Object queueLock = new Object();
	private HTTP http = new HTTP();
	// TODO do we want to use a fixed thread pool instead? this needs examining
	protected ExecutorService pool = null;

	private static String getIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException e) {

	    }
	    return null;
	}

	protected static class LogEvent {
		public LogEvent(Context context, String id, String appID) {
			eventID = id;
			eventTime = Long.toString(System.currentTimeMillis());
			// NB we'll fill this in later
			sentTime = "SENTTIMEFILLINLATER";
			this.appID = appID;
		}

		// NB we don't log device id because TeaLeaf
		// isn't always ready in time to determine it
		public String eventID;
		public String appID;
		public String eventTime;
		public String sentTime;
	}

	protected static class FirstLaunchLogEvent extends LogEvent {
		public FirstLaunchLogEvent(Context context, String appID) {
			super(context, "nativeFirstLaunch", appID);
			devicePhoneHash = Device.getNumberHash(context);
			deviceIP = getIpAddress();
			String number = Device.getNormalizedNumber(context);
			if(number.equals("NONUMBER") || number.length() < 3) {
				devicePhoneAreaCode = "000";
			} else {
				devicePhoneAreaCode = number.substring(0, 3);
			}
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			installReferrer = prefs.getString("referrer", "none");
		}

		public final String deviceManufacturer = Build.MANUFACTURER;
		public final String deviceModel = Build.MODEL;
		public final String deviceVersion = Build.VERSION.SDK;
		public String installReferrer;
		public String devicePhoneHash;
		public String devicePhoneAreaCode;
		public String deviceIP;
	}

	protected static class LaunchLogEvent extends LogEvent {
		public LaunchLogEvent(Context context, String appID) {
			super(context, "nativeLaunch", appID);
		}
	}

	protected static class ErrorLogEvent extends LogEvent {
		public ErrorLogEvent(Context context, String appID, String payload) {
			super(context, "nativeError", appID);
			eventPayload = payload;
		}

		public String eventPayload;
	}

	protected static class GLErrorLogEvent extends LogEvent {
		public GLErrorLogEvent(Context context, String appID, String payload) {
			super(context, "nativeGLError", appID);
			eventPayload = payload;
		}

		public String eventPayload;
	}

	protected static class DeviceInfoLogEvent extends LogEvent {
		public DeviceInfoLogEvent(Context context, String appID) {
			super(context, "nativeDeviceInfo", appID);
			devicePhoneHash = Device.getNumberHash(context);
			deviceIP = getIpAddress();
			String number = Device.getNormalizedNumber(context);
			if(number.equals("NONUMBER") || number.length() < 3) {
				devicePhoneAreaCode = "000";
			} else {
				devicePhoneAreaCode = number.substring(0, 3);
			}
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			installReferrer = prefs.getString("referrer", "none");
		}

		public final String deviceManufacturer = Build.MANUFACTURER;
		public final String deviceModel = Build.MODEL;
		public final String deviceVersion = Build.VERSION.SDK;
		public String installReferrer;
		public String devicePhoneHash;
		public String devicePhoneAreaCode;
		public String deviceIP;
	}

	public RemoteLogger(Context context) {
		this(context, "RemoteLogger", Executors.newCachedThreadPool());
	}
	public RemoteLogger(Context context, String appID) {
		this(context, appID, Executors.newCachedThreadPool());
	}
	public RemoteLogger(Context context, String appID, ExecutorService service) {
		this.context = context;
		this.appID = appID;
		pool = service;
		// drain the initial queue if any, we'll drain it later every time we try to send events
		pool.execute(new RemoteLogSender());
	}

	private class RemoteLogSender implements Runnable {
		private LogEvent event = null;
		public RemoteLogSender(LogEvent e) { event = e; }
		public RemoteLogSender() {}
		@Override
		public void run() {
			String evt = "";
			if(event != null) {
				// convert the event to json
				if(gson == null) {
					gson = new Gson();
				}
				evt = gson.toJson(event);
			}

			if(Connection.available(context)) {
				String events = dequeue();
				// regardless of whether or not we've got an event to send, try to drain the queue
				// if and only if we're connected
				StringBuffer content = new StringBuffer();
				if(!events.equals("")) {
					content.append(events);
					content.append(",");
				}
				content.append(evt);

				// insert the sent time
				if(!content.toString().equals("")) {
					try {
						// try to send the event--if this fails, enqueue it without a sent time instead
						String str = "{\"native\":[" + content.toString() + "]}";
						send(str.replaceAll("SENTTIMEFILLINLATER", Long.toString(System.currentTimeMillis())));
						// and drain the rest of the queue while we're at it
					} catch (Exception e) {
						// if we failed to send the event, then enqueue it
						enqueue(content.toString());
					}
				}
			} else {
				enqueue(evt);
			}
		}
	}

	@Override
	public void log(Exception e) {
		if(!logging) {
			// only try to call log once--if the first one fails, screw it, otherwise we could
			// end up with infinite recursion
			logging = true;
			StringBuffer payload = new StringBuffer();
			payload.append(e.getMessage() + "\n");
			for(StackTraceElement ex : e.getStackTrace()) {
				payload.append("\tat" + ex + "\n");
			}
			sendErrorEvent(context, payload.toString());
			logging = false;
		}
	}

	@Override
	public void sendFirstLaunchEvent(Context context) {
		send(new FirstLaunchLogEvent(context, appID));
	}

	@Override
	public void sendLaunchEvent(Context context) {
		send(new LaunchLogEvent(context, appID));
	}

	@Override
	public void sendErrorEvent(Context context, String payload) {
		send(new ErrorLogEvent(context, appID, payload));
	}

	@Override
	public void sendGLErrorEvent(Context context, String payload) {
		send(new GLErrorLogEvent(context, appID, payload));
	}

	@Override
	public void sendDeviceInfoEvent(Context context) {
		send(new DeviceInfoLogEvent(context, appID));
	}

	private void send(LogEvent event) {
		if (event != null) {
			pool.execute(new RemoteLogSender(event));
		}
	}

	private void enqueue(String event) {
			synchronized(queueLock) {
				SharedPreferences prefs = context.getSharedPreferences("backpack-events", Context.MODE_PRIVATE);
				StringBuffer events = new StringBuffer(prefs.getString("log_events", ""));
				// join events with a comma, since they're already json'd
				if(!events.toString().equals("")) {
					events.append(",");
				}
				events.append(event);
				prefs.edit().putString("log_events", events.toString()).commit();
			}
	}

	private String dequeue() {
		String events;
		synchronized(queueLock) {
			SharedPreferences prefs = context.getSharedPreferences("backpack-events", Context.MODE_PRIVATE);
			events = prefs.getString("log_events", "");
			if(!events.equals("")) {
				prefs.edit().putString("log_events", "").commit();
			}
		}
		return events;
	}

	private void send(String event) throws Exception {
		// http.post(url, event);
	}
}
