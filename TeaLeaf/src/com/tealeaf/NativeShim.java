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

import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.json.JSONObject;
import java.lang.StringBuilder;
import java.io.*;

import com.google.gson.Gson;
import com.tealeaf.event.DialogButtonClickedEvent;
import com.tealeaf.event.OnlineEvent;
import com.tealeaf.plugin.PluginManager;
import com.tealeaf.util.Connection;
import com.tealeaf.util.ILogger;
import com.tealeaf.util.XMLHttpRequest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;

import android.view.Window;

import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.os.Build;

public class NativeShim {
	private static HashMap<String, TeaLeafCallable> callables = new HashMap<String, TeaLeafCallable>();
	private SoundQueue soundQueue;
	private TextureLoader textureLoader;
	private TextManager textManager;
	private LocalStorage localStorage;
	private ContactList contactList;
	private Haptics haptics;
	private LocationManager locationManager;
	private TeaLeaf context;
	private ResourceManager resourceManager;
	private ArrayList<TeaLeafSocket> sockets = new ArrayList<TeaLeafSocket>();
	private ArrayList<String> overlayEvents = new ArrayList<String>();
	private ILogger remoteLogger;
	private ConnectivityManager connectivityManager;
	private NetworkStateReceiver networkStateReceiver;
	private boolean onlineStatus;
	private int statusBarHeight;
	private Gson gson = new Gson();
	public NativeShim(TextManager textManager, TextureLoader textureLoader, SoundQueue soundQueue,
			LocalStorage localStorage, ContactList contactList,
			LocationManager locationManager, ResourceManager resourceManager,
			TeaLeaf context) {
		this.textManager = textManager;
		this.textureLoader = textureLoader;
		new Thread(this.textureLoader).start();
		this.soundQueue = soundQueue;
		this.localStorage = localStorage;
		this.contactList = contactList;
		this.haptics = new Haptics(context);
		this.locationManager = locationManager;
		this.resourceManager = resourceManager;
		this.context = context;
		this.remoteLogger = context.getRemoteLogger();
		this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.networkStateReceiver = new NetworkStateReceiver(this);
		this.onlineStatus = false;
		this.updateOnlineStatus();

		this.statusBarHeight = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			this.statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
			logger.log("status bar height:", this.statusBarHeight);
		}

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(this.networkStateReceiver, filter);

	}

	public void onPause() {
	}

	public void onResume() {
	}

	public String getVersionCode() {
		return context.getOptions().getBuildIdentifier();
	}

	//xhr
	public void sendXHR(int id, String method, String url, String data, boolean async, String[] requestHeaders) {
		HashMap<String,String> requestHeadersMap = new HashMap<String,String>();
		if(requestHeaders != null) {
			for(int i = 0; i < requestHeaders.length / 2; i++) {
				requestHeadersMap.put(requestHeaders[i*2], requestHeaders[i*2+1]);
			}
		}
		XMLHttpRequest xhr = new XMLHttpRequest(id, method, url, data, async ,requestHeadersMap);

		if (async) {
			Thread xhrThread = new Thread(xhr);
			xhrThread.setPriority(Thread.MIN_PRIORITY+2);
			xhrThread.start();
		} else {
			xhr.run();
		}
	}

	// Display
	public DisplayMetrics getDisplayMetrics() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}

	//Haptics
	public void cancel() {
		haptics.cancel();
	}
	public void vibrate(long milliseconds) {
		haptics.vibrate(milliseconds);
	}
	public void vibrate(long[] pattern, int repeat) {
		haptics.vibrate(pattern, repeat);
	}
	public boolean hasVibrator() {
		return haptics.hasVibrator();
	}

	public void showDialog(final String title, final String text, final String imageUrl, final String[] buttons, int[] callbacks) {
		final Runnable[] cbs = new Runnable[callbacks.length];
		for(int i = 0; i < callbacks.length; i++) {
			final int callback = i;
			cbs[i] = new Runnable() {
				public void run() {
					EventQueue.pushEvent(new DialogButtonClickedEvent(callback));
				}
			};
		}
		Bitmap bitmap = null;
		if(imageUrl != null) {
			textureLoader.getImage(imageUrl);
		}
		final Bitmap image = bitmap;
		context.runOnUiThread(new Runnable() {
			public void run() {
				JSDialog.showDialog(context, image, title, text, buttons, cbs);
			}
		});
	}

	//Overlay
	public void loadOverlay(final String url) {
		context.runOnUiThread(new Runnable() { public void run() { context.getOverlay().load(url); } });
	}
	public void showOverlay() {
		context.runOnUiThread(new Runnable() { public void run() { context.getOverlay().show(); } });
	}
	public void hideOverlay() {
		context.runOnUiThread(new Runnable() { public void run() { context.getOverlay().hide(); } });
	}
	public void sendEventToOverlay(final String event) {
		context.runOnUiThread(new Runnable() { public void run() { context.getOverlay().sendEvent(event); } });
	}

	private int textInputId = 0;
	public int openPrompt(final String title, final String message, final String okText, final String cancelText, final String value, final boolean autoShowKeyboard, final boolean isPassword) {
		return InputPrompt.getInstance().open(context, title, message, okText, cancelText, value, autoShowKeyboard, isPassword);
	}
	
	public void showSoftKeyboard(final String text,
								 final String hint,
								 final boolean hasBackward,
								 final boolean hasForward,
								 final String inputType,
								 final String inputReturnButton,
								 final int maxLength,
								 final int cursorPos) {
		context.runOnUiThread(new Runnable() {
			public void run() {
				TextEditViewHandler textEditView = context.getTextEditViewHandler();
				textEditView.activate(text, hint, hasBackward, hasForward, inputType, inputReturnButton, maxLength, cursorPos);
			}
		});
	}

	public void hideSoftKeyboard() {
		context.runOnUiThread(new Runnable() {
			public void run() {
				TextEditViewHandler textEditView = context.getTextEditViewHandler();
				textEditView.closeKeyboard();
			}
		});
	}

	public int getStatusBarHeight() {
		return statusBarHeight;
	}

	public void showStatusBar() {
		context.runOnUiThread(new Runnable() {
			public void run() {
				context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		});
	}

	public void hideStatusBar() {
		context.runOnUiThread(new Runnable() {
			public void run() {
				context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		});
	}

	//TextInputView
	public int createTextBox() {
		return context.getTextInputView().createNew();
	}
	public int createTextBox(final int x, final int y, final int w, final int h, final String initialValue) {
		context.runOnUiThread(new Runnable() {
			public void run() {
				context.getTextInputView().createNew(x, y, w, h, initialValue);
			}
		});

		return 0;
	}
	public void destroyTextBox(int id) {
		context.getTextInputView().destroy(id);
	}

	public void showTextBox(int id) {
		context.getTextInputView().show(id);
	}
	public void hideTextBox(int id) {
		context.getTextInputView().hide(id);
	}
	public void textBoxSelectAll(int id) {
		context.getTextInputView().selectAll(id);
	}

	public boolean getTextBoxVisible(int id) {
		return context.getTextInputView().getVisible(id);
	}
	public int getTextBoxX(int id) {
		return context.getTextInputView().getX(id);
	}
	public int getTextBoxY(int id) {
		return context.getTextInputView().getY(id);
	}
	public int getTextBoxWidth(int id) {
		return context.getTextInputView().getWidth(id);
	}
	public int getTextBoxHeight(int id) {
		return context.getTextInputView().getHeight(id);
	}
	public int getTextBoxType(int id) {
		return context.getTextInputView().getType(id);
	}
	public String getTextBoxValue(int id) {
		return context.getTextInputView().getValue(id);
	}
	public float getTextBoxOpacity(int id) {
		return context.getTextInputView().getOpacity(id);
	}

	public void setTextBoxVisible(int id, boolean visible) {
		context.getTextInputView().setVisible(id, visible);
	}
	public void setTextBoxPosition(int id, int x, int y, int w, int h) {
		context.getTextInputView().setPosition(id, x, y, w, h);
	}
	public void setTextBoxDimensions(int id, int w, int h) {
		context.getTextInputView().setDimensions(id, w, h);
	}
	public void setTextBoxX(int id, int x) {
		context.getTextInputView().setX(id, x);
	}
	public void setTextBoxY(int id, int y) {
		context.getTextInputView().setY(id, y);
	}
	public void setTextBoxWidth(int id, int w) {
		context.getTextInputView().setWidth(id, w);
	}
	public void setTextBoxHeight(int id, int h) {
		context.getTextInputView().setHeight(id, h);
	}
	public void setTextBoxType(int id, int type) {
		context.getTextInputView().setType(id, type);
	}
	public void setTextBoxValue(int id, String value) {
		context.getTextInputView().setValue(id, value);
	}
	public void setTextBoxOpacity(int id, float value) {
		context.getTextInputView().setOpacity(id, value);
	}

	//Locale
	public String getCountry() {
		return Locale.getDefault().getCountry();
	}

	public String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	//Device
	public String getDeviceID() {
		return Device.getDeviceID(context, context.getSettings());
	}
	public String getDeviceInfo() {
		return Device.getDeviceInfo();
	}
	public void setStayAwake(final boolean on) {
		final Window w = context.getWindow();

		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (on) {
					w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					w.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			}
		});
	}
	public void reload() {
		context.reload();
	}

	//Textures
	public int measureText(String font, int size, byte[] textBytes) {
		int textSize = 0;
		try {
			String text = new String(textBytes, "UTF-8");
			textSize = textManager.measureText(font, size, text);
		} catch (Exception e) {
			logger.log(e);
		}
		return textSize;
	}

	public void loadTexture(byte[] urlBytes) {
		try {
			String url = new String(urlBytes, "UTF-8");
			textureLoader.loadTexture(url);
		} catch (Exception e) {
			logger.log(e);
		}
	}

    public int cameraGetPhoto(int width, int height, int crop) {
        int id = textureLoader.getNextCameraId();
        textureLoader.loadCameraPicture("" + id, width, height, crop);
        return id;
    }

    public int galleryGetPhoto(int width, int height, int crop) {
        int id = textureLoader.getNextGalleryId();
        textureLoader.loadGalleryPicture("" + id, width, height, crop);
        return id;
    }

	public int getNextCameraId() {
		return textureLoader.getNextCameraId();
	}

	public int getNextGalleryId() {
		return textureLoader.getNextGalleryId();
	}
	public void clearTextureData() {
		clearTextures();
	}
	public void setHalfsizedTexturesSetting(boolean on) {
		Settings settings = context.getSettings();
		settings.setBoolean("@__use_halfsized_textures__", on);
	}

	//Sound
	private static final boolean DO_SOUND = true;
	public void loadSound(final String url) {
		if (!DO_SOUND) { return; }
		soundQueue.loadSound(url);
	}
	public void playSound(String url, float volume, boolean loop) {
		if (!DO_SOUND) { return;}
		soundQueue.playSound(url, volume, loop);
	}
	public void loadBackgroundMusic(String url) {
		if (!DO_SOUND) { return;}
		soundQueue.loadBackgroundMusic(url);
	}
	public void playBackgroundMusic(final String url, final float volume, final boolean loop) {
		if (!DO_SOUND) { return;}
		soundQueue.playBackgroundMusic(url, volume, loop);
	}
	public void stopSound(String url) {
		soundQueue.stopSound(url);
	}
	public void pauseSound(String url) {
		soundQueue.pauseSound(url);
	}
	public void setVolume(String url, float volume) {
		soundQueue.setVolume(url, volume);
	}
	public void seekTo(String url, float position) {
		soundQueue.seekTo(url, position);
	}
	public void haltSounds() {
		soundQueue.haltSounds();
	}

	// Sockets
	public void sendData(int id, String data) {
		if(sockets.size() > id) {
			TeaLeafSocket socket = sockets.get(id);
			if (socket != null) {
				socket.write(data);
			} else {
				logger.log("{socket} WARNING: Send data failed on broken socket");
			}
		}
	}
	public int openSocket(String host, int port) {
		int id = sockets.size();
		logger.log("{socket} Connecting to ", host, ":", port, " (id=", id, ")");

		TeaLeafSocket socket = new TeaLeafSocket(host, port, id);
		sockets.add(socket);
		new Thread(socket).start();
		return id;
	}
	public void closeSocket(int id) {
		TeaLeafSocket socket = sockets.get(id);
		if (socket != null) {
			socket.close();
		}
	}

	//Source Loading
	public String loadSourceFile(String url) {
		TeaLeafOptions options = context.getOptions();
		String sourceString = null;
		if (options.isDevelop() && options.get("forceURL", false)) {
			// load native.js from the file system
			// read file in
			String path = resourceManager.getStorageDirectory();
			String result = null;
			DataInputStream in = null;
			try {
				File f = new File(path + url);
				byte[] buffer = new byte[(int) f.length()];
				in = new DataInputStream(new FileInputStream(f));
				in.readFully(buffer);
				result = new String(buffer);
			} catch (FileNotFoundException e) {
				logger.log("Error loading", url, "from", path);
				logger.log("File not found!");
				throw new RuntimeException("File not found in loadSourceFile");
			} catch (IOException e) {
				throw new RuntimeException("IO problem in fileToString", e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					logger.log(e);
				}
			}
			sourceString = result;

		} else {
			sourceString = resourceManager.getFileContents(url);
		}
		return sourceString;
	}

	//GL stuff
	public native static void initGL(int glName);
	public native static void setSingleShader(boolean on);
	public native static void setHalfsizedTextures(boolean on);
	//Contacts stuff
	public static native void dispatchContactCallback(int cb, long id, String name);

	public native static void textureManagerSetMaxMemory(int bytes);
	public native static void textureManagerMemoryWarning();
	public native static void textureManagerMemoryCritical();
	public native static void textureManagerResetMemoryCritical();

	//LocalStorage
	public void setData(String key, String data) {
		localStorage.setData(key, data);
	}
	public String getData(String key) {
		return localStorage.getData(key);
	}
	public void setData(String key, byte[] data) {
		try {
			String dataString = new String(data, "UTF-8");
			localStorage.setData(key, dataString);
		} catch (Exception e) {
			logger.log(e);
		}
	}
	public byte[] getDataAsBytes(String key) {
		try {
			return localStorage.getData(key).getBytes("UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	public void removeData(String key) {
		localStorage.removeData(key);
	}
	public void clearData() {
		localStorage.clear();

	}

	//location manager
	public void setLocation(String uri) {
		locationManager.setLocation(uri);
	}


	//love me some blocks
	{
	}

	//call
	public String call(String method, byte[] byteArgs) {
		String args = "{}";
		try {
			args = new String(byteArgs, "UTF-8");
		} catch (Exception e) {
			logger.log(e);
		}
		JSONObject jsonArgs = null;
		try {
			jsonArgs = new JSONObject(args);
		} catch (Exception e) {
			jsonArgs = new JSONObject();
			logger.log("NativeShim.call", e);
		}

		JSONObject jsonRet = null;
		try  {
			jsonRet = callables.get(method).call(jsonArgs);
		} catch (Exception e) {
			logger.log(e);
			jsonRet = new JSONObject();
		}

		return jsonRet.toString();
	}

	public static void RegisterCallable(String method, TeaLeafCallable callable) {
		callables.put(method, callable);
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	// plugins
	public String pluginsCall(final String className, final String methodName, final Object[] params) {
//		there may be issues not running this on the ui thread however
//		it is required for now and there doesn't seem to be any immediate
//		concerns with it running on any thread
		
		for (int i = 0; i < params.length; i++) {
			if (params[i].getClass() == byte[].class) {
				try {
					params[i] = new String((byte[])params[i], "UTF-8");
				} catch(Exception e) {
					logger.log(e);
				}
			}
		}
        return PluginManager.call(className, methodName, params);
	}

	public void pluginsRequest(final String className, final String methodName, final Object[] params, final int requestId) {
		PluginManager.request(className, methodName, params, requestId);
	}
	// native
	public String getMarketUrl() {
		return "market://details?id=" + context.getPackageName();
	}
	public void startGame(String appid) {
		logger.log("{tealeaf} Starting", appid == null ? "the default game" : appid);
		context.glView.startCrossPromo(appid);
	}
	public void applyUpdate() {
		logger.log("{tealeaf} Applying game update");
		TeaLeafOptions options = context.getOptions();
		Settings settings = context.getSettings();
		if(settings.isMarketUpdate(options.getBuildIdentifier())) {
			locationManager.setLocation(getMarketUrl());
		} else {
			Updater updater = new Updater(context, context.getOptions(), settings.getUpdateUrl(options.getBuildIdentifier()));
			if(updater.apply()) {
				settings.clear("updating_now");
				options.setBuildIdentifier(settings.getUpdateBuild(options.getBuildIdentifier()));
				settings.markUnpacked(options.getBuildIdentifier());
				startGame(null);
			}
		}
	}

	public boolean sendActivityToBack() {
		boolean success = context.moveTaskToBack(true);

		if (!success) {
			logger.log("{tealeaf} WARNING: Unable to move activity to background");
		} else {
			logger.log("{tealeaf} Moved activity to background");
		}

		return success;
	}


	public String getStorageDirectory() {
		return resourceManager.getStorageDirectory();
		//return resourceManager.resolveFile(filename);
	}

	// network status
	// TODO move all of this to TeaLeaf and register the receiver there, and make it use that directly instead
	public void updateOnlineStatus(){
		this.onlineStatus = Connection.available(context);

		if (this.onlineStatus) {
			logger.log("{reachability} Online");
		} else {
			logger.log("{reachability} Offline");
		}
	}
	public boolean getOnlineStatus() {
		return this.onlineStatus;
	}
	public void sendOnlineEvent(){
		EventQueue.pushEvent(new OnlineEvent(this.onlineStatus));
	}

	public void uploadDeviceInfo() {
		remoteLogger.sendDeviceInfoEvent(context);
	}
	public void logJavascriptError(final String message, final String url, final int lineNumber) {
		if (context.getOptions().isDevelop()) {
			context.runOnUiThread(new Runnable() {
				public void run() {
					JSDialog.showDialog(context, null, "JS Error", url + " line " + lineNumber + "\n" + message,
							new String[] {"OK"},
							new Runnable[] {new Runnable() { public void run() {} }
					});
				}
			});
		} else {
			remoteLogger.sendErrorEvent(context, "JS Error at " + url + " line " + lineNumber + ": " + message);
		}
	}

	public int[] reportGlError(int errorCode) {
		Settings settings = context.getSettings();
		String glErrorStr = settings.getString("gl_errors", "NONE");
		String errorCodeStr = Integer.toString(errorCode);


		ArrayList<String> glErrorList = null;
		if (glErrorStr.equals("NONE")) {
			glErrorList = new ArrayList<String>();
		} else {
			glErrorList = new ArrayList<String>(Arrays.asList(glErrorStr.split(",")));
		}

		int[] glErrorInts = new int[glErrorList.size()];

		if (glErrorList.contains(errorCodeStr)) {
			//create the return array, do not log as this error has been seen
			for (int i = 0; i < glErrorInts.length; i++) {
				glErrorInts[i] = Integer.parseInt(glErrorList.get(i));
			}

		} else {
			//create the return array and log
			glErrorList.add(errorCodeStr);

			//build the settings string
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < glErrorList.size(); i++) {
				stringBuilder.append(glErrorList.get(i));
				if (i != glErrorList.size() - 1) {
					stringBuilder.append(",");
				}
			}

			//save the new errors
			settings.setString("gl_errors", stringBuilder.toString());

			//log the error
			String errorString = "GL ERROR: " + errorCodeStr ;
			if (context.getOptions().isDevelop()) {
				logger.log(errorString);
			} else {
				remoteLogger.sendGLErrorEvent(context, errorString);
			}

			//create the return array
			for (int i = 0; i < glErrorInts.length; i++) {
				glErrorInts[i] = Integer.parseInt(glErrorList.get(i));
			}
		}

		return glErrorInts;
	}

	public void takeScreenshot() {
		context.takeScreenshot = true;
	}

	public int getTotalMemory() {
		return Device.getTotalMemory();
	}

	public void logNativeError() {
		Intent intent = new Intent(context, CrashRecover.class);
		context.startActivity(intent);
	}

	//build info
	public String getSDKHash() {
		return context.getOptions().getSDKHash();
	}

	public String getAndroidHash() {
		return context.getOptions().getAndroidHash();
	}

	public String getGameHash() {
		return context.getOptions().getGameHash();
	}

	public String getAndroidVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public String getSimulateID() {
		return context.getOptions().getSimulateID();
	}

	//Install stuff
	public String getInstallReferrer() {
		return context.getSettings().getString("installReferrer.referrer", "");
	}

	//Local
	public String getLocaleCountry() {
		return LocaleInfo.getCountry(context);
	}

	public String getLocaleLanguage() {
		return LocaleInfo.getLanguage(context);
	}

    public boolean isSimulator() {
        return Build.HARDWARE.contains("goldfish");
    }

	//Initialization and Running JS
	public static native boolean initIsolate();
	public static native void init(Object shim, String codeHost, String tcpHost, int codePort, int tcpPort, String entryPoint, String sourceDir, int width, int height, boolean remote_loading, String splash, String simulateID);
	public static native boolean initJS(String uri, String androidHash);
	public static native void destroy();
	public static native void reset();
	public static native void run();

	//GL stuff
	public static native void step(int dt);
	public static native void resizeScreen(int w, int h);
	public static native void reloadTextures();
	public static native void reloadCanvases();
	public static native void clearTextures();
	public static void onTextureLoaded(String url, int name, int width, int height, int originalWidth, int originalHeight, int numChannels) {
			try {
				onTextureLoaded(url.getBytes("UTF-8"), name, width, height, originalWidth, originalHeight, numChannels);
			} catch (Exception e) {
				logger.log(e);
			}
	}
	public static native void onTextureLoaded(byte[] urlBytes, int name, int width, int height, int originalWidth, int originalHeight, int numChannels);
	public static native void onTextureFailedToLoad(String url);

	//Input stuff
	public static void dispatchEvents(String[] event) {
		byte[][] event_bytes = new byte[event.length][];
		for (int i = 0; i < event.length; i++) {
			try {
				event_bytes[i] = event[i].getBytes("UTF-8");
			} catch (Exception e) {
				event_bytes[i] = new byte[1];
			}
		}
		dispatchEvents(event_bytes);
	}

	public static native void dispatchEvents(byte[][] event);
	public static native void dispatchInputEvents(int[] ids, int[] types, int[] xs, int[] ys, int count);

	public static native void saveTextures();
}
