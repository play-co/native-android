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

import java.io.InputStreamReader;

import com.tealeaf.util.IO;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class CrashRecover extends Activity {
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(new View(this));
		final AsyncTask<Void,Void,Void> task = new ReadLogcatTask(this).execute();
		new AlertDialog.Builder(this)
				.setMessage("Sorry!  The game needs to restart to run better on your device.")
				.setCancelable(false)
				.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						android.util.Log.d("CrashRecover", "Starting game again...");
						startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
						finish();
					}
				}).create().show();

		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				// 10 seconds ought to be enough to gather logcat output and prepare it, but
				// just in case logcat stops responding, don't hang around
				if(task.getStatus() == AsyncTask.Status.FINISHED) return;
				else {
					task.cancel(true);
					finish();
				}
			}
		}, 2000);
	}
}

class ReadLogcatTask extends AsyncTask<Void, Void, Void> {
	public ReadLogcatTask(CrashRecover context) {
		this.context = context;
	}
	private CrashRecover context;
	@Override
	protected Void doInBackground(Void... v) {
		// read the log
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "logcat", "-d", "-v", "threadtime" });
			// the input stream (here) is connected to the output stream (above) of the running process
			String content = IO.toString(new InputStreamReader(process.getInputStream()));
			new ParseLogcatTask(context, content).execute();
		} catch(Exception e) {
			// log the error to the analytics server--unfortunately, we won't get the real reason we crashed
			// but there's nothing we can do about it
			logger.log(e);
		}
		// parse it in a separate AsyncTask
		return null;
	}
}

class ParseLogcatTask extends AsyncTask<Void, Void, Void> {
	private static final int MAX_EXTRA_LINES = 25;

	public ParseLogcatTask(CrashRecover context, String content) {
		this.content = content;
		this.context = context;
		logger = new RemoteLogger(context);
	}
	private RemoteLogger logger = null;
	private CrashRecover context;
	private String content;
	@Override
	protected Void doInBackground(Void... v) {
		String[] lines = content.split("\\r?\\n");
		content = "";
		StringBuffer buf = new StringBuffer();
		int lineCount = 0;
		int index = 0;
		// skip to the first DEBUG line
		int len = lines.length;
		while(index < len && lines[index].indexOf("DEBUG") == -1) index++;
		if(index != len) {
			// go back to capture the first debug line too
			index--;
			while(index < len) {
				if(lines[index].indexOf("DEBUG") != -1) {
					buf.append(lines[index] + "\n");
					lineCount = 0;
				} else {
					lineCount++;
					if(lineCount >= MAX_EXTRA_LINES) {
						// we're done
						break;
					}
				}
				index++;
			}
			content = buf.toString();
		}
		android.util.Log.d("CrashRecover", "Done building the backtrace");
		// now send it to the analytics server
		if(content.length() > 0) {
			logger.sendErrorEvent(context, content);
		} else {
			logger.sendErrorEvent(context, "No backtrace available!");
		}
		return null;
	}
}
