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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.MotionEvent;
import android.os.SystemClock;

import com.tealeaf.event.InputPromptCancelEvent;
import com.tealeaf.event.InputPromptSubmitEvent;

public class InputPrompt {

	private static int textInputId = 0;
	private static InputPrompt instance = null;
	public static InputPrompt getInstance() {
		synchronized(InputPrompt.class){
			if (instance == null) {
				instance = new InputPrompt();
			}
			return instance;
		}
	}
	
	public int showInputPrompt(final TeaLeaf context, final String title, final String message, final String value, final boolean autoShowKeyboard, final boolean isPassword) {
		final int id = textInputId++;
		context.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);

				if (title != null) {
					alert.setTitle(title);
				}

				if (message != null) {
					alert.setMessage(message);
				}

				final EditText input = new EditText(context);

				if (isPassword) {
					input.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}

				if (value != null) {
					input.setText(value);
				}

				alert.setView(input);
	
				if (autoShowKeyboard) {
					input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							input.postDelayed(new Runnable() {
								@Override
								public void run() {
									input.requestFocus();
									input.dispatchTouchEvent(MotionEvent.obtain(
										SystemClock.uptimeMillis(), 
										SystemClock.uptimeMillis(), 
										MotionEvent.ACTION_DOWN , 0, 0, 0
									));
									input.dispatchTouchEvent(MotionEvent.obtain(
										SystemClock.uptimeMillis(), 
										SystemClock.uptimeMillis(), 
										MotionEvent.ACTION_UP , 0, 0, 0
									));
								}
							}, 200);
						}
					});
				}

				alert.setPositiveButton("Submit",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String value = input.getText().toString();
							EventQueue.pushEvent(new InputPromptSubmitEvent(id, value));
						}
					}
				);

				alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							EventQueue.pushEvent(new InputPromptCancelEvent(id));
						}
					}
				);
				
				alert.show();
			}
		});
		return id;
	}
}
