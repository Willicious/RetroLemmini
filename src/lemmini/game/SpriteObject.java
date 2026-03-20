package lemmini.game;

import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.commons.lang3.ArrayUtils;

import lemmini.gameutil.Sprite;
//import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemImage;
import lemmini.tools.ToolBox;

/*
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
 * Extension of {@link Sprite} to define animated level objects as exits etc.
 * @author Volker Oth
 * Modified by Ryan Sakowski
 */
public class SpriteObject extends Sprite {

    /** Type of level object */
    public static enum Type {
        /** no influence on gameplay */
        PASSIVE (false, false),
        /** Makes a lemming turn left. */
        TURN_LEFT (true, false),
        /** Makes a lemming turn right. */
        TURN_RIGHT (true, false),
        /** left arrows - no digging to the right */
        ONE_WAY_LEFT (false, true),
        /** right arrows - no digging to the left */
        ONE_WAY_RIGHT (false, true),
        /** up arrows - no digging, bashing or mining (bombers only) */
        ONE_WAY_UP (false, true),
        /** down arrows - no bashing in either direction */
        ONE_WAY_DOWN (false, true),
        /** trap triggering drowning animation */
        TRAP_DROWN (true, false),
        /** trap triggering a replacement with special death animation */
        TRAP_REPLACE (true, false),
        /** trap triggering default death animation */
        TRAP_DIE (true, false),
        /** level exit (active part!) */
        EXIT (true, false),
        /** steel */
        STEEL (false, false),
        /** level entrance */
        ENTRANCE (false, false);

        private final boolean triggeredByFoot;
        private final boolean oneWay;

        private Type(boolean triggeredByFoot, boolean sometimesIndestructible) {
            this.triggeredByFoot = triggeredByFoot;
            this.oneWay = sometimesIndestructible;
        }

        public boolean isTriggeredByFoot() {
            return triggeredByFoot;
        }

        public boolean isOneWay() {
            return oneWay;
        }
    }

    /** x position in pixels */
    private int x;
    /** y position in pixels */
    private int y;
    /** Type of level object */
    private Type type;
    /** collision mask - only this part is copied into the stencil */
    private boolean[][] mask;
    private int maskOffsetX;
    private int maskOffsetY;
    private int maskWidth;
    private int maskHeight;
    private boolean visOnTerrain;

    /**
     * Get Type depending on integer value from INI.
     */
    public static Type getType(final int t) {
        switch (t) {
            case 1:
                return Type.TURN_LEFT;
            case 2:
                return Type.TURN_RIGHT;
            case 3:
                return Type.ONE_WAY_RIGHT;
            case 4:
                return Type.ONE_WAY_LEFT;
            case 5:
                return Type.TRAP_DROWN;
            case 6:
                return Type.TRAP_REPLACE;
            case 7:
                return Type.TRAP_DIE;
            case 8:
                return Type.EXIT;
            case 9:
                return Type.STEEL;
            case 10:
            	return Type.ONE_WAY_UP;
            case 11:
            	return Type.ONE_WAY_DOWN;
            case 32:
                return Type.ENTRANCE;
            default:
                return Type.PASSIVE;
        }
    }

    /**
     * Constructor.
     */
    public SpriteObject(final LemImage sourceImg, final int animFrames, final int animSpeed, final boolean modifiable) {
        super(sourceImg, animFrames, animSpeed, modifiable);
        type = Type.PASSIVE;
        x = 0;
        y = 0;
    }

    /**
     * Constructor.
     */
    public SpriteObject(LemStyle.LvlObject obj, LemStyle.Orientation orientation, final boolean modifiable) throws LemException {
        super(obj.getImages(orientation), obj.getSpeed(), modifiable);
        animMode = obj.getAnimationMode();
        type = obj.getType();
        mask = obj.getMask();
        maskOffsetX = obj.getMaskOffsetX();
        maskOffsetY = obj.getMaskOffsetY();
        maskWidth = ArrayUtils.isNotEmpty(mask) ? mask[0].length : 0;
        maskHeight = mask.length;
        sound = obj.getSound().clone();
        x = 0;
        y = 0;
    }

    /**
     * Constructor. Create Sprite from another Sprite.
     */
    public SpriteObject(final SpriteObject src) {
        super(src);
        x = src.getX();
        y = src.getY();
        mask = src.mask.clone();
        for (int i = 0; i < mask.length; i++) {
            mask[i] = mask[i].clone();
        }
        maskOffsetX = src.getMaskOffsetX();
        maskOffsetY = src.getMaskOffsetY();
        maskWidth = src.getMaskWidth();
        maskHeight = src.getMaskHeight();
        type = src.type;
        mask = src.mask; // flat copy - no deep copy needed!
    }

    /**
     * Set the collision mask.
     */
    /*void setMask(final LemImage imgMask) {
        maskWidth = imgMask.getWidth();
        maskHeight = imgMask.getHeight();
        mask = new int[maskWidth * maskHeight];
        maskOffsetX = 0;
        maskOffsetY = 0;
        GraphicsContext g = null;
        try {
            g = imgMask.createGraphicsContext();
            g.grabPixels(imgMask, 0, 0, maskWidth, maskHeight, mask, 0, maskWidth);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }*/

    /**
     * Set the collision mask.
     */
    /*void setMask(final LemImage imgMask, final int xOffset, final int yOffset) {
        if (imgMask != null) {
            maskWidth = imgMask.getWidth();
            maskHeight = imgMask.getHeight();
            mask = new int[maskWidth * maskHeight];
            GraphicsContext g = null;
            try {
                g = imgMask.createGraphicsContext();
                g.grabPixels(imgMask, 0, 0, maskWidth, maskHeight, mask, 0, maskWidth);
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }
        maskOffsetX = xOffset;
        maskOffsetY = yOffset;
    }*/

    /**
     * Get type of level object.
     */
    int getMaskType() {
        switch (type) {
            case TURN_LEFT:
                return Stencil.MSK_TURN_LEFT;
            case TURN_RIGHT:
                return Stencil.MSK_TURN_RIGHT;
            case ONE_WAY_LEFT:
                return Stencil.MSK_ONE_WAY_LEFT;
            case ONE_WAY_RIGHT:
                return Stencil.MSK_ONE_WAY_RIGHT;
            case ONE_WAY_UP:
                return Stencil.MSK_ONE_WAY_UP;
            case ONE_WAY_DOWN:
                return Stencil.MSK_ONE_WAY_DOWN;
            case TRAP_DROWN:
                return Stencil.MSK_TRAP_LIQUID;
            case TRAP_REPLACE:
                return Stencil.MSK_TRAP_REMOVE;
            case TRAP_DIE:
                return Stencil.MSK_TRAP_FIRE;
            case EXIT:
                return Stencil.MSK_EXIT;
            case STEEL:
                return Stencil.MSK_STEEL_OBJECT;
            default:
                return -1;
        }
    }

    /**
     * Set x position.
     */
    public final void setX(final int xi) {
        x = xi;
    }

    /**
     * Get x position.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the x visual mid-point of the object
     */
    public int midX() {
        return x + (width/2);
    }

    /**
     * Set y position.
     */
    public final void setY(final int yi) {
        y = yi;
    }

    /**
     * Get y position.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the x visual mid-point of the object
     */
    public int midY() {
        return y + (height/2);
    }

    public int getMaskOffsetX() {
        return maskOffsetX;
    }

    public int getMaskOffsetY() {
        return maskOffsetY;
    }

    public int getMaskWidth() {
        return maskWidth;
    }

    public int getMaskHeight() {
        return maskHeight;
    }

    /**
     * Set whether this object is visible only on terrain.
     */
    public void setVisOnTerrain(boolean vis) {
        visOnTerrain = vis;
        if (vis && !modifiable) {
            frames = new ArrayList<>(frames);
            origColors = new int[numFrames][];
            for (ListIterator<LemImage> lit = frames.listIterator(); lit.hasNext(); ) {
                int i = lit.nextIndex();
                LemImage frame = ToolBox.copyLemImage(lit.next());
                lit.set(frame);
                origColors[i] = frame.getRGB(0, 0, width, height, null, 0, width);
            }
        } else if (!vis && modifiable) {
            origColors = null;
        }
        modifiable = vis;
    }

    /**
     * Set type of level object.
     */
    public void setType(final Type t) {
        type = t;
    }

    /**
     * Get type of level object.
     */
    public Type getType() {
        return type;
    }

    /**
     * Get whether this object is visible only on terrain.
     */
    public boolean getVisOnTerrain() {
        return visOnTerrain;
    }

    /**
     * Get mask value.
     */
    public boolean getMask(final int x, final int y) {
        int tempX = x - maskOffsetX;
        int tempY = y - maskOffsetY;
        if (type != Type.PASSIVE && type != Type.ENTRANCE &&
                tempY >= 0 && tempY < maskHeight && tempX >= 0 && tempX < maskWidth) {
            return mask[tempY][tempX];
        } else {
            return false;
        }
    }
}
