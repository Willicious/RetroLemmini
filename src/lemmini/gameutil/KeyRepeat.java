package lemmini.gameutil;

import lemmini.tools.NanosecondTimer;

/*
 * FILE MODIFIED BY RYAN SAKOWSKI
 *
 *
 * Copyright 2009 Volker Oth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A more or less generic approach to key repeat function.<br>
 * - if a key is pressed and released, a single SINGLE_CLICK is returned
 * - if a key is pressed for longer than timeFirstPress but not released,
 *   a SINGLE_CLICK event is returned. Then if the key is still not released,
 *   each timeRepeat nanoseconds, a new SINGLE_CLICK is returned.
 * - if a key is pressed, released and pressed again within timeDoubleClick
 *   nanoseconds, a DOUBLE_CLICK is fired.
 *
 * @author Volker Oth
 */
public class KeyRepeat {

    /** key repeat event */
    public static enum Event {
        /** no state reached yet */
        NONE,
        /** single click detected */
        SINGLE_CLICK,
        /** double click detected */
        DOUBLE_CLICK
    }

    /** repeat state */
    private enum State {
        /** off state */
        OFF,
        /** wait for timeFirstPress to pass */
        DETECT_FIRST,
        /** wait for timeRepeat to pass */
        REPEAT
    }

    /** double click state */
    private enum DcState {
        /** not pressed */
        OFF,
        /** pressed and released once */
        PRESSED_ONCE
    }

    /** timer used as time base */
    private final NanosecondTimer timer;
    /** internal debounce state */
    private State state;
    /** double click state */
    private DcState dcState;
    /** 32bit debounce mask - up to 32 triggers can used for the same event */
    private int mask;
    /** time after which a pressed (and not released) key is recognized as key press (nanoseconds) */
    private final long timeFirstPress;
    /** time after which a pressed key fires repeatedly (nanoseconds) */
    private final long timeRepeat;
    /** the maximum time between two clicks to be recognized as double click (nanoseconds) */
    private final long timeDoubleClick;
    /** repeat event */
    private Event event = Event.NONE;

    /**
     * Constructor.
     * @param tfirst time after which a pressed (and not released) key is recognized as key press (nanoseconds)
     * @param trep time for repeat function (nanoseconds)
     * @param tdc maximum time between two clicks to be recognized as double click (nanoseconds)
     */
    public KeyRepeat(final long tfirst, final long trep, final long tdc) {
        timer = new NanosecondTimer();
        timeFirstPress = tfirst;
        timeRepeat = trep;
        timeDoubleClick = tdc;
        init();
    }

    /**
     * Initialize/Reset all states.
     */
    public final void init() {
        state = State.OFF;
        dcState = DcState.OFF;
        mask = 0;
        timer.update();
    }

    /**
     * Button/Icon was pressed.
     * @param m trigger mask
     */
    public synchronized void pressed(final int m) {
        // check repeat states
        if ((mask & m) == m) { // already pressed - possible for keys -> ignore
            return;
        }
        switch (dcState) {
            case OFF:
                // first press -> start timer and remember event
                dcState = DcState.PRESSED_ONCE;
                event = Event.SINGLE_CLICK;
                break;
            case PRESSED_ONCE:
                // clicked and released before
                if (!timer.timePassed(timeDoubleClick)) {
                    // double click
                    dcState = DcState.OFF;
                    event = Event.DOUBLE_CLICK;
                } else {
                    // new single click
                    event = Event.SINGLE_CLICK;
                }
                break;
            default:
                break;
        }
        mask |= m;
        state = State.DETECT_FIRST;
        timer.update(); // set up lock timer
    }

    /**
     * Button/Icon was released.
     * @param m trigger mask
     */
    public synchronized void released(final int m) {
        mask &= ~m;
        if (mask == 0) {
            state = State.OFF;
        }
    }

    /**
     * Poll the last event.
     * @return repeat event
     */
    public synchronized Event fired() {
        // check if there is a pending event stored
        if (event != Event.NONE) {
            // return event, reset internal state
            Event temp = event;
            event = Event.NONE;
            return temp;
        }

        // no button pressed, no event stored -> return none
        if (mask == 0) {
            return Event.NONE;
        }

        // no event stored, but button still pressed: check repeat states
        switch (state) {
            case DETECT_FIRST:
                if (timer.timePassedUpdate(timeFirstPress)) {
                    // first event occured
                    state = State.REPEAT;
                    dcState = DcState.OFF;
                    return Event.SINGLE_CLICK;
                }
                break;
            case REPEAT:
                if (timer.timePassedUpdate(timeRepeat)) { // new repeat event occurred
                    return Event.SINGLE_CLICK;
                }
                break;
            default:
                break;
        }
        return Event.NONE;
    }
}

