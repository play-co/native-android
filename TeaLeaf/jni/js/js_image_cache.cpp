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
#include "js_image_cache.h"
extern "C" {
#include "core/image-cache/include/image_cache.h"
}

using namespace v8;

Handle<Value> js_image_cache_remove(const Arguments &args) {
    LOGFN("in image cache delete");
    HandleScope handleScope;
    String::Utf8Value str(args[0]);
    const char *url = ToCString(str);
    image_cache_remove(url);
    LOGFN("end image cache delete");
    return Undefined();
}

Handle<ObjectTemplate> js_image_cache_get_template() {
    Handle<ObjectTemplate> image_cache = ObjectTemplate::New();
    image_cache->Set(String::New("remove"), FunctionTemplate::New(js_image_cache_remove));

    return image_cache;
}
