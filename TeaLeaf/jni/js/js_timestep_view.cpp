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
#include "js/js_timestep_view.h"
#include "js/js_animate.h"
#include "js/js_image_map.h"
#include "js/js_timestep_events.h"
#include "timestep/timestep_view.h"
#include "timestep/timestep_image_map.h"
#include "js/js_context.h"
#include "core/log.h"
#include <math.h>

#include "gen/js_timestep_image_map_template.gen.h"
#include "gen/js_timestep_view_template.gen.h"
using namespace v8;

#define GET_IMAGE_MAP(thiz) (timestep_image_map*) Local<External>::Cast(thiz->GetInternalField(0))->Value()
#define GET_TIMESTEP_VIEW(thiz) (timestep_view*) Local<External>::Cast(thiz->GetInternalField(0))->Value()
#define GET_JS_WRAPPER_VIEW(v) Handle<Object>::Cast(v->GetInternalField(1))

//#define VIEW_LEAKS /* Uncomment this to print out allocated view count to get visibility into JS leaks */
#ifdef VIEW_LEAKS
static int backing_count = 0;
static int frontend_count = 0;
#endif

// View front-end finalizer
static void cb_js_finalize(Persistent<Value> ctx, void *param) {
    HandleScope handle_scope;

    // Get object _view reference to backing from front-end view object
    Handle<Value> _view = Persistent<Object>::Cast(ctx)->Get(STRING_CACHE___view);

    // If the reference has not been cleared,
    if (_view->IsObject()) {
        Local<External> wrap = Local<External>::Cast(Handle<Object>::Cast(_view)->GetInternalField(0));
        void* ptr = wrap->Value();
        timestep_view *view = static_cast<timestep_view*>(ptr);

        if (view) {
            // LOOK: This assumes that js_view is never modified after the view is created
            view->js_view.Clear();
        }
    } else {
        // Should never happen because we have the __view reference
        LOG("{view} ERROR: Front-end not found in backing finalizer!");
    }

    ctx.Dispose();
    ctx.Clear();

#ifdef VIEW_LEAKS
    --frontend_count;
    LOG("{view} WARNING: View front count = %d", frontend_count);
#endif
}

// View backing finalizer
static void js_view_finalize(Persistent<Value> ctx, void *param) {
    HandleScope handle_scope;

    timestep_view *view = static_cast<timestep_view*>( param );
    if (view) {
        timestep_view_delete(view);
    }

    ctx.Dispose();
    ctx.Clear();

#ifdef VIEW_LEAKS
    --backing_count;
    LOG("{view} WARNING: View backing count = %d", backing_count);
#endif
}

static Handle<Value> js_image_view_set_image(const Arguments& args) {
    timestep_view *view = GET_TIMESTEP_VIEW(args[0]->ToObject());
    if (view) {
        timestep_image_map *map = GET_IMAGE_MAP(args[1]->ToObject());
        if (map) {
            // Clear old reference
            if (!view->map_ref.IsEmpty()) {
                view->map_ref.Dispose();
                view->map_ref.Clear();
            }
            view->map_ref = Persistent<Object>::New(args[1]->ToObject());
            view->view_data = map;
        }
    }
    return Undefined();
}

static Handle<Value> js_timestep_image_view_render(const Arguments& args) {
    timestep_view *view = GET_TIMESTEP_VIEW(args.This());
    if (view) {
        view->timestep_view_render(view, GET_CONTEXT2D_FROM(args[0]->ToObject()));
    }

    return Undefined();
}

Handle<ObjectTemplate> js_timestep_get_template() {
    Handle<ObjectTemplate> timestep_template = ObjectTemplate::New();
    timestep_template->Set("View", js_timestep_view_get_template()->GetFunction());
    timestep_template->Set("Animator", get_animate_class()->GetFunction());
    timestep_template->Set("ImageMap", js_timestep_image_map_get_template()->GetFunction());
    timestep_template->Set("setImageOnImageView", FunctionTemplate::New(js_image_view_set_image));
    timestep_template->Set("getEvents", FunctionTemplate::New(js_timestep_events_get));
    timestep_template->Set("_imageViewRender", FunctionTemplate::New(js_timestep_image_view_render));
    return timestep_template;
}

// This is the constructor for the view backing
Handle<Value> def_timestep_view_constructor(const Arguments& args) {
    Handle<Object> thiz = args.This();

#ifdef VIEW_LEAKS
    ++frontend_count;
    ++backing_count;
    LOG("{view} WARNING: View front count = %d, backing count = %d", frontend_count, backing_count);
#endif

    // This is the front-end view object
    Handle<Object> js_view = args[0]->ToObject();

    // Create an internal C object and attach it to the view backing
    timestep_view *view = timestep_view_init();
    thiz->SetInternalField(0, External::New(view));
    thiz->SetInternalField(1, js_view);

    // Track the lifetime of the view backing
    Persistent<Object> ref = Persistent<Object>::New(thiz);
    ref.MakeWeak(view, js_view_finalize);

    // Track the lifetime of the front-end view also
    Persistent<Object> js_ref = Persistent<Object>::New(js_view);
    js_ref.MakeWeak(NULL, cb_js_finalize);

    // Add an internal C reference to the front-end view object in the view backing
    view->js_view = js_ref;

    Handle<Value> render = js_view->GetRealNamedPropertyInPrototypeChain(STRING_CACHE_render);
    bool has_js_render = false;
    if (!render.IsEmpty() && render->IsFunction()) {
        Handle<Value> type = render->ToObject()->Get(STRING_CACHE_HAS_NATIVE_IMPL);
        has_js_render = !type->IsBoolean() || !type->BooleanValue();
    }

    view->has_jsrender = has_js_render;

    Handle<Value> tick = js_view->GetRealNamedPropertyInPrototypeChain(STRING_CACHE_tick);
    view->has_jstick = !tick.IsEmpty() && tick->IsFunction();

    unsigned int type = js_view->GetRealNamedPropertyInPrototypeChain(STRING_CACHE___type)->Int32Value();

    timestep_view_set_type(view, type);

    return Undefined();
}

void timestep_view_set_compositeOperation (Local<String> property, Local<Value> value, const AccessorInfo& info) {
    Local<Object> thiz = info.Holder();
    timestep_view *view = GET_TIMESTEP_VIEW(thiz);

    if (!value->IsString()) {
        view->composite_operation = 0;
    } else {
        String::Utf8Value str(value);
        const char *code = ToCString(str);

        int op = 0;
        if (code[0] == 'l') {
            op = 1345;
        } else if (code[0] == 'x') {
            op = 1346;
        } else if (code[0] == 'c') {
            op = 1347;
        } else if (code[0] == 's') {
            if (0 == strcmp(code, "source-atop")) {
                op = 1337;
            } else if (0 == strcmp(code, "source-in")) {
                op = 1338;
            } else if (0 == strcmp(code, "source-out")) {
                op = 1339;
            } else if (0 == strcmp(code, "source-over")) {
                op = 1340;
            }
        } else if (code[0] == 'd') {
            if (0 == strcmp(code, "destination-atop")) {
                op = 1341;
            } else if (0 == strcmp(code, "destination-in")) {
                op = 1342;
            } else if (0 == strcmp(code, "destination-out")) {
                op = 1343;
            } else if (0 == strcmp(code, "destination-over")) {
                op = 1344;
            }
        }

        view->composite_operation = op;
    }
}

Handle<Value> timestep_view_get_compositeOperation(Local<String> property, const AccessorInfo& info) {
    Local<Object> thiz = info.Holder();
    timestep_view *view = GET_TIMESTEP_VIEW(thiz);

    switch (view->composite_operation) {
    case 1337:
        return String::New("source-atop");
    case 1338:
        return String::New("source-in");
    case 1339:
        return String::New("source-out");
    case 1340:
        return String::New("source-over");
    case 1341:
        return String::New("destination-atop");
    case 1342:
        return String::New("destination-in");
    case 1343:
        return String::New("destination-out");
    case 1344:
        return String::New("destination-over");
    case 1345:
        return String::New("lighter");
    case 1346:
        return String::New("xor");
    case 1347:
        return String::New("copy");
    default:
        return Undefined();
    }
}

void timestep_view_set_zIndex (Local<String> property, Local<Value> value, const AccessorInfo& info) {
    Local<Object> thiz = info.Holder();
    timestep_view *view = GET_TIMESTEP_VIEW(thiz);
    view->z_index = value->Int32Value();
    if (view->superview) {
        view->superview->dirty_z_index = true;
    }
}

void timestep_view_set_opacity (Local<String> property, Local<Value> value, const AccessorInfo& info) {
    Local<Object> thiz = info.Holder();
    timestep_view *view = GET_TIMESTEP_VIEW(thiz);

    if (view) {
        if (value->IsUndefined()) {
            view->opacity = 1;
        } else {
            view->opacity = value->NumberValue();
        }
    }
}


// -- the following setters/getters are copied from auto-gen templates for the
// -- non-underscore variants, allowing JS to set width/height using either
// -- .width/.height or ._width/._height.

v8::Handle<v8::Value> timestep_view_get__width(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
    //LOG("in timestep_view get _width");
    v8::Local<v8::Object> thiz = info.Holder();
    timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();

    double prop = obj->width;
    //LOG("done in timestep_view get _width");
    return v8::Number::New(prop);
}

void timestep_view_set__width(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
    //LOG("in timestep_view set _width");
    v8::Local<v8::Object> thiz = info.Holder();
    timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();

    obj->width = value->ToNumber()->Value();

    //LOG("done in timestep_view set _width");
}

v8::Handle<v8::Value> timestep_view_get__height(v8::Local<v8::String> property, const v8::AccessorInfo &info) {
    //LOG("in timestep_view get _height");
    v8::Local<v8::Object> thiz = info.Holder();
    timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();

    double prop = obj->height;
    //LOG("done in timestep_view get _height");
    return v8::Number::New(prop);
}

void timestep_view_set__height(v8::Local<v8::String> property, v8::Local<v8::Value> value, const v8::AccessorInfo &info) {
    //LOG("in timestep_view set _height");
    v8::Local<v8::Object> thiz = info.Holder();
    timestep_view *obj = (timestep_view*) v8::Local<v8::External>::Cast(thiz->GetInternalField(0))->Value();

    obj->height = value->ToNumber()->Value();

    //LOG("done in timestep_view set _height");
}

// -- end copy

void def_timestep_view_render(Handle<Object> js_view, Handle<Object> js_ctx, Handle<Object> js_opts) {
    Handle<Function> render = Handle<Function>::Cast(js_view->Get(STRING_CACHE_render));
    if (!render.IsEmpty() && render->IsFunction()) {
        Handle<Value> args[] = {js_ctx, js_opts};
        render->Call(js_view, 2, args);
    }
}

Handle<Object> def_get_viewport(Handle<Object> js_opts) {
    return Handle<Object>::Cast(js_opts->Get(STRING_CACHE_viewport));
}

void def_restore_viewport(Handle<Object> js_opts, Handle<Object> js_viewport) {
    js_opts->Set(STRING_CACHE_viewport, js_viewport);
}

void def_timestep_view_needs_reflow(Handle<Object> js_view, bool force) {
    if (force && !js_view.IsEmpty()) {
        Handle<Function> needs_reflow = Handle<Function>::Cast(js_view->Get(STRING_CACHE_needsReflow));
        if (!needs_reflow.IsEmpty() && needs_reflow->IsFunction()) {
            Handle<Value> args[] = {Boolean::New(force)};
            needs_reflow->Call(js_view, 1, args);
        }
    }
}

void def_timestep_view_tick(Handle<Object> js_view, double dt) {
    Handle<Function> tick = Handle<Function>::Cast(js_view->Get(STRING_CACHE_tick));
    if (!tick.IsEmpty() && tick->IsFunction()) {
        Handle<Value> args[] = {Number::New(dt)};
        tick->Call(js_view, 1, args);
    }
}

Handle<Value> def_timestep_view_addSubview(const Arguments &args) {
    Handle<Object> subview = args[0]->ToObject();
    timestep_view *view = GET_TIMESTEP_VIEW(Handle<Object>::Cast(subview->Get(STRING_CACHE___view)));
    bool result = timestep_view_add_subview(GET_TIMESTEP_VIEW(args.This()), view);
    return Boolean::New(result);
}

Handle<Value> def_timestep_view_removeSubview(const Arguments &args) {
    Handle<Object> subview = args[0]->ToObject();
    timestep_view *view = GET_TIMESTEP_VIEW(Handle<Object>::Cast(subview->Get(STRING_CACHE___view)));
    bool result = timestep_view_remove_subview(GET_TIMESTEP_VIEW(args.This()), view);
    return Boolean::New(result);
}

Handle<Value> def_timestep_view_getSuperview(const Arguments &args) {
    timestep_view *v = GET_TIMESTEP_VIEW(args.This());

    timestep_view *view = timestep_view_get_superview(v);
    if (!view) {
        return Undefined();
    }
    if (view->js_view.IsEmpty()) {
        return Undefined();
    } else {
        return view->js_view;
    }
}

Handle<Value> def_timestep_view_wrapRender(const Arguments &args) {
    Handle<Object> js_ctx = Handle<Object>::Cast(args[0]);
    Handle<Object> js_opts = Handle<Object>::Cast(args[1]);
    Handle<Object> _ctx = Handle<Object>::Cast(js_ctx->Get(STRING_CACHE__ctx));
    context_2d *ctx = GET_CONTEXT2D_FROM(_ctx);
    timestep_view_wrap_render(GET_TIMESTEP_VIEW(args.This()), ctx, js_ctx, js_opts);
    return Undefined();
}

Handle<Value> def_timestep_view_wrapTick(const Arguments &args) {
    double dt = args[0]->NumberValue();
    timestep_view_wrap_tick(GET_TIMESTEP_VIEW(args.This()), dt);
    return Undefined();
}

Handle<Value> def_timestep_view_getSubviews(const Arguments &args) {
    timestep_view *v = GET_TIMESTEP_VIEW(args.This());
    int expected_count = v->subview_count, actual_count = 0;
    Handle<Array> subviews = Array::New(expected_count);
    unsigned int i;
    for (i = 0; i < v->subview_count; i++) {
        timestep_view *subview = v->subviews[i];
        if (!subview->js_view.IsEmpty()) {
            subviews->Set(Number::New(i), subview->js_view);
            ++actual_count;
        }
    }

    // If the counts do not match,
    if (actual_count != expected_count) {
        // Do it again
        Handle<Array> subviews = Array::New(actual_count);

        int next_index = 0;
        for (i = 0; i < v->subview_count; i++) {
            timestep_view *subview = v->subviews[i];
            if (!subview->js_view.IsEmpty()) {
                subviews->Set(Number::New(next_index++), subview->js_view);
            }
        }
    }

    return subviews;
}

Handle<Value> def_timestep_view_localizePoint(const Arguments &args) {
    timestep_view *v = GET_TIMESTEP_VIEW(args.This());
    Handle<Object> pt = args[0]->ToObject();
    double x = pt->Get(STRING_CACHE_x)->NumberValue();
    double y = pt->Get(STRING_CACHE_y)->NumberValue();

    x -= v->x + v->anchor_x + v->offset_x;
    y -= v->y + v->anchor_y + v->offset_y;

    if (v->r) {
        double cosr = cos(-(v->r));
        double sinr = sin(-(v->r));
        double x2 = x;
        double y2 = y;
        x = x2 * cosr - y2 * sinr;
        y = x2 * sinr + y2 * cosr;
    }

    if (v->scale != 1) {
        double s = 1 / v->scale;
        x *= s;
        y *= s;
    }

    x += v->anchor_x;
    y += v->anchor_y;

    pt->Set(STRING_CACHE_x, Number::New(x));
    pt->Set(STRING_CACHE_y, Number::New(y));

    return pt;
}
