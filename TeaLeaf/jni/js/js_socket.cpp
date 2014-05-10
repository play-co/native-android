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
#include "js/js_socket.h"

#include "platform/socket.h"


using namespace v8;

Handle<Value> js_socket_send(const Arguments &args) {
    LOGFN("socket send");
    int id = args.This()->Get(STRING_CACHE___id)->Int32Value();
    String::Utf8Value data(args[0]);
    const char *data_str = ToCString(data);
    socket_send(id, data_str);
    LOGFN("end socket send");
    return Undefined();
}

Handle<Value> js_socket_close(const Arguments &args) {
    LOGFN("socket close");
    int id = args.This()->Get(STRING_CACHE___id)->Int32Value();
    socket_close(id);
    LOGFN("end socket close");
    return Undefined();
}

Handle<ObjectTemplate> get_socket_template() {
    Handle<ObjectTemplate> socket = ObjectTemplate::New();
    socket->Set(STRING_CACHE_send, FunctionTemplate::New(js_socket_send));
    socket->Set(STRING_CACHE_close, FunctionTemplate::New(js_socket_close));
    socket->Set(STRING_CACHE_onConnect, FunctionTemplate::New(js_socket_default_callback));
    socket->Set(STRING_CACHE_onRead, FunctionTemplate::New(js_socket_default_callback));
    socket->Set(STRING_CACHE_onClose, FunctionTemplate::New(js_socket_default_callback));
    return socket;
}

Handle<Value> js_socket_ctor(const Arguments &args) {
    String::Utf8Value host(args[0]);
    const char *host_str = ToCString(host);
    int port = args[1]->Int32Value();

    int id = socket_create(host_str, port);

    Handle<Object> socket = get_socket_template()->NewInstance();
    socket->Set(STRING_CACHE___id, Number::New(id));
    return socket;
}

Handle<Value> js_socket_default_callback(const Arguments &args) {
    return Undefined();
}
