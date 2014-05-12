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
#include "platform/sound_manager.h"
#include "platform/resource_loader.h"


static inline void do_call(const char *url, const char *method_name) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, method_name, "(Ljava/lang/String;)V");
    jstring s = env->NewStringUTF(url);
    env->CallVoidMethod(manager, method, s);
    env->DeleteLocalRef(s);
}
void sound_manager_load_sound(const char *url) {
    do_call(url, "loadSound");
}

void sound_manager_play_sound(const char *url, float volume, bool loop) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "playSound", "(Ljava/lang/String;FZ)V");
    jstring s = env->NewStringUTF(url);
    env->CallVoidMethod(manager, method, s, volume, loop);
    env->DeleteLocalRef(s);
}

void sound_manager_stop_sound(const char *url) {
    do_call(url, "stopSound");

}

void sound_manager_pause_sound(const char *url) {
    do_call(url, "pauseSound");
}

void sound_manager_set_volume(const char *url, float volume) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "setVolume", "(Ljava/lang/String;F)V");
    jstring s = env->NewStringUTF(url);
    env->CallVoidMethod(manager, method, s, volume);
    env->DeleteLocalRef(s);
}

void sound_manager_play_background_music(const char *url, float volume, bool loop) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "playBackgroundMusic", "(Ljava/lang/String;FZ)V");
    jstring s = env->NewStringUTF(url);
    env->CallVoidMethod(manager, method, s, volume, loop);
    env->DeleteLocalRef(s);
}

void sound_manager_load_background_music(const char *url) {
    do_call(url, "loadBackgroundMusic");
}

void sound_manager_seek_to(const char *url, float position) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "seekTo", "(Ljava/lang/String;F)V");
    jstring s = env->NewStringUTF(url);
    env->CallVoidMethod(manager, method, s, position);
    env->DeleteLocalRef(s);
}

void sound_manager_halt() {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jobject manager = shim->instance;
    jclass type = shim->type;
    jmethodID method = env->GetMethodID(type, "haltSounds", "()V");
    env->CallVoidMethod(manager, method);
}
