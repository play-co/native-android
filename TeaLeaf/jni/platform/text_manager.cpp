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
#include "platform/platform.h"
extern "C" {
#include "core/texture_2d.h"
#include "core/texture_manager.h"
#include "core/rgba.h"
}
#include "platform/text_manager.h"
#include <stdlib.h>
#include <stdio.h>

texture_2d *text_manager_get_filled_text(const char *font_name, int size, const char *text, rgba *color, int max_width) {

    return text_manager_get_text(font_name, size, text, color, max_width, TEXT_STYLE_FILL, 0);
}

texture_2d *text_manager_get_stroked_text(const char *font_name, int size, const char *text, rgba *color, int max_width, float stroke_width) {

    return text_manager_get_text(font_name, size, text, color, max_width, TEXT_STYLE_STROKE, stroke_width);
}

texture_2d *text_manager_get_text(const char *font_name, int size, const char *text, rgba *color, int max_width, int text_style, float stroke_width) {

	char buf[256] = {'\0'};
	snprintf(buf, sizeof(buf), "@TEXT%s|%i|%i|%i|%i|%i|%i|%i|%f|%s",
			font_name, size,
			(int) (255 * color->r),
			(int) (255 * color->g),
			(int) (255 * color->b),
			(int) (255 * color->a),
			max_width,
			text_style,
			stroke_width,
			text); // text has to go last!

	texture_2d *tex = texture_manager_load_texture(texture_manager_get(), buf);

	return tex;
}

int text_manager_measure_text(const char *font_name, int size, const char *text) {
	native_shim *shim = get_native_shim();
	JNIEnv *env = shim->env;
	jstring jfont = env->NewStringUTF(font_name);
	jint jsize = (jint) size;
	jstring jtext = env->NewStringUTF(text);

	const char *signature = "(Ljava/lang/String;ILjava/lang/String;)I";
	jmethodID id = env->GetMethodID(shim->type, "measureText", signature);
	jint width = env->CallIntMethod(shim->instance, id, jfont, jsize, jtext);
	env->DeleteLocalRef(jfont);
	env->DeleteLocalRef(jtext);

	return (int) width;
}
