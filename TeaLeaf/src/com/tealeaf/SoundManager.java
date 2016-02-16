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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.tealeaf.event.SoundDurationEvent;
import com.tealeaf.event.SoundErrorEvent;
import com.tealeaf.event.SoundLoadedEvent;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseArray;

public class SoundManager implements Runnable {
	private ConcurrentHashMap<String, SoundSpec> sounds = new ConcurrentHashMap<String, SoundSpec>();
	private final LinkedBlockingQueue<SoundSpec> loadingQueue = new LinkedBlockingQueue<SoundSpec>();
	private HashMap<String, Integer> durations = new HashMap<String, Integer>();
	private HashSet<SoundSpec> pausedSounds = new HashSet<SoundSpec>();
	private SoundPool soundPool = new SoundPool(15, AudioManager.STREAM_MUSIC, 0);
	private MediaPlayer backgroundMusic = null, loadingSound = null;
	private String backgroundMusicUrl = null;
	private boolean shouldResumeBackgroundMusic = true, shouldResumeLoadingSound = true, appPaused = false;
	private ResourceManager resourceManager;
	private TeaLeaf context;

	class SoundSpec {
		public String url;
		public int id;
		public float volume;
		public boolean loop;
		public int stream;
		public boolean loaded;
		public boolean failed;

		public SoundSpec(String url, int id, float volume, boolean loop) {
			this.url = url;
			this.id = id;
			this.volume = volume;
			this.loop = loop;
			this.stream = 0;
			this.loaded = false;
			this.failed = false;
		}
	}

	private SparseArray<SoundSpec> id2spec = new SparseArray<SoundSpec>();

	public SoundManager(TeaLeaf context, ResourceManager resourceManager) {
		this.context = context;
		this.resourceManager = resourceManager;
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				SoundSpec spec;
				synchronized (this) {
					spec = id2spec.get(sampleId);
				}
				if (spec != null) {
					synchronized (spec) {
						if (status == 0) { // success
							spec.loaded = true;
							spec.failed = false;
							SoundManager.this.sendLoadedEvent(spec.url);
						} else { // failure
							spec.failed = true;
							spec.loaded = false;
							SoundManager.this.sendErrorEvent(spec.url);
						}
						spec.notifyAll();
					}
				}
			}
		});

		new Thread(this).start();
	}

	public void run() {
		AssetManager am = context.getAssets();

		while (true) {
			SoundSpec spec;
			try {
				spec = loadingQueue.take();
			} catch (InterruptedException e) {
				continue;
			}
			synchronized (spec) {
				// try loading from the file system
				File sound = resourceManager.getFile(spec.url);
				if (sound == null || !sound.exists()) {
					try {
						// not on the file system, try loading from assets
						AssetFileDescriptor afd = am.openFd("resources/" + spec.url);
						synchronized (this) {
							spec.id = soundPool.load(afd, 1);
							id2spec.put(spec.id, spec);
						}
					} catch(IOException e) {
						spec.id = -1;
						spec.failed = true;
						spec.loaded = false;
					}
				} else {
					synchronized (this) {
						spec.id = soundPool.load(sound.getAbsolutePath(), 1);
						id2spec.put(spec.id, spec);
					}
				}
				if (spec.failed) {
					sendErrorEvent(spec.url);
					spec.notifyAll();
				}
			}
		}
	}

	public void playSound(String url, float volume, boolean loop) {
		if (url.equals(SoundQueue.LOADING_SOUND)) {
			if (loadingSound != null && shouldResumeLoadingSound) {
				if (!loadingSound.isPlaying()) {
					loadingSound.start();
				}
			} else {
				int id = context.getResources().getIdentifier(SoundQueue.LOADING_SOUND, "raw", context.getPackageName());
				if (id != 0) {
					loadingSound = MediaPlayer.create(context, id);
					if(loadingSound != null) {
						loadingSound.start();
						loadingSound.setLooping(true);
					}
				}
			}
		} else {
			SoundSpec sound = getSound(url);

			if (sound == null) {
				logger.log("{sound} ERROR: Internal sound is null");
			} else {
				int stream = soundPool.play(sound.id, volume, volume, 1, loop ? -1 : 0, 1);
				sound.stream = stream;
				if (pausedSounds.contains(sound)) {
					pausedSounds.remove(sound);
				}
			}
		}
	}

	private void setDuration(String url, MediaPlayer mp) {
		Integer dur = durations.get(url);
		if (dur == null) {
			dur = mp.getDuration();
			durations.put(url, dur);
			sendDurationEvent(url, dur / 1000.0);
		}
	}
	
	public void playBackgroundMusic(String url, float volume, boolean loop) {
		shouldResumeBackgroundMusic = true;
		// this means we probably paused it. Just resume, don't start over
		if (url.equals(backgroundMusicUrl) && backgroundMusic != null) {
			backgroundMusic.start();
			backgroundMusic.setVolume(volume, volume);
			backgroundMusic.setLooping(loop);
			return;
		}

		if (backgroundMusic != null) {
			backgroundMusic.stop();
			backgroundMusic.release();
		}
		File file = resourceManager.getFile(url);
		if (file != null && file.exists()) {
			// load it from the fs
			FileInputStream fileInputStream = null;
			backgroundMusic = new MediaPlayer();
			try {
				fileInputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// ignore files we can't find
				return;
			}
			try {
				backgroundMusic.setDataSource(fileInputStream.getFD());
			} catch (Exception e) {
				logger.log(e);
			}
		} else {
			// try loading from assets
			AssetFileDescriptor afd = null;
			try {
				afd = context.getAssets().openFd("resources/" + url);
			} catch (IOException e) {
				// ignore files we can't find
				return;
			}
			if(afd != null) {
				try {
					backgroundMusic = new MediaPlayer();
					backgroundMusic.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				} catch (Exception e) {
					logger.log(e);
					return;
				}
			}
		}

		backgroundMusicUrl = url;
		try {
			backgroundMusic.prepare();
		} catch (Exception e) {
			logger.log(e);
		}

		setDuration(url, backgroundMusic);
		backgroundMusic.setVolume(volume, volume);
		if (!appPaused) {
			backgroundMusic.start();
		}
		backgroundMusic.setLooping(loop);
	}

	public void loadBackgroundMusic(String url) {
		if (backgroundMusic != null) {
			backgroundMusic.stop();
			backgroundMusic.release();
		}
		File file = resourceManager.getFile(url);
		if(file != null && file.exists()) {
			// load it from the fs
			FileInputStream fileInputStream = null;
			backgroundMusic = new MediaPlayer();
			try {
				fileInputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				logger.log(e);
			}
			backgroundMusicUrl = url;
			try {
				backgroundMusic.setDataSource(fileInputStream.getFD());
				backgroundMusic.prepareAsync();
				setDuration(url, backgroundMusic);
			} catch (Exception e) {
				logger.log(e);
			}
		} else {
			// try loading from assets
			AssetFileDescriptor afd = null;
			try {
				afd = context.getAssets().openFd("resources/" + url);
			} catch (IOException e) {
				logger.log(e);
			}
			if(afd != null) {
				backgroundMusicUrl = url;
				try {
					backgroundMusic = new MediaPlayer();
					backgroundMusic.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
					backgroundMusic.prepareAsync();
					setDuration(url, backgroundMusic);
				} catch (Exception e) {
					logger.log(e);
				}
			}
		}
	}

	public void stopSound(String url) {
		if (url.equals(backgroundMusicUrl)) {

			if (backgroundMusic != null) {
				backgroundMusic.stop();
				backgroundMusic.release();
				backgroundMusic = null;
			}

			shouldResumeBackgroundMusic = false;
		} else if(url.equals(SoundQueue.LOADING_SOUND)) {
			if(loadingSound != null) {
				loadingSound.stop();
				loadingSound.release();
				loadingSound = null;
				shouldResumeLoadingSound = false;
			}
		} else {
			SoundSpec sound = getSound(url);
			if (sound != null) {
				soundPool.stop(sound.stream);
			}
		}
	}

	public void pauseSound(String url) {
		if (url.equals(backgroundMusicUrl)) {
			if (backgroundMusic != null) {
				backgroundMusic.pause();
				shouldResumeBackgroundMusic = false;
			}
		} else if(url.equals(SoundQueue.LOADING_SOUND)) {
			if(loadingSound != null) {
				loadingSound.pause();
			}
		} else {
			SoundSpec sound = getSound(url);
			if (sound != null) {
				soundPool.pause(sound.stream);
				pausedSounds.add(sound);
			}
		}
	}

	public void setVolume(String url, float volume) {
		if (url.equals(backgroundMusicUrl)) {
			if (backgroundMusic != null) {
				backgroundMusic.setVolume(volume, volume);
			}
		} else {
			SoundSpec sound = getSound(url);
			if (sound != null) {
				sound.volume = volume;
				soundPool.setVolume(sound.stream, volume, volume);
			}
		}
	}

	public void seekTo(String url, float position) {
		if (url.equals(backgroundMusicUrl) && backgroundMusic != null) {
			backgroundMusic.seekTo((int) (position * 1000));
		}
	}

	public void destroy(String url) {
		SoundSpec sound = sounds.get(url);
		sounds.remove(url);

		if (sound != null) {
			soundPool.stop(sound.stream);
			soundPool.unload(sound.id);
		}
	}

	public void onPause() {
		appPaused = true;
		soundPool.autoPause();
		if (backgroundMusic != null) {
			backgroundMusic.pause();
		}
	}

	public void onResume() {
		appPaused = false;
		soundPool.autoResume();
		if (backgroundMusic != null && shouldResumeBackgroundMusic) {
			backgroundMusic.start();
		}
		pausedSounds.clear();
	}

	public SoundSpec loadSound(final String url) {
		return loadSound(url, false);
	}

	public SoundSpec loadSound(final String url, boolean async) {
		SoundSpec spec;
		synchronized (sounds) {
			spec = sounds.get(url);
			if (spec == null) {
				spec = new SoundSpec(url, 0, 0, false);
				sounds.put(url, spec);
				try {
					loadingQueue.put(spec);
				} catch (InterruptedException e) {
					sendErrorEvent(spec.url);
					return null;
				}
			} else if (spec.loaded) {
				sendLoadedEvent(url);
			}
		}

		if (async) { return null; }
		// are we already loading this sound?
		synchronized (spec) {
			// did we try before and have it fail?
			if (spec.failed) { return null; }

			while (!spec.loaded) {
				try {
					spec.wait();
					if (spec.failed) {
						return null;
					}
				} catch (InterruptedException e) {
					return null;
				}
			}
		}
		
		return spec;
	}

	private SoundSpec getSound(String url) {
		SoundSpec sound = null;

		if (url != null) {
			sound = sounds.get(url);
			if (sound == null || !sound.loaded) {
				sound = loadSound(url);
			}
		}

		return sound;
	}

	private void sendDurationEvent(String url, double dur) {
		SoundDurationEvent event = new SoundDurationEvent(url, dur);
		EventQueue.pushEvent(event);
	}

	private void sendLoadedEvent(String url) {
		SoundLoadedEvent event = new SoundLoadedEvent(url);
		EventQueue.pushEvent(event);
	}

	private void sendErrorEvent(String url) {
		SoundErrorEvent event = new SoundErrorEvent(url);
		EventQueue.pushEvent(event);
	}

}
