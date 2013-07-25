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
package com.tealeaf.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.tealeaf.EventQueue;
import com.tealeaf.TeaLeaf;
import com.tealeaf.logger;
import com.tealeaf.event.XHREvent;

// NB XMLHttpRequests cannot be reused currently. If we decide that they should be at some point, we must clean
// up their private data members
public class XMLHttpRequest implements Runnable {
	private String method;
	private String url;
	private String data;
	private HashMap<String,String> requestHeaders;
	private Runnable cb;
	private HTTP.Response response;

	public XMLHttpRequest(final int id, String method, String url, String data, boolean async, HashMap<String, String> requestHeaders, Runnable cb) {
		this.method = method;
		this.url = url;
		this.data = data;
		this.requestHeaders = requestHeaders;
		this.cb = cb != null ? cb : new Runnable() {
			public void run() {
				TeaLeaf instance = TeaLeaf.get();
				if (id != -1 && instance != null) {
					String[] keys = null;
					String[] values = null;
					if (response.headers != null) {
						keys = response.headers.keySet().toArray(new String[0]);
						values = response.headers.values().toArray(new String[0]);
					}

					EventQueue.pushEvent(new XHREvent(id, 4, response.status, response.body, keys, values));
				}
			}
		};
	}
	public XMLHttpRequest(int id, String method, String url, String data, boolean async) { this(id, method, url, data, async, null); }
	public XMLHttpRequest(int id, String method, String url, String data, boolean async, HashMap<String, String> requestHeaders) {
		this(id, method, url, data, async, requestHeaders, null);
	}
	@Override
	public void run() {
		HTTP http = new HTTP();
		URI uri = null;
		if (!url.matches("^https?:.*$")) {
			url = "http:" + url;
		}
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			logger.log(e);
		}
		if (uri == null) {
			logger.log("{xhr} ERROR: Unable to create URI");
			return;
		}
		
		if (method.toUpperCase().equals("GET")) {
			response = http.makeRequest(HTTP.Methods.GET, uri, requestHeaders);
		} else if (method.toUpperCase().equals("POST")) {
			response = http.makeRequest(HTTP.Methods.POST, uri, requestHeaders, data);
		} else if (method.toUpperCase().equals("PUT")) {
			response = http.makeRequest(HTTP.Methods.PUT, uri, requestHeaders, data);
		} else if (method.toUpperCase().equals("DELETE")) {
			response = http.makeRequest(HTTP.Methods.DELETE, uri, requestHeaders);
		} else if (method.toUpperCase().equals("HEAD")) {
			response = http.makeRequest(HTTP.Methods.HEAD, uri, requestHeaders);
		} else {
			logger.log("{xhr} WARNING: Unable to handle method", method);
		}

		cb.run();
	}
}
