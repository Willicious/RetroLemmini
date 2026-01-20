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
package lemmini;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import lemmini.game.Core;
import lemmini.game.GameController;
import lemmini.game.GameController.SLTooOption;
import lemmini.game.Icons;
import lemmini.game.LemmCursor;
import lemmini.game.LemmException;
import lemmini.game.LemmFont;
import lemmini.game.Lemming;
import lemmini.game.Level;
import lemmini.game.LevelCode;
import lemmini.game.LevelPack;
import lemmini.game.LevelRecord;
import lemmini.game.Minimap;
import lemmini.game.MiscGfx;
//import lemmini.game.LemmFont.Color;
import lemmini.game.MiscGfx.Index;
import lemmini.game.Player;
import lemmini.game.ReplayLevelInfo;
import lemmini.game.SpriteObject;
import lemmini.game.Stencil;
import lemmini.game.TextScreen;
import lemmini.gameutil.Fader;
import lemmini.gameutil.Hotkey;
import lemmini.gameutil.RetroLemminiHotkeys;
import lemmini.graphics.GraphicsBuffer;
import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemmImage;
import lemmini.gui.HotkeyConfig;
import lemmini.gui.LevelCodeDialog;
import lemmini.gui.LevelDialog;
import lemmini.gui.OptionsDialog;
import lemmini.gui.PlayerDialog;
import lemmini.tools.ToolBox;

/**
 * A graphics panel in which the actual game contents is displayed.
 * @author Volker Oth
 */
public class LemminiPanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 0x01L;

    /** step size in pixels for horizontal scrolling */
    static final int X_STEP = 4;
    /** step size in pixels for fast horizontal scrolling */
    static final int X_STEP_FAST = 16;
    /** size of auto scrolling range in pixels (from the left and right border) */
    static final int AUTOSCROLL_RANGE = 20;
    /** y coordinate of score display in pixels */
    static final int SCORE_Y = LemminiFrame.LEVEL_HEIGHT;
    /** x coordinate of counter displays in pixels */
    static final int COUNTER_X = 32;
    /** y coordinate of counter displays in pixels */
    static final int COUNTER_Y = SCORE_Y + 40;
    /** x coordinate of icons in pixels */
    static final int ICONS_X = COUNTER_X;
    /** y coordinate of icons in pixels */
    static final int ICONS_Y = COUNTER_Y + 14;
    /** x coordinate of minimap in pixels */
    static final int SMALL_X = ICONS_X + 32 * 15 + 16;
    /** y coordinate of minimap in pixels */
    static final int SMALL_Y = ICONS_Y;

    private boolean needVLockIcon() {
        return GameController.getLevel() != null &&
               GameController.getLevel().getHeight() > Level.DEFAULT_HEIGHT;
    }

    private int getIconBarX() {
        if (GameController.isOptionEnabled(SLTooOption.ENHANCED_ICONBAR) ) {
            if (!needVLockIcon())
                return ICONS_X - 10;
            else
                return 0;
        }
        else return ICONS_X + 10;
    }

    private int getIconBarY() {
        if (GameController.isOptionEnabled(SLTooOption.ENHANCED_ICONBAR) ) {
            return ICONS_Y - 10;
        }
        return ICONS_Y;
    }


    private int getSmallX() {
        if (GameController.isOptionEnabled(SLTooOption.ENHANCED_ICONBAR) ) {
            return SMALL_X + 10;
        }
        return SMALL_X;
    }

    private int getSmallY() {
        if (GameController.isOptionEnabled(SLTooOption.ENHANCED_ICONBAR) ) {
            return SMALL_Y - 3;
        }
        return SMALL_Y;
    }


    private int menuOffsetX;
    /** start x position of mouse drag (for mouse scrolling) */
    private int mouseDragStartX;
    /** start y position of mouse drag (for mouse scrolling) */
    private int mouseDragStartY;
    /** x position of cursor in level */
    private int xMouse;
    /** x position of cursor on screen */
    private int xMouseScreen;
    /** y position of cursor in level */
    private int yMouse;
    /** y position of cursor on screen */
    private int yMouseScreen;
    /** mouse drag length in x direction (pixels) */
    private int mouseDx;
    /** mouse drag length in y direction (pixels) */
    private int mouseDy;
    /** flag: shift key is pressed */
    private boolean shiftPressed;
    /** flag: control key is pressed */
    private boolean controlPressed;
    /** flag: alt key is pressed */
    private boolean altPressed;
    // BOOKMARK TODO: create a combined modifier flag: SHIFT, CONTROL, ALT to more easily detect when *only* one modifier is pressed.
    /** flag: nudge view left hotkey is pressed */
    private boolean nudgeViewLeftPressed;
    /** flag: nudge view right hotkey is pressed */
    private boolean nudgeViewRightPressed;
    /** flag: nudge view up hotkey is pressed */
    private boolean nudgeViewUpPressed;
    /** flag: nudge view down hotkey is pressed */
    private boolean nudgeViewDownPressed;
    /** flag: debug draw is active */
    private boolean draw;
    private boolean isFocused;
    private boolean mouseHasEntered;
    private boolean holdingMinimap;
    private boolean showDebugCursorInfo;
    private int drawBrushSize;
    private static final int MIN_DRAW_BRUSH_SIZE = 1;
    private static final int MAX_DRAW_BRUSH_SIZE = 10;

    /** graphics buffer for information string display */
    private GraphicsBuffer outStrBuffer;
    /** offscreen image */
    private GraphicsBuffer offBuffer;
    /** monitoring object used for synchronized painting */
    private final Object paintSemaphore = new Object();
    private boolean drawNextFrame;
    private int unmaximizedWidth = 0;
    private int unmaximizedHeight = 0;

    private boolean replaySaved = false;

    /**
     * Creates new form LemminiPanel
     */
    public LemminiPanel() {
        isFocused = true;
        mouseHasEntered = true;
        holdingMinimap = false;
        shiftPressed = false;
        initComponents();
        unmaximizedWidth = getWidth();
        unmaximizedHeight = getHeight();
    }

    /**
     * Initialization.
     */
    void init() {
        setBufferSize(Core.unscale(getWidth()), Core.unscale(getHeight()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(0, 0, 0));
        setMinimumSize(new java.awt.Dimension(800, 450));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        mouseHasEntered = false;
        setSize(getWidth(), getHeight());
    }//GEN-LAST:event_formComponentResized

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        mouseDx = 0;
        mouseDy = 0;
        int x = Core.unscale(evt.getX());
        int y = Core.unscale(evt.getY());
        LemmCursor.setX(x);
        LemmCursor.setY(y);
        if (isFocused) {
            mouseHasEntered = true;
        }
    }//GEN-LAST:event_formMouseEntered

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
        switch (GameController.getGameState()) {
            case PREVIEW:
            case POSTVIEW:
            case LEVEL:
                int x = xMouseScreen + Core.scale(mouseDx);
                if (x >= getWidth()) {
                    x = getWidth() - 1;
                }
                if (x < 0) {
                    x = 0;
                }
                xMouseScreen = x;
                x = Core.unscale(x) + GameController.getXPos();
                xMouse = x;
                LemmCursor.setX(Core.unscale(xMouseScreen));

                int y = yMouseScreen + Core.scale(mouseDy);
                if (y >= getHeight()) {
                    y = getHeight() - 1;
                }
                if (y < 0) {
                    y = 0;
                }
                yMouseScreen = y;
                y = Core.unscale(y) + GameController.getYPos();
                yMouse = y;
                LemmCursor.setY(Core.unscale(yMouseScreen));
                evt.consume();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_formMouseExited

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        int x = Core.unscale(evt.getX());
        int y = Core.unscale(evt.getY());
        mouseDx = 0;
        mouseDy = 0;
        // BOOKMARK TODO: rewrite the mouse button selecting to allow changing what all the mouse buttons do
        //                for instance, right now, up to 5 mouse buttons are supported:
        //                BUTTON1 (left-click) is the main button
        //                BUTTON2 (right-click)
        boolean swapButtons = GameController.isOptionEnabled(GameController.Option.SWAP_BUTTONS);
        int buttonPressed = evt.getButton();
        int modifiers = evt.getModifiersEx();
        boolean leftMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON1_DOWN_MASK);
        boolean rightMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON3_DOWN_MASK);

        if (Fader.getState() != Fader.State.OFF
                && GameController.getGameState() != GameController.State.LEVEL) {
            return;
        }

        switch (GameController.getGameState()) {
            case INTRO:
                if (buttonPressed == MouseEvent.BUTTON1) {
                    TextScreen.Button button = TextScreen.getDialog().handleLeftClick(
                            x - Core.getDrawWidth() / 2, y - Core.getDrawHeight() / 2);

                    switch (button) {
                        case NONE:
                        case PLAY_LEVEL:
                            loadDefaultLevel();
                            break;
                        case CHOOSE_LEVEL:
                        	handleChooseLevel();
                        	TextScreen.getDialog().handleMouseReleased();
                        	break;
                        case LOAD_REPLAY:
                            handleLoadReplay();
                            TextScreen.getDialog().handleMouseReleased();
                            break;
                        case ENTER_CODE:
                            handleEnterCode();
                            TextScreen.getDialog().handleMouseReleased();
                            break;
                        case PLAYERS:
                            handlePlayers();
                            TextScreen.getDialog().handleMouseReleased();
                            break;
                        case OPTIONS:
                            handleOptions();
                            TextScreen.getDialog().handleMouseReleased();
                            break;
                        case ABOUT:
                            getParentFrame().handleAbout();
                            TextScreen.getDialog().handleMouseReleased();
                            break;
                        case EXIT:
                            getParentFrame().exit();
                            break;
                        default:
                            break;
                    }

                    evt.consume();
                }
                break;
            case PREVIEW:
                if (buttonPressed == MouseEvent.BUTTON1) {
                    TextScreen.Button button = TextScreen.getDialog().handleLeftClick(
                            x - Core.getDrawWidth() / 2, y - Core.getDrawHeight() / 2);

                    switch (button) {
                        case SHOW_HINT:
                            TextScreen.showHint();
                            break;
                        case SHOW_INFO:
                            TextScreen.showLevelInfo();
                            break;
                        case NEXT_HINT:
                            TextScreen.nextHint();
                            break;
                        case PREVIOUS_HINT:
                            TextScreen.previousHint();
                            break;
                        case NONE:
                        case START_LEVEL:
                            startLevel();
                            break;
                        case MENU:
                            exitToMenu();
                            break;
                        default:
                            break;
                    }

                    evt.consume();
                } else if (buttonPressed == MouseEvent.BUTTON3) {
                    exitToMenu();
                    evt.consume();
                }
                break;
            case POSTVIEW:
                if (buttonPressed == MouseEvent.BUTTON1) {
                    TextScreen.Button button = TextScreen.getDialog().handleLeftClick(
                            x - Core.getDrawWidth() / 2, y - Core.getDrawHeight() / 2);

                    switch (button) {
	                    case NONE:
                        case CONTINUE:
                        	findBestLevelToLoad();
                            break;
                        case RESTART:
                            GameController.requestRestartLevel(false, true);
                            break;
                        case MENU:
                            GameController.setTransition(GameController.TransitionState.TO_INTRO);
                            Fader.setState(Fader.State.OUT);
                            Core.setTitle("RetroLemmini");
                            break;
                        case REPLAY:
                            GameController.requestRestartLevel(true, true);
                            break;
                        case SAVE_REPLAY:
                            handleSaveReplay();
                            break;
                        default:
                            break;
                    }

                    evt.consume();
                } else if (buttonPressed == MouseEvent.BUTTON3) {
                    exitToMenu();
                    evt.consume();
                }
                break;
            case LEVEL:
                //  debug drawing
                if (leftMousePressed || rightMousePressed) {
                    debugDraw(x, y, leftMousePressed);
                }
                if (buttonPressed == MouseEvent.BUTTON1) {
                    if (y >= getIconBarY() && y < getIconBarY() + Icons.getIconHeight()) {
                        //System.out.println("y:" + y + " x:" + x + "\n getIconBarX():" + getIconBarX() + " getIconBarY():" + getIconBarY());
                        //clicking on icons
                        Icons.IconType type = GameController.getIconType(x - menuOffsetX - getIconBarX());
                        if (type != null) {
                            GameController.handleIconButton(type);
                        }
                    } else {
                        //clicking on lemmings
                        Lemming l = GameController.lemmUnderCursor(LemmCursor.getType());
                        if (l != null) {
                            GameController.requestSkill(l);
                        } else if (y < LemminiFrame.LEVEL_HEIGHT) {
                            GameController.stopReplayMode();
                            if (GameController.isOptionEnabled(GameController.SLTooOption.ENABLE_FRAME_STEPPING)) {
                                GameController.advanceFrame();
                            }
                        }
                    }
                    // check minimap mouse move
                    if (x >= getSmallX() + menuOffsetX && x < getSmallX() + menuOffsetX + Minimap.getVisibleWidth()
                            && y >= getSmallY() && y < getSmallY() + Minimap.getVisibleHeight()) {
                        holdingMinimap = true;
                    }
                    evt.consume();
                }
                if (buttonPressed == (swapButtons ? MouseEvent.BUTTON2 : MouseEvent.BUTTON3)) {
                    switch (LemmCursor.getType()) {
                        case NORMAL:
                            setCursor(LemmCursor.CursorType.WALKER);
                            break;
                        case LEFT:
                            setCursor(LemmCursor.CursorType.WALKER_LEFT);
                            break;
                        case RIGHT:
                            setCursor(LemmCursor.CursorType.WALKER_RIGHT);
                            break;
                        default:
                            break;
                    }
                    shiftPressed = true;
                }
                if (buttonPressed == (swapButtons ? MouseEvent.BUTTON3 : MouseEvent.BUTTON2)) {
                	GameController.togglePause();
                }
                if (buttonPressed == 4) {
                    GameController.pressMinus(GameController.KEYREPEAT_KEY);
                }
                if (buttonPressed == 5) {
                    GameController.pressPlus(GameController.KEYREPEAT_KEY);
                }
                break;
            default:
                break;
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        int x = Core.unscale(evt.getX());
        int y = Core.unscale(evt.getY());
        mouseDx = 0;
        mouseDy = 0;
        boolean swapButtons = GameController.isOptionEnabled(GameController.Option.SWAP_BUTTONS);
        int buttonPressed = evt.getButton();

        switch (GameController.getGameState()) {
            case LEVEL:
                if (buttonPressed == MouseEvent.BUTTON1) {
                    holdingMinimap = false;
                    if (y > getIconBarY() && y < getIconBarY() + Icons.getIconHeight()) {
                        Icons.IconType type = GameController.getIconType(x - menuOffsetX - getIconBarX());
                        if (type != null) {
                            GameController.releaseIcon(type);
                        }
                    }
                    // always release icons which don't stay pressed
                    // this is to avoid the icons get stuck when they're pressed,
                    // the the mouse is dragged out and released outside
                    GameController.releasePlus(GameController.KEYREPEAT_ICON);
                    GameController.releaseMinus(GameController.KEYREPEAT_ICON);
                    GameController.releaseIcon(Icons.IconType.MINUS);
                    GameController.releaseIcon(Icons.IconType.PLUS);
                    GameController.releaseIcon(Icons.IconType.NUKE);
                    GameController.releaseIcon(Icons.IconType.RESTART);
                }
                if (buttonPressed == (swapButtons ? MouseEvent.BUTTON2 : MouseEvent.BUTTON3)) {
                    switch (LemmCursor.getType()) {
                        case WALKER:
                            setCursor(LemmCursor.CursorType.NORMAL);
                            break;
                        case WALKER_LEFT:
                            setCursor(LemmCursor.CursorType.LEFT);
                            break;
                        case WALKER_RIGHT:
                            setCursor(LemmCursor.CursorType.RIGHT);
                            break;
                        default:
                            break;
                    }
                    shiftPressed = false;
                }
                if (buttonPressed == 4) {
                    GameController.releaseMinus(GameController.KEYREPEAT_KEY);
                }
                if (buttonPressed == 5) {
                    GameController.releasePlus(GameController.KEYREPEAT_KEY);
                }
                evt.consume();
                break;
            case INTRO:
            	TextScreen.getDialog().handleMouseReleased();
            	break;
            default:
                break;
        }
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        int modifiers = evt.getModifiersEx();
        boolean leftMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON1_DOWN_MASK);
        boolean middleMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON2_DOWN_MASK);
        boolean rightMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON3_DOWN_MASK);
        mouseDx = 0;
        mouseDy = 0;
        // check minimap mouse move
        switch (GameController.getGameState()) {
            case LEVEL:
                int x = Core.unscale(evt.getX());
                int y = Core.unscale(evt.getY());
                if (GameController.isOptionEnabled(GameController.Option.SWAP_BUTTONS)
                        ? rightMousePressed : middleMousePressed) {
                    int xOfsTemp = GameController.getXPos() + (x - mouseDragStartX);
                    GameController.setXPos(xOfsTemp);
                    if (!GameController.isVerticalLock()) {
                        int yOfsTemp = GameController.getYPos() + (y - mouseDragStartY);
                        GameController.setYPos(yOfsTemp);
                    }
                    Minimap.adjustXPos();
                }
                // debug drawing
                if (leftMousePressed || rightMousePressed) {
                    debugDraw(x, y, leftMousePressed);
                }
                formMouseMoved(evt);
                evt.consume();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        int oldX = xMouse;
        int oldY = yMouse;

        xMouse = Core.unscale(evt.getX()) + GameController.getXPos();
        yMouse = Core.unscale(evt.getY()) + GameController.getYPos();
        // LemmCursor
        xMouseScreen = evt.getX();
        if (xMouseScreen >= getWidth()) {
            xMouseScreen = getWidth();
        } else if (xMouseScreen < 0) {
            xMouseScreen = 0;
        }
        yMouseScreen = evt.getY();
        if (yMouseScreen >= getHeight()) {
            yMouseScreen = getHeight() - 1;
        } else if (yMouseScreen < 0) {
            yMouseScreen = 0;
        }
        LemmCursor.setX(Core.unscale(xMouseScreen));
        LemmCursor.setY(Core.unscale(yMouseScreen));

        if (isFocused) {
            mouseHasEntered = true;
        }

        switch (GameController.getGameState()) {
            case INTRO:
            case PREVIEW:
            case POSTVIEW:
                TextScreen.getDialog().handleMouseMove(
                        Core.unscale(xMouseScreen) - Core.getDrawWidth() / 2,
                        Core.unscale(yMouseScreen) - Core.getDrawHeight() / 2);
                /* falls through */
            case LEVEL:
                mouseDx = (xMouse - oldX);
                mouseDy = (yMouse - oldY);
                mouseDragStartX = Core.unscale(evt.getX());
                mouseDragStartY = Core.unscale(evt.getY());
                evt.consume();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_formMouseMoved

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        if (GameController.getGameState() == GameController.State.LEVEL && GameController.isOptionEnabled(GameController.SLTooOption.ENABLE_SCROLL_WHEEL)) {
            int wheelRotation = evt.getWheelRotation();
            if (wheelRotation > 0) {
                for (int i = 0; i < wheelRotation; i++) {
                    GameController.nextSkill();
                }
            } else if (wheelRotation < 0) {
                for (int i = 0; i > wheelRotation; i--) {
                    GameController.previousSkill();
                }
            }
        }
    }//GEN-LAST:event_formMouseWheelMoved
    
    /**
     * Replaces transparent pixels with black
     * @param img
     * @return
     */
    private static LemmImage setFullyOpaque(LemmImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;

                if (alpha < 255) {
                    img.setRGB(x, y, 0xFF000000);
                }
            }
        }

        return img;
    }

    /**
     * Set cursor type.
     * @param c Cursor
     */
    public void setCursor(final LemmCursor.CursorType c) {
        LemmCursor.setType(c);
        super.setCursor(LemmCursor.getCursor());
    }

    @Override
    public void paint(final Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        synchronized (paintSemaphore) {
            if (offBuffer != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        Core.isBilinear()
                                ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
                                : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(offBuffer.getImage().getImage(),
                        0, 0, Core.getScaledDrawWidth(), Core.getScaledDrawHeight(), null);
            }
        }
    }

    @Override
    public void update(final Graphics g) {
        paint(g);
    }

    void focusLost() {
        shiftPressed = false;
        controlPressed = false;
        nudgeViewLeftPressed = false;
        nudgeViewRightPressed = false;
        nudgeViewUpPressed = false;
        nudgeViewDownPressed = false;
        GameController.releasePlus(GameController.KEYREPEAT_ICON | GameController.KEYREPEAT_KEY);
        GameController.releaseMinus(GameController.KEYREPEAT_ICON | GameController.KEYREPEAT_KEY);
        GameController.releaseIcon(Icons.IconType.MINUS);
        GameController.releaseIcon(Icons.IconType.PLUS);
        GameController.releaseIcon(Icons.IconType.NUKE);
        GameController.releaseIcon(Icons.IconType.RESTART);
        LemmCursor.setBox(false);
        setCursor(LemmCursor.CursorType.NORMAL);
        isFocused = false;
        mouseHasEntered = false;
        holdingMinimap = false;
    }

    void focusGained() {
        isFocused = true;
    }
    
    private int getPadding() {
        int maxLemmings = GameController.getNumLemmingsMax();
        int numDigits = String.valueOf(maxLemmings).length();

        switch (numDigits) {
            case 1:
                return 40;
            case 2:
                return 30;
            case 3:
                return 20;
            default:
                return 10;
        }
    }

    /**
     * Redraw the offscreen image, then flip buffers and force repaint.
     */
    private void redraw() {
        if (offBuffer == null) {
            return;
        }

        synchronized (paintSemaphore) {
            GraphicsContext offGfx = offBuffer.getGraphicsContext();

            switch (GameController.getGameState()) {
                case INTRO:
                case PREVIEW:
                case POSTVIEW:
                    offGfx.setClip(0, 0, Core.getDrawWidth(), Core.getDrawHeight());
                    TextScreen.drawScreen(offGfx, 0, 0, Core.getDrawWidth(), Core.getDrawHeight());
                    break;
                case LEVEL:
                case LEVEL_END:
                    LemmImage fgImage = GameController.getFgImage();
                    if (fgImage != null) {
                        // store local copy of offsets to avoid sync problems with AWT threads
                        int xOfsTemp = GameController.getXPos();
                        int minimapXOfsTemp = Minimap.getXPos();
                        int yOfsTemp = GameController.getYPos();

                        int width = Core.getDrawWidth();
                        int height = Core.getDrawHeight();
                        int levelHeight = Math.min(LemminiFrame.LEVEL_HEIGHT, height);

                        Level level = GameController.getLevel();
                        if (level != null) {

                            // clear screen
                            offGfx.setClip(0, 0, width, levelHeight);
                            offGfx.setBackground(level.getBgColor());
                            offGfx.clearRect(0, 0, width, levelHeight);

                            // draw background
                            GameController.getLevel().drawBackground(offGfx, width, levelHeight, xOfsTemp, yOfsTemp);

                            // draw "behind" objects
                            GameController.getLevel().drawBehindObjects(offGfx, width, height, xOfsTemp, yOfsTemp);

                            // draw foreground
                            offGfx.drawImage(fgImage, 0, 0, width, levelHeight, xOfsTemp, yOfsTemp, xOfsTemp + width, yOfsTemp + levelHeight);

                            // draw "in front" objects
                            GameController.getLevel().drawInFrontObjects(offGfx, width, height, xOfsTemp, yOfsTemp);
                        }
                        // clear parts of the screen for menu etc.
                        offGfx.setClip(0, LemminiFrame.LEVEL_HEIGHT, width, height - LemminiFrame.LEVEL_HEIGHT);
                        offGfx.setBackground(java.awt.Color.BLACK);
                        offGfx.clearRect(0, SCORE_Y, width, height - SCORE_Y);
                        // draw counter, icons, small level pic
                        // draw menu and skill counters
                        int iconBarX = menuOffsetX + getIconBarX();
                        int iconBarY = getIconBarY();
                        int countBarX = menuOffsetX + getIconBarX();
                        int countBarY = COUNTER_Y;
                        if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                            countBarY += 7;
                        } else if (!GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR) && GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_STATUS)) {

                            iconBarY += 3;
                            countBarY += 3;
                        }

                        GameController.drawIconsAndCounters(offGfx, iconBarX, iconBarY, countBarX, countBarY);

                        // Draw iconbar filler?
                        // BOOKMARK TODO: the VLock icon is currently hidden behind the filler icon when not needed
                        //                Ideally, the VLock button simply wouldn't be drawn at all
                        int XOffset = 0;
                        int YOffset = 0;

                        if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                            if (needVLockIcon())
                                XOffset = 17;
                            else
                                XOffset = 29;
                        }
                        else {
                            if (needVLockIcon())
                                XOffset = -1; // Don't draw the filler if the V-Lock icon is needed
                            else
                                XOffset = 37;

                            YOffset = 6;
                        }

                        if (XOffset > 0) {
                            LemmImage filler = MiscGfx.getImage(MiscGfx.Index.ICONBAR_FILLER);
                            filler = setFullyOpaque(filler); // Overwrite transparency to ensure that vlock icon is hidden when not needed
                            offGfx.drawImage(filler, menuOffsetX + SMALL_X - XOffset, getIconBarY() - YOffset);
                            filler = null;
                        }

                        // draw minimap
                        if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR)) {
                            drawMiniMapLarge(offGfx, width, height, minimapXOfsTemp, yOfsTemp);
                        } else {
                            drawMiniMap(offGfx, width, height, minimapXOfsTemp, yOfsTemp);
                        }

                        // draw lemmings
                        offGfx.setClip(0, 0, width, levelHeight);
                        GameController.drawLemmings(offGfx);
                        Lemming lemmUnderCursor = GameController.lemmUnderCursor(LemmCursor.getType());
                        offGfx.setClip(0, 0, width, levelHeight);
                        // draw explosions
                        GameController.drawExplosions(offGfx, width, LemminiFrame.LEVEL_HEIGHT, xOfsTemp, yOfsTemp);
                        offGfx.setClip(0, 0, width, height);
                        //draw Visual SFX
                        GameController.drawVisualSfx(offGfx);


                        // draw info string
                        LemmImage outStrImg = outStrBuffer.getImage();
                        GraphicsContext outStrGfx = outStrBuffer.getGraphicsContext();
                        outStrGfx.clearRect(0, 0, outStrImg.getWidth(), outStrImg.getHeight());
                        int statusBarGap = 8; //8 pixels of padding between the bottom of the level and the top of the status line.
                        if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_STATUS)) {
                            statusBarGap = 18;
                        }
                        int yOffset = LemminiFrame.LEVEL_HEIGHT + statusBarGap;

                        if (Core.player.isDebugMode() && showDebugCursorInfo) {
                            Stencil stencil = GameController.getStencil();
                            if (stencil != null) {
                                int stencilVal = stencil.getMask(xMouse, yMouse);
                                int stencilObject = stencil.getMaskObjectID(xMouse, yMouse);
                                String strObj;
                                if (stencilObject >= 0) {
                                    strObj = ", Obj: " + stencilObject;
                                } else {
                                    strObj = StringUtils.EMPTY;
                                }
                                String test = String.format("X: %4d, Y: %3d, Mask: %4d%s", xMouse, yMouse, stencilVal, strObj);
                                LemmFont.strImage(outStrGfx, test);
                                offGfx.drawImage(outStrImg, menuOffsetX + 4, yOffset);
                            }
                        } else {
                            //otherwise show the standard info set
                            String lemmingInfo;
                            if (lemmUnderCursor != null) {
                            	lemmingInfo = lemmUnderCursor.getLemmingInfo();
                                // display also the total number of lemmings under the cursor
                                int num = GameController.getNumLemmsUnderCursor();
                                if (num > 1) {
                                	lemmingInfo += StringUtils.SPACE + num;
                                }
                            } else {
                            	lemmingInfo = StringUtils.EMPTY;
                            }
                            String strHome;
                            if (GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES)
                                    || GameController.getNumLemmingsMax() > 100) {
                                strHome = Integer.toString(GameController.getNumExited());
                            } else {
                                int max = GameController.getNumLemmingsMax();
                                int home = GameController.getNumExited() * 100 / max;
                                strHome = String.format("%02d%%", home);
                            }

                            // standard text-based status display
                            if (!GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_STATUS)) {
                                String status;
                                status = String.format("%-15s OUT %-4d IN %-4s TIME %s", lemmingInfo, GameController.getNumLemmings(), strHome, GameController.getTimeString());
                                //use the standard original "text-based" status bar
                                LemmFont.strImage(outStrGfx, status);
                                offGfx.drawImage(outStrImg, menuOffsetX + 4, yOffset);
                                
                            // enhanced icon-based status display
                            } else {
                                int hatchLems = GameController.getNumLemmingsUnreleased(); // number of lems still in hatch
                                int maxLevelLemm = GameController.getNumLemmingsMax(); // maximum number of lems provided from the start of the level
                                int active = GameController.getNumLemmings(); // number of lems active in the level
                                int saveRequirement = GameController.getNumToRescue(); // the level's save requirement
                                int exited = GameController.getNumExited(); // number of lems that have exited
                                int maxPossibleLemm = GameController.getNumLemmingsPossibleMax(); // maximum number of lems currently possible to save (including Blockers)
                                // show save requirement or maximum lems as the home sub-value depending on "use percentages" option
                                int homeSubValue = GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES) ? saveRequirement : maxLevelLemm;
                            	
                            	int charWidthLa = 18;
                            	int charWidthSm = 9;
                            	int iconWidth = 32;
                            	int padding = getPadding();
                            	int xSpace = 4; // space between icon and value
                            	
                                int xLemInfo = 4;
                                int xTime = 638;
                                // draw everything else in reverse order from xTime, expanding to the left if necessary
                                int xHome = xTime - ((String.valueOf(homeSubValue).length() + 1) * charWidthSm)
                                		          - (strHome.length() * charWidthLa)
                                		          - xSpace - iconWidth - padding;
                                int xActive = xHome - ((String.valueOf(maxLevelLemm).length() + 1) * charWidthSm)
                      		                        - (String.valueOf(active).length() * charWidthLa)
                      		                        - xSpace - iconWidth - padding;
                                int xHatch = xActive - ((String.valueOf(maxPossibleLemm).length() + 1) * charWidthSm)
                                		             - (String.valueOf(hatchLems).length() * charWidthLa)
                      		                         - xSpace - iconWidth - padding;                                
                                
                                // draw the lemming info status
                                LemmImage lemmInfo = LemmFont.strImage(String.format("%-15s", lemmingInfo));
                                offGfx.drawImage(lemmInfo, menuOffsetX + xLemInfo, yOffset);

                                // draw hatch lemmings status (the number of lemmings yet to spawn / the maximum possible from the start)
                                LemmImage lemmIconHatch = MiscGfx.getImage(Index.STATUS_HATCH);
                                offGfx.drawImage(lemmIconHatch, menuOffsetX + xHatch, yOffset);
                                int xHatchW = lemmIconHatch.getWidth() + xSpace;
                               
                                LemmImage lemmHatch = LemmFont.strImage(String.format("%d", hatchLems), LemmFont.LemmColor.GREEN);
                                offGfx.drawImage(lemmHatch, menuOffsetX + xHatch + xHatchW, yOffset);
                                
                                int xMaxLevelLemm = xHatch + xHatchW + lemmHatch.getWidth();
                                LemmImage lemmLevelMax = LemmFont.strImage("/" + String.format("%d", maxLevelLemm), LemmFont.LemmColor.GREEN);
                                offGfx.drawImage(lemmLevelMax, menuOffsetX + xMaxLevelLemm, yOffset + 12, 0.5);

                                // draw active lemmings status (the number of lemmings active in the level / the maximum currently possible)
                                LemmImage lemmIconActive = MiscGfx.getImage(Index.STATUS_ACTIVE);
                                offGfx.drawImage(lemmIconActive, menuOffsetX + xActive, yOffset);
                                int xActiveW = lemmIconActive.getWidth() + xSpace;
                                
                                LemmFont.LemmColor activeLemColor;
                                if (maxPossibleLemm < saveRequirement) {
                                	activeLemColor = LemmFont.LemmColor.RED; // show count in red if there aren't enough lemmings to complete the level
                                } else {
                                	activeLemColor = LemmFont.LemmColor.GREEN;
                                }
                                LemmImage lemmActive = LemmFont.strImage(String.format("%d", active), activeLemColor);
                                offGfx.drawImage(lemmActive, menuOffsetX + xActive + xActiveW, yOffset);
                                
                                int xMaxPossibleLemm = xActive + xActiveW + lemmActive.getWidth();
                                LemmImage lemmPossibleMax = LemmFont.strImage("/" + String.format("%d", maxPossibleLemm), activeLemColor);
                                offGfx.drawImage(lemmPossibleMax, menuOffsetX + xMaxPossibleLemm, yOffset + 12, 0.5);
                                
                                // draw lemmings home status (the number of lemmings exited / the save requirement)
                                LemmImage lemmIconHome = MiscGfx.getImage(Index.STATUS_HOME);
                                offGfx.drawImage(lemmIconHome, menuOffsetX + xHome, yOffset);
                                int xHomeW = lemmIconHome.getWidth() + xSpace;

                                LemmImage lemmHome;
                                if (saveRequirement > exited) {
                                    lemmHome = LemmFont.strImage(strHome, LemmFont.LemmColor.RED); // not enough saved yet, show as red
                                } else {
                                    lemmHome = LemmFont.strImage(strHome);
                                }
                                offGfx.drawImage(lemmHome, menuOffsetX + xHome + xHomeW, yOffset);
                                
                                int xhomeSubValue = xHome + xHomeW + lemmHome.getWidth();
                                LemmImage lemmHomeSubValue = LemmFont.strImage("/" + String.format("%d", homeSubValue), LemmFont.LemmColor.GREEN);
                                offGfx.drawImage(lemmHomeSubValue, menuOffsetX + xhomeSubValue, yOffset + 12, 0.5);
                                
                                // draw time display (for time limit / infinite time)
                                LemmImage lemmIconTime = MiscGfx.getImage(Index.STATUS_TIME);
                                offGfx.drawImage(lemmIconTime, menuOffsetX + xTime, yOffset);
                                int xTimeW = lemmIconTime.getWidth() + xSpace;
                                
                                LemmFont.LemmColor timeColor;
                                int time = GameController.getTime();
                                String timeString = GameController.getTimeString();

                                if (GameController.isTimed()) {
                                	timeColor = LemmFont.LemmColor.GREEN; // time limit
                                	
                                	if (time <= 59)
                                		timeColor = LemmFont.LemmColor.YELLOW;
                                	
                                	if (time <= 10)
                                		timeColor = LemmFont.LemmColor.RED;
                                } else
                                	timeColor = LemmFont.LemmColor.BLUE; // infinite time

                                LemmImage lemmTime = LemmFont.strImage(String.format("%s", timeString), timeColor);
                                offGfx.drawImage(lemmTime, menuOffsetX + xTime + xTimeW, yOffset);

                                lemmIconHatch = null;
                                lemmIconActive = null;
                                lemmIconHome = null;
                                lemmIconTime = null;

                                lemmInfo = null;
                                lemmHatch = null;
                                lemmActive = null;
                                lemmHome = null;
                                lemmTime = null;
                            }
                        }
                        
                        // Additional panel info (smaller text at the top of the panel)
                        int debugModeOffset = 0;
                        int maxExitOffset = 0;
                        int charWidth = 9;

                        // Show if debug mode is enabled, plus features thereof
                        if (Core.player.isDebugMode()) {
                            String debugModeString = "DEBUG ";
                            debugModeOffset += 6 * charWidth;

                            if (draw) {
                                debugModeString += "DRAW ";
                                debugModeOffset += 5 * charWidth;
                            }

                            if (GameController.isSuperLemming()) {
                                debugModeString += "SUPERLEMMING ";
                                debugModeOffset += 13 * charWidth;
                            }

                            LemmImage modeImage = LemmFont.strImage(String.format("%s", debugModeString), LemmFont.LemmColor.BLUE);
                            offGfx.drawImage(modeImage, menuOffsetX + 4, LemminiFrame.LEVEL_HEIGHT + 2, 0.5);
                        }

                        // Show if maximum exit physics is enabled
                        if (Core.player.isMaximumExitPhysics()) {
                            String maxExitString = "MAX-EXIT ";
                            maxExitOffset += 9 * charWidth;

                            LemmImage maxExitImage = LemmFont.strImage(String.format("%s", maxExitString), LemmFont.LemmColor.VIOLET);
                            offGfx.drawImage(maxExitImage, menuOffsetX + 4 + debugModeOffset, LemminiFrame.LEVEL_HEIGHT + 2, 0.5);
                        }

                        // Show the title of the level?
                        if (GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_STATUS) && GameController.isOptionEnabled(GameController.SLTooOption.SHOW_LEVEL_NAME)) {
                            String rating = GameController.getCurLevelPack().getRatings().get(GameController.getCurRating());
                            int levelNum = GameController.getCurLevelNumber() + 1;
                            String levelName = rating + " " + levelNum + ": " + level.getLevelName().trim();
                            LemmImage lemmLevelName = LemmFont.strImage(levelName, LemmFont.LemmColor.GREEN);
                            offGfx.drawImage(lemmLevelName, menuOffsetX + 4 + debugModeOffset + maxExitOffset, LemminiFrame.LEVEL_HEIGHT + 2, 0.5);
                            lemmLevelName = null;
                        }

                        // replay icon
                        LemmImage replayImage = GameController.getReplayImage();
                        if (replayImage != null) {
                            offGfx.drawImage(replayImage, width - 2 * replayImage.getWidth(), replayImage.getHeight());
                        }

                        // draw cursor
                        if (lemmUnderCursor != null) {
                            if (GameController.isOptionEnabled(GameController.Option.CLASSIC_CURSOR)) {
                                if (mouseHasEntered && !LemmCursor.isBox()) {
                                    LemmCursor.setBox(true);
                                    setCursor(LemmCursor.getCursor());
                                }
                            } else {
                                int lx = lemmUnderCursor.footX() - xOfsTemp; //NOTE: footX() was .midX()
                                int ly = lemmUnderCursor.midY() - yOfsTemp;
                                LemmImage cursorImg = LemmCursor.getBoxImage();
                                lx -= cursorImg.getWidth() / 2;
                                ly -= cursorImg.getHeight() / 2;
                                offGfx.drawImage(cursorImg, lx, ly);
                            }
                        } else {
                            if (GameController.isOptionEnabled(GameController.Option.CLASSIC_CURSOR)
                                    && LemmCursor.isBox()) {
                                LemmCursor.setBox(false);
                                setCursor(LemmCursor.getCursor());
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

            // fader
            Fader.apply(offGfx);

            repaint();
        }
    }

    private void drawMiniMap(GraphicsContext offGfx, final int width, final int height, final int minimapXOfsTemp, final int yOfsTemp) {
            final int BORDER_WIDTH = 4;
        // draw minimap
        offGfx.drawImage(MiscGfx.getMinimapImage(), menuOffsetX + getSmallX() - BORDER_WIDTH, getSmallY() - BORDER_WIDTH);
        offGfx.setClip(menuOffsetX + getSmallX(), getSmallY(), Minimap.getVisibleWidth(), Minimap.getVisibleHeight());
        Minimap.draw(offGfx, menuOffsetX + getSmallX(), getSmallY());
        GameController.drawMinimapLemmings(offGfx, menuOffsetX + getSmallX(), getSmallY());
        offGfx.setClip(0, 0, width, height);
        Minimap.drawFrame(offGfx, menuOffsetX + getSmallX(), getSmallY());
        // draw minimap arrows
        if (minimapXOfsTemp > 0) {
            LemmImage leftArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_LEFT);
            offGfx.drawImage(leftArrow,
                    menuOffsetX + getSmallX() - BORDER_WIDTH - leftArrow.getWidth(),
                    getSmallY() + Minimap.getVisibleHeight() / 2 - leftArrow.getHeight() / 2);
        }
        if (minimapXOfsTemp < ToolBox.scale(GameController.getWidth(), Minimap.getScaleX()) - Minimap.getVisibleWidth()) {
            LemmImage rightArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_RIGHT);
            offGfx.drawImage(rightArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() + BORDER_WIDTH,
                    getSmallY() + Minimap.getVisibleHeight() / 2 - rightArrow.getHeight() / 2);
        }
        if (yOfsTemp > 0) {
            LemmImage upArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_UP);
            offGfx.drawImage(upArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() / 2 - upArrow.getWidth() / 2,
                    getSmallY() - BORDER_WIDTH - upArrow.getHeight());
        }
        if (yOfsTemp < GameController.getHeight() - LemminiFrame.LEVEL_HEIGHT) {
            LemmImage downArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_DOWN);
            offGfx.drawImage(downArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() / 2 - downArrow.getWidth() / 2,
                    getSmallY() + Minimap.getVisibleHeight() + BORDER_WIDTH);
        }
    }

    private void drawMiniMapLarge(GraphicsContext offGfx, final int width, final int height, final int minimapXOfsTemp, final int yOfsTemp) {
         final int BORDER_WIDTH = 7;
        // draw minimap
        //draw border around minimap
        offGfx.drawImage(MiscGfx.getMinimapLargeImage(), menuOffsetX + getSmallX() - BORDER_WIDTH, getSmallY() - BORDER_WIDTH);
        offGfx.setClip(menuOffsetX + getSmallX(), getSmallY(), Minimap.getVisibleWidth(), Minimap.getVisibleHeight());
        //draw contents of minimap
        Minimap.draw(offGfx, menuOffsetX + getSmallX(), getSmallY());
        //draw lemmings onto minimap
        GameController.drawMinimapLemmings(offGfx, menuOffsetX + getSmallX(), getSmallY());
        offGfx.setClip(0, 0, width, height);
        //draw the yellow frame around what's visible
        Minimap.drawFrame(offGfx, menuOffsetX + getSmallX(), getSmallY());

        //if the minimap goes off screen??
        // draw minimap arrows
        if (minimapXOfsTemp > 0) {
            LemmImage leftArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_LEFT);
            offGfx.drawImage(leftArrow,
                    menuOffsetX + getSmallX() - BORDER_WIDTH - leftArrow.getWidth(),
                    getSmallY() + Minimap.getVisibleHeight() / 2 - leftArrow.getHeight() / 2);
        }
        if (minimapXOfsTemp < ToolBox.scale(GameController.getWidth(), Minimap.getScaleX()) - Minimap.getVisibleWidth()) {
            LemmImage rightArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_RIGHT);
            offGfx.drawImage(rightArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() + BORDER_WIDTH,
                    getSmallY() + Minimap.getVisibleHeight() / 2 - rightArrow.getHeight() / 2);
        }
        if (yOfsTemp > 0) {
            LemmImage upArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_UP);
            offGfx.drawImage(upArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() / 2 - upArrow.getWidth() / 2,
                    getSmallY() - BORDER_WIDTH - upArrow.getHeight());
        }
        if (yOfsTemp < GameController.getHeight() - LemminiFrame.LEVEL_HEIGHT) {
            LemmImage downArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_DOWN);
            offGfx.drawImage(downArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() / 2 - downArrow.getWidth() / 2,
                    getSmallY() + Minimap.getVisibleHeight() + BORDER_WIDTH);
        }
    }

    private void updateFrame() {
        LemmImage fgImage = GameController.getFgImage();
        switch (GameController.getGameState()) {
            case INTRO:
                TextScreen.setMode(TextScreen.Mode.INTRO);
                TextScreen.update();
                TextScreen.getDialog().handleMouseMove(
                        Core.unscale(xMouseScreen) - Core.getDrawWidth() / 2,
                        Core.unscale(yMouseScreen) - Core.getDrawHeight() / 2);
                break;
            case PREVIEW:
                TextScreen.setMode(TextScreen.Mode.PREVIEW);
                TextScreen.update();
                TextScreen.getDialog().handleMouseMove(
                        Core.unscale(xMouseScreen) - Core.getDrawWidth() / 2,
                        Core.unscale(yMouseScreen) - Core.getDrawHeight() / 2);
                replaySaved = false;
                break;
            case POSTVIEW:
                TextScreen.setMode(TextScreen.Mode.POSTVIEW);
                TextScreen.update();
                TextScreen.getDialog().handleMouseMove(
                        Core.unscale(xMouseScreen) - Core.getDrawWidth() / 2,
                        Core.unscale(yMouseScreen) - Core.getDrawHeight() / 2);
                maybeAutoSaveReplay();
                break;
            case LEVEL:
            case LEVEL_END:
                if (fgImage != null) {
                    GameController.update();
                    // store local copy of xOfs to avoid sync problems with AWT threads
                    // (scrolling by dragging changes xOfs as well)
                    int xOfsTemp = GameController.getXPos();
                    int minimapXOfsTemp = Minimap.getXPos();
                    int yOfsTemp = GameController.getYPos();
                    // mouse movement
                    if (holdingMinimap) {
                        int framePos = ToolBox.scale(xOfsTemp, Minimap.getScaleX()) - minimapXOfsTemp;
                        if (xMouseScreen < Core.scale(menuOffsetX + getSmallX()) && framePos <= 0) {
                            xOfsTemp -= getStepSize();
                            GameController.setXPos(xOfsTemp);
                        } else if (xMouseScreen >= Core.scale(menuOffsetX + getSmallX() + Minimap.getVisibleWidth()) && framePos >= Minimap.getVisibleWidth() - ToolBox.scale(Core.getDrawWidth(), Minimap.getScaleX())) {
                            xOfsTemp += getStepSize();
                            GameController.setXPos(xOfsTemp);
                        } else {
                            xOfsTemp = Minimap.move(Core.unscale(xMouseScreen) - getSmallX() - menuOffsetX, Core.unscale(yMouse) - getSmallY());
                            GameController.setXPos(xOfsTemp);
                        }
                        if (!GameController.isVerticalLock()) {
                            if (yMouseScreen < Core.scale(getSmallY())) {
                                yOfsTemp -= getStepSize();
                                GameController.setYPos(yOfsTemp);
                            } else if (yMouseScreen >= Core.scale(getSmallY() + Minimap.getVisibleHeight())) {
                                yOfsTemp += getStepSize();
                                GameController.setYPos(yOfsTemp);
                            }
                        }
                    } else if (mouseHasEntered) {
                        if (xMouseScreen >= getWidth() - Core.scale(AUTOSCROLL_RANGE)) {
                            xOfsTemp += getStepSize();
                            int beforeXPos = GameController.getXPos();
                            GameController.setXPos(xOfsTemp);
                            int afterXPos = GameController.getXPos();
                            xMouse += (afterXPos - beforeXPos);
                        } else if (xMouseScreen < Core.scale(AUTOSCROLL_RANGE)) {
                            xOfsTemp -= getStepSize();
                            int beforeXPos = GameController.getXPos();
                            GameController.setXPos(xOfsTemp);
                            int afterXPos = GameController.getXPos();
                            xMouse -= (beforeXPos - afterXPos);
                        }
                        if (!GameController.isVerticalLock()) {
                            if (yMouseScreen >= getHeight() - Core.scale(AUTOSCROLL_RANGE)) {
                                yOfsTemp += getStepSize();
                                int beforeYPos = GameController.getYPos();
                                GameController.setYPos(yOfsTemp);
                                int afterYPos = GameController.getYPos();
                                yMouse += (afterYPos - beforeYPos);
                            } else if (yMouseScreen < Core.scale(AUTOSCROLL_RANGE)) {
                                yOfsTemp -= getStepSize();
                                int beforeYPos = GameController.getYPos();
                                GameController.setYPos(yOfsTemp);
                                int afterYPos = GameController.getYPos();
                                yMouse -= (beforeYPos - afterYPos);
                            }
                        }
                    }
                    if (nudgeViewRightPressed && !nudgeViewLeftPressed) {
                        xOfsTemp += getStepSize();
                        GameController.setXPos(xOfsTemp);
                    } else if (nudgeViewLeftPressed && !nudgeViewRightPressed) {
                        xOfsTemp -= getStepSize();
                        GameController.setXPos(xOfsTemp);
                    }
                    if (!GameController.isVerticalLock()) {
                        if (nudgeViewDownPressed && !nudgeViewUpPressed) {
                            yOfsTemp += getStepSize();
                            GameController.setYPos(yOfsTemp);
                        } else if (nudgeViewUpPressed && !nudgeViewDownPressed) {
                            yOfsTemp -= getStepSize();
                            GameController.setYPos(yOfsTemp);
                        }
                    }
                    Minimap.adjustXPos();

                    GameController.updateLemmsUnderCursor();
                }
                break;
            default:
                break;
        }

        // fader
        GameController.fade();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
        final LemminiPanel thisPanel = this;
        ScheduledExecutorService repaintScheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable repaintTask = thisPanel::drawNextFrame;

        try {
            drawNextFrame = false;
            repaintScheduler.scheduleAtFixedRate(
                    repaintTask, 0, GameController.NANOSEC_PER_FRAME, TimeUnit.NANOSECONDS);
            while (true) {
                synchronized (this) {
                    while (!drawNextFrame) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }

                if (drawNextFrame) {
                    drawNextFrame = false;
                    // time passed -> redraw necessary
                    GameController.State gameState = GameController.getGameState();
                    // special handling for fast forward or super lemming mode only during real gameplay
                    if (gameState == GameController.State.LEVEL) {
                        // in fast forward or super lemming modes, update the game mechanics
                        // multiple times per (drawn) frame
                        if (GameController.isFastForward()) {
                        	int multiplier;
                        	if (GameController.isTurbo()) {
                        	    multiplier = GameController.TURBO_MULTI;
                        	} else if (GameController.isOptionEnabled(GameController.Option.FASTER_FAST_FORWARD)) {
                        	    multiplier = GameController.FASTER_FAST_FWD_MULTI;
                        	} else {
                        	    multiplier = GameController.FAST_FWD_MULTI;
                        	}
                            for (int f = 1; f < multiplier; f++) {
                                GameController.update();
                            }
                        } else if (GameController.isSuperLemming()) {
                            for (int f = 1; f < GameController.SUPERLEMM_MULTI; f++) {
                                GameController.update();
                            }
                        }
                    }
                    updateFrame();
                    redraw();
                }
            }
        } catch (Throwable ex) {
            ToolBox.showException(ex);
            System.exit(1);
        }
    }

    /**
     * Signals that the next frame should be drawn.
     */
    public synchronized void drawNextFrame() {
        drawNextFrame = true;
        notifyAll();
    }

    /**
     * Debug routine to draw terrain pixels in stencil and foreground image.
     * @param x x position in pixels
     * @param y y position in pixels
     * @param doDraw true: draw, false: erase
     */
    private void debugDraw(final int x, final int y, final boolean doDraw) {
        if (draw && Core.player.isDebugMode()) {
            boolean classicSteel = GameController.getLevel().getClassicSteel();
            int rgbVal = (doDraw) ? 0xffffffff : 0x0;
            int minimapVal = (doDraw || Minimap.isTinted()) ? rgbVal : GameController.getLevel().getBgColor().getRGB();
            if (doDraw && Minimap.isTinted()) {
                minimapVal = Minimap.tintColor(minimapVal);
            }
            int xOfs = GameController.getXPos();
            int yOfs = GameController.getYPos();
            LemmImage fgImage = GameController.getFgImage();
            LemmImage fgImageSmall = Minimap.getImage();
            Stencil stencil = GameController.getStencil();
            double scaleX = (double) fgImageSmall.getWidth() / (double) fgImage.getWidth();
            double scaleY = (double) fgImageSmall.getHeight() / (double) fgImage.getHeight();
            double scaleXHalf = scaleX / 2.0;
            double scaleYHalf = scaleY / 2.0;

            // Define the radius of the "paintbrush" circle
            if (drawBrushSize <= 0 || drawBrushSize >= 11)
                drawBrushSize = 5;

            int radius = drawBrushSize;

            // Loop through a circle-shaped area
            for (int ya = y - radius; ya <= y + radius; ya++) {
                for (int xa = x - radius; xa <= x + radius; xa++) {
                    // Check if the point is within the circle (using Pythagorean theorem)
                    if (Math.pow(xa - x, 2) + Math.pow(ya - y, 2) <= Math.pow(radius, 2)) {
                        double scaledY = (ya + 0.5) * scaleY % 1.0;
                        boolean drawSmallY = (scaledY >= (0.5 - scaleYHalf) % 1.0 && scaledY < (0.5 + scaleYHalf) % 1.0)
                                || Math.abs(scaleY) >= 1.0;
                        double scaledX = (xa + 0.5) * scaleX % 1.0;
                        boolean drawSmallX = (scaledX >= (0.5 - scaleXHalf) % 1.0 && scaledX < (0.5 + scaleXHalf) % 1.0)
                                || Math.abs(scaleX) >= 1.0;

                        if (xa + xOfs >= 0 && xa + xOfs < GameController.getWidth()
                                && ya + yOfs >= 0 && ya + yOfs < GameController.getHeight()) {
                            int[] objects = stencil.getIDs(xa + xOfs, ya + yOfs);
                            for (int obj : objects) {
                                SpriteObject spr = GameController.getLevel().getSprObject(obj);
                                if (spr != null && spr.getVisOnTerrain()) {
                                    if (doDraw) {
                                        if ((GameController.getLevel().getClassicSteel()
                                                        || !spr.getType().isOneWay())
                                                && !(spr.getType().isOneWay()
                                                        && BooleanUtils.toBoolean(stencil.getMask(xa + xOfs, ya + yOfs) & Stencil.MSK_NO_ONE_WAY_DRAW))) {
                                            spr.setPixelVisibility(xa + xOfs - spr.getX(), ya + yOfs - spr.getY(), true);
                                        }
                                    } else {
                                        spr.setPixelVisibility(xa + xOfs - spr.getX(), ya + yOfs - spr.getY(), false);
                                    }
                                }
                            }
                            if (doDraw) {
                                stencil.orMask(xa + xOfs, ya + yOfs, Stencil.MSK_BRICK);
                            } else {
                                stencil.andMask(xa + xOfs, ya + yOfs,
                                        classicSteel ? ~Stencil.MSK_BRICK
                                                : ~(Stencil.MSK_BRICK | Stencil.MSK_STEEL | Stencil.MSK_ONE_WAY));
                            }
                            GameController.getFgImage().setRGB(xa + xOfs, ya + yOfs, rgbVal);
                            if (drawSmallX && drawSmallY) {
                                fgImageSmall.setRGB(ToolBox.scale(xa + xOfs, scaleX), ToolBox.scale(ya + yOfs, scaleY), minimapVal);
                            }
                        }
                    }
                }
            }
        }
    }

    void handleChooseLevel() {
        LevelDialog ld = new LevelDialog(getParentFrame(), true);
        ld.setVisible(true);
        int[] level = ld.getSelectedLevel();
        if (level != null) {
            GameController.requestChangeLevel(level[0], level[1], level[2], false);
            getParentFrame().setRestartEnabled(true);
        }
    }
    
    void handleHotkeyConfig() {
        List<Hotkey> defaultHotkeys = RetroLemminiHotkeys.getDefaultHotkeys();
        HotkeyConfig hc = new HotkeyConfig(defaultHotkeys);
        hc.setVisible(true);
        GameController.activeHotkeys = hc.getAllHotkeys(); 
    }

    void loadDefaultLevel() {
        // Default to the first level of the first rating of the first pack
        int[] level = {1, 0, 0};

        // Load the last played level
        String savedLevel = Core.programProps.get("lastLevelPlayed", null);

        if (savedLevel != null && !savedLevel.isEmpty()) {
            String[] parts = savedLevel.split(",");
            if (parts.length == 3) {
                try {
                    int packIndex = Integer.parseInt(parts[0].trim());
                    int ratingIndex = Integer.parseInt(parts[1].trim());
                    int levelIndex = Integer.parseInt(parts[2].trim());

                    if (isValidLevel(packIndex, ratingIndex, levelIndex)) {
                        LevelPack pack = GameController.getLevelPack(packIndex);

                        boolean unbeatenLevelFound = false;

                        // First, check if the last level played was completed
                        if (!Core.player.getLevelRecord(pack.getName(), pack.getRatings().get(ratingIndex).toString(), levelIndex).isCompleted()) {
                            unbeatenLevelFound = true;
                        } else {
                            // Then, search for the next unbeaten level in the current pack
                            for (int r = ratingIndex; r < pack.getRatings().size(); r++) {
                                int lStart = (r == ratingIndex) ? levelIndex + 1 : 0;

                                for (int l = lStart; l < pack.getLevelCount(r); l++) {
                                    if (!Core.player.getLevelRecord(pack.getName(), pack.getRatings().get(r).toString(), l).isCompleted()) {
                                        ratingIndex = r;
                                        levelIndex = l;
                                        unbeatenLevelFound = true;
                                        break;
                                    }
                                }
                                if (unbeatenLevelFound) break;
                            }

                            // Then, wrap around to start of the same pack
                            if (!unbeatenLevelFound) {
                                for (int r = 0; r <= ratingIndex; r++) {
                                    int lEnd = (r == ratingIndex) ? levelIndex : pack.getLevelCount(r);

                                    for (int l = 0; l < lEnd; l++) {
                                        if (!Core.player.getLevelRecord(pack.getName(), pack.getRatings().get(r).toString(), l).isCompleted()) {
                                            ratingIndex = r;
                                            levelIndex = l;
                                            unbeatenLevelFound = true;
                                            break;
                                        }
                                    }
                                    if (unbeatenLevelFound) break;
                                }
                            }

                            // Finally, search all packs for the first unbeaten level
                            if (!unbeatenLevelFound) {
                                for (int p = 0; p < GameController.getLevelPackCount(); p++) {
                                    LevelPack nextPack = GameController.getLevelPack(p);
                                    for (int r = 0; r < nextPack.getRatings().size(); r++) {
                                        for (int l = 0; l < nextPack.getLevelCount(r); l++) {
                                            if (!Core.player.getLevelRecord(nextPack.getName(), nextPack.getRatings().get(r).toString(), l).isCompleted()) {
                                                packIndex = p;
                                                ratingIndex = r;
                                                levelIndex = l;
                                                unbeatenLevelFound = true;
                                                break;
                                            }
                                        }
                                        if (unbeatenLevelFound) break;
                                    }
                                    if (unbeatenLevelFound) break;
                                }
                            }
                        }

                        // If an unbeaten level was found, that's the one we load
                        if (unbeatenLevelFound) {
                            level[0] = packIndex;
                            level[1] = ratingIndex;
                            level[2] = levelIndex;
                        }
                        // otherwise, use the previously loaded default
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing lastLevelPlayed: " + savedLevel);
                }
            }
        }

        try {
            GameController.requestChangeLevel(level[0], level[1], level[2], false);
            getParentFrame().setRestartEnabled(true);
        } catch (Exception e) {
        	handleChooseLevel();
        }
    }
    
    private boolean isValidLevel(int packIndex, int ratingIndex, int levelIndex) {
        if (packIndex < 0 || packIndex >= GameController.getLevelPackCount())
            return false;

        if (ratingIndex < 0 || ratingIndex >= GameController.getLevelPack(packIndex).getRatings().size())
            return false;

        if (levelIndex < 0 || levelIndex >= GameController.getLevelPack(packIndex).getLevels(ratingIndex).size())
            return false;

        return true;
    }

    void startLevel() {
        GameController.setTransition(GameController.TransitionState.TO_LEVEL);
        Fader.setState(Fader.State.OUT);
        GameController.resetGain();
    }

    void continueToNextLevel() {
        GameController.nextLevel(); // continue to next level
        GameController.requestChangeLevel(GameController.getCurLevelPackIdx(), GameController.getCurRating(),
                GameController.getCurLevelNumber(), false);
    }
    
    public void findBestLevelToLoad() {
        if (GameController.wasLost()) {
            GameController.requestRestartLevel(false, true);
            return;
        }

        int packIndex = GameController.getCurLevelPackIdx();
        LevelPack pack = GameController.getLevelPack(packIndex);
        int rating = GameController.getCurRating();
        int level = GameController.getCurLevelNumber();

        while (true) {
            // Try to advance to next level
            level++;

            // Advance to next rating
            if (level >= pack.getLevelCount(rating)) {
                rating++;
                level = 0;

                // Load default level if there are no more levels in the current pack
                if (rating >= pack.getRatings().size()) {
                    loadDefaultLevel();
                    return;
                }
            }

            // Stop looping when the next unsolved level is found
            LevelRecord record = Core.player.getLevelRecord(pack.getName(), pack.getRatings().get(rating), level);
            if (!record.isCompleted()) {
                GameController.requestChangeLevel(packIndex, rating, level, false);
                return;
            }
        }
    }

    void exitToMenu() {
        GameController.setTransition(GameController.TransitionState.TO_INTRO);
        Fader.setState(Fader.State.OUT);
        Core.setTitle("RetroLemmini");
    }

    private void maybeAutoSaveReplay() {
        if (!GameController.isOptionEnabled(GameController.RetroLemminiOption.AUTOSAVE_REPLAYS)) return;

        if (replaySaved) return;

        if (GameController.getWasCheated() || GameController.wasLost()) return;
        
        if (GameController.cancelAutosave) return;

        Level level = GameController.getLevel();
        LevelPack levelPack = GameController.getCurLevelPack();
        int curRating = GameController.getCurRating();
        int curLevelNum = GameController.getCurLevelNumber();

        if (level == null || levelPack == null) return;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss__dd_MM_yyyy");
        String timestamp = now.format(formatter);

        String levelName = level.getLevelName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String levelPackName = levelPack.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String ratingName = levelPack.getRatings().get(curRating).replaceAll("[^a-zA-Z0-9_\\-]", "_");

        String replayFileName = String.format("%s__%s__%02d__%s__%s." + Core.REPLAY_EXTENSIONS[0],
            levelPackName, ratingName, curLevelNum + 1, levelName, timestamp);

        Path replayPath = Core.resourcePath.resolve(Core.REPLAYS_PATH).resolve(replayFileName);
        System.out.println("replayPath = " + replayPath);

        if (!GameController.saveReplay(replayPath)) {
            JOptionPane.showMessageDialog(getParent(), "Unable to auto-save replay.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            replaySaved = true;
        }
    }

    void handleSaveReplay() {
        Path replayPath = ToolBox.getFileName(getParent(), Core.resourcePath.resolve(Core.REPLAYS_PATH),
        		"Save Replay", false, false, Core.REPLAY_EXTENSIONS);
        if (replayPath != null) {
            try {
                String ext = FilenameUtils.getExtension(replayPath.getFileName().toString());
                if (ext == null || ext.isEmpty()) {
                    replayPath = replayPath.resolveSibling(replayPath.getFileName().toString() + "." + Core.REPLAY_EXTENSIONS[0]);
                }
                if (GameController.saveReplay(replayPath)) {
                    return;
                }
                // else: no success
                JOptionPane.showMessageDialog(getParent(), "Unable to save replay.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException ex) {
                ToolBox.showException(ex);
            }
        }
    }

    /**
     * This interprets the DMA Remastered level packs as simply "Lemmings" or "Oh No! More Lemmings"
     */
    private boolean checkForDMARemasters(String replayPackName, String actualPackName) {
        if (replayPackName.equals("DMA Lemmings [Remastered]")) {
            return ToolBox.looselyEquals(actualPackName, "Lemmings") ||
                   ToolBox.looselyEquals(actualPackName, "DMA Lemmings [Remastered]");
        }
        if (replayPackName.equals("DMA Oh No! More Lemmings [Remastered]")) {
            return ToolBox.looselyEquals(actualPackName, "Oh No! More Lemmings") ||
                   ToolBox.looselyEquals(actualPackName, "DMA Oh No! More Lemmings [Remastered]");
        }
        return false;
    }

    void handleLoadReplay() {
        Path replayPath = ToolBox.getFileName(getParent(), Core.resourcePath.resolve(Core.REPLAYS_PATH),
        		"Load Replay", true, false, Core.REPLAY_EXTENSIONS);
        if (replayPath != null) {
            try {
                if (FilenameUtils.getExtension(replayPath.getFileName().toString()).equalsIgnoreCase("rpl")) {
                    ReplayLevelInfo rli = GameController.loadReplay(replayPath);
                    if (rli != null) {
                        int lpn = -1;
                        int rn = -1;
                        int ln = -1;
                        LevelPack lp = null;
                        for (int i = 0; i < GameController.getLevelPackCount(); i++) {
                            LevelPack lpTemp = GameController.getLevelPack(i);
                            if (ToolBox.looselyEquals(lpTemp.getName(), rli.getLevelPack()) ||
                                    // Handle replays created with the DMA Remastered packs
                                    checkForDMARemasters(rli.getLevelPack(), lpTemp.getName())) {
                                lpn = i;
                                lp = lpTemp;
                            }
                        }
                        
                        // check external levels if no match is found
                        if (lpn < 0) { 
                        	lp = GameController.getLevelPack(0);
                        	lpn = 0;
                        }
                        
                        if (lp != null && lpn >= 0) {
                            java.util.List<String> ratings = lp.getRatings();
                            int rnTemp = rli.getRatingNumber();
                            if (rnTemp < ratings.size()) {
                                rn = rnTemp;
                            }
                            if (rn < 0 || ToolBox.looselyEquals(ratings.get(rn), rli.getRatingName())) {
                                for (int i = 0; i < ratings.size(); i++) {
                                    if (ToolBox.looselyEquals(ratings.get(i), rli.getRatingName())) {
                                        rn = i;
                                    }
                                }
                            }
                            
                            // check external levels if no match is found
                            if (rn < 0) { 
                            	rn = 0;
                            }
                            
                            if (rn >= 0) {
                                java.util.List<String> levels = lp.getLevels(rn);
                                int lnTemp = rli.getLvlNumber();
                                if (lnTemp < levels.size()) {
                                    ln = lnTemp;
                                }
                                if (ln < 0 || ToolBox.looselyEquals(levels.get(ln), rli.getLvlName())) {
                                    for (int i = 0; i < levels.size(); i++) {
                                        if (ToolBox.looselyEquals(levels.get(i), rli.getLvlName())) {
                                            ln = i;
                                        }
                                    }
                                }
                            }
                        }
                        if (lpn >= 0 && rn >= 0 && ln >= 0) {
                            // success
                            GameController.requestChangeLevel(lpn, rn, ln, true);
                            getParentFrame().setRestartEnabled(true);
                        } else {
                            // no success
                            JOptionPane.showMessageDialog(getParent(),
                                    "Level specified in replay file does not exist.",
                                    "Load Replay", JOptionPane.ERROR_MESSAGE);
                        }
                        return;
                    }
                }
                // else: no success
                JOptionPane.showMessageDialog(getParent(), "Wrong format!", "Load Replay", JOptionPane.ERROR_MESSAGE);
            } catch (LemmException ex) {
                JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Load Replay", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ToolBox.showException(ex);
            }
        }
    }

    void handleEnterCode() {
        LevelCodeDialog lcd = new LevelCodeDialog(getParentFrame(), true);
        lcd.setVisible(true);
        String levelCode = lcd.getCode();
        int lvlPack = lcd.getLevelPack();
        if (levelCode != null && !levelCode.isEmpty() && lvlPack > 0) {
            levelCode = levelCode.trim();

            if (levelCode.toLowerCase().equals("0xlemdebug")) {
                JOptionPane.showMessageDialog(getParent(), "All Levels and Debug Mode activated.", "Cheater!", JOptionPane.INFORMATION_MESSAGE);
                Core.player.setDebugMode(true);
                return;
            }
            if (levelCode.toLowerCase().equals("exitrule0k") || levelCode.toLowerCase().equals("directdr0p")) {
                JOptionPane.showMessageDialog(getParent(), "Maximum Exit Physics activated.", "Geronimo!", JOptionPane.INFORMATION_MESSAGE);
                Core.player.setMaximumExitPhysics(true);
                return;
            }

            // real level code -> get absolute level
            levelCode = levelCode.toUpperCase();
            LevelPack lpack = GameController.getLevelPack(lvlPack);
            int[] codeInfo = LevelCode.getLevel(lpack.getCodeSeed(), levelCode, lpack.getCodeOffset());
            if (codeInfo != null) {
                if (Core.player.isDebugMode()) {
                    JOptionPane.showMessageDialog(getParent(),
                            String.format("Level: %d%nPercent Saved: %d%%%nTimes Failed: %d%nUnknown: %d",
                                    codeInfo[0] + 1, codeInfo[1], codeInfo[2], codeInfo[3]),
                            "Code Info",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                // calculate level pack and relative level number from absolute number
                int[] l = GameController.relLevelNum(lvlPack, codeInfo[0]);
                int rating = l[0];
                int lvlRel = l[1];
                if (rating >= 0 && lvlRel >= 0) {
                    Core.player.setAvailable(lpack.getName(), lpack.getRatings().get(rating), lvlRel);
                    GameController.requestChangeLevel(lvlPack, rating, lvlRel, false);
                    getParentFrame().setRestartEnabled(true);
                    return;
                }
            }
        }
        // not found
        if (lvlPack != -1) {
            JOptionPane.showMessageDialog(getParent(), "Invalid Level Code.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void handlePlayers() {
        Core.player.store(); // save player in case it is changed
        PlayerDialog d = new PlayerDialog(getParentFrame(), true);
        d.setVisible(true);
        // blocked until dialog returns
        java.util.List<String> players = d.getPlayers();
        if (players != null) {
            String player = Core.player.getName(); // old player
            int playerIdx = d.getSelection();
            if (playerIdx != -1) {
                player = players.get(playerIdx); // remember selected player
            }
            // check for players to delete
            for (int i = 0; i < Core.getPlayerCount(); i++) {
                String p = Core.getPlayer(i);
                if (!players.contains(p)) {
                    Core.deletePlayer(i);
                    if (p.equals(player)) {
                        player = "default";
                    }
                }
            }
            // rebuild players list
            Core.clearPlayers();
            // add default player if missing
            if (!players.contains("default")) {
                players.add("default");
            }
            // now copy all players and create properties
            players.stream().forEachOrdered(Core::addPlayer);

            // select new default player
            if (!Core.player.getName().equals(player)
                    && GameController.getGameState() != GameController.State.INTRO) {
                if (GameController.getGameState() == GameController.State.LEVEL) {
                    GameController.setGameState(GameController.State.LEVEL_END);
                }
                GameController.setTransition(GameController.TransitionState.TO_INTRO);
                Fader.setState(Fader.State.OUT);
                Core.setTitle("RetroLemmini");
            }
            Core.player = new Player(player);
        }
    }

    void handleOptions() {
        // Store current settings
    	boolean oldMinimapOption = GameController.isOptionEnabled(GameController.RetroLemminiOption.FULL_COLOR_MINIMAP);
        boolean oldMenuBarVisOption = GameController.isOptionEnabled(GameController.RetroLemminiOption.SHOW_MENU_BAR);
        boolean oldScrollerOption = GameController.isOptionEnabled(GameController.SLTooOption.CLASSIC_SCROLLER);
        GameController.MenuThemeOption oldMenuThemeOption = GameController.getMenuThemeOption();

        // Show options dialog
        OptionsDialog d = new OptionsDialog(getParentFrame(), true);
        d.setVisible(true);

        // Update UI if options have changed
        if (oldMinimapOption != GameController.isOptionEnabled(GameController.RetroLemminiOption.FULL_COLOR_MINIMAP)) {
        	if (GameController.getGameState() == GameController.State.LEVEL) {
        		Minimap.init(1.0 / 16.0, 1.0 / 8.0, oldMinimapOption);
        	}
        }
        if (oldMenuBarVisOption != GameController.isOptionEnabled(GameController.RetroLemminiOption.SHOW_MENU_BAR)) {
            getParentFrame().toggleMenuBarVisibility();
        }
        if (oldScrollerOption != GameController.isOptionEnabled(GameController.SLTooOption.CLASSIC_SCROLLER)) {
            TextScreen.toggleScrollerType();
        }
        if (oldMenuThemeOption != GameController.getMenuThemeOption()) {
            TextScreen.setMenuTheme();
        }
        
        d.dispose();
    }

    @Override
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        LemminiFrame parentFrame = getParentFrame();
        int frameState = 0;
        if (parentFrame != null) {
            frameState = parentFrame.getExtendedState();
        }
        if (!BooleanUtils.toBoolean(frameState & Frame.MAXIMIZED_HORIZ)) {
            unmaximizedWidth = width;
        }
        if (!BooleanUtils.toBoolean(frameState & Frame.MAXIMIZED_VERT)) {
            unmaximizedHeight = height;
        }
        setScale(getWidth(), getHeight());
        Core.setDrawSize(Core.unscale(getWidth()), Core.unscale(getHeight()));
        setBufferSize(Core.getDrawWidth(), Core.getDrawHeight());
        // if possible, make sure that the screen is not positioned outside the level
        GameController.setXPos(GameController.getXPos());
        GameController.setYPos(GameController.getYPos());
    }

    private void setScale(int width, int height) {
        Dimension minSize = getMinimumSize();
        if ((double) width / (double) height >= (double) minSize.width / (double) minSize.height) {
            Core.setScale((double) height / (double) minSize.height);
        } else {
            Core.setScale((double) width / minSize.width);
        }
    }

    private void setBufferSize(int width, int height) {
        if (width <= 0 || height <= 0 || LemmFont.getHeight() <= 0) {
            return;
        }

        synchronized (paintSemaphore) {
            if (offBuffer == null) {
                offBuffer = new GraphicsBuffer(width, height, Transparency.OPAQUE, true);
            } else {
                offBuffer.setSize(width, height);
            }

            if (outStrBuffer == null) {
                outStrBuffer = new GraphicsBuffer(width, LemmFont.getHeight(), Transparency.TRANSLUCENT, true);
            } else {
                outStrBuffer.setSize(width, LemmFont.getHeight());
            }

            menuOffsetX = Math.max(0, (width - getMinimumSize().width) / 2);
        }
    }

    private LemminiFrame getParentFrame() {
        Container container = getParent();
        while (container != null && !(container instanceof LemminiFrame)) {
            container = container.getParent();
        }
        return (LemminiFrame) container;
    }

    int getUnmaximizedWidth() {
        return unmaximizedWidth;
    }

    int getUnmaximizedHeight() {
        return unmaximizedHeight;
    }

    /**
     * Get cursor x position in pixels.
     * @return cursor x position in pixels
     */
    int getCursorX() {
        return xMouse;
    }

    /**
     * Get cursor y position in pixels.
     * @return cursor y position in pixels
     */
    int getCursorY() {
        return yMouse;
    }

    /**
     * Get flag: Shift key is pressed?
     * @return true if shift key is pressed, false otherwise
     */
    boolean isShiftPressed() {
        return shiftPressed;
    }

    /**
     * Set flag: Shift key is pressed.
     * @param p true: Shift key is pressed, false otherwise
     */
    void setShiftPressed(final boolean p) {
        shiftPressed = p;
    }

    /**
     * Get flag: Control key is pressed?
     * @return true if control key is pressed, false otherwise
     */
    boolean isControlPressed() {
        return controlPressed;
    }

    /**
     * Set flag: Control key is pressed.
     * @param p true: control key is pressed, false otherwise
     */
    void setControlPressed(final boolean p) {
        controlPressed = p;
    }

    /**
     * Get flag: Alt key is pressed?
     * @return true if control key is pressed, false otherwise
     */
    boolean isAltPressed() {
        return altPressed;
    }

    /**
     * Set flag: Alt key is pressed.
     * @param p true: control key is pressed, false otherwise
     */
    void setAltPressed(final boolean p) {
        altPressed = p;
    }

    /**
     * Get/set nudge view flags:
     */
    boolean isNudgeViewLeftPressed() {
        return nudgeViewLeftPressed;
    }

    void setNudgeViewLeftPressed(final boolean p) {
    	nudgeViewLeftPressed = p;
    }

    boolean isNudgeViewRightPressed() {
        return nudgeViewRightPressed;
    }

    void setNudgeViewRightPressed(final boolean p) {
    	nudgeViewRightPressed = p;
    }

    boolean isNudgeViewUpPressed() {
        return nudgeViewUpPressed;
    }

    void setNudgeViewUpPressed(final boolean p) {
    	nudgeViewUpPressed = p;
    }

    boolean isNudgeViewDownPressed() {
        return nudgeViewDownPressed;
    }

    void setNudgeViewDownPressed(final boolean p) {
    	nudgeViewDownPressed = p;
    }

    /**
     * Get state of debug draw option.
     * @return true: debug draw is active, false otherwise
     */
    boolean getDebugDraw() {
        return draw;
    }

    /**
     * Set state of debug draw option.
     * @param d true: debug draw is active, false otherwise
     */
    void setDebugDraw(final boolean d) {
        draw = d;
    }

    /**
     * Get state of debug cursor info visiblity.
     * @return true if showDebugCursorInfo is true, false otherwise
     */
    boolean debugCursorInfoVisible() {
        return showDebugCursorInfo;
    }

    /**
     * Set state of debug cursor info visiblity.
     * @param i true: showDebugCursorInfo is true, false otherwise
     */
    void setDebugCursorInfo(final boolean i) {
        showDebugCursorInfo = i;
    }

    private int getStepSize() {
        return (shiftPressed ? X_STEP_FAST : X_STEP);
    }

    public int getDrawBrushSize() {
        return drawBrushSize;
    }

    public void setDrawBrushSize(int size) {
        if (size < MIN_DRAW_BRUSH_SIZE || size > MAX_DRAW_BRUSH_SIZE)
            return;
        this.drawBrushSize = size;
    }
    
    public void increaseDrawBrushSize() {
        setDrawBrushSize(drawBrushSize + 1);
    }

    public void decreaseDrawBrushSize() {
        setDrawBrushSize(drawBrushSize - 1);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
