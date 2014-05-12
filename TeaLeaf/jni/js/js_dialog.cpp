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
#include "js/js_dialog.h"
#include "platform/dialog.h"
#include <stdlib.h>

using namespace v8;

Handle<Value> js_dialog_show_dialog(const Arguments &args) {
    String::Utf8Value title(args[0]);
    char *title_str = strdup(ToCString(title));

    String::Utf8Value text(args[1]);
    char *text_str = strdup(ToCString(text));

    char* image_url = NULL;
    if(!args[2].IsEmpty() && !args[2]->IsUndefined() && !args[2]->IsNull()) {
        String::Utf8Value imgurl(args[2]);
        image_url = strdup(ToCString(imgurl));
    }

    Handle<Object> buttons = Handle<Array>::Cast(args[3]);
    Handle<Object> cbs = Handle<Array>::Cast(args[4]);

    int buttonLen = buttons->Get(STRING_CACHE_length)->Int32Value(),
        cbLen = cbs->Get(STRING_CACHE_length)->Int32Value();

    char** buttons_str = (char**)malloc(sizeof(char*) * buttonLen);
    int* callbacks = (int*)malloc(sizeof(int) * cbLen);

    for(int i = 0; i < buttonLen; i++) {
        String::Utf8Value str(buttons->Get(Number::New(i)));
        buttons_str[i] = strdup(ToCString(str));
    }
    for(int i = 0; i < cbLen; i++) {
        Handle<Value> obj = cbs->Get(Number::New(i));
        callbacks[i] = obj->Int32Value();
    }

    dialog_show_dialog(title_str, text_str, image_url, buttons_str, buttonLen, callbacks, cbLen);

    for(int i = 0; i < buttonLen; i++) {
        free(buttons_str[i]);
    }
    free(buttons_str);
    free(callbacks);
    free(image_url);
    free(text_str);
    free(title_str);

    return Undefined();
}

Handle<ObjectTemplate> js_dialog_get_template() {
    Handle<ObjectTemplate> dialog = ObjectTemplate::New();
    dialog->Set(STRING_CACHE__showDialog, FunctionTemplate::New(js_dialog_show_dialog));
    return dialog;
}
