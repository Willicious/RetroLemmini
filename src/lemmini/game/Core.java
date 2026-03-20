package lemmini.game;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import lemmini.LemminiFrame;
import lemmini.game.LemGame.ExitSoundOption;
import lemmini.game.LemGame.MenuThemeOption;
import lemmini.gameutil.Hotkey;
import lemmini.gameutil.MouseInput;
import lemmini.gameutil.RetroLemminiHotkeys;
import lemmini.graphics.LemmImage;
import lemmini.gui.LegalFrame;
import lemmini.tools.CaseInsensitiveFileTree;
import lemmini.tools.CommitID;
import lemmini.tools.Props;
import lemmini.tools.StyleDownloader;
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
 * @author Volker Oth
 * Modified by Ryan Sakowski, Charles Irwin and Will James
 */
public class Core {

    public static final String REVISION = "2.9.1";
    public static final String COMMIT_ID = CommitID.ID;
    public static final String REV_DATE = "March 2026";

    /** extensions accepted for level files in file dialog */
    public static final String[] LEVEL_EXTENSIONS = {"rlv", "ini", "lvl", "dat"};
    /** extensions accepted for replay files in file dialog */
    public static final String[] REPLAY_EXTENSIONS = {"rpl"};

    public static final String[] IMAGE_EXTENSIONS = {"png", "bmp", "gif", "jpg", "wbmp"};
    public static final String[] MUSIC_EXTENSIONS = {"wav", "aiff", "aifc", "au", "snd",
        "ogg", "xm", "s3m", "mod", "mid"};
    public static final String[] SOUNDBANK_EXTENSIONS = {"sf2", "dls"};
    public static final String[] SOUND_EXTENSIONS = {"wav", "aiff", "aifc", "au", "snd"};
    /** path of external level cache */
    public static final String EXTERNAL_LEVEL_CACHE_PATH = "levels/$external/";
    /** path for mods */
    public static final String MODS_PATH = "mods/";
    /** path for music */
    public static final String MUSIC_PATH = "music/";
    /** path for replays */
    public static final String REPLAYS_PATH = "replays/";
    /** path for sound */
    public static final String SOUND_PATH = "sound/";
    /** path for styles */
    public static final String STYLES_PATH = "styles/";
    /** path for temporary files */
    public static final String TEMP_PATH = "temp/";

    public static final Path[] EMPTY_PATH_ARRAY = {};

    /** name program hotkeys file */
    private static final String PROGRAM_HOTKEYS_FILE_NAME = "retrolemmini_hotkeys.ini";
    /** name program properties file */
    private static final String PROGRAM_PROPS_FILE_NAME = "retrolemmini_settings.ini";
    /** name of player properties file */
    private static final String PLAYER_PROPS_FILE_NAME = "players.ini";

    public static final Set<String> OG_STYLES = new HashSet<>(Arrays.asList(
            "brick",
            "bubble",
            "classic",
            "crystal",
            "crystal_md",
            "dirt",
            "dirt_md",
            "fire",
            "marble",
            "pillar",
            "rock",
            "snow",
            "special",
            "xmas"
        ));

    /** program properties */
    public static Props programProps;
    /** path of program hotkeys file */
    private static Path programHotkeysFilePath;
    /** path of program properties file */
    private static Path programPropsFilePath;
    /** path of player properties file */
    private static Path playerPropsFilePath;
    /** path of settings */
    public static Path settingsPath;
    /** path of resources */
    public static Path resourcePath;
    /** path of icons */
    public static Path iconsPath;
    /** path of currently run RetroLemmini instance */
    public static Path gamePath;
    /** settings directory */
    public static CaseInsensitiveFileTree settingsTree;
    /** resource directory */
    public static CaseInsensitiveFileTree resourceTree;
    /** current player */
    public static Player player;
    /** player properties */
    private static Props playerProps;
    /** list of all players */
    private static List<String> players;
    /** Zoom scale */
    private static double scale = 1.0;
    private static boolean bilinear;
    /** draw width */
    private static int drawWidth;
    /** draw height */
    private static int drawHeight;
    
    private static MouseInput mouseInput = new MouseInput();
    
    /** the directory containing the JAR */
	private static String jarDirectory;

    /**
     * Initialize some core elements (settings, resources, etc)
     */
    public static boolean init(String workingFolder) throws LemmException, IOException  {
        System.out.println("\ninitializing Core...");
        String tmp;// = java.net.URLDecoder.decode(workingFolder, "UTF-8");
        tmp = new java.io.File(workingFolder).getPath();

        gamePath = Paths.get(tmp);

        System.out.println("    gamePath detected as: "+ gamePath.toString());

        // Settings directory
        if (gamePath.toString().endsWith(".jar")) {
            settingsPath = Paths.get(gamePath.getParent().toString(), "settings");
        } else {
        	settingsPath = Paths.get(gamePath.toString(), "settings");
        }
        System.out.println("      settingsPath: " + settingsPath.toString());
        settingsTree = new CaseInsensitiveFileTree(settingsPath);
        System.out.println("    creating players folder: " + Paths.get(settingsTree.getRoot().toString(), "players/").toString());
        settingsTree.createDirectories("players/");
        programPropsFilePath = settingsPath.resolve(PROGRAM_PROPS_FILE_NAME);
        playerPropsFilePath = settingsPath.resolve(PLAYER_PROPS_FILE_NAME);
        System.out.println("    game config: " + programPropsFilePath.toString());
        System.out.println("    player data: " + playerPropsFilePath.toString());

        // read main ini file
        programProps = new Props();

        if (!programProps.load(programPropsFilePath)) {
            System.out.println("    unable to read config file... prompting disclaimer agreement ...");
            // might exist or not - if not, it's created
            // show the Legal Disclaimer. And force the user to choose "I Agree."
            // NOTE: the Legal Disclaimer is loaded from "disclaimer.htm"
            LegalFrame ld = new LegalFrame();
            ld.setVisible(true);
            ld.waitUntilClosed();
            if (!ld.isOK()) {
                // user does not agree, so we exit.
                System.out.println("    user declined agreement. quitting...");
                return false;
            } else {
                System.out.println("    user agreed");
            }
        } else {
            System.out.println("    config file read successfully");
        }
        
        // Hotkeys
        setProgramHotkeysFilePath(settingsPath.resolve(PROGRAM_HOTKEYS_FILE_NAME));
        System.out.println("\n    hotkey config: " + getProgramHotkeysFilePath().toString());
        loadHotkeys(getProgramHotkeysFilePath());
        System.out.println("    hotkey config read successfully");
        
        // Mouse Config
        getMouseInput().loadFromProgramProps(programProps);
        System.out.println("    mouse config read successfully");

        // Resources directory
        if (gamePath.toString().endsWith(".jar")) {
            resourcePath = Paths.get(gamePath.getParent().toString(), "resources");
        } else {
            resourcePath = Paths.get(gamePath.toString(), "resources");
        }
        System.out.println("      resourcePath: " + resourcePath.toString());
        resourceTree = new CaseInsensitiveFileTree(resourcePath);
        
        // Icons directory
        if (gamePath.toString().endsWith(".jar")) {
            iconsPath = Paths.get(Core.gamePath.getParent().toString(), "icons");
        } else {
            iconsPath = Paths.get(Core.gamePath.toString(), "icons");
        }
        System.out.println("      iconsPath: " + iconsPath.toString());

        bilinear = programProps.getBoolean("bilinear", true);

        // Set options
        LemGame.setOption(LemGame.Option.MUSIC_ON, programProps.getBoolean("music", true));
        LemGame.setOption(LemGame.Option.SOUND_ON, programProps.getBoolean("sound", true));
        LemGame.setMusicGain(programProps.getDouble("musicGain", 1.0));
        LemGame.setSoundGain(programProps.getDouble("soundGain", 1.0));
        LemGame.setOption(LemGame.Option.ADVANCED_SELECT, programProps.getBoolean("advancedSelect", true));
        LemGame.setOption(LemGame.Option.CLASSIC_CURSOR, programProps.getBoolean("classicalCursor", false));
        LemGame.setOption(LemGame.Option.FASTER_FAST_FORWARD, programProps.getBoolean("fasterFastForward", false));
        LemGame.setOption(LemGame.Option.PAUSE_STOPS_FAST_FORWARD, programProps.getBoolean("pauseStopsFastForward", true));
        LemGame.setOption(LemGame.Option.NO_PERCENTAGES, programProps.getBoolean("noPercentages", true));
        LemGame.setOption(LemGame.Option.REPLAY_SCROLL, programProps.getBoolean("replayScroll", false));
        LemGame.setOption(LemGame.Option.UNPAUSE_ON_ASSIGNMENT, programProps.getBoolean("unpauseOnAssignment", false));
        // Settings added in SuperLemminiToo
        LemGame.setOption(LemGame.Option.TIMED_BOMBERS, programProps.getBoolean("timedBombers", true));
        LemGame.setOption(LemGame.Option.UNLOCK_ALL_LEVELS, programProps.getBoolean("unlockAllLevels", true));
        LemGame.setOption(LemGame.Option.ENABLE_FRAME_STEPPING, programProps.getBoolean("enableFrameStepping", false));
        LemGame.setOption(LemGame.Option.VISUAL_SFX, programProps.getBoolean("visualSFX", true));
        LemGame.setOption(LemGame.Option.ENHANCED_STATUS, programProps.getBoolean("enhancedStatus", true));
        LemGame.setOption(LemGame.Option.SHOW_LEVEL_NAME, programProps.getBoolean("showLevelName", true));
        LemGame.setOption(LemGame.Option.ENHANCED_ICONBAR, programProps.getBoolean("enhancedIconBar", true));
        LemGame.setOption(LemGame.Option.ICON_LABELS, programProps.getBoolean("iconLabels", false));
        LemGame.setOption(LemGame.Option.ANIMATED_ICONS, programProps.getBoolean("animatedIcons", true));
        LemGame.setOption(LemGame.Option.CLASSIC_SCROLLER, programProps.getBoolean("classicScroller", true));
        LemGame.setOption(LemGame.Option.DEBUG_VERBOSE_PLAYER_LOAD, programProps.getBoolean("debugVerbosePlayerLoad", false));
        // Settings added in RetroLemmini
        LemGame.setOption(LemGame.Option.AUTOSAVE_REPLAYS, programProps.getBoolean("autoSaveReplays", true));
        LemGame.setOption(LemGame.Option.SHOW_MENU_BAR, programProps.getBoolean("showMenuBar", true));
        LemGame.setOption(LemGame.Option.FULL_COLOR_MINIMAP, programProps.getBoolean("fullColorMinimap", true));
        LemGame.setOption(LemGame.Option.POSTVIEW_JINGLES, programProps.getBoolean("postviewJingles", false));
        LemGame.setOption(LemGame.Option.CLICK_AIR_TO_CANCEL_REPLAY, programProps.getBoolean("clickAirToCancelReplay", true));
        LemGame.setOption(LemGame.Option.ENABLE_WHEEL_SKILL_SELECT, programProps.getBoolean("enableWheelSkillSelect", false));
        LemGame.setOption(LemGame.Option.ENABLE_WHEEL_BRUSH_SIZE, programProps.getBoolean("enableWheelBrushSize", true));
        LemGame.setOption(LemGame.Option.DIRECT_DROP, programProps.getBoolean("directDrop", false));
        // Exit sound settings
        LemGame.setExitSoundOption(ExitSoundOption.valueOf(programProps.get("exitSound", "AUTO")));
        // Menu theme settings
        LemGame.setMenuThemeOption(MenuThemeOption.valueOf(programProps.get("menuTheme", "WINLEMM")));
        // Auto-replay naming template
        LemGame.setReplayNameTemplate(programProps.get("replayNameTemplate", "{user}_{pack}_{rating}_{level}_{time}"));

        System.out.println("      all settings read from config");


        // Ensure "resources" folder exists
        if (resourcePath.toString().isEmpty()) {
            if (resourcePath.toString().isEmpty()) {
                System.out.println("    resourcePath is invalid...");
            }
            System.out.println("    quitting...");
            throw new LemmException(String.format("Resources folder is missing from " + gamePath + ". The program will now quit.", (Object[])null));
        }

        // Create folders (if they don't already exist)
        // create levels folder (with external level cache)
        System.out.println("    creating levels folder (with external levels cache): " + Paths.get(resourceTree.getRoot().toString(), EXTERNAL_LEVEL_CACHE_PATH).toString());
        resourceTree.createDirectories(EXTERNAL_LEVEL_CACHE_PATH);
        // create mods folder
        System.out.println("    creating mods folder: " + Paths.get(resourceTree.getRoot().toString(), MODS_PATH).toString());
        resourceTree.createDirectories(MODS_PATH);
        // create music folder
        System.out.println("    creating music folder: " + Paths.get(resourceTree.getRoot().toString(), MUSIC_PATH).toString());
        resourceTree.createDirectories(MUSIC_PATH);
        // create music folder
        System.out.println("    creating music folder: " + Paths.get(resourceTree.getRoot().toString(), MUSIC_PATH).toString());
        resourceTree.createDirectories(REPLAYS_PATH);
        // create sound folder
        System.out.println("    creating sound folder: " + Paths.get(resourceTree.getRoot().toString(), SOUND_PATH).toString());
        resourceTree.createDirectories(SOUND_PATH);
        // create styles folder
        System.out.println("    creating styles folder: " + Paths.get(resourceTree.getRoot().toString(), STYLES_PATH).toString());
        resourceTree.createDirectories(STYLES_PATH);
        // create temp folder
        System.out.println("    creating temp folder: " + Paths.get(resourceTree.getRoot().toString(), TEMP_PATH).toString());
        resourceTree.createDirectories(TEMP_PATH);

        System.gc(); // force garbage collection here before the game starts

        System.out.println("    loading player settings...");
        loadPlayerSettings();
        
        System.out.println("    validating default styles...");
        validateDefaultStyles();

        System.out.println("Core initialization complete.");
        return true;
    }

    public static void loadHotkeys(Path path) {
        List<Hotkey> hotkeys = RetroLemminiHotkeys.getHotkeys(RetroLemminiHotkeys.HotkeyProfile.DEFAULT);
        Path iniPath = path;

        if (Files.exists(iniPath)) {
            try (BufferedReader reader = Files.newBufferedReader(iniPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    String[] parts = line.split("=", 2);
                    if (parts.length != 2) continue;

                    String actionName = parts[0].trim();
                    String keyString = parts[1].trim();

                    try {
                        RetroLemminiHotkeys.HotkeyAction action = RetroLemminiHotkeys.HotkeyAction.valueOf(actionName);
                        Hotkey hk = hotkeys.stream()
                                            .filter(h -> h.getAction() == action)
                                            .findFirst()
                                            .orElse(null);

                        if (hk != null) {
                            String[] keyParts = keyString.split("\\+");
                            if (keyParts.length == 2) {
                                hk.setModifier(keyParts[0]);
                                hk.setKey(RetroLemminiHotkeys.getKeyCode(keyParts[1]), keyParts[1]);
                            } else {
                                hk.setModifier(null);
                                hk.setKey(RetroLemminiHotkeys.getKeyCode(keyString), keyString);
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        // unknown action, skip
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LemGame.activeHotkeys = hotkeys;
    }

	/***
     *  Reads all player settings from the players.ini file
     */
    private static void loadPlayerSettings() {
        // read player names
        playerProps = new Props();
        playerProps.load(playerPropsFilePath);
        String defaultPlayer = playerProps.get("defaultPlayer", "default");
        players = new ArrayList<>(16);
        for (int idx = 0; true; idx++) {
            String p = playerProps.get("player_" + idx, StringUtils.EMPTY);
            if (p.isEmpty()) {
                break;
            }
            players.add(p);
        }
        if (players.isEmpty()) {
            // no players yet, establish default player
            players.add("default");
            playerProps.set("player_0", "default");
        }
        player = new Player(defaultPlayer);
    }

    /***
     * Writes all applicable settings to the settings ini file
     */
    public static void saveSettings() {
        //sound settings
        programProps.setBoolean("music", LemGame.isOptionEnabled(LemGame.Option.MUSIC_ON));
        programProps.setBoolean("sound", LemGame.isOptionEnabled(LemGame.Option.SOUND_ON));
        programProps.set("mixerName", LemGame.sound.getMixers()[LemGame.sound.getMixerIdx()]);
        //graphic settings
        programProps.setBoolean("bilinear", Core.isBilinear());
        //misc settings
        programProps.setBoolean("advancedSelect", LemGame.isOptionEnabled(LemGame.Option.ADVANCED_SELECT));
        programProps.setBoolean("classicalCursor", LemGame.isOptionEnabled(LemGame.Option.CLASSIC_CURSOR));
        programProps.setBoolean("fasterFastForward", LemGame.isOptionEnabled(LemGame.Option.FASTER_FAST_FORWARD));
        programProps.setBoolean("pauseStopsFastForward", LemGame.isOptionEnabled(LemGame.Option.PAUSE_STOPS_FAST_FORWARD));
        programProps.setBoolean("noPercentages", LemGame.isOptionEnabled(LemGame.Option.NO_PERCENTAGES));
        programProps.setBoolean("replayScroll", LemGame.isOptionEnabled(LemGame.Option.REPLAY_SCROLL));
        programProps.setBoolean("unpauseOnAssignment", LemGame.isOptionEnabled(LemGame.Option.UNPAUSE_ON_ASSIGNMENT));
        // Settings added in SuperLemminiToo
        programProps.setBoolean("timedBombers", LemGame.isOptionEnabled(LemGame.Option.TIMED_BOMBERS));
        programProps.setBoolean("unlockAllLevels", LemGame.isOptionEnabled(LemGame.Option.UNLOCK_ALL_LEVELS));
        programProps.setBoolean("enableFrameStepping", LemGame.isOptionEnabled(LemGame.Option.ENABLE_FRAME_STEPPING));
        programProps.setBoolean("visualSFX", LemGame.isOptionEnabled(LemGame.Option.VISUAL_SFX));
        programProps.setBoolean("enhancedStatus", LemGame.isOptionEnabled(LemGame.Option.ENHANCED_STATUS));
        programProps.setBoolean("showLevelName", LemGame.isOptionEnabled(LemGame.Option.SHOW_LEVEL_NAME));
        programProps.setBoolean("enhancedIconBar", LemGame.isOptionEnabled(LemGame.Option.ENHANCED_ICONBAR));
        programProps.setBoolean("iconLabels", LemGame.isOptionEnabled(LemGame.Option.ICON_LABELS));
        programProps.setBoolean("animatedIcons", LemGame.isOptionEnabled(LemGame.Option.ANIMATED_ICONS));
        programProps.setBoolean("classicScroller", LemGame.isOptionEnabled(LemGame.Option.CLASSIC_SCROLLER));
        programProps.setBoolean("debugVerbosePlayerLoad", LemGame.isOptionEnabled(LemGame.Option.DEBUG_VERBOSE_PLAYER_LOAD));
        // Settings added in RetroLemmini
        programProps.setBoolean("autoSaveReplays", LemGame.isOptionEnabled(LemGame.Option.AUTOSAVE_REPLAYS));
        programProps.setBoolean("showMenuBar", LemGame.isOptionEnabled(LemGame.Option.SHOW_MENU_BAR));
        programProps.setBoolean("fullColorMinimap", LemGame.isOptionEnabled(LemGame.Option.FULL_COLOR_MINIMAP));
        programProps.setBoolean("postviewJingles", LemGame.isOptionEnabled(LemGame.Option.POSTVIEW_JINGLES));
        programProps.setBoolean("clickAirToCancelReplay", LemGame.isOptionEnabled(LemGame.Option.CLICK_AIR_TO_CANCEL_REPLAY));
        programProps.setBoolean("enableWheelSkillSelect", LemGame.isOptionEnabled(LemGame.Option.ENABLE_WHEEL_SKILL_SELECT));
        programProps.setBoolean("enableWheelBrushSize", LemGame.isOptionEnabled(LemGame.Option.ENABLE_WHEEL_BRUSH_SIZE));
        programProps.setBoolean("directDrop", LemGame.isOptionEnabled(LemGame.Option.DIRECT_DROP));
        // Exit sound
        programProps.set("exitSound", LemGame.getExitSoundOption().name());
        // Menu theme
        programProps.set("menuTheme", LemGame.getMenuThemeOption().name());
        // Replay name template
        programProps.set("replayNameTemplate", LemGame.getReplayNameTemplate());
        
        // Update file
        programProps.save(programPropsFilePath, false);
    }
    
    /***
     * Downloads the default styles (if missing)
     */
    public static void validateDefaultStyles() {
        for (String style : OG_STYLES) {
            File iniFile = new File(resourcePath + "/styles/" + style + "/" + style + ".ini");
            if (!iniFile.exists()) {
                System.out.println("Missing style detected. Downloading styles...");
                StyleDownloader.startDownload();
                StyleDownloader.reInitializeCore();
                return;
            }
        }
        System.out.println("    validation complete. All styles present.");
    }
    
    /**
     * Compares revisions stringa
     */
    public static int compareVersions(String a, String b) {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");

        int length = Math.max(aParts.length, bParts.length);

        for (int i = 0; i < length; i++) {
            int aVal = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
            int bVal = i < bParts.length ? Integer.parseInt(bParts[i]) : 0;

            if (aVal < bVal) return -1;
            if (aVal > bVal) return 1;
        }
        return 0;
    }

    public static String appendBeforeExtension(String fname, String suffix) {
        String extension = FilenameUtils.getExtension(fname);
        if (extension.isEmpty()) {
            return FilenameUtils.removeExtension(fname) + suffix;
        } else {
            return FilenameUtils.removeExtension(fname) + suffix + "." + extension;
        }
    }

    /**
     * Get Path to resource in resource path.
     */
    public static Resource findResource(String fname, boolean searchMods) throws ResourceException {
        String originalExt = FilenameUtils.getExtension(fname);
        return findResource(fname, searchMods, true, originalExt);
    }

    /**
     * Get Path to resource in resource path (searches mods and main file by default).
     */
    public static Resource findResource(String fname, String... extensions) throws ResourceException {
        return findResource(fname, true, true, extensions);
    }

    /**
     * Get Path to resource in resource path (searches main file by default).
     */
    public static Resource findResource(String fname, boolean searchMods, String... extensions) throws ResourceException {
        return findResource(fname, searchMods, true, extensions);
    }

    /**
     * Get Path to resource in resource path.
     */
    public static Resource findResource(String fname, boolean searchMods, boolean searchMain, String... extensions) throws ResourceException {
        Resource rslt = findResourceEx(fname, searchMods, searchMain, extensions);
        if (rslt == null) {
            // file still not found, so throw a ResourceException
            throw new ResourceException(fname);
        }

        return rslt;
    }

    /**
     * Get Path to resource in resource path.
     */
    public static Resource findResourceEx(String fname, boolean searchMods, boolean searchMain, String... extensions) throws ResourceException {
        String fnameNoExt = FilenameUtils.removeExtension(fname);
        if (searchMods) {        	
            // Backwards compatibility
            Map<String, String> legacyFileMap = new HashMap<>();
            legacyFileMap.put("gfx/menu/background_main_amiga", "gfx/misc/background_main");
            legacyFileMap.put("gfx/menu/background_main_retro", "gfx/misc/background_main");
            legacyFileMap.put("gfx/menu/background_level_amiga", "gfx/misc/background_level");
            legacyFileMap.put("gfx/menu/background_level_retro", "gfx/misc/background_level");
            
            // Override: If "gfx/menu/background_main" exists, use it instead of "_amiga"/"_winlemm"
            Map<String, String> overrideFileMap = new HashMap<>();
            overrideFileMap.put("gfx/menu/background_main_amiga", "gfx/menu/background_main");
            overrideFileMap.put("gfx/menu/background_main_retro", "gfx/menu/background_main");
            overrideFileMap.put("gfx/menu/background_level_amiga", "gfx/menu/background_level");
            overrideFileMap.put("gfx/menu/background_level_retro", "gfx/menu/background_level");
        	
            // Try to load the file from the mod paths with each extension
            for (String mod : LemGame.getModPaths()) {
                for (String ext : extensions) {
                    String resString = mod + "/" + fnameNoExt + "." + ext;

                    // Check if the requested file exists
                    if (resourceTree.exists(resString)) {
                        return new FileResource(fname, resString, resourceTree);
                    }
                    // Check if an override version exists
                    if (overrideFileMap.containsKey(fnameNoExt)) {
                        String overrideResString = mod + "/" + overrideFileMap.get(fnameNoExt) + "." + ext;
                        if (resourceTree.exists(overrideResString)) {
                            return new FileResource(fname, overrideResString, resourceTree);
                        }
                    }
                    // Check if a legacy version exists
                    if (legacyFileMap.containsKey(fnameNoExt)) {
                        String legacyResString = mod + "/" + legacyFileMap.get(fnameNoExt) + "." + ext;
                        if (resourceTree.exists(legacyResString)) {
                            return new FileResource(fname, legacyResString, resourceTree);
                        }
                    }
                }
            }
        }
        // file not found in mod folders or mods not searched,
        // so look for it in the main folders, again with each extension
        if (searchMain) {
            for (String ext : extensions) {
                String resString = fnameNoExt + "." + ext;
                if (resourceTree.exists(resString)) {
                    return new FileResource(fname, resString, resourceTree);
                }
            }
        }
        // file still not found, so throw a ResourceException
        return null;
    }

    public static List<String> searchForResources(String folder, boolean searchMods, String... extensions) {
        Set<String> resources = new LinkedHashSet<>(64);

        if (searchMods) {
            LemGame.getModPaths().stream().forEachOrdered(mod -> {
                String lowercasePath = ("mods/" + mod + "/" + folder).toLowerCase(Locale.ROOT);
                resourceTree.getAllPathsRegex(ToolBox.literalToRegex(lowercasePath) + "[^/]+").stream()
                        .map(file -> file.getFileName().toString())
                        .filter(fileName -> FilenameUtils.isExtension(fileName.toLowerCase(Locale.ROOT), extensions))
                        .forEachOrdered(resources::add);
            });
        }
        String lowercasePath = folder.toLowerCase(Locale.ROOT);
        resourceTree.getAllPathsRegex(ToolBox.literalToRegex(lowercasePath) + "[^/]+").stream()
                .map(file -> file.getFileName().toString())
                .filter(fileName -> FilenameUtils.isExtension(fileName.toLowerCase(Locale.ROOT), extensions))
                .forEachOrdered(resources::add);

        return new ArrayList<>(resources);
    }

    /**
     * Set the title
     */
    public static void setWindowCaption(String caption) {
        LemminiFrame.getFrame().setTitle(caption);
    }
    
    /**
     * Get the title
     */
    public static String getWindowCaption() {
        return LemminiFrame.getFrame().getTitle();
    }

    /**
     * Store program properties.
     */
    public static void saveProgramProps() {
        programProps.save(programPropsFilePath, true);
        playerProps.set("defaultPlayer", player.getName());
        playerProps.save(playerPropsFilePath, true);
        player.store();
    }
    
    public static void missingLevelError(final String rsrc) {
        String message = String.format("Missing level:<br><br>"
        		+ "The level listed in the levelpack.ini for %s could not be loaded.<br><br>"
                + "Please visit <a href='https://www.lemmingsforums.net/index.php?msg=105737'>this help topic</a> on the Lemmings Forums for help.", rsrc);
        JEditorPane pane = new JEditorPane("text/html", "<html><body style='font-family:sans-serif;'>" + message + "</body></html>");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JOptionPane.showMessageDialog(null, pane, "Error", JOptionPane.ERROR_MESSAGE);
        returnToMainMenu();
    }

    public static void resourceError(final String rsrc) {
        String message = String.format("Missing resource:<br><br>"
        		+ "resources/%s<br><br>"
                + "Please visit <a href='https://www.lemmingsforums.net/index.php?msg=105737'>this help topic</a> on the Lemmings Forums for help.", rsrc);
        JEditorPane pane = new JEditorPane("text/html", "<html><body style='font-family:sans-serif;'>" + message + "</body></html>");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JOptionPane.showMessageDialog(null, pane, "Error", JOptionPane.ERROR_MESSAGE);
        returnToMainMenu();
    }
    
    public static void musicResourceError(final String rsrc) {
        String message = String.format("Missing music resource:<br><br>"
        		+ "resources/music/%s<br><br>"
        		+ "No music will play for this level.<br><br>"
                + "Please visit <a href='https://www.lemmingsforums.net/index.php?msg=105737'>this help topic</a> on the Lemmings Forums for help.", rsrc);
        JEditorPane pane = new JEditorPane("text/html", "<html><body style='font-family:sans-serif;'>" + message + "</body></html>");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JOptionPane.showMessageDialog(null, pane, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static String stylePieceIndexError(final String style, final String type, final int idx) {
    	if (type == "object")
    		return "An object with index " + idx + " is not listed in " + style + ".ini";
    	else
    		return "Index of terrain piece (" + idx + ") is higher than the number of terrain pieces in '" + style + "'";
    }
    
    public static String stylePieceResourceError(final String style, final String type, final int idx) {
    	String suffix = type == "object" ? "o" : "";
        return String.format("<html>Missing style piece: resources/styles/%s/%s%s_%d (%s)<br><br>"
        		+ "Run 'Help > Refresh Styles' and then try loading the level again.<br>"
        		+ "If that doesn't work, contact the pack author/maintainer.<br><br>"
        		+ "The system will now return to main menu.<br><br></html>", style, style, suffix, idx, type);
    }
    
    public static void generalError(final String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    public static void returnToMainMenu() {
        LemGame.setTransition(LemGame.TransitionState.TO_INTRO);
    	TextScreen.initIntro();
    }

    /**
     * Adds the given image to the given tracker.
     */
/*    private static Image addToTracker(final MediaTracker tracker, Image image) throws ResourceException {
        if (image != null) {
            tracker.addImage(image, 0);
            try {
                tracker.waitForID(0);
                if (tracker.isErrorAny()) {
                    image = null;
                }
            } catch (Exception ex) {
                image = null;
            }
        }
        return image;
    }
    */
    /**
     * Loads an image from the given resource.
     */
    public static LemmImage loadLemmImage(final Resource resource) throws ResourceException {
        BufferedImage img = null;
        if (resource != null) {
            try (InputStream in = resource.getInputStream()) {
                img = ImageIO.read(in);
            } catch (IOException ex) {
                img = null;
            }
        }
        if (img == null) {
            throw new ResourceException(resource);
        }
        return new LemmImage(img);
    }

    /**
     * Load an image from inside the JAR or the directory of the main class.
     */
    public static LemmImage loadLemmImageJar(final String fname) throws ResourceException {
        BufferedImage img;
        try {
            img = ImageIO.read(ToolBox.findFile(fname));
        } catch (IOException ex) {
            throw new ResourceException(fname);
        }
        return new LemmImage(img);
    }

    /**
     * Get player name via index.
     */
    public static String getPlayer(final int idx) {
        return players.get(idx);
    }

    /**
     * Get number of players.
     */
    public static int getPlayerCount() {
        if (players == null) {
            return 0;
        }
        return players.size();
    }

    /**
     * Delete a player.
     */
    public static void deletePlayer(final int idx) {
        Player.deletePlayerINIFile(players.get(idx));
        players.remove(idx);
        playerProps.save(playerPropsFilePath, false);
    }

    /**
     * Reset list of players.
     */
    public static void clearPlayers() {
        players.clear();
        playerProps.clear();
    }

    /**
     * Add player.
     */
    public static void addPlayer(final String name) {
        players.add(name);
        playerProps.set("player_" + (players.size() - 1), name);
        playerProps.save(playerPropsFilePath, false);
    }

    /**
     * Get internal draw width
     */
    public static int getDrawWidth() {
        return drawWidth;
    }

    /**
     * Get scaled internal draw width
     */
    public static int getScaledDrawWidth() {
        return (scale == 1.0) ? drawWidth : (int) Math.ceil(drawWidth * scale);
    }

    /**
     * Get internal draw height
     */
    public static int getDrawHeight() {
        return drawHeight;
    }

    /**
     * Get scaled internal draw height
     */
    public static int getScaledDrawHeight() {
        return (scale == 1.0) ? drawHeight : (int) Math.ceil(drawHeight * scale);
    }

    /**
     * Set internal draw size
     */
    public static void setDrawSize(int w, int h) {
        drawWidth = w;
        drawHeight = h;
    }

    /**
     * Get zoom scale
     */
    public static double getScale() {
        return scale;
    }

    /**
     * Set zoom scale
     */
    public static void setScale(double s) {
        scale = s;
    }

    public static int scale(int n) {
        return ToolBox.scale(n, scale);
    }

    public static int unscale(int n) {
        return ToolBox.unscale(n, scale);
    }

    public static boolean isBilinear() {
        return bilinear;
    }

    public static void setBilinear(final boolean b) {
        bilinear = b;
    }

	public static Path getProgramPropsFilePath() {
		return programPropsFilePath;
	}

	public static void setProgramPropsFilePath(Path programPropsFilePath) {
		Core.programPropsFilePath = programPropsFilePath;
	}

	public static Path getProgramHotkeysFilePath() {
		return programHotkeysFilePath;
	}

	public static void setProgramHotkeysFilePath(Path programHotkeysFilePath) {
		Core.programHotkeysFilePath = programHotkeysFilePath;
	}

	public static MouseInput getMouseInput() {
		return mouseInput;
	}

	public static String getJarDirectory() {
		return jarDirectory;
	}

	public static void setJarDirectory(String jarDirectory) {
		Core.jarDirectory = jarDirectory;
	}
}
