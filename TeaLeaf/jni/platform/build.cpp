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
#include "platform/build.h"
#include "core/types.h"
#include <string.h>

static const char *sdk_hash = NULL;
static const char *android_hash = NULL;
static const char *game_hash = NULL;

const char *build_get_sdk_hash() {
    if (!sdk_hash) {
        native_shim *shim = get_native_shim();
        JNIEnv *env = shim->env;
        jclass type = shim->type;
        jobject instance = shim->instance;
        jmethodID method = env->GetMethodID(type,
                                            "getSDKHash",
                                            "()Ljava/lang/String;");
        if (!method) {
            sdk_hash = strdup("Unknown");
        } else {
            jstring result = (jstring)env->CallObjectMethod(instance, method);
            GET_STR(env, result, sdk_hash);
        }
    }
    return sdk_hash;
}
const char *build_get_android_hash() {
    if (!android_hash) {
        native_shim *shim = get_native_shim();
        JNIEnv *env = shim->env;
        jclass type = shim->type;
        jobject instance = shim->instance;
        jmethodID method = env->GetMethodID(type,
                                            "getAndroidHash",
                                            "()Ljava/lang/String;");
        if (!method) {
            android_hash = strdup("Unknown");
        } else {
            jstring result = (jstring)env->CallObjectMethod(instance, method);
            GET_STR(env, result, android_hash);
        }
    }
    return android_hash;
}

const char *build_get_game_hash() {
    if (!game_hash) {
        native_shim *shim = get_native_shim();
        JNIEnv *env = shim->env;
        jclass type = shim->type;
        jobject instance = shim->instance;
        jmethodID method = env->GetMethodID(type,
                                            "getGameHash",
                                            "()Ljava/lang/String;");
        if (!method) {
            game_hash = strdup("Unknown");
        } else {
            jstring result = (jstring)env->CallObjectMethod(instance, method);
            GET_STR(env, result, game_hash);
        }
    }
    return game_hash;
}
