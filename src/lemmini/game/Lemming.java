package lemmini.game;

import java.awt.Transparency;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import lemmini.graphics.LemmImage;
import lemmini.sound.Sound;
import lemmini.tools.Props;
import lemmini.tools.ToolBox;

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
 * Implements a Lemming.
 *
 * @author Volker Oth
 */
public class Lemming {

    public static final int HEIGHT = 20;

    /** name of the configuration file */
    private static final String LEMM_INI_STR = "gfx/lemming/lemming.ini";
    /** number of resources (animations/names) */
    private static final int NUM_RESOURCES = 18;

    /** Lemming skill type */
    public static enum Type {
        /** the typical Lemming */
        WALKER ("WALKER", 8, true, true, 0, 0),
        /** a falling Lemming */
        FALLER ("FALLER", 4, true, true, 0, 0),
        /** a climbing Lemming */
        CLIMBER ("CLIMBER", 8, true, true, 0, 0),
        /** a climbing Lemming returning to the ground */
        FLIPPER ("FLIPPER", 8, true, false, 0, 0),
        /** a Lemming with a parachute */
        FLOATER ("FLOATER", 16, true, false, 0, 0),
        /** a Lemming blowing itself up */
        FLAPPER ("FLAPPER", 16, false, false, 0, 0),
        /** a Lemming dying from a fall */
        SPLATTER ("SPLATTER", 16, false, false, 0, 0),
        /** a Lemming blocking the way for the other Lemmings */
        BLOCKER ("BLOCKER", 16, false, true, 3, 0),
        /** a Lemming drowning in the water */
        DROWNER ("DROWNER", 16, false, false, 0, 0),
        /** a Lemming killed by a trap */
        FRIER ("FRIER", 14, false, false, 0, 0),
        /** a Lemming exiting the level */
        HOMER ("HOMER", 8, false, false, 0, 0),
        /** a Lemming building stairs */
        BUILDER ("BUILDER", 16, true, true, 1, 9),
        /** a builder Lemming with no more steps in his backpack */
        SHRUGGER ("SHRUGGER", 8, true, false, 0, 0),
        /** a Lemming digging a hole in the ground */
        DIGGER ("DIGGER", 16, false, true, 1, 8),
        /** a Lemming bashing the ground before it */
        BASHER ("BASHER", 32, true, true, 4, 8),
        /** a Lemming digging a mine with a pick */
        MINER ("MINER", 24, true, true, 2, 12),
        /** a Lemming jumping over a small obstacle */
        JUMPER ("JUMPER", 1, true, true, 0, 0),
        EXPLODER ("EXPLODER", 0, false, false, 1, 0),
        /* types without a separate animation */
        /** a Lemming that is nuked */
        NUKE (StringUtils.EMPTY, 0, false, false, 0, 0),
        /** a blocker that is told to explode */
        FLAPPER_BLOCKER ("FLAPPER", 0, false, false, 0, 0),
        /** a floater before the parachute opened completely */
        FLOATER_START ("FLOATER", 0, false, false, 0, 0),
        /** allows faller to exit in direct drop mode */
        MAX_EXIT_LEM ("FALLER", 0, false, false, 0, 0);

        private final String name;
        private final int frames;
        private final boolean bidirectional;
        private final boolean loop;
        private final int maskFrames;
        private final int maskStep;

        private Type(String name, int frames, boolean bidirectional, boolean loop,
                int maskFrames, int maskStep) {
            this.name = name;
            this.frames = frames;
            this.bidirectional = bidirectional;
            this.loop = loop;
            this.maskFrames = maskFrames;
            this.maskStep = maskStep;
        }
    }

    /** Lemming heading */
    public static enum Direction {
        RIGHT,
        LEFT;

        private static final Map<Integer, Direction> LOOKUP = new HashMap<>();

        static {
            EnumSet.allOf(Direction.class).stream().forEach(s -> LOOKUP.put(s.ordinal(), s));
        }

        /**
         * Reverse lookup implemented via hashtable.
         * @param val Ordinal value
         * @return Parameter with ordinal value val
         */
        public static Direction get(final int val) {
            return LOOKUP.get(val);
        }

    }

    /** animation type */
    static enum Animation {
        NONE,
        LOOP,
        ONCE
    }

    /** a walker walks one pixel per frame */
    private static final int WALKER_STEP = 1;
    /** a climber climbs up 1 pixel per frame during the second half of the animation */
    private static final int CLIMBER_STEP = 1;
    /** at this height a walker will turn around */
    private static final int WALKER_OBSTACLE_HEIGHT = 14;
    /** check N pixels above the lemming's feet */
    private static final int BASHER_CHECK_STEP = 12;
    private static final int BASHER_CHECK_STEP_STEEL = 16;
    private static final int BASHER_CHECK_STEP_STEEL_LOW = 3;
    /** from this on a basher will become a faller */
    private static final int BASHER_FALL_DISTANCE = 6;
    /** check N pixels above the lemming's feet */
    private static final int MINER_CHECK_STEP_STEEL = 16;
    /** from this on a miner will become a faller */
    private static final int MINER_FALL_DISTANCE = 1;
    /** a faller falls down three pixels per frame */
    private static final int FALLER_STEP = 3;
    /** a floater falls down two pixels per frame */
    private static final int FLOATER_STEP = 2;
    private static final int FLOATER_STEP_SLOW = 1;
    /** a jumper moves up two pixels per frame */
    private static final int JUMPER_STEP = 2;
    /** if a walker jumps up 6 pixels, it becomes a jumper */
    private static final int JUMPER_JUMP = 6;
    private static final int DIGGER_STEP = 2;
    /** pixels a floater falls before the parachute begins to open */
    private static final int FALL_DISTANCE_FLOAT = 32;
    /** number of free pixels below needed to convert a lemming to a faller */
    private static final int FALL_DISTANCE_FALL  = 8;
    /** number of steps a builder can build */
    private static final int STEPS_MAX = 12;
    /** number of steps before the warning sound is played */
    private static final int STEPS_WARNING = 9;
    /** Lemmini runs at 33.33fps instead of 16.67fps */
    private static final int TIME_SCALE = 2;
    /** maximum seconds in bomber countdown */
    private static final int MAX_BOMB_TIMER = 5;
    /** explosion counter is decreased every 31.2 frames */
    private static final int[] MAX_EXPLODE_CTR = {31, 31, 32, 31, 31}; //allows for a maximum of 5 seconds.
    private static final int EXPLODER_LIFE = 102;
    private static final int DEF_TEMPLATE_COLOR = 0xffff00ff;

    /** resource (animation etc.) for the current Lemming */
    private LemmingResource lemRes;
    /** animation frame */
    private int frameIdx;
    /** x coordinate of foot in pixels */
    private int x;
    /** y coordinate of foot in pixels */
    private int y;
    /** Lemming's heading */
    private Direction dir;
    /** Lemming's skill/type */
    private Type type;
    /** counter used for internal state changes */
    private int counter;
    /** another counter used for internal state changes */
    private int counter2;
    /** explosion counter when nuked */
    private int explodeNumCtr;
    /** Lemming can float */
    private boolean canFloat;
    /** Lemming can climb */
    private boolean canClimb;
    /** Lemming can change its skill */
    private boolean canChangeSkill;
    private boolean flapper;
    private boolean drowner;
    /** Lemming is to be nuked */
    private boolean nuke;
    /** Lemming has died */
    private boolean hasDied;
    /** Lemming has left the level */
    private boolean hasExited;
    /** counter used to manage the explosion */
    private int explodeCtr;
    /** counter used to display the select image in replay mode */
    private int selectCtr;

    /** list of resources for each Lemming skill/type */
    private static List<LemmingResource> lemmings = new ArrayList<>(NUM_RESOURCES);
    /** font used for the explosion counter */
    private static ExplodeFont explodeFont;
    private static int templateColor;
    private static int templateColor2;

    /**
     * Constructor: Create Lemming
     * @param sx x coordinate of foot
     * @param sy y coordinate of foot
     * @param d initial direction
     */
    public Lemming(final int sx, final int sy, final Direction d) {
        frameIdx = 0;
        type = Type.FALLER; // always start with a faller
        lemRes = getResource(type);
        counter = 0;
        explodeNumCtr = 0;
        selectCtr = 0;
        dir = d;  // always start walking to the right
        x = sx;
        y = sy;
        canFloat = false; // new lemming can't float
        canClimb = false; // new lemming can't climb
        canChangeSkill = false; // a faller can not change the skill to e.g. builder
        hasDied = false;  // not yet
        hasExited = false;  // not yet
        flapper = false;
        drowner = false;
        nuke = false;
    }

    /**
     * Get number of Lemming type in internal resource array.
     * @param t Type
     * @return resource number for type
     */
    public static int getOrdinal(final Type t) {
        switch (t) {
            case FLAPPER_BLOCKER:
                return Type.FLAPPER.ordinal();
            case FLOATER_START:
                return Type.FLOATER.ordinal();
            default:
                return t.ordinal();
        }
    }

    /**
     * Update animation, move Lemming, check state transitions.
     */
    public void animate() {
        int free;
        Type oldType = type;
        Type newType = type;
        boolean explode = false;
        // first check explode state
        if (explodeNumCtr != 0) {
            if (++explodeCtr >= MAX_EXPLODE_CTR[explodeNumCtr - 1]) {
                explodeCtr -= MAX_EXPLODE_CTR[explodeNumCtr - 1];
                explodeNumCtr--;
                if (explodeNumCtr == 0) {
                    explode = true;
                }
            }
        }
        if (selectCtr > 0) {
            selectCtr--;
        }
        // lemming state machine
        switch (type) {

            case FLIPPER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    int idx = frameIdx + 1;
                    switch (idx) {
                        case 1 * TIME_SCALE:
                        case 2 * TIME_SCALE:
                        case 3 * TIME_SCALE:
                        case 4 * TIME_SCALE:
                            y -= 4;
                            break;
                        default:
                            break;
                    }
                    turnedByBlocker();
                    break;
                }

            case FALLER:
                if (explode) {
                    newType = getExploderType();
                    break;
                }
                free = freeBelow(FALLER_STEP);
                y += StrictMath.min(FALLER_STEP, free); // max: FALLER_STEP
                if (!crossedLowerBorder()) {
                    counter += free; // fall counter
                    // check conversion to floater
                    if (canFloat && counter >= FALL_DISTANCE_FLOAT) {
                        newType = Type.FLOATER_START;
                        counter2 = 0; // used for parachute opening "jump" up
                    } else if (free == 0) { // check ground hit
                        if (counter > GameController.getLevel().getMaxFallDistance()) {
                            newType = Type.SPLATTER;
                        } else {
                            newType = Type.WALKER;
                            counter = 0;
                        }
                    }
                }
                turnedByBlocker();
                break;

            case JUMPER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    int levitation = aboveGround();
                    if (levitation > JUMPER_STEP) {
                        y -= JUMPER_STEP;
                    } else {
                        // conversion to walker
                        y -= levitation;
                        newType = Type.WALKER;
                    }
                    turnedByBlocker();
                    break;
                }

            case WALKER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    if (dir == Direction.RIGHT) {
                        x += WALKER_STEP;
                    } else if (dir == Direction.LEFT) {
                        x -= WALKER_STEP;
                    }
                    if (flipDirBorder()) {
                        break;
                    }
                    // check
                    free = freeBelow(FALL_DISTANCE_FALL);
                    if (free >= FALL_DISTANCE_FALL) {
                        y += FALLER_STEP;
                    } else {
                        y += free;
                    }
                    boolean ignoreBlockers = false;
                    int levitation = aboveGround();
                    // check for flip direction
                    if (levitation < WALKER_OBSTACLE_HEIGHT && y >= 8) {
                        if (levitation >= JUMPER_JUMP) {
                            y -= JUMPER_STEP;
                            newType = Type.JUMPER;
                            break;
                        } else {
                            y -= levitation;
                        }
                    } else {
                        if (canClimb && y >= WALKER_OBSTACLE_HEIGHT) {
                            newType = Type.CLIMBER;
                            break;
                        } else {
                            flipDir();
                            ignoreBlockers = true;
                        }
                    }
                    if (free > 0) {
                        // check for conversion to faller
                        if (free >= FALL_DISTANCE_FALL) {
                            counter = FALLER_STEP + 1; // @check: is this OK? increasing counter, but using free???
                            newType = Type.FALLER;
                            y += 1;
                        }
                    }
                    if (!ignoreBlockers) {
                        turnedByBlocker();
                    }
                    break;
                }

            case FLOATER:
                if (explode) {
                    newType = getExploderType();
                    break;
                }
                free = freeBelow(FLOATER_STEP);
                y += StrictMath.min(FLOATER_STEP, free); // max: FLOATER_STEP
                if (!crossedLowerBorder()) {
                    counter += free; // fall counter
                    // check ground hit
                    if (free == 0) {
                        newType = Type.WALKER;
                        counter = 0;
                    }
                }
                turnedByBlocker();
                break;

            case FLOATER_START:
                if (explode) {
                    newType = getExploderType();
                    break;
                }
                free = freeBelow(FALLER_STEP);
                switch (counter2++) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        if (free >= FALLER_STEP) {
                            y += FALLER_STEP;
                        } else {
                            y += free;
                        }
                        break;
                    case 8:
                    case 9:
                        y -= FLOATER_STEP_SLOW;
                        break;
                    case 10:
                    case 11:
                        break;
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        if (free >= FLOATER_STEP_SLOW) {
                            y += FLOATER_STEP_SLOW;
                        } else {
                            y += free;
                        }
                        if (counter2 - 1 == 15) {
                            type = Type.FLOATER;
                        }
                        break;
                    default:
                        type = Type.FLOATER;
                        break;
                }
                if (!crossedLowerBorder()) {
                    counter += StrictMath.min(FLOATER_STEP, free); // fall counter
                    // check ground hit
                    if (free == 0) {
                        newType = Type.WALKER;
                        counter = 0;
                    }
                }
                turnedByBlocker();
                break;
            case CLIMBER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    int idx = frameIdx + 1;
                    if (idx >= lemRes.frames * TIME_SCALE) {
                        idx = 0;
                    }
                    switch (idx) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                            if (reachedPlateau(idx + 13)) {
                                counter = 0;
                                y += -idx + 4;
                                newType = Type.FLIPPER;
                            }
                            break;
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            if (!freeAboveClimber()) {
                                flipDir();
                                if (dir == Direction.LEFT) {
                                    x -= 1;
                                } else {
                                    x += 1;
                                }
                                newType = Type.FALLER;
                                counter = 0;
                            } else {
                                y -= CLIMBER_STEP;
                            }
                            break;
                        default:
                            break;
                    }
                    turnedByBlocker();
                    break;
                }

            case DIGGER:
                if (explode) {
                    newType = getExploderType();
                    break;
                }
                turnedByBlocker();
                break;

            case BASHER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }

                    Mask m;
                    int eraseMask;
                    int checkMask;
                    int idx = frameIdx + 1;
                    if (idx >= lemRes.frames * TIME_SCALE) {
                        idx = 0;
                    }
                    switch (idx) {
                        case 2 * TIME_SCALE:
                        case 3 * TIME_SCALE:
                        case 4 * TIME_SCALE:
                        case 5 * TIME_SCALE:
                            {
                                // bash mask should have the same height as the lemming
                                m = lemRes.getMask(dir);
                                eraseMask = Stencil.MSK_BRICK;
                                checkMask = 0;
                                if (!GameController.getLevel().getClassicSteel()) {
                                    eraseMask |= Stencil.MSK_ONE_WAY;
                                    checkMask |= Stencil.MSK_STEEL;
                                    checkMask |= (dir == Direction.LEFT) ? Stencil.MSK_ONE_WAY_RIGHT : Stencil.MSK_ONE_WAY_LEFT;
                                }
                                m.eraseMask(screenMaskX(), screenMaskY(), idx / TIME_SCALE - 2, eraseMask, checkMask);
                                if (idx == 5 * TIME_SCALE) {
                                    // check for conversion to walker because there are no bricks left
                                    if (!canBash()) {
                                        // no bricks any more
                                        newType = Type.WALKER;
                                    }
                                }
                                break;
                            }
                        case 18 * TIME_SCALE:
                        case 19 * TIME_SCALE:
                        case 20 * TIME_SCALE:
                        case 21 * TIME_SCALE:
                            {
                                // bash mask should have the same height as the lemming
                                m = lemRes.getMask(dir);
                                eraseMask = Stencil.MSK_BRICK;
                                checkMask = 0;
                                if (!GameController.getLevel().getClassicSteel()) {
                                    eraseMask |= Stencil.MSK_ONE_WAY;
                                    checkMask |= Stencil.MSK_STEEL;
                                    checkMask |= (dir == Direction.LEFT) ? Stencil.MSK_ONE_WAY_RIGHT : Stencil.MSK_ONE_WAY_LEFT;
                                }
                                m.eraseMask(screenMaskX(), screenMaskY(), idx / TIME_SCALE - 18, eraseMask, checkMask);
                                break;
                            }
                        case 11 * TIME_SCALE:
                        case 12 * TIME_SCALE:
                        case 13 * TIME_SCALE:
                        case 14 * TIME_SCALE:
                        case 15 * TIME_SCALE:
                        case 27 * TIME_SCALE:
                        case 28 * TIME_SCALE:
                        case 29 * TIME_SCALE:
                        case 30 * TIME_SCALE:
                        case 31 * TIME_SCALE:
                            for (int i = 0; i < 2; i++) {
                                if (dir == Direction.RIGHT) {
                                    x += 1;
                                } else {
                                    x -= 1;
                                }
                                // check for conversion to faller
                                free = freeBelow(BASHER_FALL_DISTANCE);
                                if (free >= BASHER_FALL_DISTANCE) {
                                    y += FALLER_STEP;
                                    newType = Type.FALLER;
                                    break;
                                } else {
                                    y += free;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    turnedByBlocker();
                    if (!canBashSteel(true)) {
                        flipDir();
                        newType = Type.WALKER;
                    }
                    if (flipDirBorder()) {
                        newType = Type.WALKER;
                    }
                    break;
                }

            case MINER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    Mask m;
                    int idx = frameIdx + 1;
                    if (idx >= lemRes.frames * TIME_SCALE) {
                        idx = 0;
                    }
                    int oldX = x;
                    int oldY = y;
                    switch (idx) {
                        case 0 * TIME_SCALE:
                            y += 2;
                            break;
                        case 1 * TIME_SCALE:
                        case 2 * TIME_SCALE:
                            // check for steel in mask
                            m = lemRes.getMask(dir);
                            int eraseMask = Stencil.MSK_BRICK;
                            int checkMask = 0;
                            if (!GameController.getLevel().getClassicSteel()) {
                                eraseMask |= Stencil.MSK_ONE_WAY;
                                checkMask |= Stencil.MSK_STEEL;
                                checkMask |= (dir == Direction.LEFT) ? Stencil.MSK_ONE_WAY_RIGHT : Stencil.MSK_ONE_WAY_LEFT;
                            }
                            m.eraseMask(screenMaskX(), screenMaskY(), idx / TIME_SCALE - 1, eraseMask, checkMask);
                            break;
                        case 3 * TIME_SCALE:
                            y += 2;
                            /* falls through */
                        case 15 * TIME_SCALE:
                            if (dir == Direction.RIGHT) {
                                x += 4;
                            } else {
                                x -= 4;
                            }
                            // check for conversion to faller
                            free = freeBelow(MINER_FALL_DISTANCE);
                            int free2;
                            if (dir == Direction.RIGHT) {
                                // this is needed to help prevent miners from occasionally falling through solid floors
                                int oldX2 = x;
                                x += 1;
                                free2 = freeBelow(MINER_FALL_DISTANCE);
                                x = oldX2;
                            } else {
                                free2 = free;
                            }
                            if (free2 >= MINER_FALL_DISTANCE) {
                                if (dir == Direction.RIGHT && free < MINER_FALL_DISTANCE) {
                                    x += 1;
                                }
                                y += StrictMath.min(FALLER_STEP, free2); // max: FALLER_STEP
                                newType = Type.FALLER;
                                break;
                            }
                            if (!canMine(false, true)) {
                                if (!GameController.getLevel().getClassicSteel()) {
                                    // needed to ensure that miners don't get stuck
                                    x = oldX;
                                    y = oldY;
                                }
                                flipDir();
                                newType = Type.WALKER;
                            }
                            break;
                        default:
                            break;
                    }
                    turnedByBlocker();
                    break;
                }

            case SHRUGGER:
                if (explode) {
                    newType = getExploderType();
                }
                turnedByBlocker();
                break;
            case BUILDER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    int idx = frameIdx + 1;
                    if (idx >= lemRes.frames * TIME_SCALE) {
                        // step created -> move up
                        counter++; // step counter;
                        y -= 2; // step up
                        for (int i = 0; i < 4; i++) {
                            if (dir == Direction.RIGHT) {
                                x += 1; // step forward
                            } else {
                                x -= 1;
                            }
                            int levitation = aboveGround(); // should be 0, if not, we built into a wall -> stop
                            // check for conversion to walker
                            if (counter < STEPS_MAX && ((i == 3 && !freeAboveBuilder()) || levitation > 0)) {
                                newType = Type.WALKER;
                                flipDir();
                                break;
                            }
                        }
                        // check for last step used
                        if (counter >= STEPS_MAX) {
                            newType = Type.SHRUGGER;
                            break;
                        }
                        if (y < GameController.getLevel().getTopBoundary() + 2) {
                            newType = Type.WALKER;
                            break;
                        }
                    } else if (idx == 9 * TIME_SCALE) {
                        // stair mask is the same height as a lemming
                        Mask m;
                        m = lemRes.getMask(dir);
                        m.paintStep(screenMaskX(), screenMaskY(), 0);
                    } else if (idx == 10 * TIME_SCALE) {
                        if (counter >= STEPS_WARNING) {
                            playVisualSFX(Sound.Effect.STEP_WARNING);
                        }
                    }
                    turnedByBlocker();
                    break;
                }

            case BLOCKER:
                {
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                    // check for conversion to faller
                    free = freeBelow(FALLER_STEP);
                    if (free > 0) {
                        // conversion to faller or walker -> erase blocker mask
                        eraseBlockerMask();
                        y += StrictMath.min(FALLER_STEP, free); // max: FALLER_STEP
                        counter += free;
                        if (counter >= FALL_DISTANCE_FALL) {
                            newType = Type.FALLER;
                        } else {
                            newType = Type.WALKER;
                        }
                    } else {
                        counter = 0;
                    }
                    break;
                }

            case FLAPPER_BLOCKER:
                // don't erase blocker mask before blocker finally explodes or falls
                free = freeBelow(FALLER_STEP);
                if (free > 0) {
                    // blocker falls -> erase mask and convert to normal blocker.
                    eraseBlockerMask();
                    type = Type.FLAPPER;
                    // fall through
                } else {
                    int idx = frameIdx + 1;
                    if (idx == 5 * TIME_SCALE && !nuke) {
                        playVisualSFX(Sound.Effect.OHNO);
                    }
                    break;
                }
                /* falls through */
            case FLAPPER:
                {
                    int idx = frameIdx + 1;
                    if (idx == 5 * TIME_SCALE && !nuke) {
                        playVisualSFX(Sound.Effect.OHNO);
                    }
                    free = freeBelow(FALLER_STEP);
                    y += StrictMath.min(FALLER_STEP, free); // max: FALLER_STEP
                    crossedLowerBorder();
                    break;
                }
            case DROWNER:
                if (explode) {
                    newType = getExploderType();
                    break;
                }
                if (!flapper) {
                    if (dir == Direction.RIGHT) {
                        if (x < GameController.getWidth() + GameController.getLevel().getRightBoundary() - 16
                                && !BooleanUtils.toBoolean(GameController.getStencil().getMask(x + 16, y) & Stencil.MSK_BRICK)) {
                            x += WALKER_STEP;
                        }
                    } else if (dir == Direction.LEFT) {
                        if (x >= GameController.getLevel().getLeftBoundary() + 16
                                && !BooleanUtils.toBoolean(GameController.getStencil().getMask(x - 16, y) & Stencil.MSK_BRICK)) {
                            x -= WALKER_STEP;
                        }
                    }
                }
                turnedByBlocker();
                break;
            case FRIER:
                {
                	// special case: bubble water is a fire object that makes lems instantly explode (without leaving a crater)
                	if (GameController.getLevel().getStyleName().equals("bubble")) {
                		playVisualSFX(Sound.Effect.EXPLODE);
                		addExplosion();
                		hasDied = true;
                		break;
                	}
                	
                    if (explode) {
                        newType = getExploderType();
                        break;
                    }
                }
                break;
            case EXPLODER:
                counter++;
                if (counter == 1 * TIME_SCALE) {
                    Stencil stencil = GameController.getStencil();
                    Mask m = lemRes.getMask(Direction.RIGHT);
                    playVisualSFX(Sound.Effect.EXPLODE);
                    if (!GameController.getLevel().getClassicSteel()) {
                        m.eraseMask(screenMaskX(), StrictMath.max(screenMaskY(), -8), 0,
                                Stencil.MSK_BRICK | Stencil.MSK_ONE_WAY, Stencil.MSK_STEEL);
                    } else if (x >= GameController.getLevel().getLeftBoundary()
                            && x < GameController.getWidth() + GameController.getLevel().getRightBoundary()
                            && y < GameController.getHeight()
                            && !BooleanUtils.toBoolean(stencil.getMask(x, y) & Stencil.MSK_STEEL)
                            && !BooleanUtils.toBoolean(stencil.getMask(x, y) & Stencil.MSK_EXIT) && !drowner) {
                        m.eraseMask(screenMaskX(), StrictMath.max(screenMaskY(), -8), 0, Stencil.MSK_BRICK, 0);
                    }
                } else if (counter >= EXPLODER_LIFE) {
                    hasDied = true;
                }
                break;
            default:
                // all cases not explicitly checked above should at least explode (includes HOMER)
                if (explode) {
                    newType = getExploderType();
                    break;
                }
                break;

        }
        if (y < GameController.getLevel().getTopBoundary()) {
            y = GameController.getLevel().getTopBoundary();
        }

        if (!hasDied && type != Type.EXPLODER) {
            // check collision with exit and traps
            int s = stencilFoot();
            int object = objectFoot();

            switch (s & (Stencil.MSK_TRAP | Stencil.MSK_EXIT)) {
                case Stencil.MSK_TRAP_LIQUID:
                    if (type != Type.DROWNER) {
                        SpriteObject spr = GameController.getLevel().getSprObject(object);
                        if (spr != null) {
                            boolean triggered = true;
                            if (spr.canBeTriggered() && !spr.trigger(this)) {
                                triggered = false;
                            }
                            if (triggered) {
                                if (type == Type.BLOCKER || type == Type.FLAPPER_BLOCKER) {
                                    // erase blocker mask
                                    eraseBlockerMask();
                                }
                                GameController.sound.playVisualSFX(spr);
                                drowner = true;
                                newType = Type.DROWNER;
                            }
                        }
                    }
                    break;
                case Stencil.MSK_TRAP_FIRE:
                    if (type != Type.FRIER) {
                        SpriteObject spr = GameController.getLevel().getSprObject(object);
                        if (spr != null) {
                            boolean triggered = true;
                            if (spr.canBeTriggered() && !spr.trigger(this)) {
                                triggered = false;
                            }
                            if (triggered) {
                                if (type == Type.BLOCKER || type == Type.FLAPPER_BLOCKER) {
                                    // erase blocker mask
                                    eraseBlockerMask();
                                }
                                GameController.sound.playVisualSFX(spr);
                                newType = Type.FRIER;
                            }
                        }
                    }
                    break;
                case Stencil.MSK_TRAP_REMOVE:
                    {
                        SpriteObject spr = GameController.getLevel().getSprObject(object);
                        if (spr != null) {
                            boolean triggered = true;
                            if (spr.canBeTriggered() && !spr.trigger(this)) {
                                triggered = false;
                            }
                            if (triggered) {
                                if (type == Type.BLOCKER || type == Type.FLAPPER_BLOCKER) {
                                    // erase blocker mask
                                    eraseBlockerMask();
                                }
                                GameController.sound.playVisualSFX(spr);
                                hasDied = true;
                            }
                        }
                        break;
                    }
                case Stencil.MSK_EXIT:
                    if (Core.player.isMaximumExitPhysics()) { // All lem types exit in maximum exit physics mode
                        switch (newType) {
                           case FALLER:
                           case SPLATTER:
                           case CLIMBER:
                           case FLIPPER:
                           case BLOCKER:
                           case DROWNER:
                           case FRIER:
                           case SHRUGGER:
                           case EXPLODER:
                           case NUKE:
                               newType = Type.MAX_EXIT_LEM;
                               break;
                           default:
                               break;
                        }
                    }
                    switch (newType) {
                        case FLAPPER:
                        case WALKER:
                        case FLOATER:
                        case FLOATER_START:
                        case JUMPER:
                        case BASHER:
                        case MINER:
                        case BUILDER:
                        case DIGGER:
                        case DROWNER:
                        case MAX_EXIT_LEM:
                            SpriteObject spr = GameController.getLevel().getSprObject(object);
                            if (spr != null) {
                                boolean triggered = true;
                                if (spr.canBeTriggered() && !spr.trigger(this)) {
                                    triggered = false;
                                }
                                if (triggered) {
                                    if (type == Type.BLOCKER || type == Type.FLAPPER_BLOCKER) {
                                        // erase blocker mask
                                        eraseBlockerMask();
                                    }
                                    GameController.sound.playVisualSFX(spr);
                                    newType = Type.HOMER;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        // animate
        if (oldType == newType) {
            boolean trigger = false;
            switch (lemRes.animMode) {
                case LOOP:
                    if (++frameIdx >= lemRes.frames * TIME_SCALE) {
                        frameIdx = 0;
                    }
                    if (lemRes.maskStep > 0 && frameIdx % (lemRes.maskStep * TIME_SCALE) == 0) {
                        trigger = true;
                    }
                    break;
                case ONCE:
                    if (frameIdx < lemRes.frames * TIME_SCALE - 1) {
                        frameIdx++;
                    } else {
                        trigger = true;
                    }
                    break;
                default:
                    break;
            }
            if (trigger) {
                // Trigger condition reached?
                switch (type) {
                    case FLAPPER_BLOCKER:
                        eraseBlockerMask();
                        /* falls through */
                    case FLAPPER:
                        newType = Type.EXPLODER;
                        break;
                    case DROWNER:
                        playVisualSFX(Sound.Effect.DROWN);
                        /* falls through */
                    case FRIER:
                        if (flapper) {
                            newType = Type.EXPLODER;
                            break;
                        }
                        /* falls through */
                    case SPLATTER:
                        hasDied = true;
                        break;
                    case HOMER: {
                            hasExited = true;
                            GameController.increaseExited();
                        }
                        break;
                    case FLOATER_START:
                        type = Type.FLOATER; // should never happen
                        /* falls through */
                    case FLOATER:
                        frameIdx = 16;
                        break;
                    case FLIPPER:
                        newType = Type.WALKER;
                        break;
                    case DIGGER: {
                        // the dig mask must be applied to the bottom of the lemming
                        Mask m = lemRes.getMask(dir);

                        // check for conversion to faller
                        int freeMin = Integer.MAX_VALUE;
                        int xOld = x;
                        for (int i = -4; i < 6; i++) {
                            x = xOld + i;
                            free = freeBelow(DIGGER_STEP);
                            if (free < freeMin) {
                                freeMin = free;
                            }
                        }
                        x = xOld;
                        free = freeMin;
                        if (free > 0) {
                            //convert to faller or walker
                            newType = Type.FALLER;
                        }

                        y += DIGGER_STEP; // move down

                        int eraseMask = Stencil.MSK_BRICK;
                        int checkMask = 0;
                        if (!GameController.getLevel().getClassicSteel()) {
                            eraseMask |= Stencil.MSK_ONE_WAY;
                            checkMask |= Stencil.MSK_STEEL;
                        }
                        m.eraseMask(screenMaskX(), screenMaskY(), 0, eraseMask, checkMask);

                        // check for conversion to walker when hitting steel
                        if (!canDig(true)) {
                            newType = Type.WALKER;
                        }

                        break;}
                    case SHRUGGER:
                        if (aboveGround() > 0) {
                            if (dir == Direction.RIGHT) {
                                x -= 1; // step backward
                            } else {
                                x += 1;
                            }
                        }
                        newType = Type.WALKER;
                        break;
                    default:
                        break;
                }
            }
        }
        changeType(oldType, newType);
    }

    /**
     * Plays the specified sound effect at the lemming's current location.<br/>
     * If VisualSFX are enabled, a text-graphic is shown at the lemming's location, too.
     * @param e The Sound Effect to play
     */
    public void playVisualSFX(Sound.Effect e) {
        GameController.sound.playVisualSFX(e, footX(), midY()); //NOTE: footX was midX
    }

    /**
     * Check if a Lemming is to be turned by a blocker.
     * @return true if Lemming is to be turned, false otherwise
     */
    private boolean turnedByBlocker() {
        int s = stencilFoot();

        if (BooleanUtils.toBoolean(s & Stencil.MSK_BLOCKER_LEFT) && dir == Direction.RIGHT) {
            dir = Direction.LEFT;
            return true;
        }
        if (BooleanUtils.toBoolean(s & Stencil.MSK_BLOCKER_RIGHT) && dir == Direction.LEFT) {
            dir = Direction.RIGHT;
            return true;
        }
        if (BooleanUtils.toBoolean(s & Stencil.MSK_BLOCKER_CENTER)) {
            return false;
        }
        if (BooleanUtils.toBoolean(s & Stencil.MSK_TURN_LEFT) && dir == Direction.RIGHT) {
            int id = objectFoot();
            if (id >= 0) {
                SpriteObject spr = GameController.getLevel().getSprObject(id);
                GameController.sound.playVisualSFX(spr);
            }
            dir = Direction.LEFT;
            return true;
        }
        if (BooleanUtils.toBoolean(s & Stencil.MSK_TURN_RIGHT) && dir == Direction.LEFT) {
            int id = objectFoot();
            if (id >= 0) {
                SpriteObject spr = GameController.getLevel().getSprObject(id);
                GameController.sound.playVisualSFX(spr);
            }
            dir = Direction.RIGHT;
            return true;
        }
        return false;
    }

    /**
     * Change skill/type.
     * @param oldType old skill/type of Lemming
     * @param newType new skill/type of Lemming
     */
    private void changeType(final Type oldType, final Type newType) {
        if (oldType != newType) {
            type = newType;
            lemRes = getResource(type);
            if (newType == Type.DIGGER) {
                frameIdx = lemRes.frames * TIME_SCALE - 1; // start digging immediately
            } else {
                frameIdx = 0;
            }
            switch (newType) {
                case SPLATTER:
                    counter = 0;
                    explodeNumCtr = 0;
                    playVisualSFX(Sound.Effect.SPLAT);
                    break;
                case FLAPPER:
                case FLAPPER_BLOCKER:
                    flapper = true;
                    break;
                case EXPLODER:
                    counter = 0;
                    explodeNumCtr = 0;
                    addExplosion();
                    break;
                default:
                    break;
            }

            // some types can't change the skill - check this
            switch (newType) {
                case WALKER:
                case BASHER:
                case BUILDER:
                case SHRUGGER:
                case DIGGER:
                case MINER:
                    canChangeSkill = true;
                    break;
                default:
                    canChangeSkill = false;
                    break;
            }
        }
    }

    /**
     * Adds an explosion effect at the Lemming's position.
     */
    private void addExplosion() {
        GameController.addExplosion(footX(), midY()); //note: footX was midX()
    }

    private Type getExploderType() {
        switch (type) {
            case FLIPPER:
            case JUMPER:
            case WALKER:
            case DIGGER:
            case BASHER:
            case MINER:
            case SHRUGGER:
            case BUILDER:
            case FLAPPER:
            case HOMER:
                return Type.FLAPPER;
            case BLOCKER:
            case FLAPPER_BLOCKER:
                // don't erase blocker mask!
                return Type.FLAPPER_BLOCKER;
            default: // CLIMBER, DROWNER, FALLER, FLOATER, FLOATER_START, FRIER, SHRUGGER
                return Type.EXPLODER;
        }
    }

    /**
     * Get stencil value from the foot of the lemming
     * @return stencil value from the foot of the lemming
     */
    private int stencilFoot() {
        int xm = x;
        int ym = y;
        int retval;
        if (xm >= GameController.getLevel().getLeftBoundary()
                && xm < GameController.getWidth() + GameController.getLevel().getRightBoundary()
                && ym >= GameController.getLevel().getTopBoundary()
                && ym < GameController.getHeight()) {
            retval = GameController.getStencil().getMask(xm, ym);
        } else {
            retval = Stencil.MSK_EMPTY;
        }
        return retval;
    }

    /**
     * Get object ID from the foot of the lemming
     * @return stencil value from the middle of the lemming
     */
    private int objectFoot() {
        int xm = x;
        int ym = y;
        int retval;
        if (xm >= GameController.getLevel().getLeftBoundary()
                && xm < GameController.getWidth() + GameController.getLevel().getRightBoundary()
                && ym >= GameController.getLevel().getTopBoundary()
                && ym < GameController.getHeight()) {
            retval = GameController.getStencil().getMaskObjectID(xm, ym);
        } else {
            retval = -1;
        }
        return retval;
    }

    /**
     * Check if bashing is possible.
     * @return true if bashing is possible, false otherwise.
     */
    private boolean canBash() {
        int xm = x;
        int ypos = y - BASHER_CHECK_STEP;
        int xb;
        int bricks = 0;
        for (int i = 18; i < 22; i++) {
            if (dir == Direction.RIGHT) {
                xb = xm + i;
            } else {
                xb = xm - i + 1;
            }
            if (BooleanUtils.toBoolean(GameController.getStencil().getMask(xb, ypos) & Stencil.MSK_BRICK)) {
                bricks++;
            }
        }
        return bricks > 0;
    }

    /**
     * Check if bashing is possible.
     * @return true if bashing is possible, false otherwise.
     */
    private boolean canBashSteel(final boolean playSound) {
        int yMin = y - BASHER_CHECK_STEP_STEEL;
        int yMax = y - (GameController.getLevel().getClassicSteel()
                ? BASHER_CHECK_STEP_STEEL : BASHER_CHECK_STEP_STEEL_LOW);
        int xMin;
        int xMax;
        if (dir == Direction.RIGHT) {
            xMin = x + 16;
            xMax = x + 16 + 1;
        } else {
            xMin = x - 16;
            xMax = x - 16 + 1;
        }
        for (int yb = yMin; yb <= yMax; yb++) {
            for (int xb = xMin; xb <= xMax; xb++) {
                int sval = GameController.getStencil().getMask(xb, yb);
                boolean hitOneWayLeft = BooleanUtils.toBoolean(sval & Stencil.MSK_ONE_WAY_LEFT) && dir == Direction.RIGHT;
                boolean hitOneWayRight = BooleanUtils.toBoolean(sval & Stencil.MSK_ONE_WAY_RIGHT) && dir == Direction.LEFT;
                boolean hitSteel = BooleanUtils.toBoolean(sval & Stencil.MSK_STEEL);
                if (hitOneWayLeft || hitOneWayRight || hitSteel) {
                    if (playSound) {
                        SpriteObject spr = GameController.getLevel().getSprObject(GameController.getStencil().getMaskObjectID(xb, yb));
                        if (spr != null
                                && ((hitOneWayLeft && spr.getType() == SpriteObject.Type.ONE_WAY_LEFT)
                                        || (hitOneWayRight && spr.getType() == SpriteObject.Type.ONE_WAY_RIGHT)
                                        || (hitSteel && spr.getType() == SpriteObject.Type.STEEL))) {
                            GameController.sound.playVisualSFX(spr);
                        } else {
                            playVisualSFX(Sound.Effect.STEEL);
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if digging is possible.
     * @return true if digging is possible, false otherwise.
     */
    private boolean canDig(final boolean playSound) {
        boolean classicSteel = GameController.getLevel().getClassicSteel();
        for (int i = 0; i < 2; i++) {
            for (int j = (classicSteel ? 0 : -4); j < (classicSteel ? 1 : 6); j++) {
                int ym = y + i;
                int xm = x + j;
                int sval = GameController.getStencil().getMask(xm, ym);
                if (BooleanUtils.toBoolean(sval & Stencil.MSK_BRICK) && BooleanUtils.toBoolean(sval & Stencil.MSK_STEEL)) {
                    if (playSound) {
                        SpriteObject spr = GameController.getLevel().getSprObject(GameController.getStencil().getMaskObjectID(xm, ym));
                        if (spr != null && spr.getType() == SpriteObject.Type.STEEL) {
                            GameController.sound.playVisualSFX(spr);
                        } else {
                            playVisualSFX(Sound.Effect.STEEL);
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if mining is possible.
     * @return true if mining is possible, false otherwise.
     */
    private boolean canMine(final boolean start, final boolean playSound) {
        if (x < GameController.getLevel().getLeftBoundary()
                || x >= GameController.getWidth() + GameController.getLevel().getRightBoundary()) {
            if (!start && playSound) {
                playVisualSFX(Sound.Effect.STEEL);
            }
            return false;
        }

        int yMin = y - MINER_CHECK_STEP_STEEL;
        int yMax = y - MINER_CHECK_STEP_STEEL + 1;
        int xMin;
        int xMax;
        if (dir == Direction.RIGHT) {
            xMin = x + 14;
            xMax = x + 14 + 1;
        } else {
            xMin = x - 14;
            xMax = x - 14 + 1;
        }
        for (int yb = yMin; yb <= yMax; yb++) {
            for (int xb = xMin; xb <= xMax; xb++) {
                int sval = GameController.getStencil().getMask(xb, yb);
                boolean hitOneWayLeft = BooleanUtils.toBoolean(sval & Stencil.MSK_ONE_WAY_LEFT) && dir == Direction.RIGHT;
                boolean hitOneWayRight = BooleanUtils.toBoolean(sval & Stencil.MSK_ONE_WAY_RIGHT) && dir == Direction.LEFT;
                boolean hitSteel = BooleanUtils.toBoolean(sval & Stencil.MSK_STEEL);
                if (hitOneWayLeft || hitOneWayRight || hitSteel) {
                    if (playSound) {
                        SpriteObject spr = GameController.getLevel().getSprObject(GameController.getStencil().getMaskObjectID(xb, yb));
                        if (spr != null
                                && ((hitOneWayLeft && spr.getType() == SpriteObject.Type.ONE_WAY_LEFT)
                                        || (hitOneWayRight && spr.getType() == SpriteObject.Type.ONE_WAY_RIGHT)
                                        || (hitSteel && spr.getType() == SpriteObject.Type.STEEL))) {
                            GameController.sound.playVisualSFX(spr);
                        } else {
                            playVisualSFX(Sound.Effect.STEEL);
                        }
                    }
                    return false;
                }
            }
        }
        yMin = y;
        yMax = y + 1;
        xMin = x;
        xMax = x + 1;
        for (int yb = yMin; yb <= yMax; yb++) {
            for (int xb = xMin; xb <= xMax; xb++) {
                int sval = GameController.getStencil().getMask(xb, yb);
                boolean hitOneWayLeft = !start && BooleanUtils.toBoolean(sval & Stencil.MSK_ONE_WAY_LEFT) && dir == Direction.RIGHT;
                boolean hitOneWayRight = !start && BooleanUtils.toBoolean(sval & Stencil.MSK_ONE_WAY_RIGHT) && dir == Direction.LEFT;
                boolean hitSteel = BooleanUtils.toBoolean(sval & Stencil.MSK_STEEL);
                if (hitOneWayLeft || hitOneWayRight || hitSteel) {
                    if (playSound) {
                        SpriteObject spr = GameController.getLevel().getSprObject(GameController.getStencil().getMaskObjectID(xb, yb));
                        if (spr != null
                                && ((hitOneWayLeft && spr.getType() == SpriteObject.Type.ONE_WAY_LEFT)
                                        || (hitOneWayRight && spr.getType() == SpriteObject.Type.ONE_WAY_RIGHT)
                                        || (hitSteel && spr.getType() == SpriteObject.Type.STEEL))) {
                            GameController.sound.playVisualSFX(spr);
                        } else {
                            playVisualSFX(Sound.Effect.STEEL);
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get number of free pixels below the lemming (max of step is checked).
     * @return number of free pixels below the lemming
     */
    private int freeBelow(final int step) {
        if (x < GameController.getLevel().getLeftBoundary()
                || x >= GameController.getWidth() + GameController.getLevel().getRightBoundary()) {
            return 0;
        }
        int free = 0;
        int pos;
        Stencil stencil = GameController.getStencil();
        int yb = y;
        pos = x + yb * GameController.getWidth(); // line below the lemming
        for (int i = 0; i < step; i++) {
            if (yb + i >= GameController.getHeight()) {
                return Integer.MAX_VALUE; // convert most skills to faller
            }
            int s = stencil.getMask(pos);
            if (!BooleanUtils.toBoolean(s & Stencil.MSK_BRICK)) {
                free++;
            } else {
                break;
            }
            pos += GameController.getWidth();
        }
        return free;
    }

    private void flipDir() {
        dir = (dir == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
    }

    /**
     * Check if Lemming reached the left or right border of the level and was turned.
     * @return true if lemming was turned, false otherwise.
     */
    private boolean flipDirBorder() {
        boolean flip = false;
        if (lemRes.dirs > 1) {
            if (x < GameController.getLevel().getLeftBoundary() && dir == Direction.LEFT) {
                x = GameController.getLevel().getLeftBoundary() - 1;
                flip = true;
            } else if (x >= GameController.getWidth() + GameController.getLevel().getRightBoundary()
                    && dir == Direction.RIGHT) {
                x = GameController.getWidth() + GameController.getLevel().getRightBoundary();
                flip = true;
            }
        }
        if (flip) {
            flipDir();
        }
        return flip;
    }

    /**
     * Checks whether there are any free pixels above the the builder (max of step is checked).
     * @return whether there are any free pixels above the the builder
     */
    private boolean freeAboveBuilder() {
        if (dir == Direction.LEFT && x - 3 < GameController.getLevel().getLeftBoundary()
                || dir == Direction.RIGHT && x + 4 >= GameController.getWidth() + GameController.getLevel().getRightBoundary()) {
            return false;
        }

        int yMin = y - 18;
        int yMax = y - 17;
        int xm;
        if (dir == Direction.RIGHT) {
            xm = x + 4;
        } else {
            xm = x - 3;
        }
        Stencil stencil = GameController.getStencil();
        for (int yb = yMin; yb <= yMax; yb++) {
            if (BooleanUtils.toBoolean(stencil.getMask(xm, yb) & Stencil.MSK_BRICK)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get number of free pixels above the lemming (max of step is checked).
     * @return number of free pixels above the lemming
     */
    private boolean freeAboveClimber() {
        if (x < GameController.getLevel().getLeftBoundary()
                || x >= GameController.getWidth() + GameController.getLevel().getRightBoundary()
                || x <= GameController.getLevel().getTopBoundary()) {
            return false;
        }

        int ym = y - 18;
        int xm = x;
        if (dir == Direction.LEFT) {
            xm += 1;
        } else {
            xm -= 1;
        }
        Stencil stencil = GameController.getStencil();
        return ym >= 0 && !BooleanUtils.toBoolean((stencil.getMask(xm, ym) & Stencil.MSK_BRICK));
    }

    /**
     * Check if Lemming has fallen to/through the bottom of the level.
     * @return true if Lemming has fallen to/through the bottom of the level, false otherwise
     */
    private boolean crossedLowerBorder() {
        if (y >= GameController.getHeight() + GameController.getLevel().getBottomBoundary()) {
            hasDied = true;
            playVisualSFX(Sound.Effect.DIE);
            return true;
        }
        return false;
    }

    /**
     * Get the number of pixels of walkable ground above the Lemming's foot.
     * @return number of pixels of walkable ground above the Lemming's foot.
     */
    private int aboveGround() {
        if (x < GameController.getLevel().getLeftBoundary()
                || x >= GameController.getWidth() + GameController.getLevel().getRightBoundary()) {
            return GameController.getHeight() + 1;
        }

        int ym = y - 1;
        if (ym >= GameController.getHeight()) {
            return 0;
        }
        int pos = x;
        Stencil stencil = GameController.getStencil();
        pos += ym * GameController.getWidth();
        int levitation;
        for (levitation = 0; levitation < WALKER_OBSTACLE_HEIGHT; levitation++, pos -= GameController.getWidth(), ym--) {
            if (ym < GameController.getLevel().getTopBoundary() - 1) {
                return WALKER_OBSTACLE_HEIGHT + 1; // forbid leaving level to the top
            }
            if (!BooleanUtils.toBoolean(stencil.getMask(pos) & Stencil.MSK_BRICK)) {
                break;
            }
        }
        return levitation;
    }

    /**
     * Check if climber reached a plateau he can walk on.
     * @return true if climber reached a plateau he can walk on, false otherwise
     */
    private boolean reachedPlateau(final int hand) {
        if (x - 2 < GameController.getLevel().getLeftBoundary()
                || x + 2 >= GameController.getWidth() + GameController.getLevel().getRightBoundary()) {
            return false;
        }
        int ym = y - hand;
        if (ym >= GameController.getHeight()) {
            return true;
        } else if (ym < 0 || ym <= GameController.getLevel().getTopBoundary()) {
            return false;
        }
        int pos = x;
        pos += ym * GameController.getWidth();
        return !BooleanUtils.toBoolean(GameController.getStencil().getMask(pos) & Stencil.MSK_BRICK);
    }

    private void eraseBlockerMask() {
        LemmingResource res = getResource(Type.BLOCKER);
        Mask m = res.getMask(dir);
        int maskX = x - res.maskX;
        int maskY = y - res.maskY;
        m.clearType(maskX, maskY, 0, Stencil.MSK_BLOCKER_LEFT);
        m.clearType(maskX, maskY, 1, Stencil.MSK_BLOCKER_CENTER);
        m.clearType(maskX, maskY, 2, Stencil.MSK_BLOCKER_RIGHT);
    }

    /**
     * Replace two colors in the animation frames other colors.
     * Used to shift the color of debris to level-specific colors.
     * @param replaceCol first color to replace
     * @param replaceCol2 second color to replace
     */
    public static void replaceColors(final int replaceCol, final int replaceCol2) {
        lemmings.stream().forEach(lemm -> { // go through all the lemmings
            lemm.replaceColors(templateColor, replaceCol, templateColor2, replaceCol2);
        });
    }

    /**
     * Load images used for Lemming animations.
     * @throws ResourceException
     */
    public static void loadLemmings() throws ResourceException {
        explodeFont = new ExplodeFont();
        // read lemmings definition file
        Resource resource = Core.findResource(LEMM_INI_STR, true);
        Props p = new Props();
        if (!p.load(resource)) {
            throw new ResourceException(LEMM_INI_STR);
        }
        lemmings.clear();
        // read lemmings
        templateColor = p.getInt("templateColor", DEF_TEMPLATE_COLOR) & 0x00ffffff;
        templateColor2 = p.getInt("templateColor2", templateColor) & 0x00ffffff;
        Type[] lemmTypes = Type.values();
        for (int i = 0; i < NUM_RESOURCES; i++) {
            // frames, directions, animation type
            Type type = lemmTypes[i];
            boolean bidirectional = type.bidirectional;
            LemmingResource newLemResource;
            if (type.frames > 0) {
                resource = Core.findResource(
                        "gfx/lemming/lemm_" + type.name().toLowerCase(Locale.ROOT) + ".png",
                        Core.IMAGE_EXTENSIONS);
                LemmImage sourceImg = Core.loadLemmImage(resource);
                if (bidirectional) {
                    resource = Core.findResource(
                            "gfx/lemming/lemm_" + type.name().toLowerCase(Locale.ROOT) + "_left.png",
                            Core.IMAGE_EXTENSIONS);
                    LemmImage sourceImgLeft = Core.loadLemmImage(resource);
                    newLemResource = new LemmingResource(sourceImg, sourceImgLeft, type.frames);
                } else {
                    newLemResource = new LemmingResource(sourceImg, type.frames);
                }
            } else {
                newLemResource = new LemmingResource();
            }
            newLemResource.animMode = type.loop ? Animation.LOOP : Animation.ONCE;
            // read mask
            if (type.maskFrames > 0) {
                // mask_Y: frames, directions, step
                resource = Core.findResource(
                        "gfx/lemming/mask_" + type.name().toLowerCase(Locale.ROOT) + ".png",
                        Core.IMAGE_EXTENSIONS);
                LemmImage sourceImg = Core.loadLemmImage(resource);
                List<Mask> masks = new ArrayList<>(2);
                masks.add(new Mask(sourceImg, type.maskFrames));
                if (bidirectional) {
                    resource = Core.findResource(
                            "gfx/lemming/mask_" + type.name().toLowerCase(Locale.ROOT) + "_left.png",
                            Core.IMAGE_EXTENSIONS);
                    LemmImage sourceImgLeft = Core.loadLemmImage(resource);
                    masks.add(new Mask(sourceImgLeft, type.maskFrames));
                }
                newLemResource.setMasks(masks);
                newLemResource.maskStep = type.maskStep;
            }
            // read foot position and size
            int[] val = p.getIntArray("pos_" + i, null);
            if (val != null && val.length == 3) {
                newLemResource.footX = val[0];
                newLemResource.footY = val[1];
                newLemResource.size  = val[2];
            }
            val = p.getIntArray("maskPos_" + i, null);
            if (val != null && val.length == 2) {
                newLemResource.maskX = val[0];
                newLemResource.maskY = val[1];
            }
            lemmings.add(newLemResource);
        }
    }

    /**
     * Get display info for this Lemming.
     * @return display name of this Lemming
     */
    public String getLemmingInfo() {
        String n = type.name;
        if (!n.isEmpty()) {
            if (canClimb && canFloat) {
                n += " (A)";
            } else if (canClimb && type != Type.CLIMBER && type != Type.FLIPPER) {
                n += " (C)";
            } else if (canFloat && type != Type.FLOATER && type != Type.FLOATER_START) {
                n += " (F)";
            }
        }
        return n;
    }

    /**
     * Get current skill/type of this Lemming.
     * @return current skill/type of this Lemming
     */
    public Type getSkill() {
        return type;
    }

    /**
     * Set new skill/type of this Lemming.
     * @param newSkill new skill/type
     * @param playSound
     * @param r checks if it's a replay event
     * @return true if a change was possible, false otherwise
     */
    public boolean setSkill(final Type newSkill, boolean playSound, ReplayEvent r) {
        if (r == null || newSkill != Type.FLAPPER) {
            return setSkill(newSkill, playSound);
        }

        // If the assignment is a replay event, check the Timed Bomber flag
        if (r instanceof ReplayAssignSkillEvent) {
            ReplayAssignSkillEvent rs = (ReplayAssignSkillEvent) r;

            // If the event has the Timed Bomber flag set, handle it as a Timed Bomber
            if (rs.isTimedBomber()) {
                if (explodeNumCtr == 0) {
                    explodeNumCtr = MAX_BOMB_TIMER;
                    explodeCtr = 0;
                    return playSetSkillSound(true, playSound);
                } else {
                    return playSetSkillSound(false, playSound);
                }
            } else {
                changeType(type, getExploderType());
                return playSetSkillSound(true, playSound);
            }
        }

        return setSkill(newSkill, playSound); // Should never happen, but just in case
    }

    /**
     * Set new skill/type of this Lemming
     * @param newSkill new skill/type
     * @param playSound
     * @return true if a change was possible, false otherwise
     */
    public boolean setSkill(final Type newSkill, boolean playSound) {
        if (newSkill == type || hasDied) {
            return playSetSkillSound(false, playSound);
        }

        // check types which can't even get an additional skill anymore
        switch (type) {
            case DROWNER:
            case HOMER:
            case FRIER:
            case FLAPPER:
                if (newSkill != Type.NUKE) {
                    return playSetSkillSound(false, playSound);
                }
            case SPLATTER:
            case EXPLODER:
                if (newSkill == Type.NUKE) {
                    nuke = true;
                }
                return playSetSkillSound(false, playSound);
            default:
                break;
        }

        // check additional skills
        switch (newSkill) {
            case CLIMBER:
                if (canClimb || type == Type.BLOCKER) {
                    return playSetSkillSound(false, playSound);
                } else {
                    canClimb = true;
                    return playSetSkillSound(true, playSound);
                }
            case FLOATER:
                if (canFloat || type == Type.BLOCKER) {
                    return playSetSkillSound(false, playSound);
                } else {
                    canFloat = true;
                    return playSetSkillSound(true, playSound);
                }
            case NUKE: // special case:  nuke request
                if (nuke) {
                    return playSetSkillSound(false, playSound);
                }
                nuke = true;
                if (explodeNumCtr == 0) {
                    explodeNumCtr = MAX_BOMB_TIMER;
                    explodeCtr = 0;
                    return playSetSkillSound(true, playSound);
                } else {
                    return playSetSkillSound(false, playSound);
                }
            case FLAPPER:
                if (GameController.isOptionEnabled(GameController.SLTooOption.TIMED_BOMBERS)) {
                    if (explodeNumCtr == 0) {
                        explodeNumCtr = MAX_BOMB_TIMER;
                        explodeCtr = 0;
                        return playSetSkillSound(true, playSound);
                    } else {
                        return playSetSkillSound(false, playSound);
                    }
                } else {
                    changeType(type, getExploderType());
                    return playSetSkillSound(true, playSound);
                }
            default:
                break;
        }

        // check main skills
        if (canChangeSkill) {
            switch (newSkill) {
                case DIGGER:
                    if (canDig(playSound)) {
                        changeType(type, newSkill);
                        counter = 0;
                        return playSetSkillSound(true, playSound);
                    } else {
                        playSound = false;
                        return playSetSkillSound(false, playSound);
                    }
                case MINER:
                    if (canMine(true, playSound)) {
                        y += 2;
                        changeType(type, newSkill);
                        counter = 0;
                        return playSetSkillSound(true, playSound);
                    } else {
                        playSound = false;
                        return playSetSkillSound(false, playSound);
                    }
                case BASHER:
                    if (canBashSteel(playSound)) {
                        changeType(type, newSkill);
                        counter = 0;
                        return playSetSkillSound(true, playSound);
                    } else {
                        playSound = false;
                        return playSetSkillSound(false, playSound);
                    }
                case BUILDER:
                    if (y < GameController.getLevel().getTopBoundary() + 2) {
                        return playSetSkillSound(false, playSound);
                    } else {
                        changeType(type, newSkill);
                        counter = 0;
                        return playSetSkillSound(true, playSound);
                    }
                case BLOCKER:
                    LemmingResource lem = getResource(Type.BLOCKER);
                    Mask m = lem.getMask(Direction.LEFT);
                    int maskX = x - lem.maskX;
                    int maskY = y - lem.maskY;
                    for (int i = 0; i < m.getNumFrames(); i++) {
                        if (m.checkType(maskX, maskY, i, Stencil.MSK_BLOCKER | Stencil.MSK_EXIT)) {
                            return playSetSkillSound(false, playSound); // overlaps exit or existing blocker
                        }
                    }
                    //didn't overlap, so let's change the type
                    changeType(type, newSkill);
                    counter = 0;
                    // set blocker mask
                    m.setBlockerMask(maskX, maskY);
                    return playSetSkillSound(true, playSound);
                default:
                    break;
            }
        }

        return playSetSkillSound(false, playSound);
    }

    /**
     * Play either the ASSIGN_SKILL sfx, or the INVALID sfx.
     * @param canSet is this a valid skill assign?
     * @param playSound is a sound effect supposed to be played?
     * @return true if a change was possible, false otherwise
     */
    private boolean playSetSkillSound(boolean validAssign, boolean playSound) {
        if (validAssign) {
            if (playSound) {
                GameController.sound.play(Sound.Effect.ASSIGN_SKILL, getPan());
            }
        } else {
            if (playSound) {
                GameController.sound.play(Sound.Effect.INVALID, getPan());
            }
        }
        return validAssign;
    }

    /**
     * Get width of animation frame in pixels.
     * @return width of animation frame in pixels
     */
    public int width() {
        return lemRes.width;
    }

    /**
     * Get height of animation frame in pixels.
     * @return height of animation frame in pixels
     */
    public int height() {
        return lemRes.height;
    }

    /**
     * Get static resource for a skill/type
     * @param type skill/type
     * @return static resource for this skill/type
     */
    private static LemmingResource getResource(final Type type) {
        return lemmings.get(getOrdinal(type));
    }

    /**
     * Get X coordinate of upper left corner of animation frame.
     * @return X coordinate of upper left corner of animation frame
     */
    public int screenX() {
        return x - lemRes.footX;
    }

    /**
     * Get Y coordinate of upper left corner of animation frame
     * @return Y coordinate of upper left corner of animation frame
     */
    public int screenY() {
        return y - lemRes.footY;
    }

    /**
     * Get x coordinate of middle of lemming
     * @return
     */
    public int midX() {
        // BOOKMARK TODO: figure out how to avoid a magic number of 3.
        return x + 3;
    }

    /**
     * Get y coordinate of foot less the "mid" position above the foot
     * @return
     */
    public int midY() {
        return y - lemRes.size;
    }

    /**
     * Get x coordinate of foot in pixels
     * @return
     */
    public int footX() {
        return x;
    }

    /**
     * Get y coordinate of foot in pixels
     * @return
     */
    public int footY() {
        return y;
    }

    public int screenMaskX() {
        return x - lemRes.maskX;
    }

    public int screenMaskY() {
        return y - lemRes.maskY;
    }

    /**
     * Get heading of Lemming.
     * @return heading of Lemming
     */
    public Direction getDirection() {
        return dir;
    }

    /**
     * Get current animation frame for this Lemming.
     * @return current animation frame for this Lemming
     */
    public LemmImage getImage() {
        return lemRes.getImage(dir, frameIdx / TIME_SCALE);
    }

    /**
     * Get image for explosion countdown.
     * @return image for explosion countdown (or null if no explosion countdown)
     */
    public LemmImage getCountdown() {
        if (explodeNumCtr == 0) {
            return null;
        } else {
            return explodeFont.getImage(explodeNumCtr - 1);
        }
    }

    /**
     * Used for replay: start to display the selection image.
     */
    public void setSelected() {
        selectCtr = 20;
    }

    /**
     * Get the selection image for replay.
     * @return the selection image (or null if no selection displayed)
     */
    public LemmImage getSelectImg() {
        if (selectCtr == 0) {
            return null;
        } else {
            return MiscGfx.getImage(MiscGfx.Index.SELECT);
        }
    }

    /**
     * Get: Lemming has died.
     * @return true if Lemming has died, false otherwise
     */
    public boolean hasDied() {
        return hasDied;
    }

    /**
     * Get: Lemming has exited the level.
     * @return true if Lemming has exited the level, false otherwise
     */
    public boolean hasExited() {
        return hasExited;
    }

    /**
     * Get: Lemming is to be nuked.
     * @return true if Lemming is to be nuked, false otherwise
     */
    public boolean nuke() {
        return nuke;
    }

    /**
     * Get: Lemming can float.
     * @return true if Lemming can float, false otherwise
     */
    public boolean canFloat() {
        return canFloat;
    }

    /**
     * Get: Lemming can climb.
     * @return true if Lemming can climb, false otherwise
     */
    public boolean canClimb() {
        return canClimb;
    }

    /**
     * Get: Lemming can get a new skill.
     * @return true if Lemming can get a new skill, false otherwise
     */
    public boolean canChangeSkill() {
        return canChangeSkill;
    }

    public boolean hasTimer() {
        return explodeNumCtr > 0;
    }

    public double getPan() {
        return lemmini.sound.Sound.getPan(x);
    }
}

/**
 * Storage class for a Lemming.
 * @author Volker Oth
 */
class LemmingResource {
    /** relative foot X position in pixels inside bitmap */
    int footX;
    /** relative foot Y position in pixels inside bitmap */
    int footY;
    /** "mid" position above foot in pixels */
    int size;
    int maskX;
    int maskY;
    /** width of image in pixels */
    int width;
    /** height of image in pixels */
    int height;
    /** number of animation frames */
    int frames;
    /** animation mode */
    Lemming.Animation animMode;
    /** number of directions (1 or 2) */
    int dirs;
    int maskStep;
    /** list of images to store the animation [Direction][AnimationFrame] */
    private final List<List<LemmImage>> img = new ArrayList<>(2);

    /** for recoloring purposes */
    private final List<List<LemmImage>> originalColorImg = new ArrayList<>(2);

    /** list of removal masks used for digging/bashing/mining/explosions etc. [Direction] */
    private List<Mask> mask = null;

    /**
     * Constructor.
     */
    LemmingResource() {
        width = 1;
        height = 1;
        dirs = 1;
        animMode = Lemming.Animation.NONE;
        List<LemmImage> imgTemp = new ArrayList<>(1);
        imgTemp.add(ToolBox.createLemmImage(width, height, Transparency.BITMASK));
        img.add(imgTemp);
        originalColorImg.add(new ArrayList<>(imgTemp));
    }

    /**
     * Constructor.
     * @param sourceImg  image containing animation frames (one above the other)
     * @param animFrames number of animation frames.
     */
    LemmingResource(final LemmImage sourceImg, final int animFrames) {
        frames = animFrames;
        width = sourceImg.getWidth();
        height = sourceImg.getHeight() / animFrames;
        dirs = 1;
        animMode = Lemming.Animation.NONE;
        List<LemmImage> anim = ToolBox.getAnimation(sourceImg, animFrames);
        img.add(anim);
        originalColorImg.add(new ArrayList<>(anim));
    }

    /**
     * Constructor.
     * @param sourceImg  image containing animation frames (one above the other)
     * @param sourceImgLeft
     * @param animFrames number of animation frames.
     */
    LemmingResource(final LemmImage sourceImg, final LemmImage sourceImgLeft, final int animFrames) {
        frames = animFrames;
        width = Math.min(sourceImg.getWidth(), sourceImgLeft.getWidth());
        height = Math.min(sourceImg.getHeight() / animFrames, sourceImgLeft.getHeight() / animFrames);
        dirs = 2;
        animMode = Lemming.Animation.NONE;
        List<LemmImage> animRight = ToolBox.getAnimation(sourceImg, animFrames);
        List<LemmImage> animLeft = ToolBox.getAnimation(sourceImgLeft, animFrames);
        img.add(animRight);
        img.add(animLeft);
        originalColorImg.add(new ArrayList<>(animRight));
        originalColorImg.add(new ArrayList<>(animLeft));
    }

    /**
     * Get the mask for stencil manipulation.
     * @param dir Direction
     * @return mask for stencil manipulation
     */
    Mask getMask(final Lemming.Direction dir) {
        if (dirs > 1) {
            return mask.get(dir.ordinal());
        } else {
            return mask.get(0);
        }
    }

    /**
     * Set the masks for stencil manipulation.
     * @param m list of masks for stencil manipulation
     */
    void setMasks(final List<Mask> m) {
        mask = m;
    }

    /**
     * Get specific animation frame.
     * @param dir Direction.
     * @param frame Index of animation frame.
     * @return specific animation frame
     */
    LemmImage getImage(final Lemming.Direction dir, final int frame) {
        if (dirs > 1) {
            return img.get(dir.ordinal()).get(frame);
        } else {
            return img.get(0).get(frame);
        }
    }

    void replaceColors(final int templateCol, final int replaceCol,
            final int templateCol2, final int replaceCol2) {

        for (ListIterator<List<LemmImage>> itd = originalColorImg.listIterator();
                itd.hasNext(); ) { // go though all directions
            int di = itd.nextIndex();
            for (ListIterator<LemmImage> itf = itd.next().listIterator();
                    itf.hasNext(); ) { // go through all frames
                int fi = itf.nextIndex();
                LemmImage i = ToolBox.copyLemmImage(itf.next());
                i.replaceColor(templateCol, replaceCol);
                i.replaceColor(templateCol2, replaceCol2);
                img.get(di).set(fi, i);
            }
        }
        if (mask != null) {
            mask.stream().filter(Objects::nonNull).forEach(d -> { // go though all directions
                d.replaceColors(templateCol, replaceCol, templateCol2, replaceCol2);
            });
        }
    }
}


/**
 * Used to manage the font for the explosion counter.
 * @author Volker Oth
 */
class ExplodeFont {

    /** list of images for each counter value */
    private final List<LemmImage> img;

    /**
     * Constructor.
     * @param cmp parent component
     * @throws ResourceException
     */
    ExplodeFont() throws ResourceException {
        Resource resource = Core.findResource("gfx/lemming/countdown.png", Core.IMAGE_EXTENSIONS);
        LemmImage sourceImg = Core.loadLemmImage(resource);
        img = ToolBox.getAnimation(sourceImg, 5);
    }

    /**
     * Get image for a counter value (0-9)
     * @param num counter value (0-9)
     * @return
     */
    LemmImage getImage(final int num) {
        return img.get(num);
    }
}