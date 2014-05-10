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
#include "platform/get_locale.h"
#include "platform/platform.h"

static locale_info locale = {NULL, NULL};
locale_info *locale_get_locale() {
    if (!locale.country) {
        native_shim* shim = get_native_shim();

        jmethodID country_method = shim->env->GetMethodID(shim->type, "getLocaleCountry", "()Ljava/lang/String;");
        jstring country = (jstring)shim->env->CallObjectMethod(shim->instance, country_method);
        GET_STR(shim->env, country, locale.country);

        jmethodID language_method = shim->env->GetMethodID(shim->type, "getLocaleLanguage", "()Ljava/lang/String;");
        jstring language = (jstring)shim->env->CallObjectMethod(shim->instance, language_method);
        GET_STR(shim->env, language, locale.language);
    }
    return &locale;
}
