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
#include "js/js_build.h"
#include "platform/build.h"

using namespace v8;

Handle<Value> sdk_hash_getter(Local<String> property, const AccessorInfo &info) {
    return String::New(build_get_sdk_hash());
}

Handle<Value> android_hash_getter(Local<String> property, const AccessorInfo &info) {
    return String::New(build_get_android_hash());
}

Handle<Value> game_hash_getter(Local<String> property, const AccessorInfo &info) {
    return String::New(build_get_game_hash());
}

Handle<ObjectTemplate> js_build_get_template() {
    Handle<ObjectTemplate> build = ObjectTemplate::New();

    build->SetAccessor(STRING_CACHE_sdkHash, sdk_hash_getter);
    build->SetAccessor(STRING_CACHE_androidHash, android_hash_getter);
    build->SetAccessor(STRING_CACHE_gameHash, game_hash_getter);

    return build;
}
