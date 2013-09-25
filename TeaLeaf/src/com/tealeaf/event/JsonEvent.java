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
package com.tealeaf.event;

import com.tealeaf.logger;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * An Event corresponds to a JavaScript action
 *  
 */
public class JsonEvent extends Event {

	private JSONObject data;	
	public JsonEvent(String name, JSONObject data) { super(name); this.data = data;}
	public JsonEvent(String name) { super(name); this.data = new JSONObject();}
	
	public JsonEvent put(String name, String value) {
		try {
			this.data.put(name, value);
		} catch (JSONException e) {
			logger.log(e);
		}
		return this;
	}

	public String pack() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("name", this.name);
			obj.put("priority", this.priority);
			obj.put("data", this.data);
		} catch (Exception e) {
			logger.log(e);
		}
		return obj.toString();
	}
	
	public int priority() { return priority; }

	@Override
	public int compareTo(Event event) {
		return priority() - event.priority();
	}


	
}
