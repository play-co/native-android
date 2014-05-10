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
#include "js/js_haptics.h"
#include "platform/haptics.h"
#include <stdlib.h>

using namespace v8;


Handle<Value> js_haptics_cancel(const Arguments &args) {
    haptics_cancel();

    return Undefined();
}

Handle<Value> js_haptics_vibrate(const Arguments &args) {
    Handle<Object> opts = Handle<Object>::Cast(args[0]);
    Handle<Value> milliseconds = opts->Get(STRING_CACHE_milliseconds);

    if ( milliseconds->IsUndefined() ) {

        Handle<Object> pattern = Handle<Array>::Cast( opts->Get(STRING_CACHE_pattern) );
        int repeat = opts->Get(STRING_CACHE_repeat)->Int32Value();

        int patternLen = pattern->Get(STRING_CACHE_length)->Int32Value();
        long long* patternArr = (long long*)malloc(sizeof(long long) * patternLen);

        for ( int i = 0; i < patternLen; i++ ) {
            patternArr[i] = pattern->Get(Number::New(i))->IntegerValue();
        }

        haptics_vibrate(patternArr, repeat, patternLen);

        free(patternArr);

    } else {

        haptics_vibrate(milliseconds->IntegerValue());

    }

    return Undefined();
}

Handle<Value> js_haptics_has_vibrator(Local<String> property, const AccessorInfo& info) {
    bool result = haptics_has_vibrator();

    return Boolean::New(result);
}

Handle<ObjectTemplate> js_haptics_get_template() {
    Handle<ObjectTemplate> haptics = ObjectTemplate::New();

    haptics->Set(STRING_CACHE_cancel, FunctionTemplate::New(js_haptics_cancel));
    haptics->Set(STRING_CACHE_vibrate, FunctionTemplate::New(js_haptics_vibrate));
    haptics->SetAccessor(STRING_CACHE_hasVibrator, js_haptics_has_vibrator);

    return haptics;
}

