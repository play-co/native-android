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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.tealeaf.TeaLeaf;
import com.tealeaf.TeaLeafOptions;
import com.tealeaf.util.ILogger;
import com.tealeaf.test_app.R;
import com.tealeaf.test_app.TestAppActivity;

public class DebugActivity extends TeaLeaf {
	public boolean setRestart = false;
	@Override
	public ILogger getLoggerInstance(Context context) {
		return new LocalLogger(context);
	}
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(getIntent().getExtras());

		// All our loading is remote!
		TeaLeafOptions options = glView.getOptions();
		options.setRemoteLoading(true);
	}

	public void launchServerListActivity() {
		TeaLeafOptions options = glView.getOptions();
		Bundle oldBundle = getIntent().getExtras();
		Intent intent = new Intent(this, TestAppActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("id", oldBundle.getString("id"));
		bundle.putString("appid", oldBundle.getString("appid"));
		bundle.putBoolean("isPortrait", oldBundle.getBoolean("isPortrait"));
		bundle.putString("hostValue", options.getCodeHost());
		bundle.putInt("portValue", options.getCodePort());
		bundle.putBoolean("restart", setRestart);
		intent.putExtras(bundle);
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.back) {
			launchServerListActivity();
			return true;
		} else if (id == R.id.restart) {
			setRestart = true;
			launchServerListActivity();
			return true;
		} else if(id == R.id.cleartextures) {
			clearTextures();
			return true;
		} else if(id == R.id.clearls) {
			clearLocalStorage();
			return true;
		} else if(id == R.id.exit) {
			finish();
			System.exit(0);
			return true;
		}


		return super.onOptionsItemSelected(item);
	}

	@Override
	public void reload() {
		setRestart = true;
		launchServerListActivity();
	}

}
