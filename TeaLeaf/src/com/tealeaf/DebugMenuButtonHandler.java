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

public class DebugMenuButtonHandler implements IMenuButtonHandler {
	private TeaLeaf context;
	private static boolean singleShader = false;
	private static boolean halfsizedTextures = false;
	public DebugMenuButtonHandler(TeaLeaf context) {
		this.context = context;
	}
	@Override
	public boolean onPress(int id) {
		if (id == R.id.deviceinfo) {
			// show device info
			halfsizedTextures = this.context.getSettings().getBoolean("@__use_halfsized_textures__", false);
			String useHalfsizedTexturesStr = halfsizedTextures ? "Turn off halfsized textures" : "Turn on halfsized textures";
			String[] buttons = {"Dismiss", "Swap Shader", useHalfsizedTexturesStr};
			Runnable[] cbs = {
					new Runnable() {
						public void run() {}
					},
					new Runnable() {
						public void run() {
							singleShader = !singleShader;
							NativeShim.setSingleShader(singleShader);
						}
					},new Runnable() {
						public void run() {
							halfsizedTextures = !halfsizedTextures;
							NativeShim.setHalfsizedTextures(halfsizedTextures);
							NativeShim.clearTextures();
							context.getSettings().setBoolean("@__use_halfsized_textures__", halfsizedTextures);
						}
					},

			};

			TeaLeafOptions options = context.getOptions();
			String debugInfo = String.format("Game Hash: %s\nSDK Hash: %s\nAndroid Hash: %s",
												options.getSDKHash(),
												options.getAndroidHash(),
												options.getGameHash());
			
			JSDialog.showDialog(context, null, "Debug Info", debugInfo, buttons, cbs);
			return true;
		} else {
			return false;
		}
	}

}
