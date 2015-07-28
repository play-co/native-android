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
#include "core/types.h"
#include "timestep/timestep_animate.h"
#include "js/js_animate.h"
#include "js/js_timestep_view.h"
#include "js/js_context.h"
#include "core/tealeaf_context.h"

using namespace v8;

// Handle<Value> js_view_wrap_render(const Arguments &args) {

// 	Handle<Object> thiz = args.This();
// 	Handle<Object> style = Handle<Object>::Cast(thiz->Get(STRING_CACHE_style));

// 	if (style.IsEmpty()) { return Undefined(); }
// 	Handle<Value> visible = Handle<Object>::Cast(style->Get(STRING_CACHE_visible));
// 	if (visible.IsEmpty() || !visible->ToBoolean()->Value()) { return Undefined(); }

// 	context_2d *ctx = GET_CONTEXT2D_FROM(args[0]->ToObject());


// }

// Handle<Value> js_view_wrap_tick(const Arguments &args) {

// }


static inline void build_style_frame(anim_frame *frame, Handle<Object> target) {

#define ADD_PROP(const_name, prop)								\
		_ADD_PROP(const_name, prop, false);							\
		_ADD_PROP(const_name, d ## prop, true);

#define _ADD_PROP(const_name, prop, _is_delta) do {				\
		Handle<Value> value = target->Get(String::New(#prop));		\
		if (value->IsNumber()) {									\
			style_prop *p = anim_frame_add_style_prop(frame);		\
			p->name = const_name;									\
			p->target = (double) value->NumberValue();				\
			p->is_delta = _is_delta;								\
		}															\
	} while(0)

    ADD_PROP(X, x);
    ADD_PROP(Y, y);
    ADD_PROP(WIDTH, width);
    ADD_PROP(HEIGHT, height);
    ADD_PROP(R, r);
    ADD_PROP(ANCHOR_X, anchorX);
    ADD_PROP(ANCHOR_Y, anchorY);
    ADD_PROP(OPACITY, opacity);
    ADD_PROP(SCALE, scale);
    ADD_PROP(SCALE_X, scaleX);
    ADD_PROP(SCALE_Y, scaleY);

    frame->type = STYLE_FRAME;
}

static void build_func_frame(anim_frame *frame, Handle<Function> cb) {
    frame->cb = Persistent<Function>::New(cb);
    frame->type = FUNC_FRAME;
}

#define GET_TIMESTEP_ANIMATION(thiz) ( (view_animation*) Local<External>::Cast(thiz->GetInternalField(0))->Value() )

static void build_frame(Handle<Object> target, const Arguments &args, void (*next)(view_animation *, anim_frame *, unsigned int, unsigned int)) {
    LOGFN("build_frame");

    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    anim_frame *frame = anim_frame_get();

    // TODO: what if these defaults change? it probably won't...
    unsigned int duration = 500;
    unsigned int transition = 0;
    if (target->IsFunction()) {
        duration = 0;
        build_func_frame(frame, Handle<Function>::Cast(target));
    } else {
        build_style_frame(frame, target);
    }

    if (!args[1]->IsUndefined()) {
        duration = args[1]->Int32Value();
    }

    if (!args[2]->IsUndefined()) {
        transition = args[2]->Int32Value();
    }

    next(anim, frame, duration, transition);

    LOGFN("end build_frame");
}

Handle<Value> js_animate_now(const Arguments &args) {
    Handle<Object> target = Handle<Object>::Cast(args[0]);
    if (!target->IsUndefined()) {
        build_frame(target, args, view_animation_now);
    }

    return Handle<Object>::Cast(args.This());
}

Handle<Value> js_animate_then(const Arguments &args) {
    Handle<Object> target = Handle<Object>::Cast(args[0]);
    if (!target->IsUndefined()) {
        build_frame(target, args, view_animation_then);
    }

    return Handle<Object>::Cast(args.This());
}

Handle<Value> js_animate_commit(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    view_animation_commit(anim);
    return thiz;
}

Handle<Value> js_animate_clear(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    view_animation_clear(anim);
    return thiz;
}

Handle<Value> js_animate_wait(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    int duration = args[0]->Int32Value();

    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    view_animation_wait(anim, duration);
    return thiz;
}


Handle<Value> js_animate_pause(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    view_animation_pause(anim);
    return thiz;
}

Handle<Value> js_animate_resume(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    view_animation_resume(anim);
    return thiz;
}

Handle<Value> js_animate_is_paused(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    return Boolean::New(anim->is_paused);
}

Handle<Value> js_animate_has_frames(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    view_animation *anim = GET_TIMESTEP_ANIMATION(thiz);
    return Boolean::New((bool) anim->frame_head);
}

static void js_animation_finalize(Persistent<Value> js_anim, void *param) {
    HandleScope scope;

    view_animation *anim = static_cast<view_animation*>( param );
    view_animation_release(anim);

    js_anim.Dispose();
    js_anim.Clear();
}

Handle<Value> js_animate_constructor(const Arguments &args) {
    Handle<Object> thiz = Handle<Object>::Cast(args.This());
    Handle<Object> js_timestep_view = Handle<Object>::Cast(args[0]);

    timestep_view *view = GET_TIMESTEP_VIEW(Handle<Object>::Cast(js_timestep_view->Get(STRING_CACHE___view)));
    view_animation *anim = view_animation_init(view);

    thiz->SetInternalField(0, External::New(anim));
    Persistent<Object> js_anim = Persistent<Object>::New(thiz);
    js_anim.MakeWeak(anim, js_animation_finalize);
    anim->js_anim = js_anim;

    return thiz;
}

void def_animate_add_to_group(Handle<Object> js_anim) {
    LOGFN("def_animate_add_to_group");
    Handle<Function> addToGroup = Handle<Function>::Cast(js_anim->Get(STRING_CACHE__addToGroup));
    if (!addToGroup.IsEmpty() && addToGroup->IsFunction()) {
        Handle<Value> args[] = {js_anim};
        addToGroup->Call(js_anim, 1, args);
    }
    LOGFN("end def_animate_add_to_group");
}

void def_animate_remove_from_group(Handle<Object> js_anim) {
    LOGFN("def_animate_remove_from_group");
    Handle<Function> finish = Handle<Function>::Cast(js_anim->Get(STRING_CACHE__removeFromGroup));
    if (!finish.IsEmpty() && finish->IsFunction()) {
        Handle<Value> args[] = {js_anim};
        finish->Call(js_anim, 1, args);
    }
    LOGFN("end def_animate_remove_from_group");
}

void def_animate_cb(Handle<Object> js_view, Handle<Object> cb, double tt, double t) {
    Handle<Value> args[] = {Number::New(tt), Number::New(t)};
    Handle<Function>::Cast(cb)->Call(js_view, 2, args);
}

Handle<FunctionTemplate> get_animate_class() {
    Handle<FunctionTemplate> animate_class = FunctionTemplate::New();
    animate_class->SetCallHandler(js_animate_constructor);

    Handle<Template> proto = animate_class->PrototypeTemplate();
    animate_class->InstanceTemplate()->SetInternalFieldCount(1);
    proto->Set("now", FunctionTemplate::New(js_animate_now));
    proto->Set("then", FunctionTemplate::New(js_animate_then));
    proto->Set("commit", FunctionTemplate::New(js_animate_commit));
    proto->Set("clear", FunctionTemplate::New(js_animate_clear));
    proto->Set("wait", FunctionTemplate::New(js_animate_wait));
    proto->Set("pause", FunctionTemplate::New(js_animate_pause));
    proto->Set("resume", FunctionTemplate::New(js_animate_resume));
    proto->Set("isPaused", FunctionTemplate::New(js_animate_is_paused));
    proto->Set("hasFrames", FunctionTemplate::New(js_animate_has_frames));

    return animate_class;
}

