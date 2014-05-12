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
#include "js/js_sound.h"
#include "platform/sound_manager.h"

using namespace v8;

Handle<Value> defLoadSound(const Arguments& args) {
    LOGFN("load sound");
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    sound_manager_load_sound(url);
    LOGFN("end load sound");
    return Undefined();
}

Handle<Value> defPlaySound(const Arguments& args) {
    LOGFN("play sound");
    String::Utf8Value str(args[0]);
    double volume = args[1]->NumberValue();
    bool loop = args[2]->IsTrue();
    const char *url = ToCString(str);
    sound_manager_play_sound(url, volume, loop);
    LOGFN("end play sound");
    return Undefined();
}

Handle<Value> defStopSound(const Arguments& args) {
    LOGFN("stop sound");
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    sound_manager_stop_sound(url);
    LOGFN("end stop sound");
    return Undefined();
}

Handle<Value> defPauseSound(const Arguments& args) {
    LOGFN("pause sound");
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    sound_manager_pause_sound(url);
    LOGFN("end pause sound");
    return Undefined();
}

Handle<Value> defSetVolume(const Arguments& args) {
    LOGFN("set volume sound");
    String::Utf8Value str(args[0]);
    float volume = args[1]->NumberValue();
    const char *url = ToCString(str);
    sound_manager_set_volume(url, volume);
    LOGFN("end set volume sound");
    return Undefined();
}


Handle<Value> defPlayBackgroundMusic(const Arguments& args) {
    LOGFN("play bg music");
    String::Utf8Value str(args[0]);
    double volume = args[1]->NumberValue();
    bool loop = args[2]->IsTrue();
    const char *url = ToCString(str);
    sound_manager_play_background_music(url, volume, loop);
    LOGFN("end play bg music");
    return Undefined();
}

Handle<Value> defLoadBackgroundMusic(const Arguments& args) {
    LOGFN("load bg music");
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    sound_manager_load_background_music(url);
    LOGFN("end load bg music");
    return Undefined();
}

Handle<Value> defSeekTo(const Arguments& args) {
    LOGFN("seek to position");
    String::Utf8Value str(args[0]);
    float position = args[1]->NumberValue();
    const char *url = ToCString(str);
    sound_manager_seek_to(url, position);
    LOGFN("end seek to position");
    return Undefined();
}

Handle<ObjectTemplate> js_sound_get_template() {
    Handle<ObjectTemplate> sound = ObjectTemplate::New();
    sound->Set(STRING_CACHE_playSound, FunctionTemplate::New(defPlaySound));
    sound->Set(STRING_CACHE_loadSound, FunctionTemplate::New(defLoadSound));
    sound->Set(STRING_CACHE_playBackgroundMusic, FunctionTemplate::New(defPlayBackgroundMusic));
    sound->Set(STRING_CACHE_loadBackgroundMusic, FunctionTemplate::New(defLoadBackgroundMusic));
    sound->Set(STRING_CACHE_stopSound, FunctionTemplate::New(defStopSound));
    sound->Set(STRING_CACHE_pauseSound, FunctionTemplate::New(defPauseSound));
    sound->Set(STRING_CACHE_setVolume, FunctionTemplate::New(defSetVolume));
    sound->Set(STRING_CACHE_seekTo, FunctionTemplate::New(defSeekTo));
    return sound;
}
