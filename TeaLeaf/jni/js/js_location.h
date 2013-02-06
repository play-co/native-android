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
