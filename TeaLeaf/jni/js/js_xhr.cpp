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
#include "js/js_xhr.h"
#include "platform/xhr.h"
#include "stdio.h"

using namespace v8;

Handle<Value> xhr_send(const Arguments &args) {
    LOGFN("calling xhr send");
    String::Utf8Value arg0(args[0]);
    String::Utf8Value arg1(args[1]);

    const char *method = ToCString(arg0);
    const char *url = ToCString(arg1);

    bool async = args[2]->BooleanValue();
    const char *data = NULL;
    String::Utf8Value arg3(args[3]);
    data = ToCString(arg3);

    int state = args[4]->Int32Value();

    int id = args[5]->Int32Value();

    request_header *headers = NULL;
    if(!args[6].IsEmpty() && !args[6]->IsUndefined() && !args[6]->IsNull()) {
        Local<Object> requestHeadersObj = args[6]->ToObject();
        Local<Array> requestHeaderNames = requestHeadersObj->GetPropertyNames();
        int length = requestHeaderNames->Get(STRING_CACHE_length)->Int32Value();

        for (int i = 0; i < length; i++) {
            request_header * rh = (request_header*) malloc(sizeof(request_header));
            Local<Value> prop = requestHeaderNames->Get(i);
            String::Utf8Value header(prop);
            String::Utf8Value value(requestHeadersObj->Get(prop));
            int hLen = strlen(ToCString(header)) + 1;
            rh->header = (char *) malloc(hLen);
            strlcpy(rh->header, ToCString(header), hLen);
            int vLen = strlen(ToCString(value)) + 1;
            rh->value = (char *) malloc(vLen);
            strlcpy(rh->value, ToCString(value), vLen);
            HASH_ADD(hh, headers, header, strlen(rh->header)+1, rh);
        }
    }

    xhr req;
    req.id = id;
    req.method = method;
    req.url = url;
    req.data = data;
    req.async = async;
    req.state = state;
    req.request_headers = headers;
    xhr_send(&req);

    if(headers != NULL) {
        request_header *currentHeader, *tmp;

        HASH_ITER(hh, headers, currentHeader, tmp) {
            HASH_DEL(headers, currentHeader);
            free(currentHeader->header);
            free(currentHeader->value);
            free(currentHeader);
        }
    }

    LOGFN("end xhr send");
    return Undefined();
}

Handle<ObjectTemplate> js_xhr_get_template() {
    Handle<ObjectTemplate> xhr = ObjectTemplate::New();
    xhr->Set(STRING_CACHE_send, FunctionTemplate::New(xhr_send));
    return xhr;
}
