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

#include <stdio.h>
#include "platform/native.h"
#include "platform/platform.h"
#include "core/texture_manager.h"

const char* get_market_url() {
    native_shim* shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "getMarketUrl", "()Ljava/lang/String;");
    jstring str = (jstring) shim->env->CallObjectMethod(shim->instance, method);
    const char* result = NULL;
    GET_STR(shim->env, str, result);
    shim->env->DeleteLocalRef(str);
    return result;
}

const char* get_version_code() {
    native_shim* shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "getVersionCode", "()Ljava/lang/String;");
    jstring version = (jstring)shim->env->CallObjectMethod(shim->instance, method);
    const char* result = NULL;
    GET_STR(shim->env, version, result);
    shim->env->DeleteLocalRef(version);
    return result;
}

void start_game(const char* appid) {
    native_shim* shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "startGame", "(Ljava/lang/String;)V");
    jstring jappid = shim->env->NewStringUTF(appid);
    shim->env->CallVoidMethod(shim->instance, method, jappid);
    shim->env->DeleteLocalRef(jappid);
}

void apply_update() {
    native_shim* shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "applyUpdate", "()V");
    shim->env->CallVoidMethod(shim->instance, method);
}

bool native_send_activity_to_back() {
    native_shim *shim = get_native_shim();

    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "sendActivityToBack", "()Z");
    jboolean result = env->CallBooleanMethod(shim->instance, method);

    return result == JNI_TRUE;
}

char * get_storage_directory() {
    native_shim * shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "getStorageDirectory", "()Ljava/lang/String;");
    jstring path = (jstring) shim->env->CallObjectMethod(shim->instance, method);
    char* result =NULL;
    GET_STR(shim->env,path,result);
    shim->env->DeleteLocalRef(path);
    return result;
}

void upload_contacts() {
    native_shim *shim = get_native_shim();

    jmethodID method = shim->env->GetMethodID(shim->type, "uploadContacts", "()V");
    shim->env->CallVoidMethod(shim->instance, method);
}

void upload_device_info() {
    native_shim *shim = get_native_shim();

    jmethodID method = shim->env->GetMethodID(shim->type, "uploadDeviceInfo", "()V");
    shim->env->CallVoidMethod(shim->instance, method);
}

const char* get_install_referrer() {
    native_shim* shim = get_native_shim();
    JNIEnv *env = shim->env;

    jmethodID method = env->GetMethodID(shim->type, "getInstallReferrer", "()Ljava/lang/String;");
    jstring value = (jstring) env->CallObjectMethod(shim->instance, method);
    const char* str = NULL;
    GET_STR(env, value, str);
    env->DeleteLocalRef(value);
    return str;
}

void report_gl_error(int code, gl_error **errors_hash, bool unrecoverable) {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "reportGlError", "(I)[I");
    jint error = code;
    jintArray gl_errors_arr = (jintArray)shim->env->CallObjectMethod(shim->instance, method, error);
    int length = (int)shim->env->GetArrayLength(gl_errors_arr);
    int *error_arr = (int*)shim->env->GetIntArrayElements(gl_errors_arr, 0);
    for (int i = 0; i < length; i++) {
        int err = error_arr[i];
        gl_error *error_obj = (gl_error *)malloc(sizeof(gl_error));
        error_obj->error_code = err;
        HASH_ADD_INT(*errors_hash, error_code, error_obj);
    }
    shim->env->ReleaseIntArrayElements(gl_errors_arr, error_arr, 0);

    if (unrecoverable) {
        jmethodID method = shim->env->GetMethodID(shim->type, "logNativeError", "()V");
        shim->env->CallVoidMethod(shim->instance, method);
    }
}

void set_halfsized_textures(bool on) {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "setHalfsizedTexturesSetting", "(Z)V");
    shim->env->CallVoidMethod(shim->instance, method, (jboolean)on);
    texture_manager_set_use_halfsized_textures(on);
}

void native_stay_awake(bool on) {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "setStayAwake", "(Z)V");
    shim->env->CallVoidMethod(shim->instance, method, (jboolean)on);
}

void native_reload() {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "reload", "()V");
    shim->env->CallVoidMethod(shim->instance, method);
}

char *native_call(const char *method, const char *args) {

    native_shim* shim = get_native_shim();
    jmethodID jmethod = shim->env->GetMethodID(shim->type, "call", "(Ljava/lang/String;[B)Ljava/lang/String;");

    jstring methodStr = shim->env->NewStringUTF(method);

    jbyteArray jbuff = shim->env->NewByteArray(strlen(args));
    shim->env->SetByteArrayRegion(jbuff, 0, strlen(args), (jbyte*) args);
    jstring jdata = (jstring) shim->env->CallObjectMethod(shim->instance, jmethod, methodStr, jbuff);
    char *ret_data = NULL;
    GET_STR(shim->env, jdata, ret_data);
    shim->env->DeleteLocalRef(jdata);
    shim->env->DeleteLocalRef(methodStr);
    shim->env->DeleteLocalRef(jbuff);
    return ret_data;

}
