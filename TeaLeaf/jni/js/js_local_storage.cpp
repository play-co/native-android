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
#include "js/js_local_storage.h"
#include "platform/local_storage.h"

using namespace v8;

Handle<Value> defLocalStorageSetItem(const Arguments &args) {
    LOGFN("localstorage set");
    String::Utf8Value str(args[0]);
    const char *key = ToCString(str);
    String::Utf8Value str2(args[1]);
    const char *data = ToCString(str2);
    local_storage_set_data(key, data);
    LOGFN("end localstorage set");
    return Undefined();
}

Handle<Value> defLocalStorageGetItem(const Arguments &args) {
    LOGFN("localstorage get");
    String::Utf8Value str(args[0]);
    const char *key = ToCString(str);
    const char *data = local_storage_get_data(key);
    if (data) {
        Local<String> result = String::New(data);
        free((void*)data);
        return result;
    }
    LOGFN("end localstorage get");
    return Null();
}

Handle<Value> defLocalStorageRemoveItem(const Arguments &args) {
    LOGFN("localstorage remove");
    String::Utf8Value str(args[0]);
    const char *key = ToCString(str);
    local_storage_remove_data(key);
    LOGFN("end localstorage remove");
    return Undefined();
}

Handle<Value> defLocalStorageClear(const Arguments &args) {
    LOGFN("localstorage clear");
    local_storage_clear();
    LOGFN("end localstorage clear");
    return Undefined();

}

Handle<ObjectTemplate> js_local_storage_get_template() {
    Handle<ObjectTemplate> localStorage = ObjectTemplate::New();

    localStorage->Set(STRING_CACHE_setItem, FunctionTemplate::New(defLocalStorageSetItem));
    localStorage->Set(STRING_CACHE_getItem, FunctionTemplate::New(defLocalStorageGetItem));
    localStorage->Set(STRING_CACHE_removeItem, FunctionTemplate::New(defLocalStorageRemoveItem));
    localStorage->Set(STRING_CACHE_clear, FunctionTemplate::New(defLocalStorageClear));

    return localStorage;
}
