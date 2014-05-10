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
#include "platform/status_bar.h"
#include "platform/platform.h"

int status_bar_get_height() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getStatusBarHeight", "()I");
    jint height = env->CallIntMethod(shim->instance, method);
    return height;
}

void status_bar_show() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "showStatusBar", "()V");
    env->CallVoidMethod(shim->instance, method);
}

void status_bar_hide() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "hideStatusBar", "()V");
    env->CallVoidMethod(shim->instance, method);
}
