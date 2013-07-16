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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class PerformanceTimer {
	private LinkedHashMap<String, Long> marks = new LinkedHashMap<String, Long>();
	public PerformanceTimer() {
		
	}
	
	public void mark(String key) {
		marks.put(key, System.nanoTime());
	}
	
	public void reset() {
		marks.clear();
	}
	
	public long lastElapsed() {
		long elapsed;
		int size = marks.size();
		if (size < 2) { 
			logger.log("{timer} ERROR: Only one timestamp in marks");
			elapsed = -1;
		} else {
			Collection<Long> values = marks.values();
			Iterator<Long> i = values.iterator();
			long time1, time2;
			do {
				time1 = i.next();
				time2 = i.next();
			} while (i.hasNext());
			
			elapsed = time1 - time2;
		}
		logger.log("{timer} Elapsed", elapsed);
		return elapsed;
	}
	
	public long elapsedSince(String key) {
		mark(key);
		logger.log("{timer} Marked", key);
		return lastElapsed();
	}
	
	public long elapsedBetween(String key1, String key2) {
		Long time1 = marks.get(key1);
		Long time2 = marks.get(key2);
		long elapsed;
		if (time1 == null || time2 == null) {
			elapsed = -1;
			logger.log("{overlay} ERROR: Timestamps were null while calculating elapsed time");
		} else {
			elapsed = time2 - time1;
		}
		return elapsed;
	}
	
	public LinkedHashMap<String, Long> getAll() {
		return new LinkedHashMap<String, Long>(marks);
	}
}
