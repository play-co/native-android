/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tealeaf;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;

import com.android.vending.billing.IMarketBillingService;

public class TeaLeafService extends Service implements ServiceConnection, DeathRecipient {
	private class ResponseCode {
		public static final int RESULT_OK = 0;
		// unused
		/*public static final int RESULT_USER_CANCELED = 1;
		public static final int RESULT_SERVICE_UNAVAILABLE = 2;
		public static final int RESULT_BILLING_UNAVAILABLE = 3;
		public static final int RESULT_ITEM_UNAVAILABLE = 4;
		public static final int RESULT_DEVELOPER_ERROR = 5;
		public static final int RESULT_ERROR = 6;*/
	}

	public class TeaLeafBinder extends Binder {
		public TeaLeafService getService() {
			return TeaLeafService.this;
		}
	}

	private IMarketBillingService market = null;
	private boolean canUseMarket = false;
	private boolean bound = false;
	private TeaLeafBinder binder = new TeaLeafBinder();
	private ArrayList<Intent> waitingActions = new ArrayList<Intent>();
	private static SecureRandom rand = new SecureRandom();
	private static HashSet<Long> nonces = new HashSet<Long>();
	private TeaLeafOptions options;
	private Settings settings;

	public IBinder onBind(Intent intent) {
		process(intent);
		return binder;
	}

	@Override
	public void onDestroy() {
		if(bound) {
			unbindService(this);
			canUseMarket = false;
			bound = false;
			market = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		process(intent);
		return START_STICKY;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		try {
			logger.log("{service} Connected to market service");
			market = IMarketBillingService.Stub.asInterface(service);
			service.linkToDeath(this, 0);
			bound = true;
			Bundle result = market.sendBillingRequest(buildBundle("CHECK_BILLING_SUPPORTED"));
			canUseMarket = ResponseCode.RESULT_OK == result.getInt("RESPONSE_CODE");
		} catch (RemoteException e) {
			logger.log(e);
			bound = false;
			canUseMarket = false;
			market = null;
		}

		// run any intents we may have stored up
		// copy the list to iterate over it
		Intent[] intents = new Intent[waitingActions.size()];
		waitingActions.toArray(intents);
		waitingActions.clear();
		// iterate over the copy
		for(Intent intent : intents) {
			handleIntent(intent);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {}

	private void process(final Intent intent) {
		new Thread(new Runnable() {
			public void run() {
				if(!bound) {
					boolean bindResult = TeaLeafService.this.bindService(new Intent("com.android.vending.billing.MarketBillingService.BIND"), TeaLeafService.this, BIND_AUTO_CREATE);
					logger.log("{service} Bind", bindResult ? "succeeded" : "failed");
					if(bindResult && intent != null && intent.getAction() != null) {
						waitingActions.add(intent);
					}
				} else {
					handleIntent(intent);
				}

				options = new TeaLeafOptions(TeaLeafService.this);
				settings = new Settings(TeaLeafService.this);

				TwentyFourHourAlarm.schedule(TeaLeafService.this, settings, options);
			}
		}).start();
	}

	private void handleIntent(Intent intent) {
		try {
			String action = intent.getAction();
			logger.log("{service} Got intent", action);
			if(action == null) {
				return;
			}

			if(action.equals("com.android.vending.billing.PURCHASE_STATE_CHANGED")) {
				processStateChanged(intent);
			} else if(action.equals("com.android.vending.billing.IN_APP_NOTIFY")) {
				// get the purchase (or refund) info
				logger.log("{service} Sending purchase info request for id", intent.getStringExtra("NotifyId"));
				getPurchaseInfo(intent.getStringExtra("NotifyId"));
			} else if(action.equals("com.tealeaf.CONFIRM_PURCHASE")) {
				logger.log("{service} Confirming purchase for id", intent.getStringExtra("NotifyId"));
				confirmResults(intent.getStringExtra("NotifyId"));
			} else if(action.equals("com.android.vending.billing.RESPONSE_CODE")) {
				int response = intent.getIntExtra("ResponseCode", 6);
				logger.log("{service} Got response", response, "for request", intent.getLongExtra("RequestId", -1));
				if (response != 0) {
					Intent i = new Intent("com.tealeaf.PURCHASE_RESPONSE");
					i.putExtra("responseCode", response);
					sendBroadcast(i);
				}
			}
		} catch (RemoteException e) {
			// reschedule this intent
			waitingActions.add(intent);
		}
	}


	private void processStateChanged(Intent intent) {
		// this will be a processed message from the server telling us whether or not the transaction is real
		boolean validated = intent.getBooleanExtra("SignatureResult", false);
		logger.log("{purchase} Purchase was", validated);
		try {
			JSONObject data = new JSONObject(intent.getStringExtra("JSONData"));
			if(!nonces.contains(data.getLong("nonce"))) {
				logger.log("{purchase} ERROR: Unknown nonce (hacking attempt or out of band response?)");
			}
			JSONArray orders = data.getJSONArray("orders");
			for(int i = 0; i < orders.length(); i++) {
				JSONObject order = orders.getJSONObject(i);
				String orderId = order.getString("orderId"),
					   productId = order.getString("productId"),
					   payload = order.optString("developerPayload"),
					   notificationId = order.optString("notificationId");
				int state = order.getInt("purchaseState");
				Intent msg = new Intent("com.tealeaf.PURCHASED_ITEM");
				msg.putExtra("OrderId", orderId);
				msg.putExtra("ProductId", productId);
				msg.putExtra("State", state);
				msg.putExtra("Payload", payload);
				msg.putExtra("NotifyId", notificationId);
				sendBroadcast(msg);
			}
			Intent response = new Intent("com.tealeaf.PURCHASE_RESPONSE");
			response.putExtra("responseCode", 0);
			sendBroadcast(response);
		} catch (JSONException e) {
			logger.log(e);
		}
	}

	@Override
	public void binderDied() {
		canUseMarket = false;
		bound = false;
		market = null;
	}

	private Bundle buildBundle(String method) {
		Bundle result = new Bundle();
		result.putString("BILLING_REQUEST", method);
		result.putInt("API_VERSION", 1);
		result.putString("PACKAGE_NAME", getPackageName());
		return result;
	}

	private boolean isOkResponse(Bundle b) {
		return b.getInt("RESPONSE_CODE") == ResponseCode.RESULT_OK;
	}

	public boolean getMarketSupported() {
		return canUseMarket;
	}

	public Bundle startPurchase(String id) throws RemoteException {
		Bundle request = buildBundle("REQUEST_PURCHASE");
		request.putString("ITEM_ID", id);
		Bundle result = market.sendBillingRequest(request);
		if (isOkResponse(result)) {
			return result;
		}
		logger.log("{purchase} WARNING: Request failed with id", result.getInt("RESPONSE_CODE"));
		return null;
	}

	public long getPurchaseInfo(String notifyId) throws RemoteException {
		long nonce = rand.nextLong();
		Bundle request = buildBundle("GET_PURCHASE_INFORMATION");
		nonces.add(nonce);
		request.putLong("NONCE", nonce);
		request.putStringArray("NOTIFY_IDS", new String[] { notifyId });
		Bundle result = market.sendBillingRequest(request);
		if (isOkResponse(result)) {
			return result.getLong("REQUEST_ID");
		}
		logger.log("{purchase} WARNING: Get purchase info request failed with id", result.getInt("RESPONSE_CODE"));
		return -1;
	}

	public Bundle confirmResults(String notifyId) throws RemoteException {
		logger.log("{purchase} Confirming result for id", notifyId);
		Bundle request = buildBundle("CONFIRM_NOTIFICATIONS");
		request.putStringArray("NOTIFY_IDS", new String[] { notifyId });
		Bundle result = market.sendBillingRequest(request);
		if (isOkResponse(result)) {
			return result;
		}
		logger.log("{purchase} WARNING: Confirm results request failed with id", result.getInt("RESPONSE_CODE"));
		return null;
	}

	public Bundle restoreResults() throws RemoteException {
		Bundle result = null;
		if (market != null) {
			long nonce = rand.nextLong();
			Bundle request = buildBundle("RESTORE_TRANSACTIONS");
			nonces.add(nonce);
			request.putLong("NONCE", nonce);
			result = market.sendBillingRequest(request);
			if (!isOkResponse(result)) {
				result = null;
			}
		}
		return null;
	}
}
