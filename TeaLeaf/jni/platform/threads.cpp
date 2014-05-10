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

#include "core/platform/threads.h"
#include "platform/platform.h"
#include <pthread.h>

// Internal type for ThreadsThread
struct ThreadSpec {
    pthread_t pt;
    ThreadsThreadProc proc;
    void *param;
};

// Internal wrapper for pro/epilog of thread procedure
void *thread_wrapper(void *param) {
    ThreadSpec *spec = (ThreadSpec *)param;
    if (spec) {
        // Before thread action
        native_enter_thread();

        spec->proc(param);

        // After thread action
        native_leave_thread();
    }

    return NULL;
}

// Start a thread
CEXPORT ThreadsThread threads_create_thread(ThreadsThreadProc proc, void *param) {
    // Generate a thread spec to return
    ThreadSpec *spec = (ThreadSpec *)malloc(sizeof(ThreadSpec));
    spec->proc = proc;
    spec->param = param;

    // Attempt to create a thread
    int tr = pthread_create(&spec->pt, NULL, thread_wrapper, spec);

    GC_COMPILER_FENCE;

    // If thread was not created,
    if (tr != 0) {
        // Release memory used for object
        free(spec);

        // Return NULL to indicate error
        return NULL;
    } else {
        // Return spec; now, caller must release it by calling threads_join_thread
        return spec;
    }
}

CEXPORT void threads_join_thread(ThreadsThread *thread) {
    if (thread) {
        ThreadSpec *spec = (ThreadSpec *)*thread;

        if (spec) {
            pthread_join(spec->pt, NULL);

            free(spec);

            *thread = THREADS_INVALID_THREAD;
        }
    }
}

