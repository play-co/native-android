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


import android.os.Environment;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class Overlay extends android.webkit.WebView {
	private BrowserInterface browserInterface;
	private boolean loading = false;
	private boolean visible = false;
	private ResourceManager resources;

	public Overlay(TeaLeaf context) {
		super(context);
		resources = new ResourceManager(context, context.getOptions());
		//setBackgroundColor(0);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setDatabaseEnabled(true);
        getSettings().setDatabasePath(Environment.getExternalStorageDirectory().getPath());
                
        browserInterface = new BrowserInterface();
        addJavascriptInterface(browserInterface, "tealeaf"); 
        
        setWebViewClient(new WebViewClient() {
        	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        		logger.log("{overlay} ERROR: Web view reported " + description);
        	}
        });
        setWebChromeClient(new WebChromeClient() {
        	@SuppressWarnings("unused")
			public boolean onConsoleMessage(String message, int lineNumber) {
        		logger.log("{overlay} LOG " + message + " " + lineNumber);
        		return true;
        	}
        });
	}
	
	
	public void ready() {
		if (loading) {
			this.browserInterface.sendMessage("ready");
			loading = false;
		}
	}
	public void load(String url) {
		url = resources.resolve(url);
		if (url.matches("^http(s?)://.*")) {
			loadUrl(url);
		} else {
			loadUrl("file://" + url);
		}
		loading = true;
	}
	
	public void show() {
		setVisibility(View.VISIBLE);
		this.visible = true;
	}
	
	public void hide() {
		loadUrl("about:blank");
		setVisibility(View.INVISIBLE);
		this.visible = false;
	}
	
	public boolean visible() { return this.visible; }
	
	public void sendEvent(String event) {
		logger.log("{overlay} Sending event " + event);
		this.loadUrl("javascript:GC.onMessage(" + event + ")");
	}
}
