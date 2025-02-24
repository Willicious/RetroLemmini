package lemmini.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

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
 * Wrapper class for additional images which don't fit anywhere else.
 *
 * @author Volker Oth
 */
public class MiscGfx {

    /** Index of images */
    public static enum Index {
        /** border for the minimap */
        MINIMAP_LEFT,
        MINIMAP_CENTER,
        MINIMAP_RIGHT,
        MINIMAP_LARGE_LEFT,
        MINIMAP_LARGE_CENTER,
        MINIMAP_LARGE_RIGHT,
        MINIMAP_ARROW_LEFT,
        MINIMAP_ARROW_UP,
        MINIMAP_ARROW_RIGHT,
        MINIMAP_ARROW_DOWN,
        /** RetroLemmini logo */
        RETROLEMMINI_LOGO_AMIGA,
        RETROLEMMINI_LOGO_WINLEMM,
        /** level background tile */
        BACKGROUND_LEVEL_AMIGA,
        BACKGROUND_LEVEL_WINLEMM,
        /** menu background tile */
        BACKGROUND_MAIN_AMIGA,
        BACKGROUND_MAIN_WINLEMM,
        /** replay sign 1 */
        REPLAY_1,
        /** replay sign 2 */
        REPLAY_2,
        /** selection marker for replay */
        SELECT,
        /** status icon 1: lemmings out */
        STATUS_OUT,
        /** status icon 2: lemmings in */
        STATUS_IN,
        /** status icon 3: lemmings needed */
        STATUS_NEEDED,
        /** status icon 4: time limit */
        STATUS_TIME,
        /** the red squiggle */
        ICONBAR_FILLER,
        /** the scrolling ticker-tape shown at the start of the game. */
        TICKER_TAPE,
        SCROLLER_LEMMING_LEFT,
        SCROLLER_LEMMING_RIGHT,
        /** menu cards */
        CARD_PLAY_LEVEL_LEMMING,
        CARD_CHOOSE_LEVEL_LEMMING,
        CARD_CODES,
        CARD_OPTIONS,
        CARD_PLAYERS,
        CARD_REPLAYS
    }


    /** list of images */
    private static final List<LemmImage> images = new ArrayList<>(16);
    private static final List<LemmImage> vsfx_images = new ArrayList<>(30);
    private static LemmImage minimap;
    private static LemmImage minimapLarge;
    private static int minimapWidth;

    /**
     * Initialization.
     * @param mmWidth Minimap width
     * @throws ResourceException
     */
    public static void init(int mmWidth) throws ResourceException {
        images.clear();
        Resource resource;
        LemmImage img;

        /* MINIMAP_LEFT */
        resource = Core.findResource("gfx/icons/minimap_left.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* MINIMAP_CENTER */
        resource = Core.findResource("gfx/icons/minimap_center.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* MINIMAP_RIGHT */
        resource = Core.findResource("gfx/icons/minimap_right.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);

        /* MINIMAP_LARGE_LEFT */
        resource = Core.findResource("gfx/iconbar/large_minimap_left.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* MINIMAP_LARGE_CENTER */
        resource = Core.findResource("gfx/iconbar/large_minimap_center.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* MINIMAP_LARGE_RIGHT */
        resource = Core.findResource("gfx/iconbar/large_minimap_right.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /*MINIMAP_ARROW_LEFT, MINIMAP_ARROW_UP, MINIMAP_ARROW_RIGHT, MINIMAP_ARROW_DOWN */
        resource = Core.findResource("gfx/misc/minimap_arrows.png", Core.IMAGE_EXTENSIONS);
        List<LemmImage> anim = ToolBox.getAnimation(Core.loadLemmImage(resource), 4);
        images.addAll(anim);
        
        /* RETROLEMMINI_LOGO_AMIGA */
        resource = Core.findResource("gfx/menu/retrolemmini_logo_amiga.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* RETROLEMMINI_LOGO_WINLEMM */
        resource = Core.findResource("gfx/menu/retrolemmini_logo_retro.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* BACKGROUND_LEVEL_AMIGA */
        resource = Core.findResource("gfx/menu/background_level_amiga.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* BACKGROUND_LEVEL_WINLEMM */
        resource = Core.findResource("gfx/menu/background_level_retro.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* BACKGROUND_MAIN_AMIGA */
        resource = Core.findResource("gfx/menu/background_main_amiga.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* BACKGROUND_MAIN_WINLEMM */
        resource = Core.findResource("gfx/menu/background_main_retro.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* REPLAY_1, REPLAY_2 */
        resource = Core.findResource("gfx/misc/replay.png", Core.IMAGE_EXTENSIONS);
        anim = ToolBox.getAnimation(Core.loadLemmImage(resource), 2);
        images.addAll(anim);
        /* SELECT */
        resource = Core.findResource("gfx/misc/select.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* STATUS_OUT, STATUS_IN, STATUS_NEEDED, STATUS_TIME */
        resource = Core.findResource("gfx/misc/status-icons.png", Core.IMAGE_EXTENSIONS);
        anim = ToolBox.getAnimation(Core.loadLemmImage(resource), 4);
        images.addAll(anim);
        /* ICONBAR_FILLER */
        resource = Core.findResource("gfx/iconbar/iconbar_filler.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* TICKER_TAPE */
        resource = Core.findResource("gfx/menu/ticker-tape.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* SCROLLER_LEMMING_LEFT */
        resource = Core.findResource("gfx/menu/scroller_lemming_left.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* SCROLLER_LEMMING_RIGHT */
        resource = Core.findResource("gfx/menu/scroller_lemming_right.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        
        // 2-frame card animations will later be split into frames for use as buttons
        /* CARD_PLAY_LEVEL_LEMMING */
        resource = Core.findResource("gfx/menu/card_play_level_lemming.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* CARD_CHOOSE_LEVEL_LEMMING */
        resource = Core.findResource("gfx/menu/card_choose_level_lemming.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* CARD_CODES */
        resource = Core.findResource("gfx/menu/card_codes.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* CARD_OPTIONS */
        resource = Core.findResource("gfx/menu/card_options.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* CARD_PLAYERS */
        resource = Core.findResource("gfx/menu/card_players.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);
        /* CARD_REPLAYS */
        resource = Core.findResource("gfx/menu/card_replays.png", Core.IMAGE_EXTENSIONS);
        img = Core.loadLemmImage(resource);
        images.add(img);

        /*add visual sfx images */
        resource = Core.findResource("gfx/misc/vsfxbig.png", Core.IMAGE_EXTENSIONS);
        anim = ToolBox.getAnimation(Core.loadLemmImage(resource), Vsfx.VSFX_COUNT);
        vsfx_images.addAll(anim);

        /* Assemble minimap */
        minimapWidth = -1;
        setMinimapWidth(mmWidth);
    }

    /**
     * Get image.
     * @param idx Index
     * @return image of the given index
     */
    public static LemmImage getImage(Index idx) {
        return images.get(idx.ordinal());
    }

    /**
     * Get Visual SFX image.
     * @param idx Vsfx.Vsfx_Index
     * @return image of given index
     */
    public static LemmImage getVsfxImage(Vsfx.Vsfx_Index idx) {
        return vsfx_images.get(idx.ordinal());
    }

    /**
     * Get Visual SFX image.
     * @param idx
     * @return
     */
    public static LemmImage getVsfxImage(int idx) {
        return vsfx_images.get(idx);
    }

    public static LemmImage getMinimapImage() {
        return minimap;
    }

    public static LemmImage getMinimapLargeImage() {
        return minimapLarge;
    }

    public static int getMinimapWidth() {
        return minimapWidth;
    }

    public static void setMinimapWidth(int width) {
        if (width == minimapWidth) {
            return;
        }

        minimapWidth = width;
        minimap = createMiniMapFrame(width);
        minimapLarge = createLargeMiniMapFrame(width);
    }

    private static LemmImage createMiniMapFrame(int width) {
        final int BORDER = 4;
        LemmImage minimapLeft = images.get(Index.MINIMAP_LEFT.ordinal());
        LemmImage minimapCenter = images.get(Index.MINIMAP_CENTER.ordinal());
        LemmImage minimapRight = images.get(Index.MINIMAP_RIGHT.ordinal());

        int leftWidth = Math.min(minimapLeft.getWidth(), BORDER + width);
        int centerWidth = width + BORDER - leftWidth;
        int rightWidth = minimapRight.getWidth();

        LemmImage tempMinimap = ToolBox.createLemmImage(leftWidth + centerWidth + rightWidth,
                NumberUtils.max(minimapLeft.getHeight(), minimapCenter.getHeight(), minimapRight.getHeight()));

        for (int y = 0; y < minimapLeft.getHeight(); y++) {
            for (int x = 0; x < leftWidth; x++) {
                tempMinimap.setRGB(x, y, minimapLeft.getRGB(x, y));
            }
        }
        for (int y = 0; y < minimapCenter.getHeight(); y++) {
            for (int x = 0; x < centerWidth; x++) {
                tempMinimap.setRGB(leftWidth + x, y, minimapCenter.getRGB(x % minimapCenter.getWidth(), y));
            }
        }
        for (int y = 0; y < minimapRight.getHeight(); y++) {
            for (int x = 0; x < rightWidth; x++) {
                tempMinimap.setRGB(BORDER + width + x, y, minimapRight.getRGB(x, y));
            }
        }
        return tempMinimap;
    }

    private static LemmImage createLargeMiniMapFrame(int width) {
        final int BORDER = 7;
        LemmImage minimapLeft = images.get(Index.MINIMAP_LARGE_LEFT.ordinal());
        LemmImage minimapCenter = images.get(Index.MINIMAP_LARGE_CENTER.ordinal());
        LemmImage minimapRight = images.get(Index.MINIMAP_LARGE_RIGHT.ordinal());

        int leftWidth = Math.min(minimapLeft.getWidth(), BORDER + width);
        int centerWidth = width + BORDER - leftWidth;
        int rightWidth = minimapRight.getWidth();

        LemmImage tempMinimap = ToolBox.createLemmImage(leftWidth + centerWidth + rightWidth,
                NumberUtils.max(minimapLeft.getHeight(), minimapCenter.getHeight(), minimapRight.getHeight()));

        for (int y = 0; y < minimapLeft.getHeight(); y++) {
            for (int x = 0; x < leftWidth; x++) {
                tempMinimap.setRGB(x, y, minimapLeft.getRGB(x, y));
            }
        }
        for (int y = 0; y < minimapCenter.getHeight(); y++) {
            for (int x = 0; x < centerWidth; x++) {
                tempMinimap.setRGB(leftWidth + x, y, minimapCenter.getRGB(x % minimapCenter.getWidth(), y));
            }
        }
        for (int y = 0; y < minimapRight.getHeight(); y++) {
            for (int x = 0; x < rightWidth; x++) {
                tempMinimap.setRGB(BORDER + width + x, y, minimapRight.getRGB(x, y));
            }
        }
        return tempMinimap;
    }

	public static List<LemmImage> getAnimation(Index index, int frameCount) {
	    LemmImage spriteSheet = getImage(index);
	    return ToolBox.getAnimation(spriteSheet, frameCount);
	}
}
