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
import java.net.URI;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View.OnClickListener;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.tealeaf.TeaLeafOptions;
import com.tealeaf.ResourceManager;
import com.tealeaf.AppInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tealeaf.util.HTTP;
import com.tealeaf.test_app.R;
import com.tealeaf.test_app.TestAppActivity;

public class AppAdapter extends ArrayAdapter<AppInfo>{

	TestAppActivity activity;
	int layoutResourceId;
	AppInfo data[] = null;
	String codeHost;
	int codePort;
	private static final int ID_LAUNCH = 1;
	private static final int ID_CGF = 2;
	private static final int ID_CANCEL = 3;

	public AppAdapter(TestAppActivity activity, int layoutResourceId, AppInfo[] data, String codeHost, int codePort) {
		super(activity, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.activity = activity;
		this.data = data;
		this.codeHost = codeHost;
		this.codePort = codePort;
	}

	/* Android 4.0 has an issue with BitmapFactory.decodeStream
	 * This instead caches the icons from projects being served
	 * thend loads the icon (or the cached one if it already exists)
	 * this will be useful in the future for loading of cached games
	 * offline
	 */
	void loadBitmap(final AppInfo appInfo, final AppHolder holder){
		final TestAppActivity activity = this.activity;
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			public void run() {
				final Bitmap bmp;
				TeaLeafOptions opts = new TeaLeafOptions();
				opts.setAppID(appInfo.appid);
				ResourceManager resourceManager = new ResourceManager(activity, opts);
				String storageDir = resourceManager.getStorageDirectory();
				String iconFileStr = storageDir + "/icon.png";

				if (new File(iconFileStr).exists()) {
					bmp = BitmapFactory.decodeFile(iconFileStr);
				} else {
					String base = "http://" + codeHost + ":" + codePort + "/";
					base += "projects/" + appInfo.id + "/files/";
					String url = base + appInfo.iconURL;
					HTTP http = new HTTP();
					File bmpFile = http.getFile(URI.create(url), iconFileStr);
					Bitmap possibleBMP = null;
					try {
						FileInputStream fs = new FileInputStream(bmpFile);
						possibleBMP = BitmapFactory.decodeStream(fs);
					} catch (Exception e) {
						logger.log("exception while loading bitmap:", e);
						e.printStackTrace();
					}
					bmp = possibleBMP;
				}
				handler.post(new Runnable() {
					public void run() {
						holder.imgIcon.setImageBitmap(bmp);
					}
				});
			}
		}).start();

	}

	@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			final AppHolder holder;

			if(row == null)
			{
				LayoutInflater inflater = ((Activity)activity).getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new AppHolder();
				holder.imgIcon = (ImageView)row.findViewById(R.id.app_icon);
				holder.txtTitle = (TextView)row.findViewById(R.id.app_name);

				row.setTag(holder);
			}
			else
			{
				holder = (AppHolder)row.getTag();
			}

			final AppInfo appInfo = data[position];
			holder.txtTitle.setText(appInfo.name);
			if (appInfo.iconURL == "default") {
					holder.imgIcon.setImageResource(android.R.drawable.sym_def_app_icon);
			} else {
				loadBitmap(appInfo, holder);
			}

			return row;
		}

	public void recursiveFileDelete(String path) {

		File root = new File(path);
		if (root.isDirectory()) {
			for (File f : root.listFiles()) {
				if (f.isDirectory()) {
					recursiveFileDelete(f.getPath());
				} else {
					f.delete();
				}
			}
		}
		root.delete();
	}

	static class AppHolder
	{
		ImageView imgIcon;
		TextView txtTitle;
	}
}
