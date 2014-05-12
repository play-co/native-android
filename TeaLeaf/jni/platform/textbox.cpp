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
#include "platform/textbox.h"
#include "platform/platform.h"

int textbox_create_new() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jmethodID method = env->GetMethodID(shim->type, "createTextBox", "()I");
    return env->CallIntMethod(shim->instance, method);
}

int textbox_create_init(int x, int y, int w, int h, const char* data) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "createTextBox", "(IIIILjava/lang/String;)I");
    jstring str = env->NewStringUTF(data);
    int id = env->CallIntMethod(shim->instance, method, x, y, w, h, str);
    env->DeleteLocalRef(str);
    return id;
}

void textbox_destroy(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "destroyTextBox", "(I)V");
    env->CallVoidMethod(shim->instance, method, id);
}

void textbox_show(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "showTextBox", "(I)V");
    env->CallVoidMethod(shim->instance, method, id);
}


void textbox_hide(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "hideTextBox", "(I)V");
    env->CallVoidMethod(shim->instance, method, id);
}

void textbox_set_position(int id, int x, int y, int w, int h) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxPosition", "(IIIII)V");
    env->CallVoidMethod(shim->instance, method, id, x, y, w, h);
}

void textbox_set_dimensions(int id, int w, int h) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxDimensions", "(III)V");
    env->CallVoidMethod(shim->instance, method, id, w, h);
}

void textbox_set_x(int id, int x) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxX", "(II)V");
    env->CallVoidMethod(shim->instance, method, id, x);
}

void textbox_set_y(int id, int y) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxY", "(II)V");
    env->CallVoidMethod(shim->instance, method, id, y);
}

void textbox_set_width(int id, int w) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxWidth", "(II)V");
    env->CallVoidMethod(shim->instance, method, id, w);
}

void textbox_set_height(int id, int h) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxHeight", "(II)V");
    env->CallVoidMethod(shim->instance, method, id, h);
}

void textbox_set_value(int id, const char* str) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxValue", "(ILjava/lang/String;)V");
    jstring value = env->NewStringUTF(str);
    env->CallVoidMethod(shim->instance, method, id, value);
    env->DeleteLocalRef(value);
}

void textbox_set_opacity(int id, float value) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxOpacity", "(IF)V");
    env->CallVoidMethod(shim->instance, method, id, value);
}

void textbox_set_type(int id, int type) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxType", "(II)V");
    env->CallVoidMethod(shim->instance, method, id, type);
}

void textbox_set_visible(int id, bool visible) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "setTextBoxVisible", "(IZ)V");
    env->CallVoidMethod(shim->instance, method, id, visible);
}

int textbox_get_x(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxX", "(I)I");
    return env->CallIntMethod(shim->instance, method, id);
}

int textbox_get_y(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxY", "(I)I");
    return env->CallIntMethod(shim->instance, method, id);
}

int textbox_get_width(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxWidth", "(I)I");
    return env->CallIntMethod(shim->instance, method, id);
}

int textbox_get_height(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxHeight", "(I)I");
    return env->CallIntMethod(shim->instance, method, id);
}

const char* textbox_get_value(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxValue", "(I)Ljava/lang/String;");
    jstring value = (jstring) env->CallObjectMethod(shim->instance, method, id);
    const char* str = NULL;
    GET_STR(env, value, str);
    env->DeleteLocalRef(value);
    return str;
}

float textbox_get_opacity(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxOpacity", "(I)F");
    return env->CallFloatMethod(shim->instance, method, id);
}

int textbox_get_type(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxType", "(I)I");
    return env->CallIntMethod(shim->instance, method, id);
}

bool textbox_get_visible(int id) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getTextBoxVisible", "(I)Z");
    return env->CallBooleanMethod(shim->instance, method, id) == JNI_TRUE;
}
