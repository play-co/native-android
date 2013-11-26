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

import com.google.gson.Gson;

/**
 * An Event corresponds to a JavaScript action
 *  
 */
public class Event implements Comparable<Event> {

	protected String name;
	protected int priority = 0;
	protected static Gson gson = new Gson();
	public Event(String name) { this.name = name;}
	public Event() {}
	
	public String pack() {
		String packedValue = gson.toJson(this);
		return packedValue;
	}

	public String getName() {
		return name;
	}
	
	public int priority() { return priority; }

	@Override
	public int compareTo(Event event) {
		return priority() - event.priority();
	}
	
}
