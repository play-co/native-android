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
#include "js_textbox.h"
#include "platform/textbox.h"

#include <string.h>

using namespace v8;

const char* types[] = {
    "text",				// TYPE_CLASS_TEXT
    "search",			// TYPE_CLASS_TEXT|TYPE_TEXT_FLAG_AUTO_CORRECT
    "tel",				// TYPE_CLASS_PHONE
    "url",				// TYPE_CLASS_TEXT|TYPE_TEXT_VARIATION_URI
    "email",			// TYPE_CLASS_TEXT|TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    "password",			// TYPE_CLASS_TEXT|TYPE_TEXT_VARIATION_PASSWORD
    "datetime",			// TYPE_CLASS_DATETIME
    "date",				// TYPE_CLASS_DATETIME|TYPE_DATETIME_VARIATION_DATE
    "time",				// TYPE_CLASS_DATETIME|TYPE_DATETIME_VARIATION_TIME
    "number",			// TYPE_CLASS_NUMBER
    "range"				// TYPE_CLASS_NUMBER
};

int types_int[] = {
    // See: http://developer.android.com/reference/android/text/InputType.html for these values
    0x0001,
    0x8001,
    0x0003,
    0x0011,
    0x0021,
    0x0081,
    0x0004,
    0x0014,
    0x0024,
    0x0002,
    0x0002
};

Handle<Value> js_textbox_create(const Arguments& args) {
    int id = -1;
    if(args.Length() >= 5) {
        String::Utf8Value str(args[4]);
        id = textbox_create_init(args[0]->Int32Value(), args[1]->Int32Value(), args[2]->Int32Value(), args[3]->Int32Value(), ToCString(str));
    } else {
        id = textbox_create_new();
    }

    return Integer::New(id);
}

Handle<Value> js_textbox_destroy(const Arguments& args) {
    textbox_destroy(args[0]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_show(const Arguments& args) {
    textbox_show(args[0]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_hide(const Arguments& args) {
    textbox_hide(args[0]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_position(const Arguments& args) {
    textbox_set_position(args[0]->Int32Value(), args[1]->Int32Value(), args[2]->Int32Value(), args[3]->Int32Value(), args[4]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_dimensions(const Arguments& args) {
    textbox_set_dimensions(args[0]->Int32Value(), args[1]->Int32Value(), args[2]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_x(const Arguments& args) {
    textbox_set_x(args[0]->Int32Value(), args[1]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_y(const Arguments& args) {
    textbox_set_y(args[0]->Int32Value(), args[1]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_width(const Arguments& args) {
    textbox_set_width(args[0]->Int32Value(), args[1]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_height(const Arguments& args) {
    textbox_set_height(args[0]->Int32Value(), args[1]->Int32Value());
    return Undefined();
}

Handle<Value> js_textbox_set_value(const Arguments& args) {
    String::Utf8Value value(args[1]);
    textbox_set_value(args[0]->Int32Value(), ToCString(value));
    return Undefined();
}

Handle<Value> js_textbox_set_opacity(const Arguments& args) {
    textbox_set_opacity(args[0]->Int32Value(), (float)(args[1]->NumberValue()));
    return Undefined();
}

Handle<Value> js_textbox_set_type(const Arguments& args) {
    String::Utf8Value value(args[1]);
    const char* val = ToCString(value);
    int type = 1;
    // XXX HACK: there are 11 entries in types and types_int
    for(int i = 0; i < 11; i++) {
        if(strcasestr(val, types[i]) == 0) {
            type = types_int[i];
            break;
        }
    }
    textbox_set_type(args[0]->Int32Value(), type);
    return Undefined();
}

Handle<Value> js_textbox_set_visible(const Arguments& args) {
    textbox_set_visible(args[0]->Int32Value(), args[1]->BooleanValue());
    return Undefined();
}


Handle<Value> js_textbox_get_x(const Arguments& args) {
    return Integer::New(textbox_get_x(args[0]->Int32Value()));
}

Handle<Value> js_textbox_get_y(const Arguments& args) {
    return Integer::New(textbox_get_y(args[0]->Int32Value()));
}

Handle<Value> js_textbox_get_width(const Arguments& args) {
    return Integer::New(textbox_get_width(args[0]->Int32Value()));
}

Handle<Value> js_textbox_get_height(const Arguments& args) {
    return Integer::New(textbox_get_height(args[0]->Int32Value()));
}

Handle<Value> js_textbox_get_value(const Arguments& args) {
    const char *value = textbox_get_value(args[0]->Int32Value());
    Local<String> v8value = String::New(value);
    free((void*)value);
    return v8value;
}

Handle<Value> js_textbox_get_opacity(const Arguments& args) {
    return Number::New(textbox_get_opacity(args[0]->Int32Value()));
}

Handle<Value> js_textbox_get_type(const Arguments& args) {
    return Integer::New(textbox_get_type(args[0]->Int32Value()));
}

Handle<Value> js_textbox_get_visible(const Arguments& args) {
    return Boolean::New(textbox_get_visible(args[0]->Int32Value()));
}


Handle<ObjectTemplate> js_textbox_get_template() {
    // TODO: some day, turn this into a real class instead
    Handle<ObjectTemplate> tmpl = ObjectTemplate::New();

    tmpl->Set(STRING_CACHE_create, FunctionTemplate::New(js_textbox_create));
    tmpl->Set(STRING_CACHE_destroy, FunctionTemplate::New(js_textbox_destroy));
    tmpl->Set(STRING_CACHE_show, FunctionTemplate::New(js_textbox_show));
    tmpl->Set(STRING_CACHE_hide, FunctionTemplate::New(js_textbox_hide));

    tmpl->Set(STRING_CACHE_setPosition, FunctionTemplate::New(js_textbox_set_position));
    tmpl->Set(STRING_CACHE_setDimensions, FunctionTemplate::New(js_textbox_set_dimensions));
    tmpl->Set(STRING_CACHE_setValue, FunctionTemplate::New(js_textbox_set_value));
    tmpl->Set(STRING_CACHE_setOpacity, FunctionTemplate::New(js_textbox_set_opacity));
    tmpl->Set(STRING_CACHE_setType, FunctionTemplate::New(js_textbox_set_type));
    tmpl->Set(STRING_CACHE_setVisible, FunctionTemplate::New(js_textbox_set_visible));

    tmpl->Set(STRING_CACHE_getX, FunctionTemplate::New(js_textbox_get_x));
    tmpl->Set(STRING_CACHE_getY, FunctionTemplate::New(js_textbox_get_y));
    tmpl->Set(STRING_CACHE_getWidth, FunctionTemplate::New(js_textbox_get_width));
    tmpl->Set(STRING_CACHE_getHeight, FunctionTemplate::New(js_textbox_get_height));
    tmpl->Set(STRING_CACHE_getValue, FunctionTemplate::New(js_textbox_get_value));
    tmpl->Set(STRING_CACHE_getOpacity, FunctionTemplate::New(js_textbox_get_opacity));
    tmpl->Set(STRING_CACHE_getType, FunctionTemplate::New(js_textbox_get_type));
    tmpl->Set(STRING_CACHE_getVisible, FunctionTemplate::New(js_textbox_get_visible));

    return tmpl;
}
