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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MarketBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		logger.log("{market} Received", action);
		Intent i = new Intent(action);
		i.setClass(context, TeaLeafService.class);
		if (action.equals("com.android.vending.billing.PURCHASE_STATE_CHANGED")) {
			// check the signature data to validate the purchase, then do the requested action (refund or purchase)
			// this should be done on the server, then broadcast *that* result to the service
			String signed = intent.getStringExtra("inapp_signed_data");
			String signature = intent.getStringExtra("inapp_signature");
			i.putExtra("SignatureResult", verify(signed, signature));
			i.putExtra("JSONData", intent.getStringExtra("inapp_signed_data"));
		} else if (action.equals("com.android.vending.billing.IN_APP_NOTIFY")) {
			i.putExtra("NotifyId", intent.getStringExtra("notification_id"));
		} else if (action.equals("com.android.vending.billing.RESPONSE_CODE")) {
			i.putExtra("RequestId", intent.getLongExtra("request_id", -1));
			i.putExtra("ResponseCode", intent.getIntExtra("response_code", 6));
		}
		context.startService(i);
	}
	private boolean verify(String signed, String signature) {
		return true;
	}
}
