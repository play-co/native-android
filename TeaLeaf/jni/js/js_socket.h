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
#ifndef JS_SOCKET_H
#define JS_SOCKET_H
#include "js/js.h"

using v8::Handle;
using v8::Value;
using v8::Arguments;
using v8::Object;
using v8::Persistent;


Handle<Value> js_socket_send(const Arguments &args);
Handle<Value> js_socket_close(const Arguments &args);
Handle<Value> js_socket_default_callback(const Arguments &args);
Handle<Value> js_socket_ctor(const Arguments &args);
void js_socket_connected(int id);
void js_socket_on_data(int id, const char *data);
void js_socket_error(int id);
#endif
