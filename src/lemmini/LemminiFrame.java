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

import java.awt.Desktop;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import keyrepeatfix.RepeatingReleasedEventsFixer;
import lemmini.game.Core;
import lemmini.game.GameController;
import lemmini.game.Icons;
import lemmini.game.LemmCursor;
import lemmini.game.LemmException;
import lemmini.game.Lemming;
import lemmini.game.LevelPack;
import lemmini.game.ResourceException;
import lemmini.game.Vsfx;
import lemmini.gameutil.Fader;
import lemmini.gameutil.RetroLemminiHotkeys;
import lemmini.graphics.LemmImage;
import lemmini.sound.Music;
import lemmini.tools.EditorTestMode;
import lemmini.tools.ToolBox;

/**
 * Lemmini - a game engine for Lemmings.<br>
 * This is the main window including input handling. The game logic is located in
 * {@link GameController}, some core components are in {@link Core}.
 *
 * @author Volker Oth
 */
public class LemminiFrame extends JFrame {

    public static final int LEVEL_HEIGHT = 320;
    private static final long serialVersionUID = 0x01L;

    private int unmaximizedPosX;
    private int unmaximizedPosY;
    
    private double userMusicVolume;
    private double userSoundVolume;

    /** self reference */
    static LemminiFrame thisFrame;

    /**
     * Creates new form LemminiFrame
     */
    public LemminiFrame() {
        try {
            //found at: https://stackoverflow.com/questions/2837263/how-do-i-get-the-directory-that-the-currently-executing-jar-file-is-in
            String currentFolderStr = URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
            System.out.println("Current directory: " + currentFolderStr);
            boolean successful = Core.init(currentFolderStr); // initialize Core object
            if (!successful) {
                System.exit(0);
            }
        } catch (LemmException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Throwable ex) {
            ToolBox.showException(ex);
            System.exit(1);
        }

        initComponents();
        setMinimumSize(getSize());
        RepeatingReleasedEventsFixer.install();
    }

    private static void consoleInit() {
        //logo curtesy of: https://www.coolgenerator.com/ascii-text-generator
        String logo =
                " ________  _______  _________  ________  ________  ___       _______   _____ ______   _____ ______   ___  ________   ___     \n"
              + "|\\   __  \\|\\  ___ \\|\\___   ___\\\\   __  \\|\\   __  \\|\\  \\     |\\  ___ \\ |\\   _ \\  _   \\|\\   _ \\  _   \\|\\  \\|\\   ___  \\|\\  \\    \n"
              + "\\ \\  \\|\\  \\ \\   __/\\|___ \\  \\_\\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\    \\ \\   __/|\\ \\  \\\\\\__\\ \\  \\ \\  \\\\\\__\\ \\  \\ \\  \\ \\  \\\\ \\  \\ \\  \\   \n"
              + " \\ \\   _  _\\ \\  \\_|/__  \\ \\  \\ \\ \\   _  _\\ \\  \\\\\\  \\ \\  \\    \\ \\  \\_|/_\\ \\  \\\\|__| \\  \\ \\  \\\\|__| \\  \\ \\  \\ \\  \\\\ \\  \\ \\  \\  \n"
              + "  \\ \\  \\\\  \\\\ \\  \\_|\\ \\  \\ \\  \\ \\ \\  \\\\  \\\\ \\  \\\\\\  \\ \\  \\____\\ \\  \\_|\\ \\ \\  \\    \\ \\  \\ \\  \\    \\ \\  \\ \\  \\ \\  \\\\ \\  \\ \\  \\ \n"
              + "   \\ \\__\\\\ _\\\\ \\_______\\  \\ \\__\\ \\ \\__\\\\ _\\\\ \\_______\\ \\_______\\ \\_______\\ \\__\\    \\ \\__\\ \\__\\    \\ \\__\\ \\__\\ \\__\\\\ \\__\\ \\__\\\n"
              + "    \\|__|\\|__|\\|_______|   \\|__|  \\|__|\\|__|\\|_______|\\|_______|\\|_______|\\|__|     \\|__|\\|__|     \\|__|\\|__|\\|__| \\|__|\\|__|\n";
        System.out.println(logo);
        System.out.println("===================================================================================================================");
        System.out.println("      Version " + Core.REVISION + "      Commit " + getGitCommitSHA(7) + "      Date: " + Core.REV_DATE);
        System.out.println("===================================================================================================================");
        System.out.println("");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy MMMM d  HH:mm:ss");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        System.out.println(dtf.format(now) + "\nloading RetroLemmini..." );
        System.out.println("    Java version: " + System.getProperty("java.version").toString());
        System.out.println("    OS Name: " + System.getProperty("os.name"));
        System.out.println("    OS Version: " + System.getProperty("os.version"));
        System.out.println("");
         };

    void init() {
        System.out.println("\ninitializing LemminiFrame...");
        try {
            // initialize the game controller and main panel
            GameController.init();

         // BOOKMARK TODO: This correctly sets the fullscreen bool
//            isFullScreen = GameController.isOptionEnabled(GameController.RetroLemminiOption.FULL_SCREEN);

            lemminiPanelMain.init();
            lemminiPanelMain.setCursor(LemmCursor.getCursor());

            toggleMenuBarVisibility();

            int w = Math.max(lemminiPanelMain.getWidth(), Core.programProps.getInt("frameWidth", lemminiPanelMain.getWidth()));
            int h = Math.max(lemminiPanelMain.getHeight(), Core.programProps.getInt("frameHeight", lemminiPanelMain.getHeight()));
            lemminiPanelMain.setSize(w, h);
            lemminiPanelMain.setPreferredSize(lemminiPanelMain.getSize()); // needed for pack() to keep this size

            pack();

            setLocationRelativeTo(null);
            int posX = Core.programProps.getInt("framePosX", getX());
            int posY = Core.programProps.getInt("framePosY", getY());
            setLocation(posX, posY);

            // load the maximized state
            int maximizedState = 0;
            if (Core.programProps.getBoolean("maximizedHoriz", false)) {
                maximizedState |= MAXIMIZED_HORIZ;
            }
            if (Core.programProps.getBoolean("maximizedVert", false)) {
                maximizedState |= MAXIMIZED_VERT;
            }
            setExtendedState(getExtendedState() | maximizedState);
            
            if (EditorTestMode.enabled) {
                try {
                    int levelIndex = GameController.addExternalTestLevel(
                        Paths.get(EditorTestMode.testLevelPath)
                    );
                    GameController.requestChangeLevel(
                        0,          // pack
                        0,          // rating
                        levelIndex, // newly added level
                        false
                    );
                    GameController.setGameState(GameController.State.INTRO);
                    GameController.setTransition(GameController.TransitionState.LOAD_LEVEL);
                    Fader.setState(Fader.State.OUT);
                } catch (Exception ex) {
                    ToolBox.showException(ex);
                    System.exit(1);
                }
            } else {
                GameController.setGameState(GameController.State.INTRO);
                GameController.setTransition(GameController.TransitionState.NONE);
                Fader.setState(Fader.State.IN);
            }

            Thread t = new Thread(lemminiPanelMain);
            t.start();

            setVisible(true);
        } catch (ResourceException ex) {
            Core.resourceError(ex.getMessage());
            return;
        } catch (Throwable ex) {
            ToolBox.showException(ex);
            System.exit(1);
        }
        System.out.println("LemminiFrame initialization complete.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lemminiPanelMain = new lemmini.LemminiPanel();
        jMenuBarMain = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuPlayers = new javax.swing.JMenu();
        jMenuItemManagePlayers = new javax.swing.JMenuItem();
        jMenuLevel = new javax.swing.JMenu();
        jMenuItemChooseLevel = new javax.swing.JMenuItem();
        jMenuItemRestartLevel = new javax.swing.JMenuItem();
        jMenuItemLoadReplay = new javax.swing.JMenuItem();
        jMenuItemEnterLevelCode = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemOptions = new javax.swing.JMenuItem();
        jMenuItemHotkeys = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("RetroLemmini");
        setIconImage(Toolkit.getDefaultToolkit().getImage(LemminiFrame.class.getClassLoader().getResource("icon_256.png")));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout lemminiPanelMainLayout = new javax.swing.GroupLayout(lemminiPanelMain);
        lemminiPanelMain.setLayout(lemminiPanelMainLayout);
        lemminiPanelMainLayout.setHorizontalGroup(
            lemminiPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        lemminiPanelMainLayout.setVerticalGroup(
            lemminiPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );

        jMenuFile.setText("File");

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBarMain.add(jMenuFile);

        jMenuPlayers.setText("Players");

        jMenuItemManagePlayers.setText("Manage Players...");
        jMenuItemManagePlayers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemManagePlayersActionPerformed(evt);
            }
        });
        jMenuPlayers.add(jMenuItemManagePlayers);

        jMenuBarMain.add(jMenuPlayers);

        jMenuLevel.setText("Level");
        
        jMenuItemChooseLevel.setText("Choose Level...");
        jMenuItemChooseLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemChooseLevelActionPerformed(evt);
            }
        });
        jMenuLevel.add(jMenuItemChooseLevel);

        jMenuItemRestartLevel.setText("Restart Level");
        jMenuItemRestartLevel.setEnabled(false);
        jMenuItemRestartLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRestartLevelActionPerformed(evt);
            }
        });
        jMenuLevel.add(jMenuItemRestartLevel);

        jMenuItemLoadReplay.setText("Load Replay...");
        jMenuItemLoadReplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadReplayActionPerformed(evt);
            }
        });
        jMenuLevel.add(jMenuItemLoadReplay);

        jMenuItemEnterLevelCode.setText("Enter Level Code...");
        jMenuItemEnterLevelCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemEnterLevelCodeActionPerformed(evt);
            }
        });
        jMenuLevel.add(jMenuItemEnterLevelCode);

        jMenuBarMain.add(jMenuLevel);

        jMenuOptions.setText("Options");

        jMenuItemOptions.setText("Options...");
        jMenuItemOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOptionsActionPerformed(evt);
            }
        });

        jMenuItemHotkeys.setText("Hotkeys...");
        jMenuItemHotkeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	lemminiPanelMain.handleHotkeyConfig();
            }
        });

        jMenuItemAbout.setText("About...");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleAbout();
            }
        });

        jMenuOptions.add(jMenuItemOptions);
        jMenuOptions.add(jMenuItemHotkeys);
        jMenuOptions.add(jMenuItemAbout);

        jMenuBarMain.add(jMenuOptions);

        setJMenuBar(jMenuBarMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(lemminiPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lemminiPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    void handleAbout() {
        String urlLemmini = "http://lemmini.de";
        String urlForumBoard = "https://www.lemmingsforums.net/index.php?board=10.0";
        String urlRetroLemmini = "https://www.lemmingsforums.net/index.php?topic=7030.0";

        // Create a JEditorPane with HTML content
        JEditorPane editorPane = new JEditorPane("text/html",
                "<html>"
                + "RetroLemmini Version " + Core.REVISION + "<br>"
                + "By William James<br><br>"
                + "Based on<br><br>"
                + "SuperLemminiToo by Charles Irwin<br>"
                + "SuperLemmini by Ryan Sakowski<br>"
                + "Original Lemmini by Volker Oth<br><br>"
                + "Get the latest version of RetroLemmini here: <a href='" + urlRetroLemmini + "'>" + "RetroLemmini on LemmingsForums.net" + "</a><br>"
                + "Join the Forum discussion here: <a href='" + urlForumBoard + "'>" + "Discussion board on LemmingsForums.net" + "</a><br>"
                + "Lemmini website: <a href='" + urlLemmini + "'>" + urlLemmini + "</a><br><br>"
                + "Revision Commit ID: " + getGitCommitSHA(7) + "</a><br>"
                + "Java Version: " + System.getProperty("java.version")
                + "</html>");

        editorPane.setEditable(false);
        editorPane.setBackground(new JLabel().getBackground()); // Match the background color

        // Add a HyperlinkListener to detect clicks
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JOptionPane.showConfirmDialog(
                thisFrame,
                new JScrollPane(editorPane),
                "About",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
    }
    
    public static String getGitCommitSHA(Integer length) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(".git/HEAD"));
            if (lines.isEmpty()) {
                return "Unknown Commit SHA";
            }
            String headRef = lines.get(0).trim();
            String commitSHA;
            
            if (headRef.startsWith("ref: ")) {
                String refPath = ".git/" + headRef.substring(5);
                List<String> refLines = Files.readAllLines(Paths.get(refPath));
                commitSHA = refLines.isEmpty() ? Core.COMMIT_ID : refLines.get(0).trim();
            } else {
                commitSHA = headRef;
            }
            // If length is null, < 0 or > 40, return full SHA
            if (length == null || length <= 0 || length >= 40) {
                return commitSHA;
            }
            // Otherwise, trim SHA to requested length
            return commitSHA.length() > length ? commitSHA.substring(0, length) : commitSHA;
        } catch (IOException e) {
            e.printStackTrace();
            return Core.COMMIT_ID;
        }
    }

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        storeUnmaximizedPos();
    }//GEN-LAST:event_formComponentMoved
    private void printLevelNameToConsole() {
        System.out.println(GameController.getLevelPack(GameController.getCurLevelPackIdx()).getInfo(GameController.getCurRating(),
                                                       GameController.getCurLevelNumber()).getLevelResource());
    }
    
    private void addLemmingAtCursor() {
        Lemming l = new Lemming(lemminiPanelMain.getCursorX(), lemminiPanelMain.getCursorY(), Lemming.Direction.RIGHT);
        GameController.addLemming(l);
        Vsfx v = new Vsfx(lemminiPanelMain.getCursorX(), lemminiPanelMain.getCursorY(), Vsfx.Vsfx_Index.YIPPEE);
        GameController.addVsfx(v);
    }

    private void formKeyPressed(java.awt.event.KeyEvent evt) { //GEN-FIRST:event_formKeyPressed
        int code = evt.getKeyCode();
        RetroLemminiHotkeys.HotkeyAction action = RetroLemminiHotkeys.getHotkeyActionForEvent(evt, GameController.activeHotkeys);

        boolean isIntro = (GameController.getGameState() == GameController.State.INTRO);
        boolean isPreview = (GameController.getGameState() == GameController.State.BRIEFING);
        boolean isLevel = (GameController.getGameState() == GameController.State.LEVEL);
        boolean isLevelEnd = (GameController.getGameState() == GameController.State.LEVEL_END);
        boolean isPostview = (GameController.getGameState() == GameController.State.DEBRIEFING);
        
        // Hard-coded keys
        // --- In-game modifier handling --- //
        if (isLevel) {
            switch (code) {
                case KeyEvent.VK_SHIFT:
                    lemminiPanelMain.setShiftPressed(true);
                    break;
                case KeyEvent.VK_CONTROL:
                    lemminiPanelMain.setControlPressed(true);
                    break;
                case KeyEvent.VK_ALT:
                    lemminiPanelMain.setAltPressed(true);
                    break;
                case KeyEvent.VK_ESCAPE:
                    GameController.endLevel();
                    break;
                default:
                    break;
            }
        }
        
        // --- Available on main menu --- //
        if (isIntro) {
            switch (code) {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    lemminiPanelMain.loadDefaultLevel();
                    break;
                case KeyEvent.VK_ESCAPE:
                    exit();
                    break;
                default:
                    break;
            }
        }
        
        // --- Available on preview screen --- //
        if (isPreview) {
            switch (code) {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    lemminiPanelMain.startLevel();
                    break;
                case KeyEvent.VK_ESCAPE:
                    lemminiPanelMain.exitToMenu();
                    break;
                case KeyEvent.VK_RIGHT:
                    goToNextAvailableLevel();
                    break;
                case KeyEvent.VK_LEFT:
                    goToPreviousAvailableLevel();
                    break;
                case KeyEvent.VK_UP:
                    goToNextAvailableGroup();
                    break;
                case KeyEvent.VK_DOWN:
                    goToPreviousAvailableGroup();
                    break;
                default:
                    break;
            }
        }
        
        // --- Available on postview or level end --- //
        if (isPostview || isLevelEnd) {
            switch (code) {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    lemminiPanelMain.findBestLevelToLoad();
                    break;
                case KeyEvent.VK_ESCAPE:
                    lemminiPanelMain.exitToMenu();
                    break;
                default:
                    break;
            }
        }
        
        // Custom hotkeys
        if (action == null)
        	return;
        
        // --- Available anywhere --- //
        switch (action) {
	        case HotkeyLoadReplay:
	            lemminiPanelMain.handleLoadReplay();
	            break;
            case HotkeyToggleMenuBar:
                GameController.setOption(GameController.RetroLemminiOption.SHOW_MENU_BAR, !GameController.isOptionEnabled(GameController.RetroLemminiOption.SHOW_MENU_BAR));
                toggleMenuBarVisibility();
                Core.saveSettings();
                break;
            case HotkeyManagePlayers:
                lemminiPanelMain.handlePlayers();
                break;
            case HotkeyLevelSelect:
                lemminiPanelMain.handleChooseLevel();
                break;
            case HotkeyEnterCode:
                lemminiPanelMain.handleEnterCode();
                break;
            case HotkeyOpenSettings:
                lemminiPanelMain.handleOptions();
                break;
            case HotkeyManageHotkeys:
                lemminiPanelMain.handleHotkeyConfig();
                break;
            case HotkeyAbout:
                handleAbout();
                break;
            case HotkeyCloseApp:
                exit();
                break;
            default:
            	break;
        	}
        
        // --- Available on preview screen --- //
        if (isPreview) {
        	switch (action) {
	            case HotkeySaveAsImage:
	            	saveLevelAsImageFromPreview();
                	break;
                case HotkeyNextLevel:
                    goToNextAvailableLevel();
                    break;
                case HotkeyPreviousLevel:
                    goToPreviousAvailableLevel();
                    break;
                case HotkeyNextGroup:
                    goToNextAvailableGroup();
                    break;
                case HotkeyPreviousGroup:
                    goToPreviousAvailableGroup();
                    break;
                default:
                	break;
        	}
        }
        
        // --- Available on postview or level end --- //
        if (isPostview || isLevelEnd) {
            switch (action) {
	            case HotkeySaveReplay:
	                lemminiPanelMain.handleSaveReplay();
	                break;
                default:
                	break;
            }
        }

        // --- Available in-game --- //
        if (isLevel) {
            switch (action) {
                case HotkeyToggleMusic:
                    toggleMusic();
                    break;
                case HotkeyToggleSound:
                    toggleSound();
                    break;
                case HotkeyPause:
                    GameController.togglePause();
                    break;
                case HotkeyRestart:
                    GameController.requestRestartLevel(true, false);
                    break;
                case HotkeyNuke:
                    GameController.handleIconButton(Icons.IconType.NUKE);
                    break;
                case HotkeyDecreaseRR:
                    GameController.pressMinus(GameController.KEYREPEAT_KEY);
                    break;
                case HotkeyIncreaseRR:
                    GameController.pressPlus(GameController.KEYREPEAT_KEY);
                    break;
                case HotkeySelectClimber:
                    GameController.handleIconButton(Icons.IconType.CLIMB);
                    break;
                case HotkeySelectFloater:
                    GameController.handleIconButton(Icons.IconType.FLOAT);
                    break;
                case HotkeySelectBomber:
                    GameController.handleIconButton(Icons.IconType.BOMB);
                    break;
                case HotkeySelectBlocker:
                    GameController.handleIconButton(Icons.IconType.BLOCK);
                    break;
                case HotkeySelectBuilder:
                    GameController.handleIconButton(Icons.IconType.BUILD);
                    break;
                case HotkeySelectBasher:
                    GameController.handleIconButton(Icons.IconType.BASH);
                    break;
                case HotkeySelectMiner:
                    GameController.handleIconButton(Icons.IconType.MINE);
                    break;
                case HotkeySelectDigger:
                    GameController.handleIconButton(Icons.IconType.DIG);
                    break;
                case HotkeyToggleVerticalLock:
                    GameController.setVerticalLock(!GameController.isVerticalLock());
                    GameController.pressIcon(Icons.IconType.VLOCK);
                    break;
                case HotkeyFastForward:
                    GameController.setTurbo(false);
                    GameController.setFastForward(!GameController.isFastForward());
                    GameController.pressIcon(Icons.IconType.FFWD);
                    break;
                case HotkeyTurboForward:
                    GameController.setTurbo(true);
                    GameController.setFastForward(!GameController.isFastForward());
                    GameController.pressIcon(Icons.IconType.FFWD);
                    break;
                case HotkeySelectLeft:
                    if (GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT)) {
                        if (LemmCursor.getType().isWalkerOnly()) {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER_LEFT);
                        } else {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.LEFT);
                        }
                    }
                    break;
                case HotkeySelectRight:
                    if (GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT)) {
                        if (LemmCursor.getType().isWalkerOnly()) {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER_RIGHT);
                        } else {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.RIGHT);
                        }
                    }
                    break;
                case HotkeySelectWalker:
                    if (GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT)) {
                        switch (LemmCursor.getType()) {
                            case NORMAL:
                                lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER);
                                break;
                            case LEFT:
                                lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER_LEFT);
                                break;
                            case RIGHT:
                                lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER_RIGHT);
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case HotkeyNudgeViewLeft:
                    lemminiPanelMain.setNudgeViewLeftPressed(true);
                    break;
                case HotkeyNudgeViewRight:
                    lemminiPanelMain.setNudgeViewRightPressed(true);
                    break;
                case HotkeyNudgeViewUp:
                    lemminiPanelMain.setNudgeViewUpPressed(true);
                    break;
                case HotkeyNudgeViewDown:
                    lemminiPanelMain.setNudgeViewDownPressed(true);
                    break;
                case HotkeyEndLevel:
                    GameController.endLevel();
                    break;
                case HotkeySaveReplay:
                    lemminiPanelMain.handleSaveReplay();
                    break;
                case HotkeyCancelReplay:
                    GameController.stopReplayMode();
                    break;
                case HotkeySaveAsImage:
                    saveLevelAsImage();
                    break;
                case HotkeyDebugToggleDebug:
                    Core.player.setDebugMode(!Core.player.isDebugMode());
                    GameController.setWasCheated(true); // 'Cheated' flag remains true if debug mode is entered
                    break;
                case HotkeyDebugMaxExitPhysics:
                    Core.player.setMaximumExitPhysics(!Core.player.isMaximumExitPhysics());
                    GameController.setWasCheated(true); // 'Cheated' flag remains true if maximum exit physics mode is entered
                    break;
                default:
                	break;
            }
        }
        
        // --- Available in-game and with debug mode enabled --- //
        if (isLevel && Core.player.isDebugMode()) {
            switch (action) {
                case HotkeyDebugSaveAll:
                    GameController.setNumExited(GameController.getNumLemmingsMax());
                    GameController.endLevel();
                    break;
                case HotkeyDebugInvertTimer:
                    GameController.setTimed(!GameController.isTimed());
                    break;
                case HotkeyDebugToggleSuperLemming:
                    GameController.setSuperLemming(!GameController.isSuperLemming());
                    break;
                case HotkeyDebugToggleDrawMode:
                    lemminiPanelMain.setDebugDraw(!lemminiPanelMain.getDebugDraw());
                    break;
                case HotkeyDebugDrawBrushSizeUp:
                    lemminiPanelMain.increaseDrawBrushSize();
                    break;
                case HotkeyDebugDrawBrushSizeDown:
                    lemminiPanelMain.decreaseDrawBrushSize();
                    break;
                case HotkeyDebugPrintLevelName:
                    printLevelNameToConsole();
                    break;
                case HotkeyDebugToggleCursorInfo:
                    lemminiPanelMain.setDebugCursorInfo(!lemminiPanelMain.debugCursorInfoVisible());
                    break;
                case HotkeyDebugAddLemAtCursor:
                	addLemmingAtCursor();
                    break;
                default:
                	break;
            }
        }
        evt.consume();
    }//GEN-LAST:event_formKeyPressed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        int code = evt.getKeyCode();
        boolean isLevel = GameController.getGameState() == GameController.State.LEVEL;
        
        // Hard-coded keys
        if (isLevel) {
            switch (code) {
                case KeyEvent.VK_SHIFT:
                    lemminiPanelMain.setShiftPressed(false);
                    break;
                case KeyEvent.VK_CONTROL:
                    lemminiPanelMain.setControlPressed(false);
                    break;
                case KeyEvent.VK_ALT:
                    lemminiPanelMain.setAltPressed(false);
                    break;
                default:
                    break;
            }
        }
        
        RetroLemminiHotkeys.HotkeyAction action = 
                RetroLemminiHotkeys.getHotkeyActionForEvent(evt, GameController.activeHotkeys);
        
        // Custom hotkeys
        if ((action != null) && isLevel) {
            switch (action) {
	            case HotkeyIncreaseRR:
	                GameController.releasePlus(GameController.KEYREPEAT_KEY);
	                break;
	            case HotkeyDecreaseRR:
	                GameController.releaseMinus(GameController.KEYREPEAT_KEY);
	                break;
                case HotkeyNuke:
                    GameController.releaseIcon(Icons.IconType.NUKE);
                    break;
                case HotkeySelectLeft:
                	if (GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT)) {
                        if (LemmCursor.getType() == LemmCursor.CursorType.LEFT) {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.NORMAL);
                        } else if (LemmCursor.getType() == LemmCursor.CursorType.WALKER_LEFT) {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER);
                        }
                    }
                	break;
                case HotkeySelectRight:
                    if (GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT)) {
                        if (LemmCursor.getType() == LemmCursor.CursorType.RIGHT) {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.NORMAL);
                        } else if (LemmCursor.getType() == LemmCursor.CursorType.WALKER_RIGHT) {
                            lemminiPanelMain.setCursor(LemmCursor.CursorType.WALKER);
                        }
                    }  
                    break;
                case HotkeySelectWalker:
                    if (GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT)) {
                        switch (LemmCursor.getType()) {
                            case WALKER:
                                lemminiPanelMain.setCursor(LemmCursor.CursorType.NORMAL);
                                break;
                            case WALKER_LEFT:
                                lemminiPanelMain.setCursor(LemmCursor.CursorType.LEFT);
                                break;
                            case WALKER_RIGHT:
                                lemminiPanelMain.setCursor(LemmCursor.CursorType.RIGHT);
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case HotkeyNudgeViewLeft:
                	lemminiPanelMain.setNudgeViewLeftPressed(false);
                	break;
                case HotkeyNudgeViewRight:
                	lemminiPanelMain.setNudgeViewRightPressed(false);
                	break;
                case HotkeyNudgeViewUp:
                	lemminiPanelMain.setNudgeViewUpPressed(false);
                	break;
                case HotkeyNudgeViewDown:
                	lemminiPanelMain.setNudgeViewDownPressed(false);
                	break;
                default:
                	break;
            }
        }
    }//GEN-LAST:event_formKeyReleased

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        exit();
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        exit();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        lemminiPanelMain.focusGained();
    }//GEN-LAST:event_formWindowGainedFocus

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        lemminiPanelMain.focusLost();
    }//GEN-LAST:event_formWindowLostFocus

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        exit();
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemManagePlayersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemManagePlayersActionPerformed
        lemminiPanelMain.handlePlayers();
    }//GEN-LAST:event_jMenuItemManagePlayersActionPerformed
    
    private void jMenuItemChooseLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemChooseLevelActionPerformed
        lemminiPanelMain.handleChooseLevel();
    }//GEN-LAST:event_jMenuItemChooseLevelActionPerformed

    private void jMenuItemRestartLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRestartLevelActionPerformed
        if (GameController.getLevel() == null) {
            GameController.requestChangeLevel(GameController.getCurLevelPackIdx(), GameController.getCurRating(), GameController.getCurLevelNumber(), false);
        } else {
            GameController.requestRestartLevel(false, true);
        }
    }//GEN-LAST:event_jMenuItemRestartLevelActionPerformed

    private void jMenuItemLoadReplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadReplayActionPerformed
        lemminiPanelMain.handleLoadReplay();
    }//GEN-LAST:event_jMenuItemLoadReplayActionPerformed

    private void jMenuItemEnterLevelCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEnterLevelCodeActionPerformed
        lemminiPanelMain.handleEnterCode();
    }//GEN-LAST:event_jMenuItemEnterLevelCodeActionPerformed

    private void jMenuItemOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOptionsActionPerformed
        lemminiPanelMain.handleOptions();
    }//GEN-LAST:event_jMenuItemOptionsActionPerformed

    /**
     * The main function. Entry point of the program.
     * @param args the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	// get the current commit ID
	    try {
	        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "getcommitID.bat");
	        pb.start().waitFor();
	    } catch (IOException | InterruptedException e) {
	        System.err.println("Failed to run getcommitID.bat: " + e.getMessage());
	        e.printStackTrace();
	    }
    	
        // write opening console log
        consoleInit();

        /*
         * Check JVM version
         */
        if (!SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_8)) {
            System.out.println("JVM >= 1.8 [FAIL]");
            JOptionPane.showMessageDialog(null, "RetroLemmini requires JVM 1.8 or later.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } else {
            System.out.println("JVM >= 1.8 [PASS]");
        }

        // check free memory
        long free = Runtime.getRuntime().maxMemory();
        long memReq = 96 * 1024 * 1024;
        if (free < memReq) {
            System.out.println("memory check: " + (int)(free / (1024*1024)) + "MB >= " + (int)(memReq / (1024*1024)) + "MB [FAIL]");
            JOptionPane.showMessageDialog(null, "You need at least 96MB of heap.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } else {
            System.out.println("memory check: " + (int)(free / (1024*1024)) + "MB >= " + (int)(memReq / (1024*1024)) + "MB [PASS]");
        }

        Path level = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase(Locale.ROOT)) {
                case "test":
                    if (i + 1 < args.length) {
                        EditorTestMode.enabled = true;
                        EditorTestMode.testLevelPath = args[++i];
                        System.out.println("editor test mode enabled: " + EditorTestMode.testLevelPath);
                    } else {
                        System.err.println("test argument supplied but no level path given");
                    }
                    break;
                case "-l":
                    i++;
                    if (i < args.length) {
                        level = Paths.get(args[i]);
                        System.out.println("argument detected: -L, but no level filename supplied.");
                    } else {
                        System.out.println("argument detected: -L " + level.toString());
                    }
                    break;
                default:
                    break;
            }
        }

        System.out.println("applying system \"Look and Feel\" and system specific settings...");
        /*
         * Set "Look and Feel" to system default
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            /* don't care */
        }

        /*
         * Apple menu bar for MacOS
         */
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // workaround to adjust time base to 1ms under Windows
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6435126
        new Thread() {
            {
                this.setDaemon(true);
                this.start();
            }
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch(InterruptedException ex) {
                    }
                }
            }
        };

        /* Create and display the form */
        System.out.println("\ncreating LemminiFrame...");
        thisFrame = new LemminiFrame();
        thisFrame.init();

        if (!EditorTestMode.enabled && level != null) {
            System.out.println("external level loaded. starting up inside level...");
            int[] levelPosition = GameController.addExternalLevel(level, null, true);
            GameController.requestChangeLevel(levelPosition[0], levelPosition[1], levelPosition[2], false);
        }
    }
    
    private void goToNextAvailableLevel() {
        if (Fader.getState() == Fader.State.OFF) {
            LevelPack pack = GameController.getCurLevelPack();
            String packName = pack.getName();
            int packIdx = GameController.getCurLevelPackIdx();
            List<String> ratings = pack.getRatings();
            int rating = GameController.getCurRating();
            int lvlCount = pack.getLevelCount(rating);
            int lvlNum = GameController.getCurLevelNumber() + 1;
            while (rating < ratings.size()) {
                while (lvlNum < lvlCount) {
                    if (Core.player.isAvailable(packName, ratings.get(rating), lvlNum)) {
                        GameController.requestChangeLevel(packIdx, rating, lvlNum, false);
                        return;
                    }
                    lvlNum++;
                }
                rating++;
                if (rating < ratings.size()) {
                    lvlNum = 0;
                }
            }
        }
    }
    
    private void goToPreviousAvailableLevel() {
    	if (Fader.getState() == Fader.State.OFF) {
	        LevelPack pack = GameController.getCurLevelPack();
	        String packName = pack.getName();
	        int packIdx = GameController.getCurLevelPackIdx();
	        List<String> ratings = pack.getRatings();
	        int rating = GameController.getCurRating();
	        int lvlNum = GameController.getCurLevelNumber() - 1;
	        while (rating >= 0) {
	            while (lvlNum >= 0) {
	                if (Core.player.isAvailable(packName, ratings.get(rating), lvlNum)) {
	                    GameController.requestChangeLevel(packIdx, rating, lvlNum, false);
	                    return;
	                }
	                lvlNum--;
	            }
	            rating--;
	            if (rating >= 0) {
	                lvlNum = pack.getLevelCount(rating) - 1;
	            }
	        }
    	}
    }
    
    private void goToNextAvailableGroup() {
        if (Fader.getState() == Fader.State.OFF) {
            LevelPack pack = GameController.getCurLevelPack();
            String packName = pack.getName();
            int packIdx = GameController.getCurLevelPackIdx();
            List<String> ratings = pack.getRatings();
            int rating = GameController.getCurRating() + 1;
            int lvlNum = GameController.getCurLevelNumber();
            while (rating < ratings.size()) {
                while (lvlNum >= 0) {
                    if (Core.player.isAvailable(packName, ratings.get(rating), lvlNum)) {
                        GameController.requestChangeLevel(packIdx, rating, lvlNum, false);
                        return;
                    }
                    lvlNum--;
                }
                rating++;
                if (rating < ratings.size()) {
                    lvlNum = Math.min(pack.getLevelCount(rating), GameController.getCurLevelNumber());
                }
            }
        }
    }
    
    private void goToPreviousAvailableGroup() {
    	if (Fader.getState() == Fader.State.OFF) {
            LevelPack pack = GameController.getCurLevelPack();
            String packName = pack.getName();
            int packIdx = GameController.getCurLevelPackIdx();
            List<String> ratings = pack.getRatings();
            int rating = GameController.getCurRating() - 1;
            int lvlNum = GameController.getCurLevelNumber();
            while (rating >= 0) {
                while (lvlNum >= 0) {
                    if (Core.player.isAvailable(packName, ratings.get(rating), lvlNum)) {
                        GameController.requestChangeLevel(packIdx, rating, lvlNum, false);
                        return;
                    }
                    lvlNum--;
                }
                rating--;
                if (rating >= 0) {
                    lvlNum = Math.min(pack.getLevelCount(rating), GameController.getCurLevelNumber());
                }
            }
        }
    }

    void toggleMenuBarVisibility() {
        boolean shouldShowMenuBar;

        // BOOKMARK TODO: (Fullscreen implementation) Find a way to auto-hide the menu bar
        //                                            in such a way that the user can toggle it back on, even in Fullsreen
        //shouldShowMenuBar = !GameController.isOptionEnabled(GameController.RetroLemminiOption.FULL_SCREEN);
        shouldShowMenuBar = GameController.isOptionEnabled(GameController.RetroLemminiOption.SHOW_MENU_BAR);

        if (shouldShowMenuBar)
            setJMenuBar(jMenuBarMain);
        else
            setJMenuBar(null);

        // Set the frame size to maintain the same content size (insets are borders, title bar, the menu bar itself, etc)
        validate();
        Insets insets = getInsets();
        int contentWidth = getWidth() - insets.left - insets.right;
        int contentHeight = getHeight() - insets.top - insets.bottom;
        setSize(contentWidth + insets.left + insets.right, contentHeight + insets.top + insets.bottom);
    }
    
    /**
     * Toggle music volume between user setting & mute.
     */
    void toggleMusic() {
    	double musicVol = GameController.getMusicGain();
    	
    	if (musicVol > 0) {
    		userMusicVolume = musicVol;
    	}
    	
    	if (musicVol == 0) {
    		GameController.setMusicGain(userMusicVolume > 0 ? userMusicVolume : 0.5);
    	} else {
    		GameController.setMusicGain(0);
    	}
    }
    
    /**
     * Toggle sound volume between user setting & mute.
     */
    void toggleSound() {
    	double soundVol = GameController.getSoundGain();
    	
    	if (soundVol > 0) {
    		userSoundVolume = soundVol;
    	}
    	
    	if (soundVol == 0) {
    		GameController.setSoundGain(userSoundVolume > 0 ? userSoundVolume : 0.5);
    	} else {
    		GameController.setSoundGain(0);
    	}    	
    }

    /**
     * Common exit method to use in exit events.
     */
    void exit() {
        // stop the music
        Music.close();
        Core.saveProgramProps(); // to ensure the latest version is used
        
        // store width and height
        Core.programProps.setInt("frameWidth", lemminiPanelMain.getUnmaximizedWidth());
        Core.programProps.setInt("frameHeight", lemminiPanelMain.getUnmaximizedHeight());
        // store frame pos
        Core.programProps.setInt("framePosX", unmaximizedPosX);
        Core.programProps.setInt("framePosY", unmaximizedPosY);
        // store maximized state
        Core.programProps.setBoolean("maximizedHoriz", BooleanUtils.toBoolean(getExtendedState() & MAXIMIZED_HORIZ));
        Core.programProps.setBoolean("maximizedVert", BooleanUtils.toBoolean(getExtendedState() & MAXIMIZED_VERT));
        
        Core.programProps.save(Core.getProgramPropsFilePath(), false); // to store the above settings
        
        RepeatingReleasedEventsFixer.remove();
        System.exit(0);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        storeUnmaximizedPos();
    }

    private void storeUnmaximizedPos() {
        int frameState = getExtendedState();
        if (!BooleanUtils.toBoolean(frameState & MAXIMIZED_HORIZ)) {
            unmaximizedPosX = getX();
        }
        if (!BooleanUtils.toBoolean(frameState & MAXIMIZED_VERT)) {
            unmaximizedPosY = getY();
        }
    }

    public void setCursor(final LemmCursor.CursorType c) {
        lemminiPanelMain.setCursor(c);
    }

    void setRestartEnabled(boolean restartEnabled) {
        jMenuItemRestartLevel.setEnabled(restartEnabled);
    }

    public static LemminiFrame getFrame() {
        return thisFrame;
    }

    private void saveLevelAsImage() {
        String levelName = GameController.getLevel().getLevelName();
        String baseFileName = levelName.replaceAll("[^a-zA-Z0-9._-]", "_"); // Sanitize filename
        String fileName = baseFileName + ".png";
        Path filePath = Core.resourceTree.getPath(fileName);

        int counter = 1;
        while (Files.exists(filePath)) {
            fileName = baseFileName + "(" + counter + ").png";
            filePath = Core.resourceTree.getPath(fileName);
            counter++;
        }

        LemmImage tmp = GameController.getLevel().createMinimap(GameController.getFgImage(), 1.0, 1.0, true, false, true);
        // BOOKMARK TODO: See if there's a way to draw the lemmings to this image as well
        //                If there is, maybe show a quick "options" dialog where the user can choose whether or not to include lemmings, background, etc
        try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            ImageIO.write(tmp.getImage(), "png", out);
            out.flush();
            JOptionPane.showMessageDialog(null, "Level image successfully saved to\n" + filePath, "Save Level Image", JOptionPane.PLAIN_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not save level image", "Save Level Image", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveLevelAsImageFromPreview()
    {
        LemmImage tmp = GameController.getLevel().createMinimap(GameController.getFgImage(), 1.0, 1.0, true, false, true);
            try (OutputStream out = Core.resourceTree.newOutputStream("level.png")) {
                ImageIO.write(tmp.getImage(), "png", out);
            } catch (IOException ex) {
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBarMain;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemEnterLevelCode;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemLoadReplay;
    private javax.swing.JMenuItem jMenuItemManagePlayers;
    private javax.swing.JMenuItem jMenuItemOptions;
    private javax.swing.JMenuItem jMenuItemHotkeys;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemChooseLevel;
    private javax.swing.JMenuItem jMenuItemRestartLevel;
    private javax.swing.JMenu jMenuLevel;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuPlayers;
    private lemmini.LemminiPanel lemminiPanelMain;
    // End of variables declaration//GEN-END:variables
}
