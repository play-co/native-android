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
package com.tealeaf.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.tealeaf.logger;

import dalvik.system.DexFile;

public class PluginManager {

	private static String PACKAGE_NAME = "com.tealeaf";
	private static String PLUGINS_PACKAGE_NAME = "com.tealeaf.plugin.plugins";
	private static HashMap<String, Object> classMap = new HashMap<String, Object>();

	public static void init(Context context) {

		ArrayList<String> classNames = new ArrayList<String>();
		try {

			String apkName = null;
			try {
				apkName = context.getPackageManager().getApplicationInfo(
						context.getApplicationContext().getPackageName(), 0).sourceDir;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			DexFile dexFile = new DexFile(new File(apkName));
			Enumeration<String> enumeration = dexFile.entries();

			int pluginsPackageStrLen = PLUGINS_PACKAGE_NAME.length();
			while (enumeration.hasMoreElements()) {
				String className = enumeration.nextElement();
				if (className.length() < pluginsPackageStrLen)
					continue;
				if (className.subSequence(0, pluginsPackageStrLen).equals(
						PLUGINS_PACKAGE_NAME)) {
					classNames.add(className);
				}

			}
		} catch (IOException e) {
			logger.log(e);
		}

		// TODO: Load plugin names from some json file (manifest.json?)
		if (classNames.size() > 0) {
			String[] classNamesArr = new String[classNames.size()];
			classNames.toArray(classNamesArr);
			for (String name : classNamesArr) {
				try {
					if (name.contains("$")) continue;
					classMap.put(name, Class.forName(name).newInstance());
				} catch (ClassNotFoundException e) {
					logger.log(e);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	private static Object invokeMethod(Object targetObject, Object[] parameters,
			String methodName) {

		Object obj = null;
		if (targetObject == null) {
			return obj;
		}

		for (Method method : targetObject.getClass().getMethods()) {
			if (!method.getName().equals(methodName)) {
				continue;
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			boolean match = true;
			for (int i = 0; i < parameterTypes.length; i++) {
				if (parameters[i] == null) {
					continue;
				}
				if (!parameterTypes[i].isAssignableFrom(parameters[i].getClass())) {
					match = false;
					break;
				}
			}
			if (match) {
				try {
					if (method.getReturnType().equals(Void.TYPE)) {
						method.invoke(targetObject, parameters);
					} else {
						obj = method.invoke(targetObject, parameters);
					}
				} catch (IllegalArgumentException e) {
					logger.log(e);
				} catch (IllegalAccessException e) {
					logger.log(e);
				} catch (InvocationTargetException e) {
					logger.log(e);
				}
			}
		}
		return obj;
	}

	public static Object[] callAll(String methodName, Object... params) {

		Object[] objs = new Object[classMap.size()];
		int i = 0;
		for (Entry<String, Object> classEntry : classMap.entrySet()) {
			Object o = invokeMethod(classEntry.getValue(), params, methodName);
			objs[i++] = o;
		}
		return objs;
	}

	public static Object call(String className, String methodName,
			Object... params) {
		return invokeMethod(classMap.get("com.tealeaf.plugin.plugins." + className), params, methodName);
	}

}
