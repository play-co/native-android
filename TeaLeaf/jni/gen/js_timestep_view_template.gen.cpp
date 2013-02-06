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

#include "js_timestep_view_template.gen.h"
#include "js/js_timestep_view.h"

#include "core/timestep/timestep_view.h"

#include "core/rgba.h"





v8::Handle<v8::Value> timestep_view_get_x(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get x");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->x;
	//LOG("done in timestep_view get x");
	return v8::Number::New(prop);
	
}



void timestep_view_set_x(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set x");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->x = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set x");
}



v8::Handle<v8::Value> timestep_view_get_y(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get y");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->y;
	//LOG("done in timestep_view get y");
	return v8::Number::New(prop);
	
}



void timestep_view_set_y(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set y");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->y = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set y");
}



v8::Handle<v8::Value> timestep_view_get_offsetX(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get offsetX");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->offset_x;
	//LOG("done in timestep_view get offsetX");
	return v8::Number::New(prop);
	
}



void timestep_view_set_offsetX(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set offsetX");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->offset_x = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set offsetX");
}



v8::Handle<v8::Value> timestep_view_get_offsetY(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get offsetY");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->offset_y;
	//LOG("done in timestep_view get offsetY");
	return v8::Number::New(prop);
	
}



void timestep_view_set_offsetY(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set offsetY");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->offset_y = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set offsetY");
}















v8::Handle<v8::Value> timestep_view_get_r(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get r");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->r;
	//LOG("done in timestep_view get r");
	return v8::Number::New(prop);
	
}



void timestep_view_set_r(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set r");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->r = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set r");
}



v8::Handle<v8::Value> timestep_view_get_flipX(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get flipX");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->flip_x;
	//LOG("done in timestep_view get flipX");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set_flipX(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set flipX");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->flip_x = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set flipX");
}



v8::Handle<v8::Value> timestep_view_get_flipY(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get flipY");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->flip_y;
	//LOG("done in timestep_view get flipY");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set_flipY(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set flipY");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->flip_y = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set flipY");
}



v8::Handle<v8::Value> timestep_view_get_anchorX(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get anchorX");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->anchor_x;
	//LOG("done in timestep_view get anchorX");
	return v8::Number::New(prop);
	
}



void timestep_view_set_anchorX(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set anchorX");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->anchor_x = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set anchorX");
}



v8::Handle<v8::Value> timestep_view_get_anchorY(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get anchorY");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->anchor_y;
	//LOG("done in timestep_view get anchorY");
	return v8::Number::New(prop);
	
}



void timestep_view_set_anchorY(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set anchorY");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->anchor_y = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set anchorY");
}



v8::Handle<v8::Value> timestep_view_get_opacity(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get opacity");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->opacity;
	//LOG("done in timestep_view get opacity");
	return v8::Number::New(prop);
	
}






v8::Handle<v8::Value> timestep_view_get_scale(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get scale");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	double prop = obj->scale;
	//LOG("done in timestep_view get scale");
	return v8::Number::New(prop);
	
}



void timestep_view_set_scale(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set scale");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->scale = value->ToNumber()->Value();
	
	//LOG("done in timestep_view set scale");
}



v8::Handle<v8::Value> timestep_view_get_clip(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get clip");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->clip;
	//LOG("done in timestep_view get clip");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set_clip(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set clip");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->clip = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set clip");
}



v8::Handle<v8::Value> timestep_view_get_backgroundColor(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get backgroundColor");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	rgba prop = obj->background_color;
char buf[64];
rgba_to_string(&prop, buf);
return v8::String::New(buf);

	
}



void timestep_view_set_backgroundColor(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set backgroundColor");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	if (value->IsString()) {
	String::Utf8Value s(value);
	const char *str = ToCString(s);
	rgba color;
	rgba_parse(&color, str);
	obj->background_color = color;
}

	
	//LOG("done in timestep_view set backgroundColor");
}



v8::Handle<v8::Value> timestep_view_get_visible(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get visible");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->visible;
	//LOG("done in timestep_view get visible");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set_visible(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set visible");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->visible = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set visible");
}



v8::Handle<v8::Value> timestep_view_get_hasJSRender(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get hasJSRender");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->has_jsrender;
	//LOG("done in timestep_view get hasJSRender");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set_hasJSRender(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set hasJSRender");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->has_jsrender = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set hasJSRender");
}



v8::Handle<v8::Value> timestep_view_get_hasJSTick(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get hasJSTick");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->has_jstick;
	//LOG("done in timestep_view get hasJSTick");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set_hasJSTick(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set hasJSTick");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->has_jstick = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set hasJSTick");
}



v8::Handle<v8::Value> timestep_view_get___firstRender(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get __firstRender");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	bool prop = obj->__first_render;
	//LOG("done in timestep_view get __firstRender");
	return v8::Boolean::New(prop);
	
}



void timestep_view_set___firstRender(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set __firstRender");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->__first_render = value->ToBoolean()->Value();
	
	//LOG("done in timestep_view set __firstRender");
}



v8::Handle<v8::Value> timestep_view_get_zIndex(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get zIndex");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->z_index;
	//LOG("done in timestep_view get zIndex");
	return v8::Integer::New(prop);
	
}






v8::Handle<v8::Value> timestep_view_get_filterColor(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get filterColor");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	rgba prop = obj->filter_color;
char buf[64];
rgba_to_string(&prop, buf);
return v8::String::New(buf);

	
}



void timestep_view_set_filterColor(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set filterColor");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	if (value->IsString()) {
	String::Utf8Value s(value);
	const char *str = ToCString(s);
	rgba color;
	rgba_parse(&color, str);
	obj->filter_color = color;
}

	
	//LOG("done in timestep_view set filterColor");
}



v8::Handle<v8::Value> timestep_view_get_filterType(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
	//LOG("in timestep_view get filterType");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	int prop = obj->filter_type;
	//LOG("done in timestep_view get filterType");
	return v8::Integer::New(prop);
	
}



void timestep_view_set_filterType(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
	//LOG("in timestep_view set filterType");
	v8::Local<v8::Object> thiz = info.Holder();
	timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();
	
	obj->filter_type = value->ToInteger()->Value();
	
	//LOG("done in timestep_view set filterType");
}




v8::Handle<v8::FunctionTemplate> js_timestep_view_get_template() {
	v8::Handle<v8::FunctionTemplate> templ = v8::FunctionTemplate::New();
	v8::Handle<v8::ObjectTemplate> timestep_view = templ->InstanceTemplate();
	timestep_view->SetInternalFieldCount(2);
	
	v8::Handle<v8::Value> def_timestep_view_constructor(const v8::Arguments &args);
	templ->SetCallHandler(def_timestep_view_constructor);	
	
	
	
v8::Handle<v8::Value> def_timestep_view_addSubview(const v8::Arguments &args);
timestep_view->Set(v8::String::New("addSubview"), v8::FunctionTemplate::New(def_timestep_view_addSubview));
v8::Handle<v8::Value> def_timestep_view_removeSubview(const v8::Arguments &args);
timestep_view->Set(v8::String::New("removeSubview"), v8::FunctionTemplate::New(def_timestep_view_removeSubview));
v8::Handle<v8::Value> def_timestep_view_getSuperview(const v8::Arguments &args);
timestep_view->Set(v8::String::New("getSuperview"), v8::FunctionTemplate::New(def_timestep_view_getSuperview));
v8::Handle<v8::Value> def_timestep_view_getSubviews(const v8::Arguments &args);
timestep_view->Set(v8::String::New("getSubviews"), v8::FunctionTemplate::New(def_timestep_view_getSubviews));
v8::Handle<v8::Value> def_timestep_view_wrapRender(const v8::Arguments &args);
timestep_view->Set(v8::String::New("wrapRender"), v8::FunctionTemplate::New(def_timestep_view_wrapRender));
v8::Handle<v8::Value> def_timestep_view_wrapTick(const v8::Arguments &args);
timestep_view->Set(v8::String::New("wrapTick"), v8::FunctionTemplate::New(def_timestep_view_wrapTick));
v8::Handle<v8::Value> def_timestep_view_localizePoint(const v8::Arguments &args);
timestep_view->Set(v8::String::New("localizePoint"), v8::FunctionTemplate::New(def_timestep_view_localizePoint));

	
v8::Handle<v8::Value> timestep_view_get_width(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_width(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("width"), timestep_view_get_width, timestep_view_set_width);
v8::Handle<v8::Value> timestep_view_get_height(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_height(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("height"), timestep_view_get_height, timestep_view_set_height);
v8::Handle<v8::Value> timestep_view_get_x(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_x(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("x"), timestep_view_get_x, timestep_view_set_x);
v8::Handle<v8::Value> timestep_view_get_y(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_y(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("y"), timestep_view_get_y, timestep_view_set_y);
v8::Handle<v8::Value> timestep_view_get_offsetX(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_offsetX(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("offsetX"), timestep_view_get_offsetX, timestep_view_set_offsetX);
v8::Handle<v8::Value> timestep_view_get_offsetY(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_offsetY(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("offsetY"), timestep_view_get_offsetY, timestep_view_set_offsetY);
v8::Handle<v8::Value> timestep_view_get_widthPercent(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_widthPercent(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("widthPercent"), timestep_view_get_widthPercent, timestep_view_set_widthPercent);
v8::Handle<v8::Value> timestep_view_get_heightPercent(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_heightPercent(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("heightPercent"), timestep_view_get_heightPercent, timestep_view_set_heightPercent);
v8::Handle<v8::Value> timestep_view_get_r(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_r(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("r"), timestep_view_get_r, timestep_view_set_r);
v8::Handle<v8::Value> timestep_view_get_flipX(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_flipX(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("flipX"), timestep_view_get_flipX, timestep_view_set_flipX);
v8::Handle<v8::Value> timestep_view_get_flipY(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_flipY(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("flipY"), timestep_view_get_flipY, timestep_view_set_flipY);
v8::Handle<v8::Value> timestep_view_get_anchorX(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_anchorX(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("anchorX"), timestep_view_get_anchorX, timestep_view_set_anchorX);
v8::Handle<v8::Value> timestep_view_get_anchorY(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_anchorY(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("anchorY"), timestep_view_get_anchorY, timestep_view_set_anchorY);
v8::Handle<v8::Value> timestep_view_get_opacity(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_opacity(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("opacity"), timestep_view_get_opacity, timestep_view_set_opacity);
v8::Handle<v8::Value> timestep_view_get_scale(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_scale(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("scale"), timestep_view_get_scale, timestep_view_set_scale);
v8::Handle<v8::Value> timestep_view_get_clip(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_clip(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("clip"), timestep_view_get_clip, timestep_view_set_clip);
v8::Handle<v8::Value> timestep_view_get_backgroundColor(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_backgroundColor(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("backgroundColor"), timestep_view_get_backgroundColor, timestep_view_set_backgroundColor);
v8::Handle<v8::Value> timestep_view_get_visible(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_visible(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("visible"), timestep_view_get_visible, timestep_view_set_visible);
v8::Handle<v8::Value> timestep_view_get_hasJSRender(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_hasJSRender(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("hasJSRender"), timestep_view_get_hasJSRender, timestep_view_set_hasJSRender);
v8::Handle<v8::Value> timestep_view_get_hasJSTick(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_hasJSTick(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("hasJSTick"), timestep_view_get_hasJSTick, timestep_view_set_hasJSTick);
v8::Handle<v8::Value> timestep_view_get___firstRender(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set___firstRender(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("__firstRender"), timestep_view_get___firstRender, timestep_view_set___firstRender);
v8::Handle<v8::Value> timestep_view_get_zIndex(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_zIndex(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("zIndex"), timestep_view_get_zIndex, timestep_view_set_zIndex);
v8::Handle<v8::Value> timestep_view_get_filterColor(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_filterColor(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("filterColor"), timestep_view_get_filterColor, timestep_view_set_filterColor);
v8::Handle<v8::Value> timestep_view_get_filterType(v8::Local<v8::String> property, const v8::AccessorInfo &info);

void timestep_view_set_filterType(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info);

timestep_view->SetAccessor(v8::String::New("filterType"), timestep_view_get_filterType, timestep_view_set_filterType);

	

	return templ;
}
