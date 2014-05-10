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
#include "platform/dialog.h"
#include "platform/platform.h"

void dialog_show_dialog(const char *title, const char *text, const char* imageUrl, char **buttons, int buttonLen, int* callbacks, int cbLen) {
    native_shim *shim = get_native_shim();
    JNIEnv *env = shim->env;
    jmethodID method = env->GetMethodID(shim->type, "showDialog", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;[I)V");

    jintArray cbs = env->NewIntArray(cbLen);
    jobjectArray jbuttons = env->NewObjectArray(buttonLen, env->FindClass("java/lang/String"), NULL);
    jstring jtitle = env->NewStringUTF(title);
    jstring jtext = env->NewStringUTF(text);
    jstring jimageurl = env->NewStringUTF(imageUrl);

    env->SetIntArrayRegion(cbs, 0, cbLen, callbacks);
    for(int i = 0; i < buttonLen; i++) {
        jstring buttonText = env->NewStringUTF(buttons[i]);
        env->SetObjectArrayElement(jbuttons, i, buttonText);
    }

    env->CallVoidMethod(shim->instance, method, jtitle, jtext, jimageurl, jbuttons, cbs);

    env->DeleteLocalRef(jtitle);
    env->DeleteLocalRef(jtext);
    env->DeleteLocalRef(jimageurl);
    env->DeleteLocalRef(cbs);
    env->DeleteLocalRef(jbuttons);
}
