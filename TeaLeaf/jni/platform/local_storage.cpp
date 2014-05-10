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
#include "platform/local_storage.h"


void local_storage_set_data(const char *key, const char *data) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "setData", "(Ljava/lang/String;[B)V");

    int len = strlen(data);
    jbyteArray data_str = env->NewByteArray(len);
    env->SetByteArrayRegion(data_str, 0, len, (jbyte*) data);

    jstring k = env->NewStringUTF(key);
    env->CallVoidMethod(manager, method, k, data_str);
    env->DeleteLocalRef(k);
    env->DeleteLocalRef(data_str);
}

const char *local_storage_get_data(const char *key) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "getDataAsBytes", "(Ljava/lang/String;)[B");
    jstring k = env->NewStringUTF(key);
    jbyteArray data = (jbyteArray) env->CallObjectMethod(manager, method, k);
    env->DeleteLocalRef(k);

    char *data_str = NULL;
    if (data) {
        int len = env->GetArrayLength(data);
        data_str = (char *) malloc(len + 1);
        data_str[len] = 0;
        env->GetByteArrayRegion(data, 0, len, (jbyte*) data_str);
    }

    env->DeleteLocalRef(data);
    return data_str;
}

void local_storage_remove_data(const char *key) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "removeData", "(Ljava/lang/String;)V");
    jstring k = env->NewStringUTF(key);
    env->CallVoidMethod(manager, method, k);
    env->DeleteLocalRef(k);
}

void local_storage_clear() {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "clearData", "()V");
    env->CallVoidMethod(manager, method);

}
