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
#include <stdlib.h>

#include "platform/navigator.h"
#include "platform/build.h"

#include "js/js.h"

extern "C" {
#include "core/config.h"
}

using namespace v8;

Handle<Value> js_navigator_get_online_status (Local<String> property, const AccessorInfo &info) {
    return Boolean::New(navigator_get_online_status());
}


Handle<ObjectTemplate> js_navigator_get_template() {
    Handle<ObjectTemplate> navigator = ObjectTemplate::New();

    navigator->Set(STRING_CACHE_width, Integer::New(config_get_screen_width()), ReadOnly);
    navigator->Set(STRING_CACHE_height, Integer::New(config_get_screen_height()), ReadOnly);

    navigator_info *info = navigator_info_init();
    //add the pixel ratio to the window object
    Local<Object> window = getContext()->Global();
    window->Set(STRING_CACHE_devicePixelRatio, Number::New(info->density_dpi / 160.0));

    Handle<ObjectTemplate> js_metrics = ObjectTemplate::New();
    js_metrics->Set(STRING_CACHE_densityDpi, Integer::New(info->density_dpi));
    navigator->Set(STRING_CACHE_displayMetrics, js_metrics);
    const char *android_hash = build_get_android_hash();
    const char *sdk_hash = build_get_sdk_hash();
    int MAX_LEN = 512;
    char ua[MAX_LEN];
    snprintf(ua, MAX_LEN - 1, "Android/%s TeaLeaf/%s GC/%s", info->android_version, android_hash, sdk_hash);
    navigator->Set(STRING_CACHE_userAgent, String::New(ua), ReadOnly);
    navigator->SetAccessor(STRING_CACHE_onLine, js_navigator_get_online_status);

    navigator->Set(STRING_CACHE_language, String::New(info->language), ReadOnly);
    navigator->Set(STRING_CACHE_country, String::New(info->country), ReadOnly);
    navigator_info_free(info);

    return navigator;
}
