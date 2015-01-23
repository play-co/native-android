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
#include "platform/device.h"
#include "platform/platform.h"

const char* device_global_id() {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jmethodID method = env->GetMethodID(shim->type, "getDeviceID", "()Ljava/lang/String;");
    jstring result = (jstring) env->CallObjectMethod(shim->instance, method);
    const char* str = NULL;
    GET_STR(env, result, str);
    env->DeleteLocalRef(result);
    return str;
}

const char* device_info() {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "getDeviceInfo", "()Ljava/lang/String;");
    jstring result = (jstring) shim->env->CallObjectMethod(shim->instance, method);
    const char* str = NULL;
    GET_STR(shim->env, result, str);
    shim->env->DeleteLocalRef(result);
    return str;
}

CEXPORT int device_total_memory() {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "getTotalMemory", "()I");
    jint result = shim->env->CallIntMethod(shim->instance, method);
    return result;
}

CEXPORT void device_hide_splash() {
    // On Android there is no pre-splash stuff, just the core OpenGL splash so
    // this is a no-op.
}

static float text_scale = 1.0;
CEXPORT float device_get_text_scale() {
    return text_scale;
}

CEXPORT void device_set_text_scale(float scale) {
    text_scale = scale;
}

CEXPORT bool device_is_simulator() {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "isSimulator", "()Z");
    jboolean result = shim->env->CallBooleanMethod(shim->instance, method);
    return result;
}
