/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.
 
 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.
 
 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
#ifndef JS_TIMESTEP_H
#define JS_TIMESTEP_H

#include "js/js.h"

using v8::Handle;
using v8::FunctionTemplate;
using v8::Object;

Handle<FunctionTemplate> get_animate_class();
void def_animate_cb(Handle<Object> js_view, Handle<Object> cb, double tt, double t);
void def_animate_add_to_group(Handle<Object> js_anim);
void def_animate_remove_from_group(Handle<Object> js_anim);

#endif // JS_TIMESTEP_H
