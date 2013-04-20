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
#include "platform/input_prompt.h"
#include "platform/platform.h"

int input_prompt_show(const char *title, const char *message, const char *value, bool auto_show_keyboard, bool is_password) {
	native_shim* shim = get_native_shim();
	JNIEnv *env = shim->env;
	jstring jtitle = env->NewStringUTF(title);
	jstring jmessage = env->NewStringUTF(message);
	jstring jvalue = env->NewStringUTF(value);

	jmethodID method = env->GetMethodID(shim->type, "showInputPrompt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZ)I");
	jint id = env->CallIntMethod(shim->instance, method, jtitle, jmessage, jvalue, auto_show_keyboard, is_password);

	env->DeleteLocalRef(jtitle);
	env->DeleteLocalRef(jmessage);
	env->DeleteLocalRef(jvalue);
	
	return id;
}
