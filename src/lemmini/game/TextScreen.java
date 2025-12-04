package lemmini.game;

import static lemmini.game.LemmFont.LemmColor.BLUE;
import static lemmini.game.LemmFont.LemmColor.GREEN;
import static lemmini.game.LemmFont.LemmColor.RED;
import static lemmini.game.LemmFont.LemmColor.TURQUOISE;
import static lemmini.game.LemmFont.LemmColor.VIOLET;
import static lemmini.game.LemmFont.LemmColor.YELLOW;

import java.awt.Color;
import java.awt.RenderingHints;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
 * Class to print text screens which can be navigated with the mouse.
 * Uses {@link TextDialog}
 *
 * @author Volker Oth
 */
public class TextScreen {

    /** Mode (type of screen to present) */
    public static enum Mode {
        /** initial state */
        INIT,
        /** main introduction screen */
        INTRO,
        /** level briefing screen */
        BRIEFING,
        /** level debriefing screen */
        DEBRIEFING
    }

    public static enum Button {
        /** play level */
        PLAY_LEVEL,
        /** choose level */
        CHOOSE_LEVEL,
        /** load replay */
        LOAD_REPLAY,
        /** enter code */
        ENTER_CODE,
        /** players */
        PLAYERS,
        /** options */
        OPTIONS,
        /** options */
        ABOUT,
        /** exit */
        EXIT,
        /** start level */
        START_LEVEL,
        /** show hint */
        SHOW_HINT,
        /** show info */
        SHOW_INFO,
        /** next hint */
        NEXT_HINT,
        /** previous hint */
        PREVIOUS_HINT,
        /** continue */
        CONTINUE,
        /** restart level */
        RESTART,
        /** back to menu */
        MENU,
        /** replay level */
        REPLAY,
        /** save replay */
        SAVE_REPLAY,
        /** anywhere on the screen other than a button */
        NONE;
    }

    /** synchronization monitor */
    private static final Object monitor = new Object();
    /** y position of scroll text - pixels relative to center */
    private static final int SCROLL_Y = 164;
    /** width of scroll text in pixels */
    private static final int SCROLL_WIDTH = 750;
    /** amount of extra padding in character widths */
    private static final int SCROLL_PADDING = 4;
    /** step width of scroll text in pixels */
    private static final int SCROLL_STEP = 2;
    private static final int FAILURE_THRESHOLD_FOR_HINTS = 3;
    /** counter threshold used to trigger the rotation animation (in animation update frames) */
    private static final int MAX_ROT_CTR = 99;
    private static final int ROT_ANIM_LENGTH = 21;
    /** scroll text */
    private static final String SCROLL_TEXT =
        "RetroLemmini - a game engine for Lemmings(tm) in Java. "
        + "Programmed by Will James... "
        + "Based on SuperLemminiToo by Charles... "
        + "SuperLemmini by Ryan Sakowski... "
        + "Original Lemmini by Volker Oth. "
        + "Lemmini website: www.lemmini.de. "
        + "Thanks to Martin Cameron for his IBXM library, "
        + "Mindless for his MOD conversions of the original Amiga Lemmings tunes, "
        + "the guys of DMA Design for the original Lemmings game, "
        + "ccexplore and the other nice folks at the Lemmings Forum for discussion and advice, "
        + "and Oracle for maintaining Java and providing the community with a free development environment. "
        + "We accept no responsibility for: Loss of sleep... Loss of hair... Loss of sanity! "
        + "Click 'Play Level' to begin... ";

    /** TextDialog used as base component */
    private static TextDialog textDialog;
    /** frames for rotation animation */
    private static LemmImage[] rotImg;
    /** counter used to trigger the rotation animation (in animation update frames) */
    private static int rotCtr;
    /** counter for scrolled pixels */
    private static int scrollPixCtr;
    /** image used for scroller */
    private static LemmImage scrollerImg;
    /** screen type to display */
    private static Mode mode;
    private static int hintIndex;

    /**
     * Set mode.
     * @param m mode.
     */
    public static void setMode(final Mode m) {
        synchronized (getMonitor()) {
            if (mode != m) {
                switch (m) {
                    case INTRO:
                        initIntro();
                        break;
                    case BRIEFING:
                        initBriefing();
                        break;
                    case DEBRIEFING:
                        initDebriefing();
                        break;
                    default:
                        break;
                }
            }
            mode = m;
        }
    }

    /**
     * Initialize the intro screen.
     */
    static void initIntro() {
        textDialog.clear();
        
        if (GameController.getMenuThemeOption() == GameController.MenuThemeOption.WINLEMM) {
        	createWinLemmThemeMenu();
        } else {
        	createLemminiThemeMenu();
        }
    }
    
    private static void createWinLemmThemeMenu() {
    	textDialog.setBackground(MiscGfx.getImage(MiscGfx.Index.BACKGROUND_MAIN_WINLEMM), true);
    	
        List<LemmImage> cardChoose = MiscGfx.getAnimation(MiscGfx.Index.CARD_CHOOSE_LEVEL_LEMMING, 2);
        textDialog.addButton(cardChoose.get(0), cardChoose.get(0), cardChoose.get(1), "Menu Cards", -339, -24, Button.CHOOSE_LEVEL);
        
        List<LemmImage> cardPlay = MiscGfx.getAnimation(MiscGfx.Index.CARD_PLAY_LEVEL_LEMMING, 2);
        textDialog.addButton(cardPlay.get(0), cardPlay.get(0), cardPlay.get(1), "Menu Cards", 152, -24, Button.PLAY_LEVEL);
        
        List<LemmImage> cardOptions = MiscGfx.getAnimation(MiscGfx.Index.CARD_OPTIONS, 2);
        textDialog.addButton(cardOptions.get(0), cardOptions.get(0), cardOptions.get(1), "Menu Cards", -132, -24, Button.OPTIONS);
        
        List<LemmImage> cardReplays = MiscGfx.getAnimation(MiscGfx.Index.CARD_REPLAYS, 2);
        textDialog.addButton(cardReplays.get(0), cardReplays.get(0), cardReplays.get(1), "Menu Cards", 0, -24, Button.LOAD_REPLAY);
        
        List<LemmImage> cardPlayers = MiscGfx.getAnimation(MiscGfx.Index.CARD_PLAYERS, 2);
        textDialog.addButton(cardPlayers.get(0), cardPlayers.get(0), cardPlayers.get(1), "Menu Cards", -132, 35, Button.PLAYERS);
        
        List<LemmImage> cardCodes = MiscGfx.getAnimation(MiscGfx.Index.CARD_CODES, 2);
        textDialog.addButton(cardCodes.get(0), cardCodes.get(0), cardCodes.get(1), "Menu Cards", 0, 35, Button.ENTER_CODE);
        
    	LemmImage menuLemming = MiscGfx.getImage(MiscGfx.Index.MENU_LEMMING);
    	textDialog.addButton(menuLemming, menuLemming, menuLemming, "Menu Cards", -70, -92, Button.NONE);
    	
    	addWinLemmThemeVersionNumber();
    }
    
    public static void createLemminiThemeMenu() {
    	textDialog.setBackground(MiscGfx.getImage(MiscGfx.Index.BACKGROUND_MAIN_AMIGA), true);
    	
        textDialog.addTextButton("Levels", "Levels", null, -15, -1, Button.CHOOSE_LEVEL, GREEN, YELLOW);
        textDialog.addTextButton("Play", "Play", null, -3, -1, Button.PLAY_LEVEL, TURQUOISE, YELLOW);
        textDialog.addTextButton("Options", "Options", null, 7, -1, Button.OPTIONS, GREEN, YELLOW);

        textDialog.addTextButton("Enter Code", "Enter Code", null, -15, 1, Button.ENTER_CODE, BLUE, YELLOW);
        textDialog.addTextButton("Load Replay", "Load Replay", null, 3, 1, Button.LOAD_REPLAY, BLUE, YELLOW);

        textDialog.addTextButton("Players", "Players", null, -15, 2, Button.PLAYERS, BLUE, YELLOW);
        textDialog.addTextButton("Exit", "Exit", null, 10, 2, Button.EXIT, BLUE, YELLOW);
        
        String revision = "Version " + Core.REVISION;
        int revisionX = (revision.length() / 2) + 1;
        textDialog.addTextButton(revision, revision, null, -revisionX, 4, Button.ABOUT, VIOLET, YELLOW);
    }
    
    private static void addWinLemmThemeVersionNumber() {
        String revision = " " + Core.REVISION + " @";
        int yPosition = 116;
        int charWidth = 26;
        int totalWidth = revision.length() * charWidth;

        LemmImage versionImg = MiscGfx.getImage(MiscGfx.Index.VERSION_WINLEMM);
        int versionImgX = -(versionImg.getWidth() / 2) - (totalWidth / 2);
        textDialog.addButton(versionImg, versionImg, versionImg, "versionImages", versionImgX, yPosition, Button.ABOUT);

        List<LemmImage> versionNum = MiscGfx.getAnimation(MiscGfx.Index.VERSION_NUMS_WINLEMM, 17);
        int startX = versionImgX + versionImg.getWidth() - 2; // Start number directly to the right of "version" image

        for (char c : revision.toCharArray()) {
            int frameIndex;

            switch (c) {
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    frameIndex = c - '0'; // Numbers
                    break;
                case '.':
                    frameIndex = 10; // Dot
                    break;
                case '-':
                    frameIndex = 11; // Dash
                    break;
                case 'A':
                    frameIndex = 13; // A
                    break;
                case 'B':
                    frameIndex = 14; // B
                    break;
                case '*':
                    frameIndex = 15; // Star
                    break;
                case '@':
                    frameIndex = 16; // Sunshine
                    break;
                default:
                    frameIndex = 12; // Blank character for everything else
                    break;
            }

            LemmImage img = versionNum.get(frameIndex);
            textDialog.addButton(img, img, img, "versionImages", startX, yPosition, Button.ABOUT);
            startX += charWidth; // Move to the next character
        }
    }

    /**
     * Initialize the briefing screen.
     */
    static void initBriefing() {
        textDialog.clear();
        hintIndex = 0;
        drawBackground();
        Level level = GameController.getLevel();
        textDialog.addImage(GameController.getMapPreview(), null, -225);
        addLevelName(level);
        showLevelInfo();
        textDialog.addTextButton("Start Level", "Start Level", null, -12, 6, Button.START_LEVEL, BLUE, YELLOW);
        textDialog.addTextButton("Menu", "Menu", null, 4, 6, Button.MENU, BLUE, YELLOW);
    }

    /**
     * Initialize the debriefing screen.
     */
    static void initDebriefing() {
        textDialog.clear();
        drawBackground();
        int numLemmings = GameController.getNumLemmingsMax();
        int toRescue = GameController.getNumToRescue();
        int rescued = GameController.getNumExited();
        int toRescuePercent = toRescue * 100 / numLemmings; // % to rescue of total number
        int rescuedPercent = rescued * 100 / numLemmings; // % rescued of total number
        int rescuedOfToRescue; // % rescued of no. to rescue
        if (toRescue == 0) {
            rescuedOfToRescue = 0;
        } else {
            rescuedOfToRescue = rescued * 100 / toRescue;
        }
        int timeElapsed = GameController.getLevelRecord().getTimeElapsed();
        int skillsUsed = GameController.getLevelRecord().getSkillsUsed();
        int score = GameController.getScore();
        if (GameController.getTime() == 0 && GameController.isTimed()) {
            textDialog.addStringCentered("Time is up.", null, -7, TURQUOISE);
        } else {
            textDialog.addStringCentered("All lemmings accounted for.", null, -7, TURQUOISE);
        }
        if (GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES) || numLemmings > 100) {
            textDialog.addStringCentered(String.format("You needed %d - You rescued %d", toRescue, rescued), null, -5, VIOLET);
        } else {
            textDialog.addStringCentered(String.format("You needed %d%% - You rescued %d%%", toRescuePercent, rescuedPercent), null, -5, VIOLET);
        }
        if (!GameController.wasLost()) {
            String timeTaken = String.format("%d:%02d", timeElapsed / 60, timeElapsed % 60);
            textDialog.addStringCentered(String.format("Time taken %s - Skills used %d", timeTaken, skillsUsed), null, -4, VIOLET);
            String pointWord = (score == 1) ? "point" : "points";
            textDialog.addStringCentered(String.format("Your score is %d %s", score, pointWord), null, -3, GREEN);
        }
        LevelPack lp = GameController.getCurLevelPack();
        List<String> debriefings = lp.getDebriefings();
        if (GameController.wasLost()) {
            String debriefing;
            if (GameController.getNumExited() <= 0) {
                debriefing = debriefings.get(0);
            } else if (rescuedOfToRescue < 50) {
                debriefing = debriefings.get(1);
            } else if (rescuedPercent < toRescuePercent - 5) {
                debriefing = debriefings.get(2);
            } else if (rescuedPercent < toRescuePercent - 1) {
                debriefing = debriefings.get(3);
            } else {
                debriefing = debriefings.get(4);
            }
            textDialog.addStringCentered(debriefing, null, -1, RED);
            textDialog.addTextButton("Retry Level", "Retry Level", null, -12, 5, Button.RESTART, BLUE, YELLOW);
        } else {
            String debriefing;
            if (rescued <= toRescue && rescued < numLemmings) {
                debriefing = debriefings.get(5);
            } else if (rescuedPercent < toRescuePercent + 20 && rescued < numLemmings) {
                debriefing = debriefings.get(6);
            } else if (rescued < numLemmings) {
                debriefing = debriefings.get(7);
            } else {
                debriefing = debriefings.get(8);
            }
            textDialog.addStringCentered(debriefing, null, -1, RED);
            int lpn = GameController.getCurLevelPackIdx();
            int r = GameController.getCurRating();
            int ln = GameController.getCurLevelNumber();
            // Check if ALL levels in this rating are completed
            if (allLevelsCompleted(lp, r)) {
                List<String> ratings = lp.getRatings();
                textDialog.addStringCentered("Congratulations!", null, 2, YELLOW);
                textDialog.addStringCentered(String.format("You finished all the %s levels!", ratings.get(GameController.getCurRating())), null, 3, GREEN);
            // Now check if there's a next level to display the access code
            } else if (lp.getLevelCount(r) > ln + 1) {
                int absLevel = GameController.absLevelNum(lpn, r, ln + 1);
                String code = LevelCode.create(lp.getCodeSeed(), absLevel, rescuedPercent,
                        GameController.getTimesFailed(), 0, lp.getCodeOffset());
                if (!(lpn == 0 && r == 0) && code != null) {
                    textDialog.addStringCentered(String.format("Your access code for level %d%nis %s", ln + 2, code), null, 2, YELLOW);
                }
            }
            if ((r < lp.getRatings().size() - 1) || (lp.getLevelCount(r) > ln + 1)) {
            	textDialog.addTextButton("Continue", "Continue", null, -11, 5, Button.CONTINUE, BLUE, YELLOW);
            }
        }
        if (!GameController.getWasCheated()) {
            textDialog.addTextButton("View Replay", "View Replay", null, 1, 5, Button.REPLAY, BLUE, YELLOW);
            textDialog.addTextButton("Save Replay", "Save Replay", null, 1, 6, Button.SAVE_REPLAY, BLUE, YELLOW);
        }
        textDialog.addTextButton("Menu", "Menu", null, -9, 6, Button.MENU, BLUE, YELLOW);
        
        // store the last level played
        Core.programProps.set("lastLevelPlayed", GameController.getLastLevelPlayedString());
        Core.programProps.save(Core.getProgramPropsFilePath(), false);
    }
    
    public static boolean allLevelsCompleted(LevelPack pack, int rating) {
        for (int l = 0; l < pack.getLevelCount(rating); l++) {
            if (!Core.player.getLevelRecord(pack.getName(), pack.getRatings().get(rating), l).isCompleted()) {
                return false; // Found an incomplete level
            }
        }
        return true; // All levels are completed
    }
    
    private static void drawBackground() {
    	LemmImage backgroundImg;
    	
        if (GameController.getMenuThemeOption() == GameController.MenuThemeOption.WINLEMM)
        	backgroundImg = MiscGfx.getImage(MiscGfx.Index.BACKGROUND_LEVEL_WINLEMM);
        else
        	backgroundImg = MiscGfx.getImage(MiscGfx.Index.BACKGROUND_LEVEL_AMIGA);
        
        textDialog.setBackground(backgroundImg, true);
    }
    
    public static void addLevelName(Level level) {
    	int levelNumber = GameController.getCurLevelNumber() + 1;
    	String levelText = String.format("Level %-3d", levelNumber);
    	textDialog.addString(levelText, null, -21, -4, RED);
    	
    	String levelName = level.getLevelName();
    	int levelNameX = levelName.length() <= 22 ? 0 - levelName.length() / 2 : -11;
    	textDialog.addString(String.format("%s", levelName), null, levelNameX, -4, RED);
    }

    public static void showLevelInfo() {
        synchronized (getMonitor()) {
            textDialog.clearGroup("info");
            Level level = GameController.getLevel();
            String rating = GameController.getCurLevelPack().getRatings().get(GameController.getCurRating());
            textDialog.addString(String.format("Number of Lemmings %d", level.getNumLemmings()), "info", -9, -2, BLUE);
            if (GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES) || level.getNumLemmings() > 100) {
                textDialog.addString(String.format("%d to be saved", level.getNumToRescue()), "info", -9, -1, GREEN);
            } else {
                textDialog.addString(String.format("%d%% to be saved", level.getNumToRescue() * 100 / level.getNumLemmings()), "info", -9, -1, GREEN);
            }
            textDialog.addString(String.format("Release Rate %d", level.getReleaseRate()), "info", -9, 0, YELLOW);
            int minutes = level.getTimeLimitSeconds() / 60;
            int seconds = level.getTimeLimitSeconds() % 60;
            if (!GameController.isTimed()) {
                textDialog.addString("No time limit", "info", -9, 1, TURQUOISE);
            } else if (seconds == 0) {
                String minuteWord = (minutes == 1) ? "Minute" : "Minutes";
                textDialog.addString(String.format("Time      %d %s", minutes, minuteWord), "info", -9, 1, TURQUOISE);
            } else {
                textDialog.addString(String.format("Time      %d-%02d", minutes, seconds), "info", -9, 1, TURQUOISE);
            }
            textDialog.addString(String.format("Rating    %s", rating), "info", -9, 2, VIOLET);
            String author = level.getAuthor();
            if (StringUtils.isNotEmpty(author)) {
                textDialog.addString(String.format("Author    %s", author), "info", -9, 3, RED);
            }
            if (level.getNumHints() > 0 && (Core.player.isDebugMode() || GameController.getTimesFailed() >= FAILURE_THRESHOLD_FOR_HINTS)) {
                textDialog.addTextButton("Show Hint", "Show Hint", "info", -4, 5, Button.SHOW_HINT, BLUE, YELLOW);
            }
        }
    }

    public static void showHint() {
        synchronized (getMonitor()) {
            textDialog.clearGroup("info");
            Level level = GameController.getLevel();
            textDialog.addString(String.format("Hint %d", hintIndex + 1), "info", -3, -2, TURQUOISE);
            textDialog.addStringCentered(level.getHint(hintIndex), "info", 0, GREEN);
            textDialog.addTextButton("Show Info", "Show Info", "info", -4, 5, Button.SHOW_INFO, BLUE, YELLOW);
            if (hintIndex > 0) {
                textDialog.addTextButton("Previous Hint", "Previous Hint", "info", -19, 5, Button.PREVIOUS_HINT, BLUE, YELLOW);
            }
            if ((Core.player.isDebugMode() && hintIndex < level.getNumHints() - 1)
                     || (hintIndex < Math.min(level.getNumHints() - 1, GameController.getTimesFailed() - FAILURE_THRESHOLD_FOR_HINTS))) {
                textDialog.addTextButton("Next Hint", "Next Hint", "info", 8, 5, Button.NEXT_HINT, BLUE, YELLOW);
            }
        }
    }

    public static void nextHint() {
        hintIndex++;
        showHint();
    }

    public static void previousHint() {
        hintIndex--;
        showHint();
    }

    /**
     * Get text dialog.
     * @return text dialog.
     */
    public static TextDialog getDialog() {
        synchronized (getMonitor()) {
            return textDialog;
        }
    }

    /**
     * Initialize text screen.
     */
    public static void init() {
        rotCtr = 0;
        drawLogo();
        scrollPixCtr = 0;
        drawScroller();

        textDialog = new TextDialog();
    }

    /**
     * Update the text screen (for animations)
     */
    public static void update() {
        synchronized (getMonitor()) {
            switch (mode) {
                case INTRO:
                    update_intro();
                    break;
                case BRIEFING:
                    update_briefing();
                    break;
                case DEBRIEFING:
                    update_debriefing();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Update the into screen.
     */
    private static void update_intro() {
        textDialog.clearGroup("introAnimation");
        int logoY = -140;
        
        if (GameController.getMenuThemeOption() == GameController.MenuThemeOption.WINLEMM)
        	logoY = -128;
        
        // manage logo rotation
        if (++rotCtr > MAX_ROT_CTR) {
            // animate
            int rotImgFrameIdx = rotCtr - MAX_ROT_CTR;
            if (rotImgFrameIdx >= rotImg.length) {
                rotImgFrameIdx = rotImgFrameIdx - ((rotImgFrameIdx - ROT_ANIM_LENGTH + 1) * 2);
            }
            if (rotImgFrameIdx <= 0) {
                // reset only after two rounds (flipped back)
                rotImgFrameIdx = 0;
                rotCtr = 0;
            }
            LemmImage rotImgFrame = rotImg[rotImgFrameIdx];
            textDialog.addImage(rotImgFrame, "introAnimation", logoY - (int) (rotImgFrame.getHeight() / 2));
        } else {
            // display original image
            textDialog.addImage(rotImg[0], "introAnimation", logoY - rotImg[0].getHeight() / 2);
        }
        // manage scroller
        LemmImage subimage = scrollerImg.getSubimage(scrollPixCtr, 0, SCROLL_WIDTH, scrollerImg.getHeight());
        textDialog.addImage(subimage, "introAnimation", SCROLL_Y);
        LemmImage lScrollerLem = MiscGfx.getImage(MiscGfx.Index.SCROLLER_LEMMING_LEFT);
        LemmImage rScrollerLem = MiscGfx.getImage(MiscGfx.Index.SCROLLER_LEMMING_RIGHT);
        textDialog.addImage(lScrollerLem, "introAnimation", -(SCROLL_WIDTH / 2) - 16, SCROLL_Y - 16);
        textDialog.addImage(rScrollerLem, "introAnimation", (SCROLL_WIDTH / 2) - 48, SCROLL_Y - 16);

        scrollPixCtr += SCROLL_STEP;
        if (scrollPixCtr >= scrollerImg.getWidth() - SCROLL_WIDTH) {
            scrollPixCtr = 0;
        }
    }

    /**
     * Update the briefing screen.
     */
    private static void update_briefing() {
    }

    /**
     * Update the debriefing screen.
     */
    private static void update_debriefing() {
    }

    /**
     * Draw the text screen to the given graphics object.
     * @param g graphics object to draw the text screen to
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void drawScreen(GraphicsContext g, int x, int y, int width, int height) {
        synchronized (getMonitor()) {
            textDialog.drawScreen(g, x, y, width, height);
        }
    }
    
    /**
     * Switch between menu themes
     */
    public static void setMenuTheme() {
        synchronized (getMonitor()) {
            switch (mode) {
                case INTRO:
                    initIntro();
                    drawLogo();
                    break;
                case BRIEFING:
                    initBriefing();
                    drawLogo();
                    break;
                case DEBRIEFING:
                    initDebriefing();
                    drawLogo();
                    break;
                default:
                    break;
            }
        }
    }
    
    private static MiscGfx.Index getLogoImageIndex() {
    	if (GameController.getMenuThemeOption() == GameController.MenuThemeOption.AMIGA)
    		return MiscGfx.Index.RETROLEMMINI_LOGO_AMIGA;
    	else
    		return MiscGfx.Index.RETROLEMMINI_LOGO_WINLEMM;
    }
    
    private static void drawLogo() {
        synchronized (getMonitor()) {
        	MiscGfx.Index img = getLogoImageIndex();
        	
        	int width = GameController.getMenuThemeOption() == GameController.MenuThemeOption.WINLEMM ? 480 : 560;
        	int height = GameController.getMenuThemeOption() == GameController.MenuThemeOption.WINLEMM ? 140 : 185;
        	 
            rotImg = new LemmImage[ROT_ANIM_LENGTH];
            rotImg[0] = MiscGfx.getImage(img).getScaledInstance(width,  height);
            for (int i = 1; i < rotImg.length; i++) {
                if (i < ROT_ANIM_LENGTH - (ROT_ANIM_LENGTH / 2)) {
                    rotImg[i] = rotImg[0].getScaledInstance(rotImg[0].getWidth(),
                            Math.max((int) (rotImg[0].getHeight()
                                    * (((ROT_ANIM_LENGTH - 1.0) - (i * 2.0)) / (ROT_ANIM_LENGTH - 1.0))), 1),
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
                } else {
                    rotImg[i] = rotImg[ROT_ANIM_LENGTH - 1 - i].transform(false, false, true);
                }
            }
        }
    }
    
    /**
     * Toggle between regular and classic scroller
     */
    public static void toggleScrollerType() {
        synchronized (getMonitor()) {
            drawScroller();
        }
    }

    /**
     * Draw the animated scroller
     */
    public static void drawScroller() {
        synchronized (getMonitor()) {
            LemmImage tempScrollerImg = ToolBox.createLemmImage(
                    LemmFont.getWidth() * (LemmFont.getCharCount(SCROLL_TEXT) + SCROLL_PADDING * 2) + SCROLL_WIDTH * 2,
                    LemmFont.getHeight());
            
            LemmFont.LemmColor textColor = //(GameController.getMenuThemeOption() == GameController.MenuThemeOption.AMIGA)
			            		           //? 
			            		           YELLOW
			            		           //: BLUE
			            		           ;

	        GraphicsContext scrollerGfx = null;
	        try {
	            scrollerGfx = tempScrollerImg.createGraphicsContext();
	            scrollerGfx.setBackground(new Color(0, 0, 0, 0));
	            LemmFont.strImage(scrollerGfx, SCROLL_TEXT, SCROLL_WIDTH + LemmFont.getWidth() * SCROLL_PADDING, 0, textColor);
	        } finally {
	            if (scrollerGfx != null) {
	                scrollerGfx.dispose();
	            }
	        }

            LemmImage tickerTape = //(GameController.getMenuThemeOption() == GameController.MenuThemeOption.AMIGA)
            		               //? 
            		               MiscGfx.getImage(MiscGfx.Index.TICKER_TAPE_BLUE)
            		               //: MiscGfx.getImage(MiscGfx.Index.TICKER_TAPE_PINK)
            		               ;
            	
            double scaleHeight = 1;
            double scaleWidth = 1;

            if (GameController.isOptionEnabled(GameController.SLTooOption.CLASSIC_SCROLLER)) {
                scaleHeight = 0.8;
                scaleWidth = 0.8;
            }

            double padding = 60;
            scrollerImg = ToolBox.createLemmImage(
                    (int) (tempScrollerImg.getWidth() * scaleWidth + padding) + (tickerTape.getWidth() * 2),
                    Math.max((int) (tempScrollerImg.getHeight() * scaleHeight), tickerTape.getHeight()));

            try {
                scrollerGfx = scrollerImg.createGraphicsContext();
                scrollerGfx.setBackground(new Color(0, 0, 0, 0)); // Transparent background.

                if (GameController.isOptionEnabled(GameController.SLTooOption.CLASSIC_SCROLLER)) {
                    int idx = 0;
                    do {
                        scrollerGfx.drawImage(tickerTape, idx, 0);
                        idx += tickerTape.getWidth();
                    } while (idx < scrollerImg.getWidth());

                    scrollerGfx.drawImage(tempScrollerImg, 0, 6, scaleHeight);
                } else {
                    scrollerGfx.drawImage(tempScrollerImg, 0, 0, scrollerImg.getWidth(), scrollerImg.getHeight());
                }
            } finally {
                if (scrollerGfx != null) {
                    scrollerGfx.dispose();
                }
            }
        }
    }

    /**
     * Getter method for monitor
     */
    public static Object getMonitor() {
        return monitor;
    }
}
