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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

public class JSDialog {
	public static void showDialog(Context context, Bitmap image, String title, String text,
			String[] buttons, Runnable[] callbacks) {
		if(buttons.length != callbacks.length) {
			logger.log("{dialog} WARNING: Callback length does not match button text length");
			return; 
		}

		final Dialog dialog = new Dialog(context);
		dialog.setTitle(title);

		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);

		if(image != null) {
			ImageView iv = new ImageView(context);
			iv.setImageBitmap(image);
			iv.setAdjustViewBounds(true);
			iv.setScaleType(ScaleType.FIT_CENTER);
			ll.addView(iv);
		}

		TextView tv = new TextView(context);
		tv.setText(text);
		tv.setPadding(4, 0, 4, 10);
		tv.setMovementMethod(new ScrollingMovementMethod());
		tv.setMaxHeight(300);
		ll.addView(tv);

		for(int i = 0; i < buttons.length; i++) {
			Button b = new Button(context);
			b.setText(buttons[i]);
			final Runnable callback = callbacks[i];
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
					callback.run();
				}
			});
			ll.addView(b, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}

		dialog.setContentView(ll);
		dialog.show();
	}
}
