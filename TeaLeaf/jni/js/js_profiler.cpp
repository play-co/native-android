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
#include "js/js_profiler.h"
#include "platform/profiler.h"
#include "core/config.h"
#include "js/js.h"

using namespace v8;

Handle<Value> js_profiler_start_profile(const Arguments &args) {
#ifdef PROFILER_ENABLED
    String::Utf8Value tag_str(args[0]);
    const char *tag = ToCString(tag_str);
    LOG("{profiler} Starting %s", tag);
    profiler_start_profile(tag);
#endif

    return Undefined();
}

Handle<Value> js_profiler_stop_profile(const Arguments &args) {
#ifdef PROFILER_ENABLED
    LOG("{profiler} Stopped");
    profiler_stop_profile();
#endif
    return Undefined();
}




Handle<ObjectTemplate> js_profiler_get_template() {
    Handle<ObjectTemplate> profiler = ObjectTemplate::New();

    profiler->Set(STRING_CACHE_start, FunctionTemplate::New(js_profiler_start_profile));
    profiler->Set(STRING_CACHE_stop, FunctionTemplate::New(js_profiler_stop_profile));
#ifdef PROFILER_ENABLED
#define __ENABLED true
#else
#define __ENABLED false
#endif
    profiler->Set(STRING_CACHE_enabled, Boolean::New(__ENABLED));

    return profiler;
}
