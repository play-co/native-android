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
package com.tealeaf.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class IO {
	public static String toString(Reader input) throws IOException {
		StringBuilder sw = new StringBuilder();
		copy(input, sw);
		return sw.toString();
	}
	public static boolean save(Reader input, String path) throws IOException {
		FileWriter fw = new FileWriter(path);
		if(copy(input, fw) > 0) {
			fw.flush();
			fw.close();
			return true;
		}
		return false;
	}
	public static int copy(Reader input, Writer output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}
	public static int copy(Reader input, StringBuilder output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
				return -1;
		}
		return (int) count;
	}
	public static long copyLarge(Reader input, Writer output) throws IOException {
		char[] buffer = new char[1024*4];
		long count = 0;
		int n = 0;
		while(-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	public static long copyLarge(Reader input, StringBuilder output) throws IOException {
		char[] buffer = new char[1024*4];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.append(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
