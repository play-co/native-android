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
#include "platform/overlay.h"
#include "platform/platform.h"

void overlay_load(const char *url) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jstring str = env->NewStringUTF(url);
    jmethodID load_id = env->GetMethodID(type, "loadOverlay", "(Ljava/lang/String;)V");
    env->CallVoidMethod(manager, load_id, str);
    env->DeleteLocalRef(str);
}

void overlay_show() {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID show_id = env->GetMethodID(type, "showOverlay", "()V");
    env->CallVoidMethod(manager, show_id);
}

void overlay_hide() {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID hide_id = env->GetMethodID(type, "hideOverlay", "()V");
    env->CallVoidMethod(manager, hide_id);
}

void overlay_send_event(const char *event) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jstring str = env->NewStringUTF(event);
    jmethodID send_id = env->GetMethodID(type, "sendEventToOverlay", "(Ljava/lang/String;)V");
    env->CallVoidMethod(manager, send_id, str);
    env->DeleteLocalRef(str);
}
