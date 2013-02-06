/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
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
	private String[] keys;
	private String[] values;

	public XMLHttpRequest(final int id, String method, String url, String data, boolean async, HashMap<String, String> requestHeaders, Runnable cb) {
		this.method = method;
		this.url = url;
		this.data = data;
		this.requestHeaders = requestHeaders;
		this.cb = cb != null ? cb : new Runnable() {
			public void run() {
				TeaLeaf instance = TeaLeaf.get();
				if (id != -1 && instance != null) {
					//TODO: if there is no glView, what should we do with the cb
					// that means there's no javascript currently running.
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
		} else {
			logger.log("{xhr} WARNING: Unable to handle method", method);
		}
		
		int count = response.headers.size();
		keys = new String[count];
		values = new String[count];
		response.headers.keySet().toArray(keys);
		response.headers.values().toArray(values);
		cb.run();
	}
}
