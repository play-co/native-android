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
