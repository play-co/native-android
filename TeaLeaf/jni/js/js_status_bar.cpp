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

#include "js/js_status_bar.h"
#include "platform/status_bar.h"

using namespace v8;

Handle<Value> js_status_bar_show(const Arguments &args) {
    status_bar_show();
    return Undefined();
}

Handle<Value> js_status_bar_hide(const Arguments &args) {
    status_bar_hide();
    return Undefined();
}

Handle<ObjectTemplate> js_status_bar_get_template() {
    Handle<ObjectTemplate> status_bar = ObjectTemplate::New();
    status_bar->Set(STRING_CACHE_show_status_bar, FunctionTemplate::New(js_status_bar_show));
    status_bar->Set(STRING_CACHE_hide_status_bar, FunctionTemplate::New(js_status_bar_hide));
    status_bar->Set(STRING_CACHE_status_bar_height, Integer::New(status_bar_get_height()), ReadOnly);
    return status_bar;
}

