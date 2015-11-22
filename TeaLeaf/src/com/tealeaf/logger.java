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
import com.tealeaf.plugin.PluginManager;
import com.tealeaf.util.ILogger;

import android.util.Log;

public class logger {

	public static boolean DISABLE_DEBUG = true;
	public static boolean DISABLE_LOG = false;
	public static boolean SHOW_DIALOGS = false;
	private static ILogger remoteLogger;
	private static TeaLeaf context;
	public static void buildLogger(TeaLeaf tealeaf, ILogger remoteLogger) {
		context = tealeaf;
		// disable the log by default, allow for overriding in the manifest
		DISABLE_LOG = tealeaf.getOptions().get("disableLogs", true);
		logger.remoteLogger = null;
	}
	public static void debug(Object... text) {
		if (DISABLE_DEBUG) { return; }
		String s = "";
		for (Object t : text) {
			s += t + " ";
		}
		Log.e("JSDEBUG", s);
	}

	public static void log(Object... text) {
		if (DISABLE_LOG) { return; }
		String s = "";
		for (Object t : text) {
			s += t + " ";
		}
		Log.e("JS", s);
	}

	public static void log(Exception e) {
		String info = e.toString() + "\n";
		boolean shouldPrint = context == null ? false : context.getOptions().isDevelop();
		if (shouldPrint) {
			log(info);
		}

		String stackTrace = "";
		StackTraceElement[] elements = e.getStackTrace();
		for (StackTraceElement s : elements) {
			stackTrace += "\n" + s;
			if (shouldPrint) {
				log("\tat " + s);
			}
		}
		info += stackTrace;
		//if we're not printing to the console, log it remotely
		if (!shouldPrint && remoteLogger != null) {
			remoteLogger.sendErrorEvent(context, info);
			PluginManager.callAll("logError", info);
		} else if(SHOW_DIALOGS && context != null) {
			final String str = info;
			context.runOnUiThread(new Runnable() {
				public void run() {
					JSDialog.showDialog(context, null, "Java Exception", str,
							new String[] {"OK"},
							new Runnable[] {new Runnable() { public void run() {} }
					});
				}
			});
		}
	}
}
