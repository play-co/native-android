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
#include "js/js_input_prompt.h"
#include "platform/input_prompt.h"

using namespace v8;

Handle<Value> js_input_prompt_show(const Arguments &args) {
	String::Utf8Value title_str(args[0]);
	String::Utf8Value message_str(args[1]);
	String::Utf8Value value_str(args[2]);
	bool auto_show_keyboard = args[3]->BooleanValue();
	bool is_password = args[4]->BooleanValue();
	
	const char *title = ToCString(title_str);
	const char *message = ToCString(message_str);
	const char *value = ToCString(value_str);
	
	int id = input_prompt_show(title, message, value, auto_show_keyboard, is_password);
	return Number::New(id);
}

Handle<Value> js_input_prompt_show_soft_keyboard(const Arguments &args) {
	String::Utf8Value curr_val_str(args[0]);
	String::Utf8Value hint_str(args[1]);
	bool has_backward = args[2]->BooleanValue();
	bool has_forward = args[3]->BooleanValue();
	String::Utf8Value input_type_str(args[4]);
	int max_length = args[5]->Int32Value();

	const char *curr_val = ToCString(curr_val_str);
	const char *hint = ToCString(hint_str);
	const char *input_type = ToCString(input_type_str);

    input_prompt_show_soft_keyboard(curr_val, hint, has_backward, has_forward, input_type, max_length);
    return Undefined();
}

Handle<Value> js_input_prompt_hide_soft_keyboard(const Arguments &args) {
    input_prompt_hide_soft_keyboard();
    return Undefined();
}

Handle<Value> js_input_prompt_show_status_bar(const Arguments &args) {
    input_prompt_show_status_bar();
    return Undefined();
}

Handle<Value> js_input_prompt_hide_status_bar(const Arguments &args) {
    input_prompt_hide_status_bar();
    return Undefined();
}

Handle<ObjectTemplate> js_input_prompt_get_template() {
	Handle<ObjectTemplate> input = ObjectTemplate::New();
	input->Set(STRING_CACHE_show, FunctionTemplate::New(js_input_prompt_show));
    input->Set(STRING_CACHE_show_soft_keyboard, FunctionTemplate::New(js_input_prompt_show_soft_keyboard));
    input->Set(STRING_CACHE_hide_soft_keyboard, FunctionTemplate::New(js_input_prompt_hide_soft_keyboard));
	input->Set(STRING_CACHE_show_status_bar, FunctionTemplate::New(js_input_prompt_show_status_bar));
	input->Set(STRING_CACHE_hide_status_bar, FunctionTemplate::New(js_input_prompt_hide_status_bar));
	return input;
}

