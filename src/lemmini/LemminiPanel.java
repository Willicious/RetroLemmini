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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import lemmini.game.Core;
import lemmini.game.LemGame;
import lemmini.game.LemGame.Option;
import lemmini.game.Icons;
import lemmini.game.LemCursor;
import lemmini.game.LemException;
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
import lemmini.gameutil.MouseInput;
import lemmini.graphics.GraphicsBuffer;
import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemmImage;
import lemmini.gui.HotkeyConfig;
import lemmini.gui.LevelCodeDialog;
import lemmini.gui.LevelDialog;
import lemmini.gui.MouseConfig;
import lemmini.gui.OptionsDialog;
import lemmini.gui.PlayerDialog;
import lemmini.tools.ToolBox;

/**
 * A graphics panel in which the actual game contents is displayed.
 * @author Volker Oth
 * Modified by Ryan Sakowski, Charles Irwin and Will James
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
        return LemGame.getLevel() != null &&
               LemGame.getLevel().getHeight() > Level.DEFAULT_HEIGHT;
    }

    private int getIconBarX() {
        if (LemGame.isOptionEnabled(Option.ENHANCED_ICONBAR) ) {
            if (!needVLockIcon())
                return ICONS_X - 10;
            else
                return 0;
        }
        else return ICONS_X + 10;
    }

    private int getIconBarY() {
        if (LemGame.isOptionEnabled(Option.ENHANCED_ICONBAR) ) {
            return ICONS_Y - 10;
        }
        return ICONS_Y;
    }


    private int getSmallX() {
        if (LemGame.isOptionEnabled(Option.ENHANCED_ICONBAR) ) {
            return SMALL_X + 10;
        }
        return SMALL_X;
    }

    private int getSmallY() {
        if (LemGame.isOptionEnabled(Option.ENHANCED_ICONBAR) ) {
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
        LemGame.resetModifierKeys();
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
        LemCursor.setX(x);
        LemCursor.setY(y);
        if (isFocused) {
            mouseHasEntered = true;
        }
    }//GEN-LAST:event_formMouseEntered

    private void formMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
        switch (LemGame.getGameState()) {
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
                x = Core.unscale(x) + LemGame.getXPos();
                xMouse = x;
                LemCursor.setX(Core.unscale(xMouseScreen));

                int y = yMouseScreen + Core.scale(mouseDy);
                if (y >= getHeight()) {
                    y = getHeight() - 1;
                }
                if (y < 0) {
                    y = 0;
                }
                yMouseScreen = y;
                y = Core.unscale(y) + LemGame.getYPos();
                yMouse = y;
                LemCursor.setY(Core.unscale(yMouseScreen));
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
        int buttonPressed = evt.getButton();
        int modifiers = evt.getModifiersEx();
        boolean leftMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON1_DOWN_MASK);
        //boolean middleMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON2_DOWN_MASK);
        boolean rightMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.BUTTON3_DOWN_MASK);

        if (Fader.getState() != Fader.State.OFF
                && LemGame.getGameState() != LemGame.State.LEVEL) {
            return;
        }

        switch (LemGame.getGameState()) {
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
                            LemGame.requestRestartLevel(false, true);
                            break;
                        case MENU:
                            LemGame.setTransition(LemGame.TransitionState.TO_INTRO);
                            Fader.setState(Fader.State.OUT);
                            Core.setWindowCaption("RetroLemmini");
                            break;
                        case REPLAY:
                            LemGame.requestRestartLevel(true, true);
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
                boolean isDebugDraw = Core.player.isDebugMode() && draw;
                boolean mouseOverPanel = y >= getIconBarY() && y < getIconBarY() + Icons.getIconHeight();
                
                //  debug drawing
                if (isDebugDraw && (leftMousePressed || rightMousePressed)) {
                    debugDraw(x, y, leftMousePressed);
                } else {
	                if (buttonPressed == MouseEvent.BUTTON1) {
	                	LemGame.leftMouseButtonHeld = true;
	                    if (mouseOverPanel) {
	                        //clicking on icons
	                        Icons.IconType type = LemGame.getIconType(x - menuOffsetX - getIconBarX());
	                        if (type != null) {
	                            LemGame.handleIconButton(type);
	                        }
	                    } else {
	                        //clicking on lemmings
	                        Lemming l = LemGame.lemmUnderCursor(LemCursor.getType());
	                        if (l != null) {
	                            LemGame.requestSkill(l);
	                            LemGame.lastAssignedLemming = l;
	                        } else if (y < LemminiFrame.LEVEL_HEIGHT) {
	                        	if (LemGame.isOptionEnabled(LemGame.Option.CLICK_AIR_TO_CANCEL_REPLAY))
	                        		LemGame.stopReplayMode();
	                            if (LemGame.isOptionEnabled(LemGame.Option.ENABLE_FRAME_STEPPING))
	                            	LemGame.advanceFrame();
	                        }
	                    }
	                    // check minimap mouse move
	                    if (x >= getSmallX() + menuOffsetX && x < getSmallX() + menuOffsetX + Minimap.getVisibleWidth()
	                            && y >= getSmallY() && y < getSmallY() + Minimap.getVisibleHeight()) {
	                        holdingMinimap = true;
	                    }
	                    evt.consume();
	                } else {
	                	for (MouseInput.MouseAction action :
	                		Core.getMouseInput().getActionsForButton(buttonPressed)) {
			                    switch (action) {
			                        case TOGGLEPAUSE:
			                            LemGame.togglePause();
			                            break;		
			                        case SELECTWALKER:
			                            pressSelectWalker();
			                            break;		
			                        case FASTSCROLL:
			                            LemGame.setShiftPressed(true);
			                            break;
			                        case RELEASERATEDOWN:
			                        	LemGame.pressMinus(LemGame.KEYREPEAT_KEY);
			                        	break;
			                        case RELEASERATEUP:
			                        	LemGame.pressPlus(LemGame.KEYREPEAT_KEY);
			                        	break;
			                        default:
			                            break;
			                    }
			                }
		                }
	                }
            	break;
            default:
                break;
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        LemGame.leftMouseButtonHeld = false;
        LemGame.attemptingHoldToAssign = false;
        LemGame.lastAssignedLemming = null;
    	
    	int x = Core.unscale(evt.getX());
        int y = Core.unscale(evt.getY());
        mouseDx = 0;
        mouseDy = 0;
        int buttonPressed = evt.getButton();

        switch (LemGame.getGameState()) {
            case LEVEL:
                if (buttonPressed == MouseEvent.BUTTON1) {
                    holdingMinimap = false;
                    if (y > getIconBarY() && y < getIconBarY() + Icons.getIconHeight()) {
                        Icons.IconType type = LemGame.getIconType(x - menuOffsetX - getIconBarX());
                        if (type != null) {
                            LemGame.releaseIcon(type);
                        }
                    }
                    // always release icons which don't stay pressed
                    LemGame.releasePlus(LemGame.KEYREPEAT_ICON);
                    LemGame.releaseMinus(LemGame.KEYREPEAT_ICON);
                    LemGame.releaseIcon(Icons.IconType.MINUS);
                    LemGame.releaseIcon(Icons.IconType.PLUS);
                    LemGame.releaseIcon(Icons.IconType.NUKE);
                    LemGame.releaseIcon(Icons.IconType.RESTART);
                } else {
                	for (MouseInput.MouseAction action :
                        Core.getMouseInput().getActionsForButton(buttonPressed)) {
		                    switch (action) {
	                        case SELECTWALKER:
	    	                	releaseSelectWalker();
	                        	break;
	                        case FASTSCROLL:
	    	            	    LemGame.setShiftPressed(false);
	                        	break;
	                        case RELEASERATEDOWN:
	                        	LemGame.releaseMinus(LemGame.KEYREPEAT_KEY);
	                        	break;
	                        case RELEASERATEUP:
	                        	LemGame.releasePlus(LemGame.KEYREPEAT_KEY);
	                        	break;
	                    	default:
	                    		break;
		                    }
		                }
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
        boolean backwardMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.getMaskForButton(4));
        boolean forwardMousePressed = BooleanUtils.toBoolean(modifiers & InputEvent.getMaskForButton(5));
        mouseDx = 0;
        mouseDy = 0;
        // check minimap mouse move
        switch (LemGame.getGameState()) {
            case LEVEL:
                int x = Core.unscale(evt.getX());
                int y = Core.unscale(evt.getY());
                
            	// debug drawing
                boolean isDebugDraw = draw && Core.player.isDebugMode(); 
                if (isDebugDraw && (leftMousePressed || rightMousePressed)) {
                    debugDraw(x, y, leftMousePressed);
                } else {
                	int button = MouseEvent.NOBUTTON;
                	if (leftMousePressed) {
                	    button = MouseEvent.BUTTON1;
                	}
                	else if (middleMousePressed) {
                	    button = MouseEvent.BUTTON2;
                	}
                	else if (rightMousePressed) {
                	    button = MouseEvent.BUTTON3;
                	}
                	else if (backwardMousePressed) {
                	    button = 4;
                	}
                	else if (forwardMousePressed) {
                	    button = 5;
                	}                	
                	for (MouseInput.MouseAction action :
                        Core.getMouseInput().getActionsForButton(button)) {
		                    switch (action) {
	                        case DRAGVIEWAREA:
	                        	dragViewArea(x, y);
	                        	break;
	                    	default:
	                    		break;
		                    }
		                }
	                formMouseMoved(evt);
	                evt.consume();
                }
                break;
            default:
                break;
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        int oldX = xMouse;
        int oldY = yMouse;

        xMouse = Core.unscale(evt.getX()) + LemGame.getXPos();
        yMouse = Core.unscale(evt.getY()) + LemGame.getYPos();
        // LemCursor
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
        LemCursor.setX(Core.unscale(xMouseScreen));
        LemCursor.setY(Core.unscale(yMouseScreen));

        if (isFocused) {
            mouseHasEntered = true;
        }

        switch (LemGame.getGameState()) {
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
        if (LemGame.getGameState() != LemGame.State.LEVEL)
        	return;
        
        boolean isDebugDraw = draw && Core.player.isDebugMode();
        boolean enableWheelBrushSize = isDebugDraw && LemGame.isOptionEnabled(LemGame.Option.ENABLE_WHEEL_BRUSH_SIZE);
        boolean enableWheelSkillSelect = LemGame.isOptionEnabled(LemGame.Option.ENABLE_WHEEL_SKILL_SELECT);
        
        if (!enableWheelSkillSelect && !enableWheelBrushSize)
        	return;
    	
        int wheelRotation = evt.getWheelRotation();
        boolean wheelUp = wheelRotation < 0;
        boolean wheelDown = wheelRotation > 0;
        
    	if (enableWheelBrushSize) {
            if (wheelUp) {
                for (int i = 0; i > wheelRotation; i--) {
                	increaseDrawBrushSize();
                }
            } else if (wheelDown) {
                for (int i = 0; i < wheelRotation; i++) {
                	decreaseDrawBrushSize();
                }
            }
    	} else if (enableWheelSkillSelect) {
            if (wheelUp) {
                for (int i = 0; i > wheelRotation; i--) {
                    LemGame.previousSkill();
                }
            } else if (wheelDown) {
                for (int i = 0; i < wheelRotation; i++) {
                    LemGame.nextSkill();
                }
            }
        }
    }//GEN-LAST:event_formMouseWheelMoved
    
    private void dragViewArea(int x, int y) {
        int xOfsTemp = LemGame.getXPos() + (x - mouseDragStartX);
        LemGame.setXPos(xOfsTemp);
        if (!LemGame.isVerticalLock()) {
            int yOfsTemp = LemGame.getYPos() + (y - mouseDragStartY);
            LemGame.setYPos(yOfsTemp);
        }
        Minimap.adjustXPos();
    }
    
    /**
     * Replaces transparent pixels with black
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
     */
    public void setCursor(final LemCursor.CursorType c) {
        LemCursor.setType(c);
        super.setCursor(LemCursor.getCursor());
    }
    
    public void pressSelectWalker() {
    	switch (LemCursor.getType()) {
	        case NORMAL:
	            setCursor(LemCursor.CursorType.WALKER);
	            break;
	        case LEFT:
	            setCursor(LemCursor.CursorType.WALKER_LEFT);
	            break;
	        case RIGHT:
	            setCursor(LemCursor.CursorType.WALKER_RIGHT);
	            break;
	        default:
	            break;
	    }
    }
    
    public void releaseSelectWalker() {
    	switch (LemCursor.getType()) {
	        case WALKER:
	            setCursor(LemCursor.CursorType.NORMAL);
	            break;
	        case WALKER_LEFT:
	            setCursor(LemCursor.CursorType.LEFT);
	            break;
	        case WALKER_RIGHT:
	            setCursor(LemCursor.CursorType.RIGHT);
	            break;
	        default:
	            break;
	    }
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
    	LemGame.resetModifierKeys();
        nudgeViewLeftPressed = false;
        nudgeViewRightPressed = false;
        nudgeViewUpPressed = false;
        nudgeViewDownPressed = false;
        LemGame.releasePlus(LemGame.KEYREPEAT_ICON | LemGame.KEYREPEAT_KEY);
        LemGame.releaseMinus(LemGame.KEYREPEAT_ICON | LemGame.KEYREPEAT_KEY);
        LemGame.releaseIcon(Icons.IconType.MINUS);
        LemGame.releaseIcon(Icons.IconType.PLUS);
        LemGame.releaseIcon(Icons.IconType.NUKE);
        LemGame.releaseIcon(Icons.IconType.RESTART);
        LemCursor.setBox(false);
        setCursor(LemCursor.CursorType.NORMAL);
        isFocused = false;
        mouseHasEntered = false;
        holdingMinimap = false;
    }

    void focusGained() {
        isFocused = true;
    }
    
    private int getPadding() {
        int maxLemmings = LemGame.getNumLemmingsMax();
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

            switch (LemGame.getGameState()) {
                case INTRO:
                case PREVIEW:
                case POSTVIEW:
                    offGfx.setClip(0, 0, Core.getDrawWidth(), Core.getDrawHeight());
                    TextScreen.drawScreen(offGfx, 0, 0, Core.getDrawWidth(), Core.getDrawHeight());
                    break;
                case LEVEL:
                case LEVEL_END:
                    LemmImage fgImage = LemGame.getFgImage();
                    if (fgImage != null) {
                        // store local copy of offsets to avoid sync problems with AWT threads
                        int xOfsTemp = LemGame.getXPos();
                        int minimapXOfsTemp = Minimap.getXPos();
                        int yOfsTemp = LemGame.getYPos();

                        int width = Core.getDrawWidth();
                        int height = Core.getDrawHeight();
                        int levelHeight = Math.min(LemminiFrame.LEVEL_HEIGHT, height);

                        Level level = LemGame.getLevel();
                        if (level != null) {

                            // clear screen
                            offGfx.setClip(0, 0, width, levelHeight);
                            offGfx.setBackground(level.getBgColor());
                            offGfx.clearRect(0, 0, width, levelHeight);

                            // draw background
                            LemGame.getLevel().drawBackground(offGfx, width, levelHeight, xOfsTemp, yOfsTemp);

                            // draw "behind" objects
                            LemGame.getLevel().drawBehindObjects(offGfx, width, height, xOfsTemp, yOfsTemp);

                            // draw foreground
                            offGfx.drawImage(fgImage, 0, 0, width, levelHeight, xOfsTemp, yOfsTemp, xOfsTemp + width, yOfsTemp + levelHeight);

                            // draw "in front" objects
                            LemGame.getLevel().drawInFrontObjects(offGfx, width, height, xOfsTemp, yOfsTemp);
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
                        if (LemGame.isOptionEnabled(LemGame.Option.ENHANCED_ICONBAR)) {
                            countBarY += 7;
                        } else if (!LemGame.isOptionEnabled(LemGame.Option.ENHANCED_ICONBAR) && LemGame.isOptionEnabled(LemGame.Option.ENHANCED_STATUS)) {

                            iconBarY += 3;
                            countBarY += 3;
                        }

                        LemGame.drawIconsAndCounters(offGfx, iconBarX, iconBarY, countBarX, countBarY);

                        // Draw iconbar filler?
                        // BOOKMARK TODO: the VLock icon is currently hidden behind the filler icon when not needed
                        //                Ideally, the VLock button simply wouldn't be drawn at all
                        int XOffset = 0;
                        int YOffset = 0;

                        if (LemGame.isOptionEnabled(LemGame.Option.ENHANCED_ICONBAR)) {
                            if (needVLockIcon())
                                XOffset = 17;
                            else
                                XOffset = 29;
                        } else {
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
                        if (LemGame.isOptionEnabled(LemGame.Option.ENHANCED_ICONBAR)) {
                            drawMiniMapLarge(offGfx, width, height, minimapXOfsTemp, yOfsTemp);
                        } else {
                            drawMiniMap(offGfx, width, height, minimapXOfsTemp, yOfsTemp);
                        }

                        // draw lemmings
                        offGfx.setClip(0, 0, width, levelHeight);
                        LemGame.drawLemmings(offGfx, LemGame.getXPos(), LemGame.getYPos(), false);
                        Lemming lemmUnderCursor = LemGame.lemmUnderCursor(LemCursor.getType());
                        offGfx.setClip(0, 0, width, levelHeight);
                        // draw explosions
                        LemGame.drawExplosions(offGfx, width, LemminiFrame.LEVEL_HEIGHT, xOfsTemp, yOfsTemp);
                        offGfx.setClip(0, 0, width, height);
                        //draw Visual SFX
                        LemGame.drawVisualSfx(offGfx);


                        // draw info string
                        LemmImage outStrImg = outStrBuffer.getImage();
                        GraphicsContext outStrGfx = outStrBuffer.getGraphicsContext();
                        outStrGfx.clearRect(0, 0, outStrImg.getWidth(), outStrImg.getHeight());
                        int statusBarGap = 8; //8 pixels of padding between the bottom of the level and the top of the status line.
                        if (LemGame.isOptionEnabled(LemGame.Option.ENHANCED_STATUS)) {
                            statusBarGap = 18;
                        }
                        int yOffset = LemminiFrame.LEVEL_HEIGHT + statusBarGap;

                        if (Core.player.isDebugMode() && showDebugCursorInfo) {
                            Stencil stencil = LemGame.getStencil();
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
                                int num = LemGame.getNumLemmsUnderCursor();
                                if (num > 1) {
                                	lemmingInfo += StringUtils.SPACE + num;
                                }
                            } else {
                            	lemmingInfo = StringUtils.EMPTY;
                            }
                            String strHome;
                            if (LemGame.isOptionEnabled(LemGame.Option.NO_PERCENTAGES)
                                    || LemGame.getNumLemmingsMax() > 100) {
                                strHome = Integer.toString(LemGame.getNumExited());
                            } else {
                                int max = LemGame.getNumLemmingsMax();
                                int home = LemGame.getNumExited() * 100 / max;
                                strHome = String.format("%02d%%", home);
                            }

                            // standard text-based status display
                            if (!LemGame.isOptionEnabled(LemGame.Option.ENHANCED_STATUS)) {
                                String status;
                                status = String.format("%-15s OUT %-4d IN %-4s TIME %s", lemmingInfo, LemGame.getNumLemmings(), strHome, LemGame.getTimeString());
                                //use the standard original "text-based" status bar
                                LemmFont.strImage(outStrGfx, status);
                                offGfx.drawImage(outStrImg, menuOffsetX + 4, yOffset);
                                
                            // enhanced icon-based status display
                            } else {
                                int hatchLems = LemGame.getNumLemmingsUnreleased(); // number of lems still in hatch
                                int maxLevelLemm = LemGame.getNumLemmingsMax(); // maximum number of lems provided from the start of the level
                                int active = LemGame.getNumLemmings(); // number of lems active in the level
                                int saveRequirement = LemGame.getNumToRescue(); // the level's save requirement
                                int exited = LemGame.getNumExited(); // number of lems that have exited
                                int maxPossibleLemm = LemGame.getNumLemmingsPossibleMax(); // maximum number of lems currently possible to save (including Blockers)
                                // show save requirement or maximum lems as the home sub-value depending on "use percentages" option
                                int homeSubValue = LemGame.isOptionEnabled(LemGame.Option.NO_PERCENTAGES) ? saveRequirement : maxLevelLemm;
                            	
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
                                int time = LemGame.getTime();
                                String timeString = LemGame.getTimeString();

                                if (LemGame.isTimed()) {
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

                            if (LemGame.isSuperLemming()) {
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
                        if (LemGame.isOptionEnabled(LemGame.Option.ENHANCED_STATUS) && LemGame.isOptionEnabled(LemGame.Option.SHOW_LEVEL_NAME)) {
                            String rating = LemGame.getCurLevelPack().getRatings().get(LemGame.getCurRating());
                            int levelNum = LemGame.getCurLevelNumber() + 1;
                            String levelName = rating + " " + levelNum + ": " + level.getLevelName().trim();
                            LemmImage lemmLevelName = LemmFont.strImage(levelName, LemmFont.LemmColor.GREEN);
                            offGfx.drawImage(lemmLevelName, menuOffsetX + 4 + debugModeOffset + maxExitOffset, LemminiFrame.LEVEL_HEIGHT + 2, 0.5);
                            lemmLevelName = null;
                        }
                        
                        // fall distance ruler
                        LemmImage ruler = MiscGfx.getImage(MiscGfx.Index.RULER);
                        if (ruler != null) {
                        	if (LemGame.drawRulerAtCursor == true) {
                        		int rx = LemCursor.getX() - ruler.getWidth() / 2;
                        		int ry = LemCursor.getY() - ruler.getHeight() / 2;
                        		int rWidth = ruler.getWidth();
                        		int fallDist = LemGame.getLevel().getMaxFallDistance();
                        		int rHeight = fallDist <= 0 ? ruler.getHeight() : fallDist;
                        		offGfx.drawImage(ruler, rx, ry, rWidth, rHeight);
                        	}
                        }

                        // replay icon
                        LemmImage replayImage = LemGame.getReplayImage();
                        if (replayImage != null) {
                            offGfx.drawImage(replayImage, width - 2 * replayImage.getWidth(), replayImage.getHeight());
                        }

                        // draw cursor
                        if (lemmUnderCursor != null) {
                            if (LemGame.isOptionEnabled(LemGame.Option.CLASSIC_CURSOR)) {
                                if (mouseHasEntered && !LemCursor.isBox()) {
                                    LemCursor.setBox(true);
                                    setCursor(LemCursor.getCursor());
                                }
                            } else {
                                int lx = lemmUnderCursor.footX() - xOfsTemp; //NOTE: footX() was .midX()
                                int ly = lemmUnderCursor.midY() - yOfsTemp;
                                LemmImage cursorImg = LemCursor.getBoxImage();
                                lx -= cursorImg.getWidth() / 2;
                                ly -= cursorImg.getHeight() / 2;
                                offGfx.drawImage(cursorImg, lx, ly);
                            }
                        } else {
                            if (LemGame.isOptionEnabled(LemGame.Option.CLASSIC_CURSOR)
                                    && LemCursor.isBox()) {
                                LemCursor.setBox(false);
                                setCursor(LemCursor.getCursor());
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
        LemGame.drawMinimapLemmings(offGfx, menuOffsetX + getSmallX(), getSmallY());
        offGfx.setClip(0, 0, width, height);
        Minimap.drawFrame(offGfx, menuOffsetX + getSmallX(), getSmallY());
        // draw minimap arrows
        if (minimapXOfsTemp > 0) {
            LemmImage leftArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_LEFT);
            offGfx.drawImage(leftArrow,
                    menuOffsetX + getSmallX() - BORDER_WIDTH - leftArrow.getWidth(),
                    getSmallY() + Minimap.getVisibleHeight() / 2 - leftArrow.getHeight() / 2);
        }
        if (minimapXOfsTemp < ToolBox.scale(LemGame.getWidth(), Minimap.getScaleX()) - Minimap.getVisibleWidth()) {
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
        if (yOfsTemp < LemGame.getHeight() - LemminiFrame.LEVEL_HEIGHT) {
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
        LemGame.drawMinimapLemmings(offGfx, menuOffsetX + getSmallX(), getSmallY());
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
        if (minimapXOfsTemp < ToolBox.scale(LemGame.getWidth(), Minimap.getScaleX()) - Minimap.getVisibleWidth()) {
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
        if (yOfsTemp < LemGame.getHeight() - LemminiFrame.LEVEL_HEIGHT) {
            LemmImage downArrow = MiscGfx.getImage(MiscGfx.Index.MINIMAP_ARROW_DOWN);
            offGfx.drawImage(downArrow,
                    menuOffsetX + getSmallX() + Minimap.getVisibleWidth() / 2 - downArrow.getWidth() / 2,
                    getSmallY() + Minimap.getVisibleHeight() + BORDER_WIDTH);
        }
    }

    private void updateFrame() {
        LemmImage fgImage = LemGame.getFgImage();
        switch (LemGame.getGameState()) {
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
                    LemGame.update();
                    // store local copy of xOfs to avoid sync problems with AWT threads
                    // (scrolling by dragging changes xOfs as well)
                    int xOfsTemp = LemGame.getXPos();
                    int minimapXOfsTemp = Minimap.getXPos();
                    int yOfsTemp = LemGame.getYPos();
                    // mouse movement
                    if (holdingMinimap) {
                        int framePos = ToolBox.scale(xOfsTemp, Minimap.getScaleX()) - minimapXOfsTemp;
                        if (xMouseScreen < Core.scale(menuOffsetX + getSmallX()) && framePos <= 0) {
                            xOfsTemp -= getStepSize();
                            LemGame.setXPos(xOfsTemp);
                        } else if (xMouseScreen >= Core.scale(menuOffsetX + getSmallX() + Minimap.getVisibleWidth()) && framePos >= Minimap.getVisibleWidth() - ToolBox.scale(Core.getDrawWidth(), Minimap.getScaleX())) {
                            xOfsTemp += getStepSize();
                            LemGame.setXPos(xOfsTemp);
                        } else {
                            xOfsTemp = Minimap.move(Core.unscale(xMouseScreen) - getSmallX() - menuOffsetX, Core.unscale(yMouse) - getSmallY());
                            LemGame.setXPos(xOfsTemp);
                        }
                        if (!LemGame.isVerticalLock()) {
                            if (yMouseScreen < Core.scale(getSmallY())) {
                                yOfsTemp -= getStepSize();
                                LemGame.setYPos(yOfsTemp);
                            } else if (yMouseScreen >= Core.scale(getSmallY() + Minimap.getVisibleHeight())) {
                                yOfsTemp += getStepSize();
                                LemGame.setYPos(yOfsTemp);
                            }
                        }
                    } else if (mouseHasEntered) {
                        if (xMouseScreen >= getWidth() - Core.scale(AUTOSCROLL_RANGE)) {
                            xOfsTemp += getStepSize();
                            int beforeXPos = LemGame.getXPos();
                            LemGame.setXPos(xOfsTemp);
                            int afterXPos = LemGame.getXPos();
                            xMouse += (afterXPos - beforeXPos);
                        } else if (xMouseScreen < Core.scale(AUTOSCROLL_RANGE)) {
                            xOfsTemp -= getStepSize();
                            int beforeXPos = LemGame.getXPos();
                            LemGame.setXPos(xOfsTemp);
                            int afterXPos = LemGame.getXPos();
                            xMouse -= (beforeXPos - afterXPos);
                        }
                        if (!LemGame.isVerticalLock()) {
                            if (yMouseScreen >= getHeight() - Core.scale(AUTOSCROLL_RANGE)) {
                                yOfsTemp += getStepSize();
                                int beforeYPos = LemGame.getYPos();
                                LemGame.setYPos(yOfsTemp);
                                int afterYPos = LemGame.getYPos();
                                yMouse += (afterYPos - beforeYPos);
                            } else if (yMouseScreen < Core.scale(AUTOSCROLL_RANGE)) {
                                yOfsTemp -= getStepSize();
                                int beforeYPos = LemGame.getYPos();
                                LemGame.setYPos(yOfsTemp);
                                int afterYPos = LemGame.getYPos();
                                yMouse -= (beforeYPos - afterYPos);
                            }
                        }
                    }
                    if (nudgeViewRightPressed && !nudgeViewLeftPressed) {
                        xOfsTemp += getStepSize();
                        LemGame.setXPos(xOfsTemp);
                    } else if (nudgeViewLeftPressed && !nudgeViewRightPressed) {
                        xOfsTemp -= getStepSize();
                        LemGame.setXPos(xOfsTemp);
                    }
                    if (!LemGame.isVerticalLock()) {
                        if (nudgeViewDownPressed && !nudgeViewUpPressed) {
                            yOfsTemp += getStepSize();
                            LemGame.setYPos(yOfsTemp);
                        } else if (nudgeViewUpPressed && !nudgeViewDownPressed) {
                            yOfsTemp -= getStepSize();
                            LemGame.setYPos(yOfsTemp);
                        }
                    }
                    Minimap.adjustXPos();

                    LemGame.updateLemmsUnderCursor();
                }
                break;
            default:
                break;
        }

        // fader
        LemGame.fade();
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
                    repaintTask, 0, LemGame.NANOSEC_PER_FRAME, TimeUnit.NANOSECONDS);
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
                    LemGame.State gameState = LemGame.getGameState();
                    // special handling for fast forward or super lemming mode only during real gameplay
                    if (gameState == LemGame.State.LEVEL) {
                        // in fast forward or super lemming modes, update the game mechanics
                        // multiple times per (drawn) frame
                        if (LemGame.isFastForward()) {
                        	int multiplier;
                        	if (LemGame.isTurbo()) {
                        	    multiplier = LemGame.TURBO_MULTI;
                        	} else if (LemGame.isOptionEnabled(LemGame.Option.FASTER_FAST_FORWARD)) {
                        	    multiplier = LemGame.FASTER_FAST_FWD_MULTI;
                        	} else {
                        	    multiplier = LemGame.FAST_FWD_MULTI;
                        	}
                            for (int f = 1; f < multiplier; f++) {
                                LemGame.update();
                            }
                        } else if (LemGame.isSuperLemming()) {
                            for (int f = 1; f < LemGame.SUPERLEMM_MULTI; f++) {
                                LemGame.update();
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
     */
    private void debugDraw(final int x, final int y, final boolean doDraw) {
        if (draw && Core.player.isDebugMode()) {
            boolean classicSteel = LemGame.getLevel().getClassicSteel();
            int rgbVal = (doDraw) ? 0xffffffff : 0x0;
            int minimapVal = (doDraw || Minimap.isTinted()) ? rgbVal : LemGame.getLevel().getBgColor().getRGB();
            if (doDraw && Minimap.isTinted()) {
                minimapVal = Minimap.tintColor(minimapVal);
            }
            int xOfs = LemGame.getXPos();
            int yOfs = LemGame.getYPos();
            LemmImage fgImage = LemGame.getFgImage();
            LemmImage fgImageSmall = Minimap.getImage();
            Stencil stencil = LemGame.getStencil();
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

                        if (xa + xOfs >= 0 && xa + xOfs < LemGame.getWidth()
                                && ya + yOfs >= 0 && ya + yOfs < LemGame.getHeight()) {
                            int[] objects = stencil.getIDs(xa + xOfs, ya + yOfs);
                            for (int obj : objects) {
                                SpriteObject spr = LemGame.getLevel().getSprObject(obj);
                                if (spr != null && spr.getVisOnTerrain()) {
                                    if (doDraw) {
                                        if ((LemGame.getLevel().getClassicSteel()
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
                            LemGame.getFgImage().setRGB(xa + xOfs, ya + yOfs, rgbVal);
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
        	LemGame.replayCaption = null;
            LemGame.requestChangeLevel(level[0], level[1], level[2], false);
            getParentFrame().setRestartEnabled(true);
        }
    }
    
    public void handleHotkeyConfig() {
        HotkeyConfig hc = new HotkeyConfig();
        hc.setVisible(true);
        LemGame.activeHotkeys = hc.getAllHotkeys(); 
    }
    
    public void handleMouseConfig() {
        MouseConfig mc = new MouseConfig(Core.getMouseInput());
        mc.setVisible(true);
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
                        LevelPack pack = LemGame.getLevelPack(packIndex);

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
                                for (int p = 0; p < LemGame.getLevelPackCount(); p++) {
                                    LevelPack nextPack = LemGame.getLevelPack(p);
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
            LemGame.requestChangeLevel(level[0], level[1], level[2], false);
            getParentFrame().setRestartEnabled(true);
        } catch (Exception e) {
        	handleChooseLevel();
        }
    }
    
    private boolean isValidLevel(int packIndex, int ratingIndex, int levelIndex) {
        if (packIndex < 0 || packIndex >= LemGame.getLevelPackCount())
            return false;

        if (ratingIndex < 0 || ratingIndex >= LemGame.getLevelPack(packIndex).getRatings().size())
            return false;

        if (levelIndex < 0 || levelIndex >= LemGame.getLevelPack(packIndex).getLevels(ratingIndex).size())
            return false;

        return true;
    }

    void startLevel() {
        LemGame.setTransition(LemGame.TransitionState.TO_LEVEL);
        Fader.setState(Fader.State.OUT);
        LemGame.resetGain();
    }

    void continueToNextLevel() {
        LemGame.nextLevel(); // continue to next level
        LemGame.requestChangeLevel(LemGame.getCurLevelPackIdx(), LemGame.getCurRating(),
                LemGame.getCurLevelNumber(), false);
    }
    
    public void findBestLevelToLoad() {
        if (LemGame.wasLost()) {
            LemGame.requestRestartLevel(false, true);
            return;
        }

        int packIndex = LemGame.getCurLevelPackIdx();
        LevelPack pack = LemGame.getLevelPack(packIndex);
        int rating = LemGame.getCurRating();
        int level = LemGame.getCurLevelNumber();

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
                LemGame.requestChangeLevel(packIndex, rating, level, false);
                return;
            }
        }
    }

    void exitToMenu() {
        LemGame.setTransition(LemGame.TransitionState.TO_INTRO);
        Fader.setState(Fader.State.OUT);
        Core.setWindowCaption("RetroLemmini");
    }
    
    public static String buildReplayFileName(String template, String user, String pack, String rating, String level, String time) {
		Map<String, String> tags = new HashMap<>();
		tags.put("{user}", user);
		tags.put("{pack}", pack);
		tags.put("{rating}", rating);
		tags.put("{level}", level);
		tags.put("{time}", time);
		
		List<String> values = new ArrayList<>();
		
		int index = 0;
		while (index < template.length()) {
			boolean found = false;
			for (String key : tags.keySet()) {
				if (template.startsWith(key, index)) {
					values.add(tags.get(key));
					index += key.length();
					found = true;
					break;
				}
			}
			if (!found)
				index++;
		}
		String name = String.join("__", values);
		return name + "." + Core.REPLAY_EXTENSIONS[0];
    }

    private void maybeAutoSaveReplay() {
        if (!LemGame.isOptionEnabled(LemGame.Option.AUTOSAVE_REPLAYS)) return;

        if (replaySaved) return;

        if (LemGame.getWasCheated() || LemGame.wasLost()) return;
        
        if (LemGame.cancelAutosave) return;

        Level level = LemGame.getLevel();
        LevelPack levelPack = LemGame.getCurLevelPack();
        int curRating = LemGame.getCurRating();
        int curLevelNum = LemGame.getCurLevelNumber();

        if (level == null || levelPack == null) return;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss__dd_MM_yyyy");
        String timestamp = now.format(formatter);

        String userName = Core.player.getName();
        String levelName = level.getLevelName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String levelWithNumber = String.format("%02d_%s", curLevelNum + 1, levelName);
        String levelPackName = levelPack.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String ratingName = levelPack.getRatings().get(curRating).replaceAll("[^a-zA-Z0-9_\\-]", "_");
        
        String template = LemGame.getReplayNameTemplate();

        String replayFileName = buildReplayFileName(template, userName, levelPackName, ratingName, levelWithNumber, timestamp);

        Path replayPath = Core.resourcePath.resolve(Core.REPLAYS_PATH).resolve(replayFileName);
        System.out.println("replayPath = " + replayPath);

        if (!LemGame.saveReplay(replayPath)) {
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
                if (LemGame.saveReplay(replayPath)) {
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
                    ReplayLevelInfo rli = LemGame.loadReplay(replayPath);
                    if (rli != null) {
                        int lpn = -1;
                        int rn = -1;
                        int ln = -1;
                        LevelPack lp = null;
                        for (int i = 0; i < LemGame.getLevelPackCount(); i++) {
                            LevelPack lpTemp = LemGame.getLevelPack(i);
                            if (ToolBox.looselyEquals(lpTemp.getName(), rli.getLevelPack()) ||
                                    // Handle replays created with the DMA Remastered packs
                                    checkForDMARemasters(rli.getLevelPack(), lpTemp.getName())) {
                                lpn = i;
                                lp = lpTemp;
                            }
                        }
                        
                        // check external levels if no match is found
                        if (lpn < 0) { 
                        	lp = LemGame.getLevelPack(0);
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
                        
                        // fallback - search by level name only
                        if (lpn < 0 || rn < 0 || ln < 0) {
                        	LemGame.replayCaption = "RetroLemmini - Closest match for '" + rli.getLvlName() + "' (" + rli.getLevelPack() + ")";
                            outer:
                            for (int p = 0; p < LemGame.getLevelPackCount(); p++) {
                                LevelPack pack = LemGame.getLevelPack(p);
                                List<String> ratings = pack.getRatings();

                                for (int r = 0; r < ratings.size(); r++) {
                                    List<String> levels = pack.getLevels(r);

                                    for (int l = 0; l < levels.size(); l++) {
                                        if (ToolBox.looselyEquals(levels.get(l), rli.getLvlName())) {
                                            lpn = p;
                                            rn = r;
                                            ln = l;
                                            break outer; // first match wins
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (lpn >= 0 && rn >= 0 && ln >= 0) {
                            // success
                            LemGame.requestChangeLevel(lpn, rn, ln, true);
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
            } catch (LemException ex) {
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
            LevelPack lpack = LemGame.getLevelPack(lvlPack);
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
                int[] l = LemGame.relLevelNum(lvlPack, codeInfo[0]);
                int rating = l[0];
                int lvlRel = l[1];
                if (rating >= 0 && lvlRel >= 0) {
                    Core.player.setAvailable(lpack.getName(), lpack.getRatings().get(rating), lvlRel);
                    LemGame.requestChangeLevel(lvlPack, rating, lvlRel, false);
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
                    && LemGame.getGameState() != LemGame.State.INTRO) {
                if (LemGame.getGameState() == LemGame.State.LEVEL) {
                    LemGame.setGameState(LemGame.State.LEVEL_END);
                }
                LemGame.setTransition(LemGame.TransitionState.TO_INTRO);
                Fader.setState(Fader.State.OUT);
                Core.setWindowCaption("RetroLemmini");
            }
            Core.player = new Player(player);
        }
    }

    void handleOptions() {
        // Store current settings
    	boolean oldDirectDrop = LemGame.isOptionEnabled(LemGame.Option.DIRECT_DROP);
    	boolean oldMinimapOption = LemGame.isOptionEnabled(LemGame.Option.FULL_COLOR_MINIMAP);
        boolean oldMenuBarVisOption = LemGame.isOptionEnabled(LemGame.Option.SHOW_MENU_BAR);
        boolean oldScrollerOption = LemGame.isOptionEnabled(LemGame.Option.CLASSIC_SCROLLER);
        LemGame.MenuThemeOption oldMenuThemeOption = LemGame.getMenuThemeOption();

        // Show options dialog
        OptionsDialog d = new OptionsDialog(this, getParentFrame(), true);
        d.setVisible(true);

        // Update UI if options have changed
        if (oldMinimapOption != LemGame.isOptionEnabled(LemGame.Option.FULL_COLOR_MINIMAP)) {
        	if (LemGame.getGameState() == LemGame.State.LEVEL) {
        		Minimap.init(1.0 / 16.0, 1.0 / 8.0, oldMinimapOption);
        	}
        }
        if (oldMenuBarVisOption != LemGame.isOptionEnabled(LemGame.Option.SHOW_MENU_BAR)) {
            getParentFrame().toggleMenuBarVisibility();
        }
        if (oldScrollerOption != LemGame.isOptionEnabled(LemGame.Option.CLASSIC_SCROLLER)) {
            TextScreen.toggleScrollerType();
        }
        if (oldMenuThemeOption != LemGame.getMenuThemeOption()) {
            TextScreen.setMenuTheme();
        }
        
        // handle direct drop change if mid-level
        if (LemGame.getGameState() == LemGame.State.LEVEL) {
        	boolean isDirectDrop = LemGame.isOptionEnabled(LemGame.Option.DIRECT_DROP);
            if (oldDirectDrop != isDirectDrop) {
            	LemGame.requestRestartLevel(true, false); // restart level to apply change (ensures replay stability)
            }
            LemGame.setDirectDrop(isDirectDrop);
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
        LemGame.setXPos(LemGame.getXPos());
        LemGame.setYPos(LemGame.getYPos());
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
     */
    int getCursorX() {
        return xMouse;
    }

    /**
     * Get cursor y position in pixels.
     */
    int getCursorY() {
        return yMouse;
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
     */
    boolean getDebugDraw() {
        return draw;
    }

    /**
     * Set state of debug draw option.
     */
    void setDebugDraw(final boolean d) {
        draw = d;
    }

    /**
     * Get state of debug cursor info visiblity.
     */
    boolean debugCursorInfoVisible() {
        return showDebugCursorInfo;
    }

    /**
     * Set state of debug cursor info visiblity.
     */
    void setDebugCursorInfo(final boolean i) {
        showDebugCursorInfo = i;
    }

    private int getStepSize() {
        return (LemGame.isShiftPressed() ? X_STEP_FAST : X_STEP);
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
