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
#ifndef JS_LOCATION_H
#define JS_LOCATION_H

#include "js/js.h"

void native_initialize_location(const char *uri);

// setLocation() handler
v8::Handle<v8::Value> native_set_location(const v8::Arguments &args);

// window.location accessors
v8::Handle<v8::Value> jsGetLocation(v8::Local<v8::String> name, const v8::AccessorInfo &info);
void jsSetLocation(v8::Local<v8::String> name, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

#endif
