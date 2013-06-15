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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout;
import android.widget.EditText;

@SuppressWarnings("deprecation")
public class TextInputView extends AbsoluteLayout {

	private class TextBox extends EditText {
		private float opacity = 1.0f;
		private AnimationSet set = new AnimationSet(true);
		public TextBox(Context context) {
			super(context);
			setSingleLine();
			setFocusableInTouchMode(true);
			setBackgroundColor(Color.TRANSPARENT);
			setTextColor(Color.BLACK);
			setVisibility(View.VISIBLE);
			setGravity(Gravity.BOTTOM);
			set.setFillAfter(true);
			set.setFillEnabled(true);
		}

		public float getOpacity() { return opacity; }
		public void setOpacity(float value) {
			AlphaAnimation anim = new AlphaAnimation(opacity, value);
			set.addAnimation(anim);
			startAnimation(set);
			postInvalidate();
			opacity = value;
		}
		@Override
		public void setLayoutParams(android.view.ViewGroup.LayoutParams params) {
			// update the font size
			LayoutParams p = (LayoutParams) params;
			int height = p.height + getPaddingBottom();
			p.height = height + getPaddingBottom();
			super.setLayoutParams(p);

			float size = (height - getPaddingTop());
			if(size > 0) {
				setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

				if (p.width > 0) {
					Bitmap bmp = Bitmap.createBitmap(p.width, height - getPaddingBottom(), Config.ARGB_8888);
					Canvas c = new Canvas(bmp);
					Paint paint = new Paint();
					paint.setColor(Color.WHITE);
					paint.setStyle(Paint.Style.STROKE);
					Rect rect = c.getClipBounds();
					rect.bottom--;
					rect.right--;
					c.drawRect(rect, paint);
					setBackgroundDrawable(new BitmapDrawable(bmp));
				}
			}
		}
	}

	private Activity ctx;
	private ArrayList<TextBox> views = new ArrayList<TextBox>();

	public TextInputView(Activity context) {
		super(context);
		setBackgroundColor(Color.TRANSPARENT);
		setFocusableInTouchMode(true);
		this.ctx = context;
	}

	public int createNew() {
		return createNew(0, 0, 300, 75, "");
	}
	public int createNew(int x, int y, int w, int h, String initialText) {
		return createNew(new LayoutParams(w, h, x, y), initialText);
	}
	public int createNew(final LayoutParams params, final String initialText) {
		final int id = views.size();
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				TextBox text = new TextBox(ctx);
				text.setText(initialText);

				views.add(id, text);
				addView(text, params);
				requestLayout();
			}
		});
		return id;
	}
	public void destroy(final int id) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if(id < views.size()) {
					TextInputView.this.hide(id);
					removeView(views.get(id));
					views.set(id, null);
				}
			}
		});
	}

	public void setVisible(final int id, final boolean visible) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					text.setVisibility(visible ? VISIBLE : INVISIBLE);
				}
			}
		});
	}
	public boolean getVisible(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return text.getVisibility() == VISIBLE;
		}
		return false;
	}

	public void show(int id) { setVisible(id, true); }
	public void hide(int id) { setVisible(id, false); }

	public void setPosition(final int id, final int x, final int y, final int w, final int h) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					LayoutParams params = (LayoutParams) text.getLayoutParams();
					params.x = x;
					params.y = y;
					params.width = w;
					params.height = h;
					updateViewLayout(text, params);
					invalidate();
				}
			}
		});
	}
	public void setDimensions(final int id, final int w, final int h) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					LayoutParams params = (LayoutParams) text.getLayoutParams();
					params.width = w;
					params.height = h;
					updateViewLayout(text, params);
					invalidate();
				}
			}
		});
	}

	public void setX(final int id, final int x) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					LayoutParams params = (LayoutParams) text.getLayoutParams();
					params.x = x;
					updateViewLayout(text, params);
					invalidate();
				}
			}
		});
	}
	public void setY(final int id, final int y) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					LayoutParams params = (LayoutParams) text.getLayoutParams();
					params.y = y;
					updateViewLayout(text, params);
					invalidate();
				}
			}
		});
	}
	public void setWidth(final int id, final int w) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					LayoutParams params = (LayoutParams) text.getLayoutParams();
					params.width = w;
					updateViewLayout(text, params);
					invalidate();
				}
			}
		});
	}
	public void setHeight(final int id, final int h) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					LayoutParams params = (LayoutParams) text.getLayoutParams();
					params.height = h;
					updateViewLayout(text, params);
					invalidate();
				}
			}
		});
	}
	public void setOpacity(final int id, final float value) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					text.setOpacity(value);
					invalidate();
				}
			}
		});
	}
	public void setType(final int id, final int type) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					text.setInputType(type);
				}
			}
		});
	}
	public void setValue(final int id, final String value) {
		ctx.runOnUiThread(new Runnable() {
			public void run() {
				if (id < views.size()) {
					TextBox text = views.get(id);
					text.setText(value);
				}
			}
		});
	}

	public int getX(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return ((LayoutParams)text.getLayoutParams()).x;
		}
		return -1;
	}
	public int getY(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return ((LayoutParams)text.getLayoutParams()).y;
		}
		return -1;
	}
	public int getWidth(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return text.getWidth();
		}
		return -1;
	}
	public int getHeight(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return text.getHeight();
		}
		return -1;
	}
	public String getValue(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return text.getText().toString();
		}
		return "";
	}
	public int getType(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return text.getInputType();
		}
		return -1;
	}

	public float getOpacity(int id) {
		if (id < views.size()) {
			TextBox text = views.get(id);
			return text.getOpacity();
		}
		return -1.0f;
	}

	public void selectAll(int id) {
		if (id < views.size()) {
			EditText text = views.get(id);
			text.selectAll();
		}
	}
	public void defocus() {
		InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromInputMethod(getWindowToken(), 0);
		for (TextBox text : views) {
			text.clearFocus();
			imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
		}
	}
}
