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
#include "platform/location_manager.h"


void location_manager_set_location(const char *uri) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "setLocation", "(Ljava/lang/String;)V");
    jstring location = env->NewStringUTF(uri);
    env->CallVoidMethod(manager, method, location);
    env->DeleteLocalRef(location);
}

