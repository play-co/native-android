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

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;

public class ServiceWrapper implements ServiceConnection, DeathRecipient {
	private Context context = null;
	private TeaLeafService service = null;
	private IBinder serviceBinder = null;
	private ArrayList<ServiceEvent> serviceEvents = new ArrayList<ServiceEvent>();
	public TeaLeafService get() { return service; }
	public boolean serviceReady() { return service != null; }
	public ServiceWrapper(Context context) {
		this.context = context;
		rebind();
	}
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		if(this.service == null) {
			this.serviceBinder = service;
			this.service = ((TeaLeafService.TeaLeafBinder)service).getService();
			try {
				service.linkToDeath(this, 0);
			} catch (RemoteException e) {
				// the endpoint already hung up?
				this.service = null;
				return;
			}
			dispatchEvents();
		}
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.service = null;
	}
	@Override
	public void binderDied() {
		this.serviceBinder = null;
		this.service = null;
	}

	public void unbind() {
		if(this.service != null) {
			context.unbindService(this);
			this.serviceBinder.unlinkToDeath(this, 0);
			this.service = null;
		}
	}

	public void rebind() {
		if(this.service == null) {
			context.bindService(new Intent(context, TeaLeafService.class), this, Context.BIND_AUTO_CREATE);
		}
	}

	public void buy(String id) {
		if (service != null) {
			doBuy(id);
		} else {
			serviceEvents.add(new BuyEvent(id));
			rebind();
		}
	}
		
	private void doBuy(String id) {
		try {
			Bundle bundle = service.startPurchase(id);
			PendingIntent intent = bundle.getParcelable("PURCHASE_INTENT");
			context.startIntentSender(intent.getIntentSender(), new Intent(), 0, 0, 0);
		} catch (Exception e) {
			logger.log(e);
		}
	}
	
	private void dispatchEvents() {
		for (ServiceEvent event : serviceEvents) {
			if (event.type.equals("Buy")) {
				BuyEvent b = (BuyEvent)event;
				doBuy(b.id);
			} else {
				logger.log("{services} ERROR: Unrecognized event type", event.type);
			}
		}
		serviceEvents.clear();
	}
	
	class ServiceEvent {
		protected String type;
		public ServiceEvent(String type) {
			this.type = type;
		}
	}
	
	class BuyEvent extends ServiceEvent {
		public String id;
		public BuyEvent(String id) {
			super("Buy");
			this.id = id;
		}
	}
}
