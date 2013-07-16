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
#ifndef JS_OVERLAY_H
#define JS_OVERLAY_H

#include "js/js.h"
using v8::Handle;
using v8::ObjectTemplate;

void js_overlay_load(const char *url);
void js_overlay_show();
void js_overlay_hide();
void js_overlay_send_event(const char *event);
void js_overlay_on_event(const char *event);
void js_overlay_dispatch_events();
Handle<ObjectTemplate> js_overlay_get_template();

#endif
