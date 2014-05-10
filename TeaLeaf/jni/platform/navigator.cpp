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
#include "platform/navigator.h"


bool navigator_get_online_status() {
    native_shim *shim = get_native_shim();
    jmethodID method = shim->env->GetMethodID(shim->type, "getOnlineStatus", "()Z");
    jboolean result = shim->env->CallBooleanMethod(shim->instance, method);

    return result == JNI_TRUE;
}

navigator_info* navigator_info_init() {
    native_shim *shim = get_native_shim();

    jclass display_metrics_class = (jclass)shim->env->FindClass("android/util/DisplayMetrics");
    jfieldID density_dpi = shim->env->GetFieldID(display_metrics_class, "densityDpi", "I");

    jmethodID method = shim->env->GetMethodID(shim->type, "getDisplayMetrics", "()Landroid/util/DisplayMetrics;");
    jobject result = shim->env->CallObjectMethod(shim->instance, method);

    navigator_info *info = (navigator_info *) malloc(sizeof(navigator_info));
    info->density_dpi = shim->env->GetIntField(result, density_dpi);

    jstring android_version;
    jmethodID get_android_version = shim->env->GetMethodID(shim->type, "getAndroidHash", "()Ljava/lang/String;");
    android_version = (jstring)shim->env->CallObjectMethod(shim->instance, get_android_version);
    GET_STR(shim->env, android_version, info->android_version);

    jstring language;
    jmethodID get_language = shim->env->GetMethodID(shim->type, "getLanguage", "()Ljava/lang/String;");
    language = (jstring)shim->env->CallObjectMethod(shim->instance, get_language);
    GET_STR(shim->env, language, info->language);

    jstring country;
    jmethodID get_country = shim->env->GetMethodID(shim->type, "getCountry", "()Ljava/lang/String;");
    country = (jstring)shim->env->CallObjectMethod(shim->instance, get_country);
    GET_STR(shim->env, country, info->country);

    shim->env->DeleteLocalRef(result);
    shim->env->DeleteLocalRef(android_version);
    shim->env->DeleteLocalRef(language);
    shim->env->DeleteLocalRef(country);
    return info;
}

void navigator_info_free(navigator_info *info) {
    free(info->language);
    free(info->android_version);
    free(info);
}
