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
#include "js/js_timestep_events.h"
#include "timestep/timestep_events.h"

using namespace v8;

Handle<Value> js_timestep_events_get(const Arguments& args) {
    LOGFN("js_timestep_events_get");

    Local<Object> thiz = args.This();
    //TODO cache this.
    Handle<Function> input_event_ctor = Handle<Function>::Cast(thiz->Get(STRING_CACHE_InputEvent));

    input_event_list list = timestep_events_get();
    Handle<v8::Array> arr = Array::New(list.count);
    for (unsigned int i = 0; i < list.count; ++i) {
        Handle<Value> args[] = {
            Number::New(list.events[i].id),
            Number::New(list.events[i].type),
            Number::New(list.events[i].x),
            Number::New(list.events[i].y),
        };
        Handle<Object> obj = input_event_ctor->NewInstance(4, args);
        //Handle<v8::Array> item = Array::New(4);
        arr->Set(Number::New(i), obj);
    }

    LOGFN("end js_timestep_events_get");
    return arr;
}

