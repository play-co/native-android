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
#include "js/js_overlay.h"
#include "platform/overlay.h"

using namespace v8;

Handle<Value> defLoadOverlay(const Arguments &args) {
    LOGFN("JS load overlay");
    String::Utf8Value url(args[0]);

    const char *url_str = ToCString(url);
    overlay_load(url_str);
    LOGFN("ENDJS load overlay");
    return Undefined();
}

Handle<Value> defShowOverlay(const Arguments &args) {
    LOGFN("JS show overlay");
    overlay_show();
    LOGFN("end js show overlay");
    return Undefined();
}

Handle<Value> defHideOverlay(const Arguments &args) {
    LOGFN("JS hide overlay");
    overlay_hide();
    LOGFN("endJS hide overlay");
    return Undefined();
}

Handle<Value> defSendOverlayEvent(const Arguments &args) {
    LOGFN("send overlay event");
    String::Utf8Value event(args[0]);
    const char *event_str = ToCString(event);
    overlay_send_event(event_str);
    LOGFN("end send overlay event");
    return Undefined();
}


Handle<ObjectTemplate> js_overlay_get_template() {
    Handle<ObjectTemplate> overlay = ObjectTemplate::New();
    overlay->Set(STRING_CACHE_load, FunctionTemplate::New(defLoadOverlay));
    overlay->Set(STRING_CACHE_show, FunctionTemplate::New(defShowOverlay));
    overlay->Set(STRING_CACHE_hide, FunctionTemplate::New(defHideOverlay));
    overlay->Set(STRING_CACHE_send, FunctionTemplate::New(defSendOverlayEvent));
    return overlay;
}
