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
	jmethodID method = env->GetMethodID(type, "setData", "(Ljava/lang/String;Ljava/lang/String;)V");
	jstring k = env->NewStringUTF(key);
	jstring d = env->NewStringUTF(data);
	env->CallVoidMethod(manager, method, k, d);
	env->DeleteLocalRef(k);
	env->DeleteLocalRef(d);
}

const char *local_storage_get_data(const char *key) {
	native_shim *shim = get_native_shim();
	JNIEnv *env = shim->env;
	jobject manager = shim->instance;
	jclass type = shim->type;
	jmethodID method = env->GetMethodID(type, "getData", "(Ljava/lang/String;)Ljava/lang/String;");
	jstring k = env->NewStringUTF(key);
	jstring data = (jstring) env->CallObjectMethod(manager, method, k);
	env->DeleteLocalRef(k);
	const char *ret_data = NULL;
	GET_STR(env, data, ret_data);
	env->DeleteLocalRef(data);
	return ret_data;
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
