package lemmini.game;

//import java.awt.Image;
//import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import lemmini.LemminiFrame;
import lemmini.game.GameController.ExitSoundOption;
import lemmini.graphics.LemmImage;
import lemmini.gui.LegalFrame;
import lemmini.tools.CaseInsensitiveFileTree;
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
 * Well, this started as some kind of core class to collect all global stuff
 * Now lots of the functionality moved to GameController.
 * Would need some cleaning up, maybe remove the whole thing?
 * @author Volker Oth
 */
public class Core {

	public static final String REVISION = "2.0";
	public static final String REV_DATE = "Jan 2025";
    
    /** extensions accepted for level files in file dialog */
    public static final String[] LEVEL_EXTENSIONS = {"ini", "lvl", "dat"};
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
    
    /** name of the INI file */
    private static final String PROGRAM_PROPS_FILE_NAME = "retrolemmini_settings.ini";
    /** name of player properties file */
    private static final String PLAYER_PROPS_FILE_NAME = "players.ini";
    
    public static final Set<String> OG_STYLES = new HashSet<>(Arrays.asList(
            "brick", "bubble", "crystal", "dirt", "fire",
            "marble", "pillar", "rock", "snow", "xmas"
        ));
    
    /** program properties */
    public static Props programProps;
    /** path of resources */
    public static Path resourcePath;
    /** path of currently run RetroLemmini instance */
    public static Path gamePath;
    /** list of all the game resources in an lzp file. */
    public static CaseInsensitiveFileTree resourceTree;
    public static CaseInsensitiveFileTree gameDataTree;
    /** current player */
    public static Player player;
    
    /** name of program properties file */
    private static Path programPropsFilePath;
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
    
    
    /**
     * Initialize some core elements.
     * Loads settings from ini file.
     * @return true if loading was successful
     * @throws LemmException
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static boolean init(String workingFolder) throws LemmException, IOException  {
    	System.out.println("\ninitializing Core...");
    	String tmp;// = java.net.URLDecoder.decode(workingFolder, "UTF-8");
    	tmp = new java.io.File(workingFolder).getPath();
    	
    	gamePath = Paths.get(tmp);

    	System.out.println("    gamePath detected as: "+ gamePath.toString());
    	
    	// Data directory   	
        gameDataTree = new CaseInsensitiveFileTree(gamePath);

        // Settings directory
        System.out.println("    creating settings folder: " + Paths.get(gameDataTree.getRoot().toString(), "settings/").toString());
    	try {
    		gameDataTree.createDirectories("settings/");
    	} catch (IOException e) {
    	    System.err.println("Failed to create settings directory: " + e.getMessage());
    	    e.printStackTrace();
    	}
    	if (gamePath.toString().endsWith(".jar"))
    		programPropsFilePath = Paths.get(gamePath.getParent().toString(), "settings");
    	else
    		programPropsFilePath = Paths.get(gamePath.toString(), "settings");
        programPropsFilePath = programPropsFilePath.resolve(PROGRAM_PROPS_FILE_NAME);
        System.out.println("    game config: " + programPropsFilePath.toString());
        
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
        
    	// Resources directory
        if (gamePath.toString().endsWith(".jar")) {
    		resourcePath = Paths.get(gamePath.getParent().toString(), "resources");
    	} else {
    		resourcePath = Paths.get(gamePath.toString(), "resources");
    	}
        System.out.println("      resourcePath: " + resourcePath.toString());
        resourceTree = new CaseInsensitiveFileTree(resourcePath);
        
        bilinear = programProps.getBoolean("bilinear", false);
        
        // Set options
        GameController.setOption(GameController.Option.MUSIC_ON, programProps.getBoolean("music", true));
        GameController.setOption(GameController.Option.SOUND_ON, programProps.getBoolean("sound", true));
        GameController.setMusicGain(programProps.getDouble("musicGain", 1.0));
        GameController.setSoundGain(programProps.getDouble("soundGain", 1.0));
        GameController.setOption(GameController.Option.ADVANCED_SELECT, programProps.getBoolean("advancedSelect", true));
        GameController.setOption(GameController.Option.CLASSIC_CURSOR, programProps.getBoolean("classicalCursor", false));
        GameController.setOption(GameController.Option.SWAP_BUTTONS, programProps.getBoolean("swapButtons", false));
        GameController.setOption(GameController.Option.FASTER_FAST_FORWARD, programProps.getBoolean("fasterFastForward", false));
        GameController.setOption(GameController.Option.PAUSE_STOPS_FAST_FORWARD, programProps.getBoolean("pauseStopsFastForward", true));
        GameController.setOption(GameController.Option.NO_PERCENTAGES, programProps.getBoolean("noPercentages", true));
        GameController.setOption(GameController.Option.REPLAY_SCROLL, programProps.getBoolean("replayScroll", true));
        GameController.setOption(GameController.Option.UNPAUSE_ON_ASSIGNMENT, programProps.getBoolean("unpauseOnAssignment", true));
        // Settings added in SuperLemminiToo
        GameController.setOption(GameController.SLTooOption.TIMED_BOMBERS, programProps.getBoolean("timedBombers", true));
        GameController.setOption(GameController.SLTooOption.UNLOCK_ALL_LEVELS, programProps.getBoolean("unlockAllLevels", true));
        GameController.setOption(GameController.SLTooOption.DISABLE_SCROLL_WHEEL, programProps.getBoolean("disableScrollWheel", true));
        GameController.setOption(GameController.SLTooOption.DISABLE_FRAME_STEPPING, programProps.getBoolean("disableFrameStepping", true));
        GameController.setOption(GameController.SLTooOption.VISUAL_SFX, programProps.getBoolean("visualSFX", true));
        GameController.setOption(GameController.SLTooOption.ENHANCED_STATUS, programProps.getBoolean("enhancedStatus", true));
        GameController.setOption(GameController.SLTooOption.SHOW_STATUS_TOTALS, programProps.getBoolean("showStatusTotals", true));
        GameController.setOption(GameController.SLTooOption.SHOW_LEVEL_NAME, programProps.getBoolean("showLevelName", true));
        GameController.setOption(GameController.SLTooOption.ENHANCED_ICONBAR, programProps.getBoolean("enhancedIconBar", true));
        GameController.setOption(GameController.SLTooOption.ICON_LABELS, programProps.getBoolean("iconLabels", false));
        GameController.setOption(GameController.SLTooOption.ANIMATED_ICONS, programProps.getBoolean("animatedIcons", true));
        GameController.setOption(GameController.SLTooOption.CLASSIC_SCROLLER, programProps.getBoolean("classicScroller", true));
        GameController.setOption(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD, programProps.getBoolean("debugVerbosePlayerLoad", false));
        // Settings added in RetroLemmini
        GameController.setOption(GameController.RetroLemminiOption.AUTOSAVE_REPLAYS, programProps.getBoolean("autoSaveReplays", false));
        GameController.setOption(GameController.RetroLemminiOption.SHOW_MENU_BAR, programProps.getBoolean("showMenuBar", true));
        GameController.setOption(GameController.RetroLemminiOption.FULL_SCREEN, programProps.getBoolean("fullScreen", false));
        // Exit sound settings
        GameController.setExitSoundOption(ExitSoundOption.valueOf(programProps.get("exitSound", "AUTO")));

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
        
        System.out.println("Core initialization complete.");
        return true;
    }
    
    /***
     *  Reads all player settings from the players.ini file
     */
    private static void loadPlayerSettings() {
        // read player names
        playerProps = new Props();
        playerProps.load(PLAYER_PROPS_FILE_NAME);
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
    	programProps.setBoolean("music", GameController.isOptionEnabled(GameController.Option.MUSIC_ON));
        programProps.setBoolean("sound", GameController.isOptionEnabled(GameController.Option.SOUND_ON));
        programProps.set("mixerName", GameController.sound.getMixers()[GameController.sound.getMixerIdx()]);
        //graphic settings
        programProps.setBoolean("bilinear", Core.isBilinear());
        //misc settings
        programProps.setBoolean("advancedSelect", GameController.isOptionEnabled(GameController.Option.ADVANCED_SELECT));
        programProps.setBoolean("classicalCursor", GameController.isOptionEnabled(GameController.Option.CLASSIC_CURSOR));
        programProps.setBoolean("swapButtons", GameController.isOptionEnabled(GameController.Option.SWAP_BUTTONS));
        programProps.setBoolean("fasterFastForward", GameController.isOptionEnabled(GameController.Option.FASTER_FAST_FORWARD));
        programProps.setBoolean("pauseStopsFastForward", GameController.isOptionEnabled(GameController.Option.PAUSE_STOPS_FAST_FORWARD));
        programProps.setBoolean("noPercentages", GameController.isOptionEnabled(GameController.Option.NO_PERCENTAGES));
        programProps.setBoolean("replayScroll", GameController.isOptionEnabled(GameController.Option.REPLAY_SCROLL));
        programProps.setBoolean("unpauseOnAssignment", GameController.isOptionEnabled(GameController.Option.UNPAUSE_ON_ASSIGNMENT));
        // Settings added in SuperLemminiToo
        programProps.setBoolean("timedBombers", GameController.isOptionEnabled(GameController.SLTooOption.TIMED_BOMBERS));
        programProps.setBoolean("unlockAllLevels", GameController.isOptionEnabled(GameController.SLTooOption.UNLOCK_ALL_LEVELS));
        programProps.setBoolean("disableScrollWheel", GameController.isOptionEnabled(GameController.SLTooOption.DISABLE_SCROLL_WHEEL));
        programProps.setBoolean("disableFrameStepping", GameController.isOptionEnabled(GameController.SLTooOption.DISABLE_FRAME_STEPPING));
        programProps.setBoolean("visualSFX", GameController.isOptionEnabled(GameController.SLTooOption.VISUAL_SFX));
        programProps.setBoolean("enhancedStatus", GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_STATUS));
        programProps.setBoolean("showStatusTotals", GameController.isOptionEnabled(GameController.SLTooOption.SHOW_STATUS_TOTALS));
        programProps.setBoolean("showLevelName", GameController.isOptionEnabled(GameController.SLTooOption.SHOW_LEVEL_NAME));
        programProps.setBoolean("enhancedIconBar", GameController.isOptionEnabled(GameController.SLTooOption.ENHANCED_ICONBAR));
        programProps.setBoolean("iconLabels", GameController.isOptionEnabled(GameController.SLTooOption.ICON_LABELS));
        programProps.setBoolean("animatedIcons", GameController.isOptionEnabled(GameController.SLTooOption.ANIMATED_ICONS));
        programProps.setBoolean("classicScroller", GameController.isOptionEnabled(GameController.SLTooOption.CLASSIC_SCROLLER));
        programProps.setBoolean("debugVerbosePlayerLoad", GameController.isOptionEnabled(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD));
        // Settings added in RetroLemmini
        programProps.setBoolean("autoSaveReplays", GameController.isOptionEnabled(GameController.RetroLemminiOption.AUTOSAVE_REPLAYS));
        programProps.setBoolean("showMenuBar", GameController.isOptionEnabled(GameController.RetroLemminiOption.SHOW_MENU_BAR));
        programProps.setBoolean("fullScreen", GameController.isOptionEnabled(GameController.RetroLemminiOption.FULL_SCREEN));
        // Exit sound
        programProps.set("exitSound", GameController.getExitSoundOption().name());
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
     * @param fname file name (without resource path)
     * @param searchMods
     * @return resource object
     * @throws ResourceException if file is not found
     */
    public static Resource findResource(String fname, boolean searchMods) throws ResourceException {
        String originalExt = FilenameUtils.getExtension(fname);
        return findResource(fname, searchMods, true, originalExt);
    }
    
    /**
     * Get Path to resource in resource path (searches mods and main file by default).
     * @param fname file name (without resource path)
     * @param extensions 
     * @return resource object
     * @throws ResourceException if file is not found
     */
    public static Resource findResource(String fname, String... extensions) throws ResourceException {
        return findResource(fname, true, true, extensions);
    }
    
    /**
     * Get Path to resource in resource path (searches main file by default).
     * @param fname file name (without resource path)
     * @param searchMods are mods included in the search?
     * @param extensions 
     * @return resource object
     * @throws ResourceException if file is not found
     */
    public static Resource findResource(String fname, boolean searchMods, String... extensions) throws ResourceException {
        return findResource(fname, searchMods, true, extensions);
    }

    /**
     * Get Path to resource in resource path.
     * @param fname file name (without resource path)
     * @param searchMods are mods included in the search?
     * @param searchMain is the main folder included in the search?
     * @param extensions 
     * @return resource object
     * @throws ResourceException if file is not found
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
     * @param fname file name (without resource path)
     * @param searchMods
     * @param extensions 
     * @return resource object
     * @throws ResourceException if file is not found
     */
    public static Resource findResourceEx(String fname, boolean searchMods, boolean searchMain, String... extensions) throws ResourceException {
        String fnameNoExt = FilenameUtils.removeExtension(fname);
        if (searchMods) {
            // try to load the file from the mod paths with each extension
            for (String mod : GameController.getModPaths()) {
                for (String ext : extensions) {
                    String resString = mod + "/" + fnameNoExt + "." + ext;
                    if (resourceTree.exists(resString)) {
                        return new FileResource(fname, resString, resourceTree);
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
            GameController.getModPaths().stream().forEachOrdered(mod -> {
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
     * @param title
     */
    public static void setTitle(String title) {
        LemminiFrame.getFrame().setTitle(title);
    }
    
    /**
     * Store program properties.
     */
    public static void saveProgramProps() {
        programProps.save(programPropsFilePath);
        playerProps.set("defaultPlayer", player.getName());
        playerProps.save(PLAYER_PROPS_FILE_NAME);
        player.store();
    }
    
    /**
     * Output error message box in case of a missing resource.
     * @param rsrc name of missing resource.
     */
    public static void resourceError(final String rsrc) {
        String out = String.format("The resource %s is missing.%n", rsrc);
        JOptionPane.showMessageDialog(null, out, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    /**
     * Adds the given image to the given tracker.
     * @param tracker media tracker
     * @param image image to add
     * @return given image if operation was successful; null otherwise
     * @throws ResourceException
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
     * @return Image
     * @throws ResourceException
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
     * @param fname
     * @return Image
     * @throws ResourceException
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
     * @param idx player index
     * @return player name
     */
    public static String getPlayer(final int idx) {
        return players.get(idx);
    }
    
    /**
     * Get number of players.
     * @return number of player.
     */
    public static int getPlayerCount() {
        if (players == null) {
            return 0;
        }
        return players.size();
    }
    
    /**
     * Delete a player.
     * @param idx index of player to delete
     */
    public static void deletePlayer(final int idx) {
        Player.deletePlayerINIFile(players.get(idx));
        players.remove(idx);
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
     * @param name player name
     */
    public static void addPlayer(final String name) {
        players.add(name);
        playerProps.set("player_" + (players.size() - 1), name);
    }
    
    /**
     * Get internal draw width
     * @return internal draw width
     */
    public static int getDrawWidth() {
        return drawWidth;
    }
    
    /**
     * Get scaled internal draw width
     * @return scaled internal draw width
     */
    public static int getScaledDrawWidth() {
        return (scale == 1.0) ? drawWidth : (int) Math.ceil(drawWidth * scale);
    }
    
    /**
     * Get internal draw height
     * @return internal draw width
     */
    public static int getDrawHeight() {
        return drawHeight;
    }
    
    /**
     * Get scaled internal draw height
     * @return scaled internal draw width
     */
    public static int getScaledDrawHeight() {
        return (scale == 1.0) ? drawHeight : (int) Math.ceil(drawHeight * scale);
    }
    
    /**
     * Set internal draw size
     * @param w draw width
     * @param h draw height
     */
    public static void setDrawSize(int w, int h) {
        drawWidth = w;
        drawHeight = h;
    }
    
    /**
     * Get zoom scale
     * @return zoom scale
     */
    public static double getScale() {
        return scale;
    }
    
    /**
     * Set zoom scale
     * @param s zoom scale
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
}
