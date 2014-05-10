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

int input_open_prompt(const char *title, const char *message, const char *ok_text, const char *cancel_text, const char *value, bool auto_show_keyboard, bool is_password) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jstring jtitle = env->NewStringUTF(title);
    jstring jmessage = env->NewStringUTF(message);
    jstring jvalue = env->NewStringUTF(value);
    jstring jok = env->NewStringUTF(ok_text);
    jstring jcancel = env->NewStringUTF(cancel_text);

    jmethodID method = env->GetMethodID(shim->type, "openPrompt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZ)I");
    jint id = env->CallIntMethod(shim->instance, method, jtitle, jmessage, jok, jcancel, jvalue, auto_show_keyboard, is_password);

    env->DeleteLocalRef(jtitle);
    env->DeleteLocalRef(jmessage);
    env->DeleteLocalRef(jvalue);
    env->DeleteLocalRef(jok);
    env->DeleteLocalRef(jcancel);

    return id;
}

void input_show_keyboard(const char *curr_val, const char *hint, bool has_backward, bool has_forward, const char *input_type, const char *input_return_button, int max_length, int cursorPos) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jstring jcurr_val = env->NewStringUTF(curr_val);
    jstring jhint = env->NewStringUTF(hint);
    jstring jinput_type = env->NewStringUTF(input_type);
    jstring jinput_return_button = env->NewStringUTF(input_return_button);

    jmethodID method = env->GetMethodID(shim->type, "showSoftKeyboard", "(Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;II)V");
    env->CallVoidMethod(shim->instance, method, jcurr_val, jhint, has_backward, has_forward, jinput_type, jinput_return_button, max_length, cursorPos);
}

void input_hide_keyboard() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "hideSoftKeyboard", "()V");
    env->CallVoidMethod(shim->instance, method);
}

