/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.
 
 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.
 
 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
#ifndef JS_GL_H
#define JS_GL_H

#include "js/js.h"

extern "C" {
	#include "core/tealeaf_context.h"
}
using v8::Handle;
using v8::Value;
using v8::Arguments;
using v8::Local;
using v8::ObjectTemplate;
using v8::External;

#define GET_CONTEXT2D() (static_cast<context_2d*>(Local<External>(Local<External>::Cast(args.This()->GetInternalField(0)))->Value()))
#define GET_CONTEXT2D_FROM(obj) (static_cast<context_2d*>(Local<External>(Local<External>::Cast(obj->GetInternalField(0)))->Value()))

Handle<Value> context_2d_class_ctor(const Arguments& args);
Handle<Value> defFlushImages(const Arguments& args);
Handle<Value> defLoadImage(const Arguments& args);
Handle<Value> defNewTexture(const Arguments& args);
Handle<Value> defDestroyImage(const Arguments& args);
Handle<Value> defFillTextBitmap(const Arguments &args);

Handle<ObjectTemplate> js_gl_get_template();
Handle<Value> js_gl_delete_textures(const Arguments& args);

#endif
