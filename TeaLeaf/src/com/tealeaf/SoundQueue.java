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

import java.util.concurrent.LinkedBlockingQueue;

public class SoundQueue implements Runnable {
	private SoundManager soundManager;
	private final LinkedBlockingQueue<Event> queue;

	private enum EventType {
		LOAD,
		PLAY_SOUND,
		PLAY_MUSIC,
		PAUSE,
		STOP,
		APP_PAUSED,
		APP_RESUMED,
		SET_VOLUME,
		LOAD_BG_MUSIC,
		SEEK_TO
	};
	
	static final public String LOADING_SOUND = "loadingsound";  
	
	private class Event {
		EventType type;
		String url;
		float volume;
		boolean loop;
		float position;
		
		public Event(EventType type) { this(type, null); }
		public Event(EventType type, String url) { this(type, url, 0); }
		public Event(EventType type, String url, float volume) { this(type, url, volume, false); }
		public Event(EventType type, String url, float volume, boolean loop) { this(type, url, volume, loop, 0); }
		public Event(EventType type, String url, float volume, boolean loop, float position) {
			this.type = type;
			this.url = url;
			this.volume = volume;
			this.loop = loop;
			this.position = position;
		}
	}
	
	public SoundQueue(TeaLeaf context, ResourceManager resourceManager) {
		queue = new LinkedBlockingQueue<Event>();
		soundManager = new SoundManager(context, resourceManager);
		new Thread(this).start();
	}
	
	public void loadSound(final String url) { addEvent(new Event(EventType.LOAD, url)); }
	
	public void playSound(String url) { playSound(url, 1, false); }
	public void playSound(String url, float volume) { playSound(url, volume, false); }
	public void playSound(String url, float volume, boolean loop) {
		addEvent(new Event(EventType.PLAY_SOUND, url, volume, loop));
	}

	public void loadBackgroundMusic(String url) {
		addEvent(new Event(EventType.LOAD_BG_MUSIC, url));
	}
	
	public void playBackgroundMusic(String url, float volume, boolean loop) {
		addEvent(new Event(EventType.PLAY_MUSIC, url, volume, loop));
	}
	
	
	public void pauseSound(String url) {
		addEvent(new Event(EventType.PAUSE, url));
	}
	
	public void stopSound(String url) {
		addEvent(new Event(EventType.STOP, url));
	}
	
	public void setVolume(String url, float volume) { 
		addEvent(new Event(EventType.SET_VOLUME, url, volume));
	}
	
	public void seekTo(String url, float position) { 
		addEvent(new Event(EventType.SEEK_TO, url, 0, false, position));
	}
	
	public void haltSounds() {
		addEvent(new Event(EventType.APP_PAUSED));
	}
	
	public void onPause() { addEvent(new Event(EventType.APP_PAUSED)); }
	public void onResume() { addEvent(new Event(EventType.APP_RESUMED)); }
	
	private void addEvent(Event event) {
		queue.offer(event);
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				Event e = queue.take();
				try {
					switch (e.type) {
						case LOAD:
							soundManager.loadSound(e.url, true);
							break;
						case LOAD_BG_MUSIC:
							soundManager.loadBackgroundMusic(e.url);
							break;
						case APP_PAUSED:
							soundManager.onPause();
							break;
						case APP_RESUMED:
							soundManager.onResume();
							break;
						case PLAY_SOUND:
							soundManager.playSound(e.url, e.volume, e.loop);
							break;
						case PLAY_MUSIC:
							soundManager.playBackgroundMusic(e.url, e.volume, e.loop);
							break;
						case PAUSE:
							soundManager.pauseSound(e.url);
							break;
						case STOP:
							soundManager.stopSound(e.url);
							break;
						case SET_VOLUME:
							soundManager.setVolume(e.url, e.volume);
							break;
						case SEEK_TO:
							soundManager.seekTo(e.url, e.position);
							break;
					}
				} catch (IllegalStateException ex) {
					logger.log(ex);
				}
			}
		} catch (InterruptedException ex2) {
			// do nothing if we got interrupted
		}
	}
}
