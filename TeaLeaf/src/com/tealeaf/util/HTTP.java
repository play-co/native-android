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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import com.tealeaf.logger;

import android.net.http.AndroidHttpClient;
import android.util.Pair;

public class HTTP {

	public static final String userAgent = "Android tealeaf/1.0";
	private static boolean caughtIOException = false;

	private static CookieStore cookieStore = new BasicCookieStore();
	private static HttpContext localContext = new BasicHttpContext();
	
	static {
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public Pair<String, Integer> getPush(URI uri) {
		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent);

		HttpGet request = new HttpGet();
		request.setURI(uri);
		HttpResponse response = null;
		try {
			response = client.execute(request, localContext);
		} catch (SocketTimeoutException e) {
			// forget it--we don't care that the user couldn't connect
			// TODO log this to JS
		} catch (IOException e) {
			if(!caughtIOException) {
				caughtIOException = true;
				logger.log(e);
			}
		}
		if (response != null) {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = 0;
			if (statusLine != null) {
				statusCode = statusLine.getStatusCode();
			}
			if (statusCode == 200) {
				String page = readContent(response);
				Header header = response.getFirstHeader("Retry-After");
				// default is to read the value from the manifest
				int retry = -1;
				if (header != null) {
					String retryAfter = header.getValue();
					try {
						retry = Integer.parseInt(retryAfter);
					} catch (NumberFormatException e) {
						// if we got a bad number, use the default
						retry = -1;
					}
				}
				client.close();
				return new Pair<String, Integer>(page, retry);
			}
		}
		client.close();
		return null;
	}

	// === start legacy methods (deprecated)
	public String get(URI uri) {
		return get(uri, null).body;
	}

	public String post(URI uri, String data) {
		return post(uri, data, null).body;
	}

	public Response post(URI uri, String data, HashMap<String, String> requestHeaders) {
		return makeRequest(Methods.POST, uri, requestHeaders, data);
	}


	public Response get(URI uri, HashMap<String, String> requestHeaders) {
		return makeRequest(Methods.GET, uri, requestHeaders, null);
	}
	// === end legacy methods

	public class Response {
		int status;
		String body;
		HashMap<String, String> headers;
	}

	public enum Methods {
		GET,
		POST,
		PUT,
		DELETE,
		HEAD
	};

	public Response makeRequest(Methods method, URI uri, HashMap<String, String> requestHeaders) {
		return makeRequest(method, uri, requestHeaders, null);
	}

	public Response makeRequest(Methods method, URI uri, HashMap<String, String> requestHeaders, String data) {

		HttpRequestBase request = null;

		try {
			if (method == Methods.GET) {
				request = new HttpGet();
			} else if (method == Methods.POST) {
				request = new HttpPost();
				if (data != null) {
					((HttpPost) request).setEntity(new StringEntity(data, "UTF-8"));
				}
			} else if (method == Methods.PUT) {
				request = new HttpPut();
				if (data != null) {
					((HttpPut) request).setEntity(new StringEntity(data, "UTF-8"));
				}
			} else if (method == Methods.DELETE){
				request = new HttpDelete();
			} else if (method == Methods.HEAD){
				request = new HttpHead();
			}
		} catch (UnsupportedEncodingException e) {
			logger.log(e);
		}
		request.setURI(uri);
		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent);
		if(requestHeaders != null) {
			for(Map.Entry<String, String> entry : requestHeaders.entrySet()) {
				request.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
			}
		}

		HttpResponse response = null;
		Response retVal = new Response();
		retVal.headers = new HashMap<String, String>();
		try {
			response = client.execute(request, localContext);
		} catch (SocketTimeoutException e) {
			// forget it--we don't care that the user couldn't connect
			// TODO hand this back as an error to JS
		} catch (IOException e) {
			if(!caughtIOException) {
				caughtIOException = true;
				logger.log(e);
			}
		} catch (Exception e) {
			logger.log(e);
		}
		if (response != null) {
			retVal.status = response.getStatusLine().getStatusCode();
			retVal.body = readContent(response);
			for(Header header : response.getAllHeaders()) {
				retVal.headers.put(header.getName(), header.getValue());
			}
		}
		client.close();
		return retVal;
	}

	private String readContent(HttpResponse response) {
		BufferedReader in = null;
		String page = null;
		if (response != null) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					in = new BufferedReader(new InputStreamReader(entity.getContent()));
					StringBuffer sb = new StringBuffer("");
					String line = "";
					String NL = System.getProperty("line.separator");
					while ((line = in.readLine()) != null) {
						sb.append(line + NL);
					}
					in.close();
					page = sb.toString();
				} catch (IOException e) {
					logger.log(e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// we don't care if the stream fails to close
						}
					}
				}
			}
		}
		return page;
	}

	public File getFile(URI uri, String fileName) {
		return getFile(uri, fileName, null);
	}

	public File getFile(URI uri, String fileName, HashMap<String, String> requestHeaders) {
		InputStream in = null;
		File file = new File(fileName);
		new File(file.getParent()).mkdirs();
		FileOutputStream fo = null;
		try {
			file.createNewFile();
			fo = new FileOutputStream(file);
		} catch (IOException e1) {
			logger.log(e1);
			return null;
		}
		AndroidHttpClient client = null;
		try {
			client = AndroidHttpClient.newInstance(userAgent);
			HttpGet request = new HttpGet();

			if(requestHeaders != null) {
				for(Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					request.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
				}
			}

			request.setURI(uri);
			HttpResponse response = client.execute(request, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				client.close();
				return null;
			}
			in = response.getEntity().getContent();
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1)  {
				fo.write(buffer, 0, bytesRead);
			}
			fo.close();
			in.close();
			client.close();
		} catch (SocketTimeoutException e) {
			// forget it--we don't care that the user failed to connect
		} catch (IllegalArgumentException e) {
			// forget it--we don't care that the user failed to connect
			logger.log("{http} WARNING: Illegal argument" + uri);
		} catch (IOException e) {
			if(!caughtIOException) {
				caughtIOException = true;
				logger.log(e);
			}
			file = null;
		} finally {
			if(client != null) {
				client.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}
		return file;
	}

	public HttpResponse getFileResponse(URI uri, String fileName, HashMap<String, String> requestHeaders) {
		HttpResponse response = null;
		InputStream in = null;
		File file = new File(fileName);
		FileOutputStream fo = null;
		AndroidHttpClient client = null;
		try {
			client = AndroidHttpClient.newInstance(userAgent);
			HttpGet request = new HttpGet();

			if(requestHeaders != null) {
				for(Map.Entry<String, String> entry : requestHeaders.entrySet()) {
					request.addHeader(new BasicHeader(entry.getKey(),entry.getValue()));
				}
			}

			request.setURI(uri);
			response = client.execute(request, localContext);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				client.close();
				return null;
			}

			try {
				new File(file.getParent()).mkdirs();
				file.createNewFile();
				fo = new FileOutputStream(file);
			} catch (IOException e1) {
				logger.log(e1);
				return null;
			}

			in = response.getEntity().getContent();
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1)  {
				fo.write(buffer, 0, bytesRead);
			}
			fo.close();
			in.close();
			client.close();
		} catch (SocketTimeoutException e) {
			// forget it--we don't care that the user failed to connect
		} catch (IllegalArgumentException e) {
			// forget it--we don't care that the user failed to connect
			logger.log("{http} WARNING: Illegal argument" + uri);
		} catch (IOException e) {
			if(!caughtIOException) {
				caughtIOException = true;
				logger.log(e);
			}
			file = null;
			response = null;
		} finally {
			if(client != null) {
				client.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}
		return response;
	}

}

