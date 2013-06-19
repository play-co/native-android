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
#ifndef JS_TIMER_H
#define JS_TIMER_H

#include "js/js.h"
#include "util/detect.h"
#include "core/timer.h"

using v8::Handle;
using v8::Persistent;
using v8::Function;
using v8::Value;
using v8::Arguments;

typedef struct js_timer_t {
	Persistent<Function> callback;
	Handle<Value> *arguments;
} js_timer;
Handle<Value> defSetTimeout(const Arguments &args);
Handle<Value> defClearTimeout(const Arguments &args);
Handle<Value> defSetInterval(const Arguments &args);
CEXPORT void js_timer_unlink(core_timer* timer);
CEXPORT void js_timer_fire(core_timer *timer);
Handle<Value> defClearInterval(const Arguments &args);
#endif
