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
#include "platform/plugins.h"
#include "platform/platform.h"
#include "platform/native.h"

char *plugins_send_event(const char *pluginClass, const char *pluginClassMethod, const char *data) {


    native_shim* shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "pluginsCall", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
    jbyteArray jbuff = shim->env->NewByteArray(strlen(data));
    shim->env->SetByteArrayRegion(jbuff, 0, strlen(data), (jbyte*) data);
    jstring className = shim->env->NewStringUTF(pluginClass);
    jstring methodName = shim->env->NewStringUTF(pluginClassMethod);

    jobjectArray params = NULL;
    params = (jobjectArray) shim->env->NewObjectArray(1, shim->env->FindClass("java/lang/Object"), NULL);
    shim->env->SetObjectArrayElement(params, 0, jbuff);

    jstring jdata = (jstring) shim->env->CallObjectMethod(shim->instance, method, className, methodName, params);
    char *ret_data = NULL;
    GET_STR(shim->env, jdata, ret_data);
    shim->env->DeleteLocalRef(jdata);
    shim->env->DeleteLocalRef(jbuff);
    shim->env->DeleteLocalRef(className);
    shim->env->DeleteLocalRef(methodName);
    shim->env->DeleteLocalRef(params);
    return ret_data;
}

void plugins_send_request(const char *pluginClass, const char *pluginClassMethod, const char *data, int request_id) {
    native_shim* shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "pluginsRequest", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;I)V");
    jstring str = shim->env->NewStringUTF(data);
    jstring className = shim->env->NewStringUTF(pluginClass);
    jstring methodName = shim->env->NewStringUTF(pluginClassMethod);

    jobjectArray params = NULL;
    params = (jobjectArray) shim->env->NewObjectArray(1, shim->env->FindClass("java/lang/Object"), NULL);
    shim->env->SetObjectArrayElement(params, 0, str);

    shim->env->CallVoidMethod(shim->instance, method, className, methodName, params, (jint)request_id);
    shim->env->DeleteLocalRef(str);
    shim->env->DeleteLocalRef(className);
    shim->env->DeleteLocalRef(methodName);
    shim->env->DeleteLocalRef(params);
}

