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
package com.tealeaf.test_app;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.tealeaf.AppInfo;
import com.tealeaf.AppListView;
import com.tealeaf.DebugActivity;
import com.tealeaf.ResourceDownloaderTask;
import com.tealeaf.ServerListView;
import com.tealeaf.logger;
import com.tealeaf.test_app.R;

public class TestAppActivity extends Activity{
	private ServerListView listView;
	public AppListView appListView;
	private String host;
	private int port;
	private boolean isTestApp = true;
	private AppInfo currentAppInfo;
	private boolean serversShowing = false;

	private ProgressDialog progressDialog;
	private FrameLayout group;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (bundle == null) {
			group = new FrameLayout(this);
			appListView = new AppListView(this);
			listView = new ServerListView(this);
			showServerSelector();
			setContentView(group);

			// All our loading is remote!
			//TeaLeafOptions options = glView.getOptions();
			//options.setRemoteLoading(true);
			//get code host and port and set them
			Bundle intentBundle = getIntent().getExtras();
			if (intentBundle != null) {

				host = intentBundle.getString("hostValue");
				boolean toApps = false;
				if (host != null && !host.equals("")) {
					port = intentBundle.getInt("portValue");
					toApps = true;
				}

				if (intentBundle.getBoolean("restart", false)) {
					intentBundle.putBoolean("restart", false);
					String id = intentBundle.getString("id");
					String appid = intentBundle.getString("appid");
					boolean isPortrait = intentBundle.getBoolean("isPortrait", false);
					setAppInfo(new AppInfo(null, appid, isPortrait, id, null));
					downloadDebugAppResources();
				}

				if (toApps) {
					switchToApps();
				}

			}
		}
	}


	public void showAppLoadingDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

		progressDialog = ProgressDialog.show(this, "Loading Games", "Please Wait...", true);
		progressDialog.setCancelable(true);
	}

	public void dismissAppLoadingDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public void setAppInfo(AppInfo appInfo) {
		this.currentAppInfo = appInfo;

	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setIsTestApp(boolean isTestApp) {
		this.isTestApp = isTestApp;
	}

	private void showServerSelector() {
		if (!serversShowing) {
			group.addView(listView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			serversShowing = true;
		}
	}

	public void downloadDebugAppResources() {
		if (currentAppInfo != null) {
			//first we need to download everything
			new ResourceDownloaderTask(this, host, port, currentAppInfo).execute(this);
		}
	}

	public void launchDebugApp() {
		logger.log("launching");
		Intent intent = new Intent(this, DebugActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("isTestApp", true);
		bundle.putString("hostValue", host);
		bundle.putInt("portValue",port);
		bundle.putString("id", currentAppInfo.id);
		bundle.putString("appid", currentAppInfo.appid);
		bundle.putBoolean("isPortrait", currentAppInfo.isPortrait);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();
	}


	private void hideServerSelector() {
		serversShowing = false;
		group.removeView(listView);
	}

	private void showAppSelector() {
		group.addView(appListView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		appListView.invalidate();
	}

	private void hideAppSelector() {
		group.removeView(appListView);
	}

	public void refreshAppList() {
		this.appListView.refresh();
	}

	public void switchToApps() {
		hideServerSelector();
		showAppSelector();
		this.appListView.refresh();
	}

	public void switchToServers() {
		hideAppSelector();
		showServerSelector();
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.test_app_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.exit_ta) {
			finish();
			System.exit(0);
			return true;
		} else if (id == R.id.reset_ta) {
			if (!serversShowing) {
				hideAppSelector();
			} else {
				group.removeView(listView);
				serversShowing = false;
			}
			listView = new ServerListView(this);
			showServerSelector();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		dismissAppLoadingDialog();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}


}
