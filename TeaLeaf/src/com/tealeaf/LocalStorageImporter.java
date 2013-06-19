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

import android.content.Intent;
import android.net.Uri;

public class LocalStorageImporter {
	
	private TeaLeaf tealeaf;
	public LocalStorageImporter(TeaLeaf tealeaf) {
		this.tealeaf = tealeaf;
	}
	public void run() {
		if (tealeaf.getSettings().is("first_import")) {
			tealeaf.getSettings().mark("first_import");
			Uri uri = Uri.parse(tealeaf.getCodeHost() + tealeaf.getOptions().getAppID() + "/?exportSettings=true&protocol=" + tealeaf.getOptions().getProtocol() + "&nocache=" + new java.util.Date().getTime());
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
			tealeaf.startActivity(launchBrowser);
		}
	}
}
