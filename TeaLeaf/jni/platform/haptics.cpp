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
#include "platform/haptics.h"
#include "platform/platform.h"


void haptics_cancel() {
    native_shim *shim = get_native_shim();

    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "cancel", "()V");
    env->CallVoidMethod(shim->instance, method);
}

void haptics_vibrate(long long milliseconds) {
    native_shim *shim = get_native_shim();

    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "vibrate", "(J)V");
    env->CallVoidMethod(shim->instance, method, milliseconds);
}

void haptics_vibrate(long long* pattern, int repeat, int patternLen) {
    native_shim *shim = get_native_shim();

    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "vibrate", "([JI)V");

    jlongArray jPattern = env->NewLongArray(patternLen);
    env->SetLongArrayRegion(jPattern, 0, patternLen, pattern);

    env->CallVoidMethod(shim->instance, method, jPattern, repeat);
}

bool haptics_has_vibrator() {
    native_shim* shim = get_native_shim();

    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "hasVibrator", "()Z");
    jboolean result = env->CallBooleanMethod(shim->instance, method);

    return result == JNI_TRUE;
}
