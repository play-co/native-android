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


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.tealeaf.event.LogEvent;

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.text.TextPaint;
import android.graphics.Paint;
import android.graphics.Typeface;


public class TextManager {
	private TeaLeaf tealeaf;
	private HashMap<String, String> customFonts = new HashMap<String, String>();
	private ArrayList<String> unsupportedFonts = new ArrayList<String>();
	private HashMap<String, Typeface> typeFaces = new HashMap<String, Typeface>();
	public TextManager(TeaLeaf tealeaf) {
		this.tealeaf = tealeaf;

		try {
			String[] files = tealeaf.getAssets().list("resources/resources/fonts");
			int n = files.length;
			for (int i = 0; i < n; ++i) {
				String fullFontKey = files[i].toLowerCase().replace("-", " ");
				customFonts.put(fullFontKey, files[i]);
			}
		} catch (IOException e) {
			logger.log(e.toString());
		}
	}

	public Bitmap getText(String fontName, int fontSize, int textStyle, String text,  int r, int g, int b, int a, int maxWidth, float strokeWidth) {
		return this.loadNewText(fontName, fontSize, textStyle, text, r, g, b, a, maxWidth, strokeWidth);
	}

	final int MAX_TEXT_PIECES = 10;

	public Bitmap getText(String hash) {
		String[] parts = unhash(hash, MAX_TEXT_PIECES);

		if (parts.length < MAX_TEXT_PIECES) {
			logger.log("{text} ERROR: Only", parts.length, "parts to text tag");
			return null;
		}

		Bitmap bmp = null;
		try {
			String fontName = parts[0];
			int fontSize = Integer.parseInt(parts[1]);
			int r = Integer.parseInt(parts[2]);
			int g = Integer.parseInt(parts[3]);
			int b = Integer.parseInt(parts[4]);
			int a = Integer.parseInt(parts[5]);
			int maxWidth = Integer.parseInt(parts[6]);
			int textStyle = Integer.parseInt(parts[7]);
			float strokeWidth = Float.parseFloat(parts[8]);
			String text = parts[9];
			bmp = getText(fontName, fontSize, textStyle, text, r, g, b, a, maxWidth, strokeWidth);
		} catch (NumberFormatException e) {
			logger.log(e);
		}
		return bmp;
	}

	public int measureText(String font, int size, String text) {
		TextPaint textPaint = getTextPaint(font, size, 0, text, 0, 0, 0, 0, 0);
		return (int)textPaint.measureText(text);
	}

	public Typeface getTypeface(String fontKey) {
		// fontName contains only the name and is in the original casing
		// fontKey includes the font weight and name and is lower case
		String fontName = fontKey;
		fontKey = fontKey.toLowerCase();
		Typeface tf = null;
		boolean isBold = false;

		if (fontKey.startsWith("normal ")) {
			fontName = fontName.substring(7);
			fontKey = fontKey.substring(7);
		}

		if(typeFaces.containsKey(fontKey)) {
			// cached
			tf = typeFaces.get(fontKey);
		} else {
			if (fontKey.startsWith("bold ")) {
				isBold = true;
				fontName = fontName.substring(5);
			}

			String fullFontKey = fontKey.toLowerCase() + ".ttf";
			if (customFonts.containsKey(fullFontKey)) {
				// match either the exact font string with weight, e.g. "bold helvetica"
				tf = Typeface.createFromAsset(tealeaf.getAssets(), "resources/resources/fonts/" + customFonts.get(fullFontKey));
				typeFaces.put(fontKey, tf);
			} else {
				// couldn't match the font, so use the closest system font?
				if (!unsupportedFonts.contains(fontKey)) {
					String warning = "font " + fontKey +" is not supported.  Did you forget to include it?";
					EventQueue.pushEvent(new LogEvent(warning));
					unsupportedFonts.add(fontKey);
					// TODO try to read it from the internet?
				}

				fontName = fontName.replace(" ", "-");
				tf = Typeface.create(fontName, isBold ? Typeface.BOLD : Typeface.NORMAL);
				typeFaces.put(fontKey, tf);
			}
		}

		return tf;
	}

	private TextPaint getTextPaint(String fontKey, int fontSize, int textStyle, String text, int r, int g, int b, int a, float strokeWidth) {
		TextPaint textPaint = new TextPaint();
		textPaint.setAntiAlias(true);
		textPaint.setARGB(a, r, g, b);
		Typeface tf = getTypeface(fontKey);
	
		textPaint.setTypeface(tf);
		textPaint.setTextSize(fontSize);

        //default as fill
        Paint.Style style = Paint.Style.FILL;
        if (textStyle == 1) {
            style = Paint.Style.STROKE;
            textPaint.setStrokeWidth(strokeWidth);
        }
        textPaint.setStyle(style);
		return textPaint;
	}

	private Bitmap loadNewText(String fontName, int fontSize, int textStyle, String text, int r, int g, int b, int a, int maxWidth, float strokeWidth) {
		// If text style is not stroke,
		if (textStyle != 1) {
			// Ignore provided stroke width and use 0
			strokeWidth = 0;
		}

		TextPaint textPaint = getTextPaint(fontName, fontSize, textStyle, text, r, g, b, a, strokeWidth);

		// Measure initial text width
		float width = textPaint.measureText(text);

		// If we are constraining the max width,
		if (maxWidth > 0) {
			// While max width is exceeded and we can reduce font size,
			while (width > maxWidth && fontSize > 0) {
				// Reduce font size and re-measure
				textPaint.setTextSize(--fontSize);
				width = textPaint.measureText(text);
			}
		}

		int strokeAdd = (int)Math.ceil((strokeWidth * 2));
		float ascent = textPaint.ascent();
		float descent = textPaint.descent();

		// Add a little extra to the width and height so stroke does not get clipped
		width = (int)Math.max(Math.ceil(width), 2) + strokeAdd;
		int height = (int)Math.ceil(descent - ascent) + strokeAdd;

		if (width <= 0) {
			width = 1;
		}

		if (height <= 0) {
			height = 1;
		}

		Bitmap bitmap = Bitmap.createBitmap((int)width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, strokeWidth, -ascent, textPaint);

		return bitmap;
	}

	private String[] unhash(String hash, int maxParts) {
		return hash.replace("@TEXT", "").split("\\|", maxParts);
	}

}
