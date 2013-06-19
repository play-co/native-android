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
#include "js/js.h"

using v8::Handle;
using v8::ObjectTemplate;
using v8::String;
using v8::Local;
using v8::AccessorInfo;
using v8::Value;

Handle<Value> js_device_global_id(Local<String> name, const AccessorInfo &info);
Handle<ObjectTemplate> js_device_get_template();
