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
package com.tealeaf.plugin;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;

public interface IPlugin {

	public void onCreate(Activity activity, Bundle savedInstanceState);
	public void onCreateApplication(Context applicationContext);
	public void onResume();
	public void onStart();
	public void onPause();
	public void onStop();
	public void onDestroy();
	public void onNewIntent(Intent intent);
	public void onActivityResult(Integer request, Integer result, Intent data);
	public void setInstallReferrer(String referrer);

}
