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
#include "platform/profiler.h"
#include "profiler/prof.h"
#include "core/config.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define LIBRARY_NAME "libtealeaf.so"
#define PATH "/sdcard/"
#ifdef PROFILER_ENABLED
static char *current_tag = NULL;
#endif

void profiler_start_profile(const char *tag) {
#ifdef PROFILER_ENABLED
    if (current_tag) {
        profiler_stop_profile();
    }
    current_tag = strdup(tag);
    monstartup(LIBRARY_NAME);
#endif
}


void profiler_stop_profile() {
#ifdef PROFILER_ENABLED
    size_t buf_len = strlen(PATH) + strlen(current_tag) + 1;
    char *buf = (char*)malloc(buf_len);
    snprintf(buf, buf_len, "%s%s", PATH, current_tag);
    setenv("CPUPROFILE", buf, 1);
    free(current_tag);
    current_tag = NULL;
    moncleanup();
#endif
}
