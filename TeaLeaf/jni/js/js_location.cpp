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
#include "js_location.h"
#include "platform/location_manager.h"

using namespace v8;

static Persistent<String> m_location;

Handle<Value> jsGetLocation(Local<String> name, const AccessorInfo &info) {

    return m_location;
}

static void set_location(Handle<String> location) {
    m_location.Dispose();
    m_location = Persistent<String>::New(location);

    String::Utf8Value str(m_location);
    const char *utf8_location = ToCString(str);

    location_manager_set_location(utf8_location);

    LOG("{location} Set to %s", utf8_location);
}

void jsSetLocation(Local<String> name, Local<Value> value, const AccessorInfo &info) {
    set_location(value->ToString());
}

Handle<Value> native_set_location(const Arguments &args) {
    LOGFN("in native set location");

    if (args.Length() >= 1 && args[0]->IsString()) {
        set_location(args[0]->ToString());
    }

    LOGFN("end native set location");
    return Undefined();
}

void native_initialize_location(const char *uri) {
    m_location = Persistent<String>::New(String::New(uri));
}

