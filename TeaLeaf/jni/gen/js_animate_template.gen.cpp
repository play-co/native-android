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

#include "js_animate_template.gen.h"
#include "js/js_animate.h"






v8::Handle<v8::FunctionTemplate> js_animate_get_template() {
	v8::Handle<v8::FunctionTemplate> templ = v8::FunctionTemplate::New();
	v8::Handle<v8::ObjectTemplate> animate = templ->InstanceTemplate();
	animate->SetInternalFieldCount(2);
	
	v8::Handle<v8::Value> def_animate_constructor(const v8::Arguments &args);
	templ->SetCallHandler(def_animate_constructor);	
	
	
	
v8::Handle<v8::Value> def_animate_now(const v8::Arguments &args);
animate->Set(v8::String::New("now"), v8::FunctionTemplate::New(def_animate_now));
v8::Handle<v8::Value> def_animate_then(const v8::Arguments &args);
animate->Set(v8::String::New("then"), v8::FunctionTemplate::New(def_animate_then));
v8::Handle<v8::Value> def_animate_commit(const v8::Arguments &args);
animate->Set(v8::String::New("commit"), v8::FunctionTemplate::New(def_animate_commit));
v8::Handle<v8::Value> def_animate_clear(const v8::Arguments &args);
animate->Set(v8::String::New("clear"), v8::FunctionTemplate::New(def_animate_clear));
v8::Handle<v8::Value> def_animate_wait(const v8::Arguments &args);
animate->Set(v8::String::New("wait"), v8::FunctionTemplate::New(def_animate_wait));
v8::Handle<v8::Value> def_animate_pause(const v8::Arguments &args);
animate->Set(v8::String::New("pause"), v8::FunctionTemplate::New(def_animate_pause));
v8::Handle<v8::Value> def_animate_resume(const v8::Arguments &args);
animate->Set(v8::String::New("resume"), v8::FunctionTemplate::New(def_animate_resume));
v8::Handle<v8::Value> def_animate_isPaused(const v8::Arguments &args);
animate->Set(v8::String::New("isPaused"), v8::FunctionTemplate::New(def_animate_isPaused));
v8::Handle<v8::Value> def_animate_hasFrames(const v8::Arguments &args);
animate->Set(v8::String::New("hasFrames"), v8::FunctionTemplate::New(def_animate_hasFrames));

	

	

	return templ;
}
