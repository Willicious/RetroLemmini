package lemmini.game;

import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
//import java.util.ListIterator;
import java.util.Locale;
//import java.util.Map;

//import org.apache.commons.lang3.math.NumberUtils;

import lemmini.game.GameController.SLTooOption;
import lemmini.gameutil.Sprite;
import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemmImage;
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
 * Handle the control icons.
 *
 * @author Volker Oth
 */
public class Icons {

    private static final int DEFAULT_PITCH = 4;
    /** a special counter for animation frames 31.2 frames/second */
    private static final int[] ANIMATION_CTR = {3, 3, 4, 3, 3}; //allows for a maximum of 5 seconds.

    /** internal counter tracking when to move to the next frame of animation */
    private static int animateCtr = 0;
    private static int currentAnimateFrame = 0;

    /**
     * icon width in pixels (of currently selected icon bar)
     * @return
     */
    public static int getIconWidth() {
        if (GameController.isOptionEnabled(SLTooOption.ENHANCED_ICONBAR)) {
            return ENHANCED_WIDTH;
        } else {
            return ORIGINAL_WIDTH;
        }
    }

    /**
     * icon height in pixels (of currently selected icon bar)
     * @return
     */
    public static int getIconHeight() {
        if (GameController.isOptionEnabled(SLTooOption.ENHANCED_ICONBAR)) {
            return ENHANCED_HEIGHT;
        } else {
            return ORIGINAL_HEIGHT;
        }
    }

    private static final int ORIGINAL_WIDTH = 32;
    private static final int ORIGINAL_HEIGHT = 40;

    private static final int ENHANCED_WIDTH = 34;
    private static final int ENHANCED_HEIGHT = 54;

    public static enum IconType {
        MINUS,
        PLUS,
        CLIMB,
        FLOAT,
        BOMB,
        BLOCK,
        BUILD,
        BASH,
        MINE,
        DIG,
        PAUSE,
        FFWD,
        NUKE,
        RESTART,
        VLOCK,
        EMPTY
    }

    /**
     * The currently chosen order of icons
     * @return
     */
    public static List<IconType> CurrentIconOrder() {
        List<IconType> rslt;
        rslt = new ArrayList<IconType>();
        rslt.add(IconType.MINUS);
        rslt.add(IconType.PLUS);
        rslt.addAll(SkillIconOrder());
        rslt.add(IconType.PAUSE);
        rslt.add(IconType.NUKE);
        rslt.add(IconType.FFWD);
        rslt.add(IconType.RESTART);
          rslt.add(IconType.VLOCK);

        return rslt;
    }

    /**
     * List of basic skillset icons, in order.
     * @return
     */
    public static List<IconType> SkillIconOrder() {
        List<IconType> rslt;
        rslt = new ArrayList<IconType>();
        rslt.add(IconType.CLIMB);
        rslt.add(IconType.FLOAT);
        rslt.add(IconType.BOMB);
        rslt.add(IconType.BLOCK);
        rslt.add(IconType.BUILD);
        rslt.add(IconType.BASH);
        rslt.add(IconType.MINE);
        rslt.add(IconType.DIG);
        return rslt;
    }

    /** buffered image that contains the whole icon bar in its current state */
    private static LemmImage iconImg;
    /** graphics object used to draw on iconImg */
    private static GraphicsContext iconGfx = null;
    /** the currently selected skill */
    private static IconType selectedSkill = null;

    /** list of Sprites that contains the icons */
    private static final List<Sprite> icons = new ArrayList<>(IconType.values().length);
    /** list of Sprites that contains the standard-sized background icons */
    private static final List<Sprite> bgIcons = new ArrayList<>(IconType.values().length);
    /** list of Sprites that contains the larger enhanced-size background icons */
    private static final List<Sprite> bgIconsLarge = new ArrayList<>(IconType.values().length);
    /** list of Sprites the contain the icon labels */
    private static final List<Sprite> iconLabels = new ArrayList<>(IconType.values().length);

    /** the current frame for each icon's sprite */
    private static final HashMap<IconType, Integer> iconFrame = new HashMap<>(IconType.values().length);
    /** the current pressed state of each icon (true is pressed, false is not pressed) */
    private static final HashMap<IconType, Boolean> iconPressed = new HashMap<>(IconType.values().length);

    /**
     * Initialization.
     * @throws ResourceException
     */
    public static void init() throws ResourceException {
        //load the hashmap with initial values for each of the IconTypes
        if (iconFrame.isEmpty()) {
            for(IconType x : IconType.values()) {
                iconFrame.put(x, 0);
            }
        }
        //load the hashmap with initial values for each of the IconTypes
        if (iconPressed.isEmpty()) {
            for(IconType x : IconType.values()) {
                iconPressed.put(x, false);
            }
        }
        LoadIconResources();
        //reset the icon bar to draw it fresh
        reset();
    }


    private static boolean ModHasEnhancedIconBar() throws ResourceException {
        //create a list of all possible images in the EnhancedIconBar
        //we're going to check is any one of these exists.
        //If so, we'll assume this mod supports the Enhanced Toolbar,
        //and so we won't use any static icons.
        List<String> iconbarImages;
        iconbarImages = new ArrayList<String>();
        iconbarImages.add("icon_empty_large");
        iconbarImages.add("iconbar_filler");
        //static transparent icons
        iconbarImages.add("ticon_bash");
        iconbarImages.add("ticon_block");
        iconbarImages.add("ticon_bomb");
        iconbarImages.add("ticon_build");
        iconbarImages.add("ticon_climb");
        iconbarImages.add("ticon_dig");
        iconbarImages.add("ticon_float");
        iconbarImages.add("ticon_mine");
        iconbarImages.add("ticon_minus");
        iconbarImages.add("ticon_plus");
        iconbarImages.add("ticon_pause");
        iconbarImages.add("ticon_ffwd");
        iconbarImages.add("ticon_nuke");
        iconbarImages.add("ticon_restart");
        iconbarImages.add("ticon_vlock");
        //animated icons
        iconbarImages.add("anim_bash");
        iconbarImages.add("anim_block");
        iconbarImages.add("anim_bomb");
        iconbarImages.add("anim_build");
        iconbarImages.add("anim_climb");
        iconbarImages.add("anim_dig");
        iconbarImages.add("anim_float");
        iconbarImages.add("anim_mine");
        iconbarImages.add("anim_minus");
        iconbarImages.add("anim_plus");
        iconbarImages.add("anim_pause");
        iconbarImages.add("anim_ffwd");
        iconbarImages.add("anim_nuke");
        iconbarImages.add("anim_restart");
        iconbarImages.add("anim_vlock");
        //large mini map
        iconbarImages.add("large_minimap_center");
        iconbarImages.add("large_minimap_left");
        iconbarImages.add("large_minimap_right");

        Resource resource = null;
        //now check for a Mod resource for each of the images listed above.
        for (String img : iconbarImages) {
            String resString ="gfx/iconbar/" + img + ".png";
            resource = Core.findResourceEx(
                    resString,
                    true, false,
                    Core.IMAGE_EXTENSIONS);
            if (resource != null) {
                //if one exists, then this mod is Enhanced-Iconbar-Aware!
                return true;
            }
        }
        return false;
    }

    public static void LoadIconResources() throws ResourceException {
        bgIcons.clear();
        bgIconsLarge.clear();
        icons.clear();
        iconLabels.clear();

        if (iconGfx != null) {
            iconGfx.dispose();
        }
        List<IconType> iconOrder = CurrentIconOrder();
        iconImg = ToolBox.createLemmImage(getIconWidth() * (iconOrder.size()), getIconHeight());
        iconGfx = iconImg.createGraphicsContext();

        boolean bModUsesEnhancedIconBar;
        bModUsesEnhancedIconBar = ModHasEnhancedIconBar();

        //get the background image we're going to use...
        for (int i = 0; i < iconOrder.size(); i++) {
            LemmImage sourceImg;
            Resource resource = null;
            Sprite icon;

            String iconName = iconOrder.get(i).toString().toLowerCase(Locale.ROOT);


            //animated icons we need to load a little differently.
            // 1) we try the animated icon for the mod *only*
            // 2) if that's not found, we try the transparent static icon for the mod
            // 3) if that's not found, we try the original static icon (with background) for the mod
            // 4) if that's not found, we try the standard animated icon
            // 5) if there's no standard animated icon, we load the standard transparent static icon.

            // 1) check for animated mods
            if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                resource = Core.findResourceEx(
                        "gfx/iconbar/anim_" + iconName + ".png",
                        true, false,
                        Core.IMAGE_EXTENSIONS);
            }
            // 2) check for static mods
            if (resource == null && bModUsesEnhancedIconBar) {
                resource = Core.findResourceEx(
                        "gfx/iconbar/ticon_" + iconName + ".png",
                        true, false,
                        Core.IMAGE_EXTENSIONS);
            }
            // 3) check for old-style static mods (with background)
            if (resource == null && !bModUsesEnhancedIconBar) {
                resource = Core.findResourceEx(
                        "gfx/icons/icon_" + iconName + ".png",
                        true, false,
                        Core.IMAGE_EXTENSIONS);
            }
            if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                // 4) check for animated standard
                if (resource == null) {
                    resource = Core.findResourceEx(
                            "gfx/iconbar/anim_" + iconName + ".png",
                            false, true,
                            Core.IMAGE_EXTENSIONS);
                }
            }
            // 5) check for static standard
            if (resource == null) {
                resource = Core.findResourceEx(
                        "gfx/iconbar/ticon_" + iconName + ".png",
                        false, true,
                        Core.IMAGE_EXTENSIONS);
            }
            // if we still can't find anything, then this should throw an error.
            if (resource == null)
                resource = Core.findResource("gfx/iconbar/ticon_" + iconName + ".png", Core.IMAGE_EXTENSIONS);
            sourceImg = Core.loadLemmImage(resource);
            int frames = sourceImg.getHeight() / 40;
            icon = new Sprite(sourceImg, frames, 1, false);
            icons.add(icon);

            //load standard size backgrounds
            // BOOKMARK TODO: allow for multiple different background objects
            resource = Core.findResource("gfx/icons/icon_empty.png", Core.IMAGE_EXTENSIONS);
            sourceImg = Core.loadLemmImage(resource);
            icon = new Sprite(sourceImg, 2, 1, false);
            bgIcons.add(icon);

            //load larger background icons
            // BOOKMARK TODO: allow for multiple different background objects
            resource = Core.findResource("gfx/iconbar/icon_empty_large.png", Core.IMAGE_EXTENSIONS);
            sourceImg = Core.loadLemmImage(resource);
            icon = new Sprite(sourceImg, 2, 1, false);
            bgIconsLarge.add(icon);

            //load the label overlays
            resource = Core.findResource(
                    "gfx/icon_labels/label_" + iconName + ".png",
                    Core.IMAGE_EXTENSIONS);
            sourceImg = Core.loadLemmImage(resource);
            icon = new Sprite(sourceImg, 2, 1, false);
            iconLabels.add(icon);
        }
    }


    /**
     * Get icon type by x position.
     * @param x x position inside bar in pixels
     * @return Icon type
     */
    public static IconType getType(final int x) {
        List<IconType> iconOrder = CurrentIconOrder();
        if (x < 0 || x >= iconOrder.size() * getIconWidth()) {
            return null; // invalid
        }
        return iconOrder.get(x / getIconWidth());
    }

    /**
     * Get buffered image that contains the whole icon bar in its current state.
     * @return image of icon bar
     */
    public static LemmImage getImg() {
        return iconImg;
    }

    /**
     * Get pressed state of the given icon
     * @param type
     * @return
     */
    static boolean isPressed(IconType type) {
        List<IconType> iconOrder = CurrentIconOrder();
        int idx = iconOrder.indexOf(type);
        if (idx == -1) {
            return false;
        }
        return (icons.get(idx).getFrameIdx() != 0);
    }

    /**
     * Press down icon.
     * @param type Icon Type
     */
    static void press(final IconType type) {
        List<IconType> iconOrder = CurrentIconOrder();
        int idx = iconOrder.indexOf(type);
        if (idx == -1) {
            return;
        }

        switch (type) {
            case PAUSE:
            case FFWD:
            case VLOCK:
                //these three icons are toggle icons.
                if (idx < iconOrder.size()) {
                    //Sprite icon = icons.get(idx);
                    boolean toggleFrame = !iconPressed.get(type).booleanValue();
                    setIconFrame(idx, toggleFrame);
                    drawIcon(idx);
                }
                break;
            case CLIMB:
            case FLOAT:
            case BOMB:
            case BLOCK:
            case BUILD:
            case BASH:
            case MINE:
            case DIG:
                //update the icons for the "Radio Buttons" i.e. the skills
                for (IconType x : SkillIconOrder()) {
                    int i = iconOrder.indexOf(x);
                    if (i != -1 && i != idx) {
                        //reset all the skills *not* selected to show as unselected
                        //the skill that *is* selected will be handled below.
                        setIconFrame(i, false);
                        drawIcon(i);
                    }
                }
                selectedSkill = type;
                /* falls through */
            case MINUS:
            case PLUS:
            case NUKE:
            case RESTART:
                setIconFrame(idx, true);
                drawIcon(idx);
                break;
            default:
                break;
        }
    }

    /**
     * Update the frame for animated icons.
     */
    static public void Animate() {
        if (++animateCtr >= ANIMATION_CTR[currentAnimateFrame % ANIMATION_CTR.length]) {
            animateCtr -= ANIMATION_CTR[currentAnimateFrame % ANIMATION_CTR.length];
            currentAnimateFrame = currentAnimateFrame++ % ANIMATION_CTR.length;

            List<IconType> iconOrder = CurrentIconOrder();
            for (IconType x:iconPressed.keySet()) {
                if(iconPressed.get(x)) {
                    int idx = iconOrder.indexOf(x);
                    if (idx != -1) {
                        if (idx < icons.size()-1) {
                            int frameCount = icons.get(idx).getNumFrames();
                            if (frameCount > 2) {
                                int oldFrame = iconFrame.get(x);
                                int newFrame = (oldFrame + 1) % frameCount;
                                iconFrame.replace(x, newFrame);
                                drawIcon(idx);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
    if (++explodeCtr >= MAX_EXPLODE_CTR[explodeNumCtr - 1]) {
        explodeCtr -= MAX_EXPLODE_CTR[explodeNumCtr - 1];
        explodeNumCtr--;
        if (explodeNumCtr == 0) {
            explode = true;
        }
    }
    */

    /**
     * Release icon.
     * @param type Icon Type
     */
    static void release(final IconType type) {
        List<IconType> iconOrder = CurrentIconOrder();
        int idx = iconOrder.indexOf(type);
        if (idx == -1) {
            return;
        }

        switch (type) {
            case MINUS:
            case PLUS:
            case RESTART:
                setIconFrame(idx, false);
                drawIcon(idx);
                break;
            case NUKE:
                if (!GameController.isNuked()) {
                    setIconFrame(idx, false);
                    drawIcon(idx);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Sets the current Sprite Index for the
     * @param iconIdx the index of the icon, from the CurrentIconOrder()
     */
    private static void setIconFrame(int iconIdx, boolean pressed) {
        if (iconIdx < CurrentIconOrder().size()) {
            int frameIdx = pressed ? 1 : 0;
            IconType type = CurrentIconOrder().get(iconIdx);
            iconFrame.replace(type, frameIdx);
            iconPressed.replace(type,  pressed);
        }
    }

    /**
     * Draws the background and icon of the selected icon button.
     * @param idx
     */
    private static void drawIcon(int idx) {
        if (idx <= CurrentIconOrder().size()) {
            IconType type = CurrentIconOrder().get(idx);
            int x = 0;
            int y = 0;
            Sprite bgIcon;
            if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                bgIcon = bgIconsLarge.get(idx);
                x = 1; //the larger icons have an added pixel on the side of padding.
                y = 14; //the larger icons have 14 pixels more headroom (for the numbers)
            } else {
                bgIcon = bgIcons.get(idx);
            }
            Sprite iconLabel = iconLabels.get(idx);
            Sprite icon = icons.get(idx);

            //set the frame to use for each Sprite
            int frameIdx = Math.min(iconFrame.get(type), icon.getNumFrames()-1);
            int staticFrameIdx = (iconPressed.get(type)) ? 1 : 0;
            bgIcon.setFrameIdx(staticFrameIdx);
            iconLabel.setFrameIdx(staticFrameIdx);
            icon.setFrameIdx(frameIdx);

            iconGfx.drawImage(bgIcon.getImage(), getIconWidth() * idx, 0);

            //these 5 icon types don't have numbers with them, so we can move the icons up a tad
            int yIcon = 0;
            int yLabel = 30;
            if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                yLabel = 44;

                if (GameController.isOptionEnabled(GameController.SLTooOption.ICON_LABELS))
                    yIcon = -7;
                else
                    yIcon = -4;

                // Fine tuning of y positions
                switch(type) {
                    case BLOCK:
                        yIcon -= 1;
                        break;
                    case BASH:
                    case MINE:
                    case DIG:
                        yIcon -= 2;
                        break;
                    case BUILD:
                    case FFWD:
                    case PAUSE:
                    case RESTART:
                    case VLOCK:
                    case NUKE:
                        yIcon -= 3;
                        break;
                    default:
                }
            }

            iconGfx.drawImage(icon.getImage(), getIconWidth() * idx + x, 0 + y + yIcon);
            if (GameController.isOptionEnabled(GameController.SLTooOption.ICON_LABELS))
                iconGfx.drawImage(iconLabel.getImage(), getIconWidth() * idx + x, yLabel);
        }
    }

    /**
     * Get the selected skill icon.
     * @return the selected skill icon if one is pressed, or null if none is pressed
     */
    static IconType getSelectedSkill() {
        return selectedSkill;
    }

    static IconType getNextRadioIcon(IconType type) {
        List<IconType> skillOrder = SkillIconOrder();
        int skillIdx = skillOrder.indexOf(type);
        if (skillIdx == -1) {
            //outside the range of skills
            return null;
        }

        //get the next index up (if we're at the end, get the 1st index)
        int nextSkillIdx;
        if (skillIdx == skillOrder.size() - 1) {
            nextSkillIdx = 0;
        } else {
            nextSkillIdx = skillIdx + 1;
        }

        return skillOrder.get(nextSkillIdx);
    }

    static IconType getPreviousRadioIcon(IconType type) {
        List<IconType> skillOrder = SkillIconOrder();
        int skillIdx = skillOrder.indexOf(type);
        if (skillIdx == -1) {
            //outside the range of skills
            return null;
        }

        //get the next index up (if we're at the start, get the last index)
        int nextSkillIdx;
        if (skillIdx == 0) {
            nextSkillIdx = skillOrder.size() - 1;
        } else {
            nextSkillIdx = skillIdx - 1;
        }

        return skillOrder.get(nextSkillIdx);
    }

    /**
     * Reset icon bar.
     */
    static void reset() {
        for (int i = 0; i < CurrentIconOrder().size(); i++) {
            setIconFrame(i, false);
            drawIcon(i);
        }
        selectedSkill = null;
    }

    /**
     * Redraws the icon bar, with current states.
     */
    static void redraw() {
        if (iconGfx != null) {
            iconGfx.dispose();
        }
        List<IconType> iconOrder = CurrentIconOrder();
        iconImg = ToolBox.createLemmImage(getIconWidth() * (iconOrder.size()), getIconHeight());
        iconGfx = iconImg.createGraphicsContext();
        for (int i = 0; i < iconOrder.size(); i++) {
            drawIcon(i);
        }
        selectedSkill = null;
    }

    /**
     * Returns the pitch of the tone to play when clicking the button.
     * @param type
     * @return
     */
    static int GetPitch(IconType type) {
        List<IconType> iconOrder = CurrentIconOrder();
        int idx = iconOrder.indexOf(type);
        if (idx == -1)
            return DEFAULT_PITCH;

        switch (type) {
            case MINUS:
            case PLUS:
            case EMPTY:
                return DEFAULT_PITCH;
            default:
                int pitch = 0;
                for (int i = 0; i < CurrentIconOrder().size(); i++) {
                    IconType tmpType = iconOrder.get(i);
                    if (tmpType == type) {
                        return pitch;
                    } else if (tmpType != IconType.MINUS && tmpType != IconType.PLUS && tmpType != IconType.EMPTY) {
                        pitch++;
                    }
                }
        }

        return DEFAULT_PITCH;
    }

}
