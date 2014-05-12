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
#include "platform/socket.h"
#include "platform/platform.h"


void socket_send(int id, const char *data) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jstring jdata = env->NewStringUTF(data);
    jint jid = (jint) id;

    jmethodID send_id = env->GetMethodID(type, "sendData", "(ILjava/lang/String;)V");
    env->CallVoidMethod(manager, send_id, jid, jdata);
    env->DeleteLocalRef(jdata);
}

void socket_close(int id) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID close_id = env->GetMethodID(type, "closeSocket", "(I)V");
    jint jid = (jint) id;
    env->CallVoidMethod(manager, close_id, jid);
}

int socket_create(const char *host, int port) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jstring jhost = env->NewStringUTF(host);
    jint jport = (jint) port;
    jmethodID connect_id = env->GetMethodID(type, "openSocket", "(Ljava/lang/String;I)I");
    jint id = env->CallIntMethod(manager, connect_id, jhost, jport);
    env->DeleteLocalRef(jhost);
    return (int) id;
}

