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
#include "js/js_plugins.h"
#include "platform/plugins.h"

using namespace v8;


Handle<Value> js_plugins_send_event(const Arguments& args) {
	LOGFN("plugins send event");
	if (args[0]->IsString() && args[1]->IsString() && args[2]->IsString()) {
		String::Utf8Value str_plugin_class(args[0]->ToString());
		String::Utf8Value str_plugin_method(args[1]->ToString());
		String::Utf8Value str_data(args[2]->ToString());

		const char* plugin_class = ToCString(str_plugin_class);
		const char* plugin_method = ToCString(str_plugin_method);
		const char* data = ToCString(str_data);
		plugins_send_event(plugin_class, plugin_method, data);
	} else {
		LOG("{plugins} WARNING: send event should be called with 3 string arguments");
	}

	LOGFN("end plugins send event");
	return Undefined();
}


Handle<ObjectTemplate> js_plugins_get_template() {
	Handle<ObjectTemplate> actions = ObjectTemplate::New();
	actions->Set(STRING_CACHE_sendEvent, FunctionTemplate::New(js_plugins_send_event));
	return actions;
}

