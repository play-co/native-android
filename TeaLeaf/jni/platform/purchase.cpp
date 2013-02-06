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
#include "platform/purchase.h"
#include "platform/platform.h"


void purchase_buy(const char* id) {
	native_shim *shim = get_native_shim();
	JNIEnv *env = shim->env;
	jobject manager = shim->instance;
	jclass type = shim->type;
	jmethodID method = env->GetMethodID(type, "buy", "(Ljava/lang/String;)V");
	jstring jid = env->NewStringUTF(id);
	env->CallVoidMethod(manager, method, jid);
	env->DeleteLocalRef(jid);
}

void purchase_restore(void) {
	native_shim *shim = get_native_shim();
	JNIEnv *env = shim->env;
	jobject manager = shim->instance;
	jclass type = shim->type;
	jmethodID method = env->GetMethodID(type, "restore", "()V");
	env->CallVoidMethod(manager, method);
}

void purchase_confirm(const char *id) {
	native_shim *shim = get_native_shim();
	JNIEnv *env = shim->env;
	jobject manager = shim->instance;
	jclass type = shim->type;
	jmethodID method = env->GetMethodID(type, "confirmPurchase", "(Ljava/lang/String;)V");
	jstring jid = env->NewStringUTF(id);
	env->CallVoidMethod(manager, method, jid);
	env->DeleteLocalRef(jid);
}

bool purchase_available(void) {
	native_shim *shim = get_native_shim();
	JNIEnv *env = shim->env;
	jobject manager = shim->instance;
	jclass type = shim->type;
	jmethodID method = env->GetMethodID(type, "marketAvailable", "()Z");
	jboolean result = env->CallBooleanMethod(manager, method);
	return result == JNI_TRUE;
}
