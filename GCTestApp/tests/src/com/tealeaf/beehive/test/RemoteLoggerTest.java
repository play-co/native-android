package com.tealeaf.backpack.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tealeaf.backpack.HTTPFactory;
import com.tealeaf.backpack.RemoteLogger;
import com.tealeaf.backpack.MockHTTP;
import com.tealeaf.backpack.Connection;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;

class RunInForegroundExecutorService implements ExecutorService {
	@Override
	public void execute(Runnable command) {
		command.run();
	}
	@Override
	public void shutdown() { }
	@Override
	public List<Runnable> shutdownNow() {
		return null;
	}
	@Override
	public boolean isShutdown() {
		return false;
	}
	@Override
	public boolean isTerminated() {
		return false;
	}
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return true;
	}
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		throw new UnsupportedOperationException();
	}
	@Override
	public Future<?> submit(Runnable task) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
		throw new UnsupportedOperationException();
	}
	@Override
	public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}
	@Override
	public <T> T invokeAny(Collection<Callable<T>> tasks) throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}
	@Override
	public <T> T invokeAny(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}
}

class TestContext extends IsolatedContext {
	private Context targetContext;
	private ContentResolver targetResolver;
	private SharedPreferences sharedPreferences = new SharedPreferences() {
		private HashMap<String, Object> map = new HashMap<String, Object>() {{
			// default key/values
		}};

		@Override
		public Map<String, ?> getAll() { return map; }
		@Override
		public String getString(String key, String defValue) { return map.containsKey(key) ? (String)map.get(key) : defValue; }
		@Override
		public int getInt(String key, int defValue) { return map.containsKey(key) ? (Integer)map.get(key) : defValue; }
		@Override
		public long getLong(String key, long defValue) { return map.containsKey(key) ? (Long)map.get(key) : defValue; }
		@Override
		public float getFloat(String key, float defValue) { return map.containsKey(key) ? (Float)map.get(key) : defValue; }
		@Override
		public boolean getBoolean(String key, boolean defValue) { return map.containsKey(key) ? (Boolean)map.get(key) : defValue; }
		@Override
		public boolean contains(String key) { return map.containsKey(key); }

		@Override
		public Editor edit() {
			return new Editor() {
				@Override
				public Editor putString(String key, String value) { map.put(key, value); return this; }
				@Override
				public Editor putInt(String key, int value) { map.put(key, value); return this; }
				@Override
				public Editor putLong(String key, long value) { map.put(key, value); return this; }
				@Override
				public Editor putFloat(String key, float value) { map.put(key, value); return this; }
				@Override
				public Editor putBoolean(String key, boolean value) { map.put(key, value); return this; }
				@Override
				public Editor remove(String key) { map.remove(key); return this; }
				@Override
				public Editor clear() { map.clear(); return this; }
				@Override
				public boolean commit() { return true; }
			};
		}

		@Override
		public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
		@Override
		public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
	};

	public TestContext(ContentResolver resolver, Context target) {
		super(resolver, target);
		targetContext = target;
		targetResolver = resolver;
	}

	public Object getSystemService(String name) { return targetContext.getSystemService(name); }

	public SharedPreferences getSharedPreferences(String target, int mode) {
		return sharedPreferences;
	}
}

public class RemoteLoggerTest extends AndroidTestCase {
	private static final String appID = "testcases";
	private RemoteLogger remoteLogger = null;
	private IsolatedContext context = null;
	private MockHTTP http = null;
	protected void setUp() {
		context = new TestContext(getContext().getContentResolver(), getContext());
		HTTPFactory.setTestingMode(true);
		Connection.setTestingMode(true);
		http = (MockHTTP)HTTPFactory.get();
		remoteLogger = new RemoteLogger(context, appID, new RunInForegroundExecutorService());
	}
	protected void tearDown() {
		remoteLogger = null;
		HTTPFactory.setTestingMode(false);
	}

	public void testFirstLaunchEventResults() throws JSONException {
		remoteLogger.sendFirstLaunchEvent(context);
		String content = http.getLastContent();

		// make sure we got content first
		assertNotNull(content);
		JSONObject obj = new JSONObject(content);
		JSONArray arr = obj.optJSONArray("native");
		// make sure the content is contained in a 'native' wrapper object
		assertNotNull(arr);
		assertEquals(1, arr.length());
		assertNotNull(arr.optJSONObject(0));

		obj = arr.getJSONObject(0);
		assertEquals("nativeFirstLaunch", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));

		assertEquals(android.os.Build.MANUFACTURER, obj.getString("deviceManufacturer"));
		assertEquals(android.os.Build.MODEL, obj.getString("deviceModel"));
		assertEquals(android.os.Build.VERSION.SDK, obj.getString("deviceVersion"));
	}

	public void testLaunchEventResults() throws JSONException {
		remoteLogger.sendLaunchEvent(context);
		String content = http.getLastContent();

		// make sure we got content first
		assertNotNull(content);
		JSONObject obj = new JSONObject(content);
		// make sure the content is contained in a 'native' wrapper object
		JSONArray arr = obj.optJSONArray("native");
		// make sure the content is contained in a 'native' wrapper object
		assertNotNull(arr);
		assertEquals(1, arr.length());
		assertNotNull(arr.optJSONObject(0));

		obj = arr.getJSONObject(0);
		assertEquals("nativeLaunch", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));
		// there are no specific fields on nativeLaunch that need testing
	}

	public void testDeviceInfoEventResults() throws JSONException {
		remoteLogger.sendDeviceInfoEvent(context);
		String content = http.getLastContent();

		// make sure we got content first
		assertNotNull(content);
		JSONObject obj = new JSONObject(content);
		// make sure the content is contained in a 'native' wrapper object
		JSONArray arr = obj.optJSONArray("native");
		// make sure the content is contained in a 'native' wrapper object
		assertNotNull(arr);
		assertEquals(1, arr.length());
		assertNotNull(arr.optJSONObject(0));

		obj = arr.getJSONObject(0);
		assertEquals("nativeDeviceInfo", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));

		assertEquals(android.os.Build.MANUFACTURER, obj.getString("deviceManufacturer"));
		assertEquals(android.os.Build.MODEL, obj.getString("deviceModel"));
		assertEquals(android.os.Build.VERSION.SDK, obj.getString("deviceVersion"));
	}

	public void testErrorEventResults() throws JSONException {
		remoteLogger.sendErrorEvent(context, "content");
		String content = http.getLastContent();

		// make sure we got content first
		assertNotNull(content);
		JSONObject obj = new JSONObject(content);
		// make sure the content is contained in a 'native' wrapper object
		JSONArray arr = obj.optJSONArray("native");
		// make sure the content is contained in a 'native' wrapper object
		assertNotNull(arr);
		assertEquals(1, arr.length());
		assertNotNull(arr.optJSONObject(0));

		obj = arr.getJSONObject(0);
		assertEquals("nativeError", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));

		assertEquals("content", obj.optString("eventPayload"));
	}

	public void testNoConnectivityEnqueuesEvent() {
		// turn off the network, make sure it's off
		Connection.setNetworkState(false);
		assertFalse(Connection.available(context));

		// send an error event (arbitrary)
		remoteLogger.sendErrorEvent(context, "content");
		String content = http.getLastContent();

		// make sure we got NO content
		assertNull(content);

		// make sure the event got saved in shared prefs
		SharedPreferences sp = context.getSharedPreferences("", 0);
		assertNotNull(sp.getString("log_events", null));
		Connection.setNetworkState(true);
	}

	public void testMultipleEvents() throws JSONException {
		// disable connectivity
		Connection.setNetworkState(false);
		assertFalse(Connection.available(context));

		// send an error event (arbitrary)
		remoteLogger.sendErrorEvent(context, "content");
		String content = http.getLastContent();

		// make sure we got NO content
		assertNull(content);

		// send an device info event (arbitrary)
		remoteLogger.sendDeviceInfoEvent(context);
		content = http.getLastContent();

		// make sure we got NO content still
		assertNull(content);

		// turn on connectivity
		Connection.setNetworkState(true);
		assertTrue(Connection.available(context));

		remoteLogger.sendLaunchEvent(context);
		content = http.getLastContent();

		// make sure we DID get content this time
		assertNotNull(content);
		// make sure we have a 'native' object wtih an array
		JSONObject obj = new JSONObject(content);
		assertNotNull(obj.optJSONArray("native"));

		JSONArray arr = obj.getJSONArray("native");
		// make sure there are 3 elements
		assertEquals(3, arr.length());

		// make sure the first one is an error event
		obj = arr.getJSONObject(0);
		assertEquals("nativeError", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));
		assertEquals("content", obj.optString("eventPayload"));

		// make sure the second one is a device info event
		obj = arr.getJSONObject(1);
		assertEquals("nativeDeviceInfo", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));

		assertEquals(android.os.Build.MANUFACTURER, obj.getString("deviceManufacturer"));
		assertEquals(android.os.Build.MODEL, obj.getString("deviceModel"));
		assertEquals(android.os.Build.VERSION.SDK, obj.getString("deviceVersion"));

		// make sure the third one is a launch event
		obj = arr.getJSONObject(2);
		assertEquals("nativeLaunch", obj.optString("eventID"));
		assertEquals(appID, obj.optString("appID"));

		// make sure there are no pending events in the queue
		assertEquals("", context.getSharedPreferences("", 0).getString("log_events", null));
	}
}
