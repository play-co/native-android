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

using namespace v8;

Handle<Value> js_camera_get_next_id(const Arguments& args) {
	return Number::New(camera_get_next_id());
}

Handle<ObjectTemplate> js_camera_get_template() {
	Handle<ObjectTemplate> camera = ObjectTemplate::New();
	camera->Set(STRING_CACHE_getNextId, FunctionTemplate::New(js_camera_get_next_id));
	return camera;
}

Handle<Value> js_gallery_get_next_id(const Arguments& args) {
	return Number::New(gallery_get_next_id());
}

Handle<ObjectTemplate> js_gallery_get_template() {
	Handle<ObjectTemplate> gallery = ObjectTemplate::New();
	gallery->Set(STRING_CACHE_getNextId, FunctionTemplate::New(js_gallery_get_next_id));
	return gallery;
}
