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
#ifndef JS_H
#define JS_H
#include "v8/v8.h"
#include "core/core_js.h"

#include "platform/platform.h"

#include "js/js_string_cache.h"

#define JS_OBJECT_WRAPPER v8::Handle<v8::Object>
#define PERSISTENT_JS_OBJECT_WRAPPER v8::Persistent<v8::Object>

void js_object_wrapper_init(PERSISTENT_JS_OBJECT_WRAPPER *obj);
void js_object_wrapper_root(PERSISTENT_JS_OBJECT_WRAPPER *obj, JS_OBJECT_WRAPPER target);
void js_object_wrapper_delete(PERSISTENT_JS_OBJECT_WRAPPER *obj);

v8::Handle<v8::Value> log(const v8::Arguments& args);

const char* ToCString(const v8::String::Utf8Value& value);
void ReportException(v8::TryCatch* try_catch);
v8::Handle<v8::Value> ExecuteString(v8::Handle<v8::String> source,
                   v8::Handle<v8::Value> name,
                   bool report_exceptions);

void dispatchData(JNIEnv *env, jint id, jstring data);
void dispatchEvent(jstring event);

bool js_init_isolate();

v8::Handle<v8::Context> getContext();
v8::Isolate *getIsolate();

#endif
