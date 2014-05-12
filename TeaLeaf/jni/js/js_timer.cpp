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
#include "js/js_timer.h"
#include "core/timer.h"
#include <stdlib.h>

using namespace v8;

CEXPORT void js_timer_unlink(core_timer* timer) {
    js_timer *t = (js_timer*)timer->js_data;
    t->callback.Dispose();
}

CEXPORT void js_timer_fire(core_timer *timer) {
    Locker l(getIsolate());
    HandleScope handle_scope;
    Handle<Context> context = getContext();
    Context::Scope context_scope(context);
    TryCatch try_catch;

    js_timer *t = (js_timer*) timer->js_data;
    Handle<Value> ret = t->callback->Call(context->Global(), 0, NULL);
    if (ret.IsEmpty()) {
        ReportException(&try_catch);
    }
}

static js_timer *get_timer(Handle<Object> callback) {
    Persistent<Function> cb = Persistent<Function>::New(Handle<Function>::Cast(callback));

    js_timer *timer = (js_timer*)malloc(sizeof(js_timer));
    timer->callback = cb;
    timer->arguments = NULL;//FIXME make passing arguments to settimeout work
    return timer;
}


static int schedule_timer(Handle<Object> cb, int time, bool repeat) {
    js_timer *js_timer = get_timer(cb);
    core_timer *timer = core_get_timer((void*)js_timer, time, repeat);
    core_timer_schedule(timer);
    return timer->id;
}


Handle<Value> defSetTimeout(const Arguments &args) {
    LOGFN("settimeout");
    if (args[0].IsEmpty() || !args[0]->IsFunction()) {
        return Undefined();
    }
    Handle<Object> cb = args[0]->ToObject();

    int time = args[1]->Int32Value();
    int id = schedule_timer(cb, time, false);
    LOGFN("end settimeout");
    return Number::New(id);
}

Handle<Value> defSetInterval(const Arguments &args) {
    LOGFN("setInterval");
    if (args[0].IsEmpty() || !args[0]->IsFunction()) {
        return Undefined();
    }
    Handle<Object> cb = args[0]->ToObject();

    int time = args[1]->Int32Value();
    int id = schedule_timer(cb, time, true);
    LOGFN("end setInterval");
    return Number::New(id);
}

Handle<Value> defClearTimeout(const Arguments &args) {
    LOGFN("cleartimeout");
    int id = args[0]->Int32Value();
    core_timer_clear(id);
    LOGFN("end cleartimeout");
    return Undefined();
}

Handle<Value> defClearInterval(const Arguments &args) {
    LOGFN("clearInterval");
    int id = args[0]->Int32Value();
    core_timer_clear(id);
    LOGFN("end clearInterval");
    return Undefined();
}
