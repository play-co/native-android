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
#include "platform/photo.h"
#include "platform/platform.h"

int camera_get_photo(const char *url, int width, int height, int crop) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jstring jurl = env->NewStringUTF(url);
    jmethodID method = env->GetMethodID(shim->type, "cameraGetPhoto", "(III)I");
    jint result = env->CallIntMethod(shim->instance, method, width, height, crop);
    env->DeleteLocalRef(jurl);
    return result;
}

int gallery_get_photo(const char *url, int width, int height, int crop) {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jstring jurl = env->NewStringUTF(url);
    jmethodID method = env->GetMethodID(shim->type, "galleryGetPhoto", "(III)I");
    jint result = env->CallIntMethod(shim->instance, method, width, height, crop);
    env->DeleteLocalRef(jurl);
    return result;
}

int camera_get_next_id() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jmethodID method = env->GetMethodID(shim->type, "getNextCameraId", "()I");
    jint result = env->CallIntMethod(shim->instance, method);
    return result;
}

int gallery_get_next_id() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;
    jmethodID method = env->GetMethodID(shim->type, "getNextGalleryId", "()I");
    jint result = env->CallIntMethod(shim->instance, method);
    return result;
}
