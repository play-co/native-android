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

import com.tealeaf.event.PurchaseEvent;
import com.tealeaf.event.SMSEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TeaLeafReceiver extends BroadcastReceiver {
	private TeaLeaf tealeaf;

	public TeaLeafReceiver(TeaLeaf tealeaf) {
		this.tealeaf = tealeaf;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		logger.log("{receiver} Got action", action);
		if (action.equals("com.tealeaf.SMS_SENT")) {
			int cb = intent.getIntExtra("CallbackId", -1);
			EventQueue.pushEvent(new SMSEvent(cb, getResultCode(), true));
		} else {
			String order, product, payload, notifyId;
			int state;
			PurchaseEvent event = null;
			if (tealeaf != null) {
				if (action.equals("com.tealeaf.PURCHASED_ITEM")) {
					order = intent.getStringExtra("OrderId");
					product = intent.getStringExtra("ProductId");
					payload = intent.getStringExtra("Payload");
					state = intent.getIntExtra("State", 6);
					notifyId = intent.getStringExtra("NotifyId");
					logger.log("{purchase} Sending purchase event for order", order, "on product", product, "in state", state);
					event = new PurchaseEvent(order, product, payload, state, notifyId);
				} else if (action.equals("com.tealeaf.PURCHASE_RESPONSE")) {
					order = product = payload = "";
					state = intent.getIntExtra("State", 6);
					notifyId = null;
					if (state == 6) {
						logger.log("{purchase} Sending error response");
						event = new PurchaseEvent(order, product, payload, state, notifyId);
					}
				}
				if (event != null) {
					EventQueue.pushEvent(event);
				}
			}
		}
	}
}
