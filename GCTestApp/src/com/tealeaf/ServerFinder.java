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

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import android.text.Html;
import android.text.Spanned;

import android.os.Handler;
import android.net.wifi.*;
import android.widget.ArrayAdapter;
import android.content.Context;

import com.tealeaf.test_app.TestAppActivity;

import javax.jmdns.*;

public class ServerFinder {
	WifiManager.MulticastLock lock;
	private String type = "_tealeaf._tcp.local.";
	private JmDNS jmdns;
	private ServiceListener listener;
	private ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
	private ArrayAdapter<Spanned> adapter;
	private final Handler handler;
	private boolean isLoading = true;

	public ServerFinder(final TestAppActivity activity) {
		Context context = activity.getApplicationContext();
		WifiManager wifi = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("tealeaflock");
		lock.setReferenceCounted(true);
		lock.acquire();

		handler = new Handler();

		isLoading = true;
		// don't block the main thread
		new Thread(new Runnable() {
			public void run() {
				try {
					jmdns = JmDNS.create();
					ServiceInfo[] stuff = jmdns.list(type);
					for (ServiceInfo s : stuff) {
						logger.log(s);
					}
					jmdns.addServiceListener(type, listener = new ServiceListener() {
						public void serviceResolved(ServiceEvent ev) {
							addService(ev.getInfo().getQualifiedName(), ev.getInfo().getPort(), ev.getInfo().getInetAddresses());
						}
						public void serviceRemoved(ServiceEvent ev) {
							removeService(ev.getName());
						}
						public void serviceAdded(ServiceEvent ev) {
							// Required to force serviceResolved to be called again
							// (after the first search)
							jmdns.requestServiceInfo(ev.getType(), ev.getName(), 1);
						}
					});
				} catch (IOException e) {
					logger.log("couldn't do zeroconf", e);
				}

			}
		}).start();
	}

	private void addService(String host, int port, InetAddress[] addresses) {
		if (isLoading && adapter.getCount() > 0) {
			isLoading = false;
			handler.post(new Runnable() {
				public void run() {
					adapter.remove(adapter.getItem(0));
					adapter.notifyDataSetChanged();
				}
			});

		}

		final Spanned description =  Html.fromHtml("<font color=\"#6495ED\">" + host + "</font>\n<font color=\"#FFFFFF\">\n" + addresses[0] + ":" + port + "</font>");
		servers.add(new ServerInfo(addresses[0].getCanonicalHostName(), port, description));
		handler.post(new Runnable() {
			public void run() {
				adapter.add(description);
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void removeService(final String name) {
		int i = adapter.getPosition(Html.fromHtml(name));
		servers.remove(i);
		handler.post(new Runnable() {
			public void run() {
				adapter.remove(Html.fromHtml(name));
			}
		});
	}

	protected void destroy() {
		jmdns.removeServiceListener(type, listener);
		try {
			jmdns.close();
		} catch (IOException e) {
			logger.log("failed to close the jmdns", e);
		}
		if (lock != null) lock.release();
	}

	public void setAdapter(ArrayAdapter<Spanned> adapter) {
		this.adapter = adapter;
		adapter.add(Html.fromHtml("LOADING"	));
	}

	public ServerInfo getServer(int index) {
		if (servers.size()  == 0) return null;
		return servers.get(index);
	}

	class ServerInfo {
		public ServerInfo(String host, int port, Spanned description) {
			this.host = host;
			this.port = port;
			this.description = description;

		}
		public String host;
		public int port;
		public Spanned description;
	}
}
