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

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import android.content.pm.ActivityInfo;
import com.tealeaf.ActivityState;
import com.tealeaf.event.BackButtonEvent;
import com.tealeaf.event.JSUpdateNotificationEvent;
import com.tealeaf.event.KeyboardScreenResizeEvent;
import com.tealeaf.event.LaunchTypeEvent;
import com.tealeaf.event.MarketUpdateNotificationEvent;
import com.tealeaf.event.OnUpdatedEvent;
import com.tealeaf.event.PauseEvent;
import com.tealeaf.event.PhotoBeginLoadedEvent;
import com.tealeaf.event.WindowFocusAcquiredEvent;
import com.tealeaf.event.WindowFocusLostEvent;
import com.tealeaf.plugin.PluginManager;
import com.tealeaf.util.ILogger;

import android.graphics.Rect;
import android.view.ViewTreeObserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.ComponentCallbacks2;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.app.ActivityManager;

import org.json.JSONObject;

/*
 * FIXME general things
 * there's /a lot/ of stuff in this class.	Much of it needs to be moved into
 * other places so that each unit only has one concern.
 *
 * Longer term, this activity should become the activity that always starts and
 * then figures out which game activity to run.
 */
public class TeaLeaf extends FragmentActivity {
	private TeaLeafOptions options;

	public TeaLeafGLSurfaceView glView;

	private boolean glViewPaused;
	private boolean isFullScreen;
	protected FrameLayout group;
	protected Overlay overlay;
	protected TextInputView textboxview;
	private TextEditViewHandler textEditView;

	private Uri launchURI;

	private int lastVisibleHeight = -1;
	private ContactList contactList;
	private SoundQueue soundQueue;
	protected LocalStorage localStorage;
	private ResourceManager resourceManager;
	private Settings settings;
	private IMenuButtonHandler menuButtonHandler;

	private BroadcastReceiver screenOffReciever;

	private ILogger remoteLogger;

	public boolean takeScreenshot = false;

	private static TeaLeaf instance = null;
	public static TeaLeaf get() { return instance; }

	private boolean paused = true;

	//variables for pause and resume
	boolean didPause = true;
	boolean didResume = false;
	boolean didLoseFocus = true;
	boolean didRegainFocus = false;

	//edit text
	public EditTextView editText;

	/**
	 *
	 * @return true if the app is running and in foreground and false if it's
	 * either not running or in the background.
	 */
	public static boolean inForeground() {
		TeaLeaf tealeaf = TeaLeaf.get();
		boolean inForeground = false;
		if (tealeaf != null) {
			inForeground = !tealeaf.paused;
		}
		return inForeground;
	}

	public ILogger getLoggerInstance(Context context) {
		return new RemoteLogger(context);
	}

	public String getLaunchUri() { return launchURI.toString(); }
	public String getCodeHost() { return "http://" + options.getCodeHost() + ":" + options.getCodePort() + "/"; }
	public TeaLeafOptions getOptions() { return options; }
	public ILogger getRemoteLogger() { return remoteLogger; }
	public ContactList getContactList() { return contactList; }
	public SoundQueue getSoundQueue() { return soundQueue; }
	public Settings getSettings() { return settings; }
	public LocalStorage getLocalStorage() { return localStorage; }
	public ResourceManager getResourceManager() { return resourceManager; }

	// FIXME this shouldn't be necessary, but TeaLeafGLSurfaceView needs to know if there's a textbox layer
	public boolean hasTextInputView() { return textboxview != null; }
	// FIXME this shouldn't be necessary, but TeaLeafGLSurfaceView needs to know if there's an overlay
	public boolean hasOverlay() { return overlay != null; }
	public synchronized Overlay getOverlay() {
		if(overlay == null) {
			overlay = new Overlay(this);
			group.addView(overlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			overlay.bringToFront();
		}
		return overlay;
	}

	public synchronized TextInputView getTextInputView() {
		if(textboxview == null) {
			textboxview = new TextInputView(TeaLeaf.this);
			group.addView(textboxview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			if(overlay != null) {
				overlay.bringToFront();
			}
		}
		return textboxview;
	}
	protected void moveViewsToFront() {
		// if the textbox view is available, it should be ontop of the gl view
		if(textboxview != null) {
			textboxview.bringToFront();
		}
		// if the overlay is available, it should be the absolute topmost layer
		if(overlay != null) {
			overlay.bringToFront();
		}
	}


	public void clearLocalStorage() { localStorage.clear(); }
	public void clearTextures() {
		if(glView != null) {
			glView.clearTextures();
		}
	}
	public void restartGLView() {
		logger.log("{tealeaf} Restarting GL View");
		if(glView != null) {
			glView.restart();
			glViewPaused = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (getOptions().isDevelop()) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.debugmenu, menu);
			return true;
		} else {
			return false;
		}

	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (!menuButtonHandler.onPress(id)) {
			return super.onOptionsItemSelected(item);
		} else {
			return true;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        PluginManager.init(this);
		instance = this;
		setFullscreenFlag();
		configureActivity();
		String appID = findAppID();
		options = new TeaLeafOptions(this);

		PluginManager.callAll("onCreate", this, savedInstanceState);

		//check intent for test app info
		Bundle bundle = getIntent().getExtras();
		boolean isTestApp = false;
		if (bundle != null) {
		   isTestApp = bundle.getBoolean("isTestApp", false);

		   if (isTestApp) {
			   options.setAppID(appID);
			   boolean isPortrait = bundle.getBoolean("isPortrait", false);
			   if (isPortrait) {
				   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			   } else {
				   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			   }
			   options.setCodeHost(bundle.getString("hostValue"));
			   options.setCodePort(bundle.getInt("portValue"));
			   String simulateID = bundle.getString("simulateID");
			   options.setSimulateID(simulateID);
		   }
		}

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		group = new FrameLayout(this);
		setContentView(group);

		// TextEditViewHandler setup
		textEditView = new TextEditViewHandler(this);

		settings = new Settings(this);
		remoteLogger = (ILogger)getLoggerInstance(this);

		checkUpdate();
		compareVersions();
		setLaunchUri();

		// defer building all of these things until we have the absolutely correct options
		logger.buildLogger(this, remoteLogger);
		resourceManager = new ResourceManager(this, options);
		contactList = new ContactList(this, resourceManager);
		soundQueue = new SoundQueue(this, resourceManager);
		localStorage = new LocalStorage(this, options);

		// start push notifications, but defer for 10 seconds to give us time to start up
		PushBroadcastReceiver.scheduleNext(this, 10);

		glView = new TeaLeafGLSurfaceView(this);
		glViewPaused = false;

		// default screen dimensions
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		int orientation = getRequestedOrientation();

		// gets real screen dimensions without nav bars on recent API versions
		if (isFullScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Point screenSize = new Point();
			try {
				display.getRealSize(screenSize);
				width = screenSize.x;
				height = screenSize.y;
			} catch (NoSuchMethodError e) {}
		}

		// flip width and height based on orientation
		if ((orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && height > width)
			|| (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && width > height))
		{
			int tempWidth = width;
			width = height;
			height = tempWidth;
		}

		final AbsoluteLayout absLayout = new AbsoluteLayout(this);
		absLayout.setLayoutParams(new android.view.ViewGroup.LayoutParams(width, height));
		absLayout.addView(glView, new android.view.ViewGroup.LayoutParams(width, height));

		group.addView(absLayout);
		editText = EditTextView.Init(this);

		if (isTestApp) {
			startGame();
		}

		soundQueue.playSound(SoundQueue.LOADING_SOUND);
		doFirstRun();
		remoteLogger.sendLaunchEvent(this);

		paused = false;
		menuButtonHandler = MenuButtonHandlerFactory.getButtonHandler(this);

		group.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
			public void onGlobalLayout(){
				// get visible area of the view
				Rect r = new Rect();
				group.getWindowVisibleDisplayFrame(r);
				
				int visibleHeight = r.bottom;

				// TODO
				// maybe this should be renamed
				if (visibleHeight != lastVisibleHeight) {
					lastVisibleHeight = visibleHeight;
					EventQueue.pushEvent(new KeyboardScreenResizeEvent(visibleHeight));
				}
			}
		});
	}

	public Bitmap getBitmapFromView(EditText view) {
		//Define a bitmap with the same size as the view
		Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
		//Bind a canvas to it
		Canvas canvas = new Canvas(returnedBitmap);
		//Get the view's background
		Drawable bgDrawable =view.getBackground();
		if (bgDrawable!=null) {
			//has background drawable, then draw it on the canvas
			bgDrawable.draw(canvas);
		} 
		// draw the view on the canvas
		view.draw(canvas);
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		p.setTextSize(24);
		canvas.drawText("'ello mate", 0, 0, p);
		//return the bitmap
		return returnedBitmap;
	}

	public void pauseGL() {
		logger.log("{tealeaf} pauseGL called");
		if (glView != null && !glViewPaused) {
			logger.log("{tealeaf} Pausing glView");
			glView.onPause();
			glViewPaused = true;
		}
	}

	public void resumeGL() {
		logger.log("{tealeaf} resumeGL called");
		if (glView != null) {
			logger.log("{tealeaf} Resuming glView");
			glView.queueResumeEvent();
			glView.onResume();
			glViewPaused = false;
		}
	}

	private void checkUpdate() {
		if(settings.isUpdateReady(options.getBuildIdentifier())) {
			if(settings.isMarketUpdate(options.getBuildIdentifier())) {
				// redirect the user to the market
				logger.log("{updates} Got a startup market update");
				EventQueue.pushEvent(new MarketUpdateNotificationEvent());
			}
		}
	}

	public void makeOverlay() {
		overlay = new Overlay(this);
		group.addView(overlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		overlay.bringToFront();
	}


	public void setServer(String host, int port) {
		options.setCodeHost(host);
		options.setTcpHost(host);
		options.setCodePort(port);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("@__prev_host__", host);
		editor.putInt("@__prev_port__", port);
		editor.commit();
		setLaunchUri();
	}

	@Override
	protected void onStart() {
		super.onStart();
		PluginManager.callAll("onStart");

	}

	@Override
	public void onNewIntent(Intent intent) {
		PluginManager.callAll("onNewIntent", intent);
	}

	private void getLaunchType(Intent intent) {
		Uri data = intent.getData();
		LaunchTypeEvent event;
		//launch type "notification" has been taken out
		if (data != null) {
			logger.log("{tealeaf} Launched with intent url:", data.toString());
			event = new LaunchTypeEvent("url", data.toString());
		} else {
			event = new LaunchTypeEvent("standard");
		}
		EventQueue.pushEvent(event);
	}

	// This is called when focus and onResume have both been achieved
	private void onRealResume() {
		logger.log("{tealeaf} Activity has resumed");
		if (glView != null) {
			logger.log("{tealeaf} Resuming GL");
			resumeGL();
			soundQueue.onResume();
			soundQueue.playSound(SoundQueue.LOADING_SOUND);
			PluginManager.callAll("onResume");

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				final ActivityManager activityManager = (ActivityManager) instance.getSystemService(Context.ACTIVITY_SERVICE);
				ActivityManager.RunningAppProcessInfo currentState = activityManager.getRunningAppProcesses().get(0);
				ActivityManager.getMyMemoryState(currentState);
				glView.onMemoryWarning(currentState.lastTrimLevel);
			}
		}


	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			logger.log("{focus} Gained focus");
			ActivityState.onWindowFocusAcquired();
			if (ActivityState.hasResumed(true)) {
				onRealResume();
			}
			registerScreenOffReceiver();

			// always send acquired focus event
			EventQueue.pushEvent(new WindowFocusAcquiredEvent());

			// games are inherently full screen and immersive, hide OS UI bars
			if (isFullScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				int uiFlag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					uiFlag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
					uiFlag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
					uiFlag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
					uiFlag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						uiFlag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
					}
				}
				getWindow().getDecorView().setSystemUiVisibility(uiFlag);
			}
		} else {
			logger.log("{focus} Lost focus");
			ActivityState.onWindowFocusLost();
			pause();
			unregisterReceiver(screenOffReciever);
			if (jsRunning) {
				//always send lost focus event
				String[] events = {new WindowFocusLostEvent().pack()};

				// DANGER: Calling dispatchEvents() is NOT thread-safe.
				// Doing it here because the GLThread is paused.
				NativeShim.dispatchEvents(events);
			}
		}
	}

	// Functions/variable used to indicate if javascript steps
	// should execute and if events should be dispatched
	private boolean jsRunning = true;
	public void stopJS() {
		jsRunning = false;
	}

	public void startJS() {
		jsRunning = true;
	}

	public boolean isJSRunning() {
		return jsRunning;
	}

	@Override
	protected void onPause() {
		logger.log("{tealeaf} Activity got onPause");
		super.onPause();

		ActivityState.onPause();
		pauseGL();
		paused = true;
		pause();
	}

	private void pause() {
		if (ActivityState.hasPaused(true)) {
			//TODO only dispatch this event when JS is running.
			if (glView != null && glView.running()) {
				if (!glView.isResumeEventQueued()) {
					PluginManager.callAll("onPause");
					if (jsRunning) {
						String[] events = {new PauseEvent().pack()};

						// DANGER: Calling dispatchEvents() is NOT thread-safe.
						// Doing it here because the GLThread is paused.
						NativeShim.dispatchEvents(events);
					}
					glView.setRendererStateReloading();
				}
			}
			soundQueue.onPause();
			soundQueue.pauseSound(SoundQueue.LOADING_SOUND);
		}
	}

	public void onConfigurationChanged(Configuration config) {

		super.onConfigurationChanged(config);
	}

	@Override
	protected void onResume() {
		logger.log("{tealeaf} Resume");

		super.onResume();
		ActivityState.onResume();
		paused = false;
		if(settings.isUpdateReady(options.getBuildIdentifier())) {
			if(settings.isMarketUpdate(options.getBuildIdentifier())) {
				// market update
				logger.log("{updates} Got a resume market update");
				EventQueue.pushEvent(new MarketUpdateNotificationEvent());
			} else {
				// js update
				logger.log("{updates} Got a resume JS update");
				EventQueue.pushEvent(new JSUpdateNotificationEvent());
			}
		}

		if (ActivityState.hasResumed(true)) {
			onRealResume();
		}

		getLaunchType(getIntent());
	}

	@Override
	protected void onStop() {
		super.onStop();
		PluginManager.callAll("onStop");
	}

	@Override
	public void onBackPressed() {
		String [] objs = PluginManager.callAll("consumeOnBackPressed");

		boolean consume = true;
		
		for (String o : objs) {
			if (o != null && Boolean.valueOf(o)) {
				consume = true;
				break;
			}
			consume = false;
		}

		if (consume) {
			EventQueue.pushEvent(new BackButtonEvent());
		} else {
			PluginManager.callAll("onBackPressed");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PluginManager.callAll("onDestroy");
		logger.log("{tealeaf} Destroy");
		glView.destroy();
		NativeShim.reset();
	}

	private void setFullscreenFlag() {
		try {
			Bundle metaData = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
			isFullScreen = metaData.getBoolean("fullscreen", true);
		} catch (NameNotFoundException e) {
			logger.log(e);
			//Default to fullscreen mode.
			isFullScreen = true;
		}
	}

	private String findAppID() {
		String appid = getIntent().getStringExtra("appid");
		if(appid != null) {
			return appid;
		}
		// FIXME HACK: find a better way to determine the appID
		try {
			Bundle metaData = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
			return metaData.containsKey("appID") ? metaData.get("appID").toString() : "tealeaf";
		} catch (NameNotFoundException e) {
			logger.log(e);
			return "tealeaf";
		}
	}

	private void configureActivity() {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		if (isFullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void compareVersions() {
		String newVersion = options.getBuildIdentifier();
		String oldVersion = settings.getString("version", null);
		boolean firstRun = oldVersion == null;
		if(!newVersion.equals(oldVersion)) {
			// new version, send an event and clean the old files
			EventQueue.pushEvent(new OnUpdatedEvent(oldVersion, newVersion, firstRun));
			settings.setString("version", newVersion);
		}
	}

	private void doFirstRun() {
		if (settings.isFirstRun()) {
			remoteLogger.sendFirstLaunchEvent(this);
			settings.markFirstRun();
		}
	}

	private void setLaunchUri() {
		launchURI = getIntent().getData();
		if (launchURI == null) {
			launchURI = Uri.parse(getCodeHost() + options.getAppID() + "/");
		} else {
			if(launchURI.isRelative()) {
				launchURI = Uri.parse("http:" + launchURI.toString());
			} else {
				launchURI = Uri.parse(launchURI.toString().replace(launchURI.getScheme(), "http"));
			}
		}
	}

	public void startGame() {
		glView.start();
		glView.setVisibility(View.VISIBLE);
	}

	protected void reset() {
		group.removeView(glView);
		glViewPaused = false;
		glView.destroy();
		NativeShim.reset();
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	private void makeScreenOffReceiver() {
		screenOffReciever = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub

				soundQueue.onPause();
				soundQueue.pauseSound(SoundQueue.LOADING_SOUND);
			}
		};

	}

	private void registerScreenOffReceiver() {
		if(screenOffReciever == null) {
			makeScreenOffReceiver();
		}
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.SCREEN_OFF");
		registerReceiver(screenOffReciever, filter);

	}

    static int ROTATE_0 = 0;
    static int ROTATE_90 = 90;
    static int ROTATE_180 = 180;
    static int ROTATE_270 = 270;
    private Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        // rotate as needed
        Bitmap bmp;

		//only allow the largest size to be 768 for now, several phones
		//including the galaxy s3 seem to crash with rotating very large 
		//images (out of memory errors) from the gallery
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap scaled = bitmap;
		if (w > h && w > 768) {
			float ratio = 768.f / (float)w;		
			w = 768;
			h = (int) (ratio * h);
			scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);
			if (bitmap != scaled) {
				bitmap.recycle();
			}
		} if (h > w && h > 768) {
			float ratio = 768.f / (float)h;		
			h = 768;
			w = (int) (ratio * w);
			scaled = Bitmap.createScaledBitmap(bitmap, w, h, true);
			if (bitmap != scaled) {
				bitmap.recycle();
			}
		}

        int newWidth = scaled.getWidth();
        int newHeight = scaled.getHeight();

        int degrees = 0;
        if (rotate == ROTATE_90 || rotate == ROTATE_270) {
            newWidth = scaled.getHeight();
            newHeight = scaled.getWidth();
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);

        bmp = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(),
                scaled.getHeight(), matrix, true);
        if (scaled != bmp) {
            scaled.recycle();
        }

        return bmp;
    }

	// TODO: can this be called after your activity is recycled, meaning we're never going to see these events?
	protected void onActivityResult(int request, int result, Intent data) {
		super.onActivityResult(request, result, data);
		PluginManager.callAll("onActivityResult", request, result, data);
		logger.log("GOT ACTIVITY RESULT WITH", request, result);
		

		switch(request) {
			case PhotoPicker.CAPTURE_IMAGE:
				if(result == RESULT_OK) {
					EventQueue.pushEvent(new PhotoBeginLoadedEvent());
					Bitmap bmp = null;

					if (data != null) {
						Bundle extras = data.getExtras();
						//try and get bitmap off of intent
						if (extras != null) {
							bmp = (Bitmap) extras.get("data");
						}

					}

					//try the large file on disk
					final File f = PhotoPicker.getCaptureImageTmpFile();
					if (f != null && f.exists()) {
						new Thread(new Runnable() {
							public void run(){
								Bitmap bmp = null;
								String filePath = f.getAbsolutePath();

								try {
									bmp = BitmapFactory.decodeFile(filePath);	
								} catch (OutOfMemoryError e) {
									System.gc();
									BitmapFactory.Options options = new BitmapFactory.Options();
									options.inSampleSize = 4;
									bmp = BitmapFactory.decodeFile(filePath, options);
								}

								if (bmp != null) {
									try {
										File f = new File(filePath);
										ExifInterface exif = new ExifInterface(
											f.getAbsolutePath());
										int orientation = exif.getAttributeInt(
											ExifInterface.TAG_ORIENTATION,
											ExifInterface.ORIENTATION_NORMAL);
										if (orientation != ExifInterface.ORIENTATION_NORMAL) {
											int rotateBy = 0;
											switch(orientation) {
												case ExifInterface.ORIENTATION_ROTATE_90:
													rotateBy = ROTATE_90;
													break;
												case ExifInterface.ORIENTATION_ROTATE_180:
													rotateBy = ROTATE_180;
													break;
												case ExifInterface.ORIENTATION_ROTATE_270:
													rotateBy = ROTATE_270;
													break;
											}
											Bitmap rotatedBmp = rotateBitmap(bmp, rotateBy);
											if (rotatedBmp != bmp) {
												bmp.recycle();
											}
											bmp = rotatedBmp;
										}
									} catch(Exception e) {
										logger.log(e);
									}
									f.delete();

									if (bmp != null) {
										glView.getTextureLoader()
											.saveCameraPhoto(glView.getTextureLoader().getCurrentPhotoId(), bmp);
										glView.getTextureLoader()
											.finishCameraPicture();
									} else {
										glView.getTextureLoader().failedCameraPicture();
									}
								}
							}
						}).start();

					} else {
						glView.getTextureLoader().saveCameraPhoto(glView.getTextureLoader().getCurrentPhotoId(), bmp);
						glView.getTextureLoader().finishCameraPicture();
					}
				} else {
					glView.getTextureLoader().failedCameraPicture();
				}
				break;
			case PhotoPicker.PICK_IMAGE:
				if(result == RESULT_OK) {
					final Uri selectedImage = data.getData();
					EventQueue.pushEvent(new PhotoBeginLoadedEvent());
					
					String[] filePathColumn = { MediaColumns.DATA,
												MediaStore.Images.ImageColumns.ORIENTATION };

					String _filepath = null;

					try {
						Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
						cursor.moveToFirst();

						int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						_filepath = cursor.getString(columnIndex);
						columnIndex = cursor.getColumnIndex(filePathColumn[1]);
						int orientation = cursor.getInt(columnIndex);
						cursor.close();
					} catch (Exception e) {
					
					}

					final String filePath = _filepath;

					new Thread(new Runnable() {
						public void run(){
							if (filePath == null) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								InputStream inputStream;
								Bitmap bmp = null;

								try {
									inputStream = getContentResolver().openInputStream(selectedImage);
									bmp = BitmapFactory.decodeStream(inputStream, null, options);
									inputStream.close();
								} catch (Exception e) {
									logger.log(e);

								}

								if (bmp != null) {
									glView.getTextureLoader()
									.saveGalleryPicture(glView.getTextureLoader().getCurrentPhotoId(), bmp);
									glView.getTextureLoader()
									.finishGalleryPicture();
								} else {
									glView.getTextureLoader().failedGalleryPicture();
								}

							} else {
								Bitmap bmp = null;

								try {
									bmp = BitmapFactory.decodeFile(filePath);	
								} catch (OutOfMemoryError e) {
									System.gc();
									BitmapFactory.Options options = new BitmapFactory.Options();
									options.inSampleSize = 4;
									bmp = BitmapFactory.decodeFile(filePath, options);
								}

								if (bmp != null) {
									try {
										File f = new File(filePath);
										ExifInterface exif = new ExifInterface(
												f.getAbsolutePath());
										int orientation = exif.getAttributeInt(
												ExifInterface.TAG_ORIENTATION,
												ExifInterface.ORIENTATION_NORMAL);
										if (orientation != ExifInterface.ORIENTATION_NORMAL) {
											int rotateBy = 0;
											switch(orientation) {
												case ExifInterface.ORIENTATION_ROTATE_90:
													rotateBy = ROTATE_90;
													break;
												case ExifInterface.ORIENTATION_ROTATE_180:
													rotateBy = ROTATE_180;
													break;
												case ExifInterface.ORIENTATION_ROTATE_270:
													rotateBy = ROTATE_270;
													break;
											}
											Bitmap rotatedBmp = rotateBitmap(bmp, rotateBy);
											if (rotatedBmp != bmp) {
												bmp.recycle();
											}
											bmp = rotatedBmp;
										}
									} catch(Exception e) {
										logger.log(e);
									}

									if (bmp != null) {
										glView.getTextureLoader()
										.saveGalleryPicture(glView.getTextureLoader().getCurrentPhotoId(), bmp);
										glView.getTextureLoader()
										.finishGalleryPicture();
									} else {
										glView.getTextureLoader().failedGalleryPicture();
									}
								}
							}
						}
					}).start();

				} else {
					glView.getTextureLoader().failedGalleryPicture();
				}
				break;
		}
	}

	public TextEditViewHandler getTextEditViewHandler() {
		return textEditView;
	}

	public FrameLayout getGroup() {
		return group;	
	}

	public void reload() {

	}

	static {
		System.loadLibrary("tealeaf");
	}

	@Override
	public void onLowMemory() {
		if (glView != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				NativeShim.textureManagerMemoryCritical();
			} else {
				glView.onMemoryWarning(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
			}
		}
	}

	@Override
	public void onTrimMemory(int level) {
		if (glView != null) {
			glView.onMemoryWarning(level);
		}
	}

}
