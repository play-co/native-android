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
#include "js_photo.h"
#include "platform/photo.h"
#include "qr/adapter/QRCodeProcessor.h"

using namespace v8;

Handle<Value> js_camera_get_photo(const Arguments& args) {
    String::Utf8Value str(args[0]);
    int width = args[1]->Int32Value();
    int height = args[2]->Int32Value();
    int crop = args[3]->Int32Value();
    const char *cstr = ToCString(str);
    return Number::New(camera_get_photo(cstr, width, height, crop));
}

Handle<Value> js_camera_process_qr(const Arguments& args) {
    String::Utf8Value str(args[0]);
    const char *cstr = ToCString(str);

    char text[512];
    qr_process_base64_image(cstr, text);

    return String::New(text);
}

Handle<Value> js_camera_encode_qr(const Arguments& args) {
    String::Utf8Value str(args[0]);
    const char *cstr = ToCString(str);

    int width, height;
    char *b64image = qr_generate_base64_image(cstr, &width, &height);

    return String::New(b64image);
}

Handle<Value> js_camera_get_next_id(const Arguments& args) {
    return Number::New(camera_get_next_id());
}

Handle<ObjectTemplate> js_camera_get_template() {
    Handle<ObjectTemplate> camera = ObjectTemplate::New();
    camera->Set(STRING_CACHE_getNextId, FunctionTemplate::New(js_camera_get_next_id));
    camera->Set(STRING_CACHE_getPhoto, FunctionTemplate::New(js_camera_get_photo));
    camera->Set(STRING_CACHE_processQR, FunctionTemplate::New(js_camera_process_qr));
    camera->Set(STRING_CACHE_encodeQR, FunctionTemplate::New(js_camera_encode_qr));
    return camera;
}

Handle<Value> js_gallery_get_photo(const Arguments& args) {
    String::Utf8Value str(args[0]);
    int width = args[1]->Int32Value();
    int height = args[2]->Int32Value();
    int crop = args[3]->Int32Value();
    const char *cstr = ToCString(str);
    return Number::New(gallery_get_photo(cstr, width, height, crop));
}

Handle<Value> js_gallery_get_next_id(const Arguments& args) {
    return Number::New(gallery_get_next_id());
}

Handle<ObjectTemplate> js_gallery_get_template() {
    Handle<ObjectTemplate> gallery = ObjectTemplate::New();
    gallery->Set(STRING_CACHE_getNextId, FunctionTemplate::New(js_gallery_get_next_id));
    gallery->Set(STRING_CACHE_getPhoto, FunctionTemplate::New(js_gallery_get_photo));
    return gallery;
}
