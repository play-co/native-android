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

#include "js_timestep_image_map_template.gen.h"
#include "js/js_timestep_image_map.h"

#include "core/timestep/timestep_image_map.h"





v8::Handle<v8::Value> timestep_image_map_get_x(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get x");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->x;
	//LOG("done in timestep_image_map get x");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_x(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set x");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->x = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set x");
}



v8::Handle<v8::Value> timestep_image_map_get_y(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get y");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->y;
	//LOG("done in timestep_image_map get y");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_y(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set y");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->y = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set y");
}



v8::Handle<v8::Value> timestep_image_map_get_width(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get width");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->width;
	//LOG("done in timestep_image_map get width");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_width(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set width");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->width = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set width");
}



v8::Handle<v8::Value> timestep_image_map_get_height(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get height");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->height;
	//LOG("done in timestep_image_map get height");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_height(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set height");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->height = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set height");
}



v8::Handle<v8::Value> timestep_image_map_get_marginTop(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get marginTop");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->margin_top;
	//LOG("done in timestep_image_map get marginTop");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_marginTop(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set marginTop");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->margin_top = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set marginTop");
}



v8::Handle<v8::Value> timestep_image_map_get_marginRight(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get marginRight");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->margin_right;
	//LOG("done in timestep_image_map get marginRight");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_marginRight(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set marginRight");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->margin_right = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set marginRight");
}



v8::Handle<v8::Value> timestep_image_map_get_marginBottom(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get marginBottom");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->margin_bottom;
	//LOG("done in timestep_image_map get marginBottom");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_marginBottom(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set marginBottom");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->margin_bottom = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set marginBottom");
}



v8::Handle<v8::Value> timestep_image_map_get_marginLeft(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get marginLeft");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->margin_left;
	//LOG("done in timestep_image_map get marginLeft");
	return v8::Integer::New(prop);
	
}



void timestep_image_map_set_marginLeft(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set marginLeft");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->margin_left = value->ToInteger()->Value();
	
	//LOG("done in timestep_image_map set marginLeft");
}



v8::Handle<v8::Value> timestep_image_map_get_url(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map get url");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	char* prop = obj->url;
	//LOG("done in timestep_image_map get url");
	return v8::String::New(prop);
	
}



void timestep_image_map_set_url(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_image_map set url");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_image_map *obj = (timestep_image_map*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	if (obj->url) {
	free(obj->url);
}
v8::String::Utf8Value s(value);
const char *str = ToCString(s);
obj->url = strdup(str);

	
	//LOG("done in timestep_image_map set url");
}




v8::Handle<v8::FunctionTemplate> js_timestep_image_map_get_template() {
	v8::Handle<v8::FunctionTemplate> templ = v8::FunctionTemplate::New();
	v8::Handle<v8::ObjectTemplate> timestep_image_map = templ->InstanceTemplate();
	timestep_image_map->SetInternalFieldCount(2);
	
	v8::Handle<v8::Value> def_timestep_image_map_constructor(const v8::Arguments &args);
	templ->SetCallHandler(def_timestep_image_map_constructor);	
	
	
	

	
v8::Handle<v8::Value> timestep_image_map_get_x(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_x(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("x"), timestep_image_map_get_x, timestep_image_map_set_x);
v8::Handle<v8::Value> timestep_image_map_get_y(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_y(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("y"), timestep_image_map_get_y, timestep_image_map_set_y);
v8::Handle<v8::Value> timestep_image_map_get_width(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_width(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("width"), timestep_image_map_get_width, timestep_image_map_set_width);
v8::Handle<v8::Value> timestep_image_map_get_height(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_height(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("height"), timestep_image_map_get_height, timestep_image_map_set_height);
v8::Handle<v8::Value> timestep_image_map_get_marginTop(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_marginTop(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("marginTop"), timestep_image_map_get_marginTop, timestep_image_map_set_marginTop);
v8::Handle<v8::Value> timestep_image_map_get_marginRight(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_marginRight(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("marginRight"), timestep_image_map_get_marginRight, timestep_image_map_set_marginRight);
v8::Handle<v8::Value> timestep_image_map_get_marginBottom(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_marginBottom(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("marginBottom"), timestep_image_map_get_marginBottom, timestep_image_map_set_marginBottom);
v8::Handle<v8::Value> timestep_image_map_get_marginLeft(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_marginLeft(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("marginLeft"), timestep_image_map_get_marginLeft, timestep_image_map_set_marginLeft);
v8::Handle<v8::Value> timestep_image_map_get_url(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_image_map_set_url(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_image_map->SetAccessor(v8::String::New("url"), timestep_image_map_get_url, timestep_image_map_set_url);

	

	return templ;
}
