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
#include "platform/xhr.h"
#include "platform/platform.h"

void xhr_send(xhr *req) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID methodID = env->GetMethodID(type, "sendXHR", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z[Ljava/lang/String;)V");
    jint id = (jint) req->id;
    jstring method = env->NewStringUTF(req->method);
    jstring url = env->NewStringUTF(req->url);
    jstring data = env->NewStringUTF(req->data);
    jboolean async = (jboolean) req->async;

    jobjectArray headers = NULL;
    if(req->request_headers != NULL) {
        int hashCount = HASH_COUNT(req->request_headers);
        headers = (jobjectArray) env->NewObjectArray(hashCount * 2, env->FindClass("java/lang/String"), NULL);
        int i = 0;

        request_header *currentHeader, *tmp;
        HASH_ITER(hh, req->request_headers, currentHeader, tmp) {
            env->SetObjectArrayElement(headers, i * 2, env->NewStringUTF(currentHeader->header));
            env->SetObjectArrayElement(headers, i * 2 + 1, env->NewStringUTF(currentHeader->value));
            i++;
        }
    }

    //add hashmap of request headers here
    env->CallVoidMethod(manager, methodID, id, method, url, data, async, headers);
    env->DeleteLocalRef(method);
    env->DeleteLocalRef(url);
    env->DeleteLocalRef(data);
}

