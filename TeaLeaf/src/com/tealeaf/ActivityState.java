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
package com.tealeaf;

class ActivityState {

    private static boolean onPauseOccurred = false;
    private static boolean onResumeOccurred = false;
    private static boolean onWindowFocusLostOccurred = false;
    private static boolean onWindowFocusAcquiredOccurred = false;

    private static int STATE_NA = -1, STATE_PAUSED = 0, STATE_RESUMED = 1;
    private static int state = STATE_NA;

    private static void updateState() {
        if (onPauseOccurred && onWindowFocusLostOccurred) {
            state = STATE_PAUSED;
        } else if (onResumeOccurred && onWindowFocusAcquiredOccurred) {
            state = STATE_RESUMED;
        }
    }

    public static void onPause() {
        onResumeOccurred = false;
        onPauseOccurred = true;
        updateState();
    }

    public static void onResume() {
        onPauseOccurred = false;
        onResumeOccurred = true;
        updateState();
    }

    public static void onWindowFocusLost() {
        onWindowFocusAcquiredOccurred = false;
        onWindowFocusLostOccurred = true;
        updateState();
    }

    public static void onWindowFocusAcquired() {
        onWindowFocusAcquiredOccurred = true;
        onWindowFocusLostOccurred = false;
        updateState();
    }

    public static boolean hasPaused(boolean consume) {
        if (consume && state == STATE_PAUSED) {
            onPauseOccurred = onWindowFocusLostOccurred = false;
        }
        return state == STATE_PAUSED;
    }

    public static boolean hasPaused() {
        return hasPaused(false);
    }

    public static boolean hasResumed(boolean consume) {
        if (consume && state == STATE_RESUMED) {
            onResumeOccurred = onWindowFocusAcquiredOccurred = false;
        }
        return state == STATE_RESUMED;
    }

    public static boolean hasResumed() {
        return hasResumed(false);
    }

}
