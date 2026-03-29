package lemmini.game;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

//import lemmini.game.PlayerRecords.GroupRecord;
//import lemmini.game.PlayerRecords.PackRecord;
import lemmini.tools.Props;
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
 * Stores player progress.
 * @author Volker Oth
 * Modified by Ryan Sakowski
 */
public class Player {

    /** property class to store player settings persistently */
    private Props props;
    
    /** stores player progress */
    private Map<String, PlayerProgress> playerProgress;
    
    /** debug mode enabled? */
    private boolean debugMode;
    /** maximum exit physics enabled? */
    private boolean maximumExitPhysics;
    /** player's name */
    private String name;

    /**
     * Constructor.
     */
    public Player(final String n) {
        System.out.println("    initalizing player: " + n);
        name = n;
        playerProgress = new LinkedHashMap<>();
        props = new Props();
        Path iniFilePath = getPlayerINIFilePath(name);
        
        System.out.println("    loading player level stats: " + iniFilePath);
        if (props.load(iniFilePath)) {
        	loadLegacyPlayerRecords();
            migrateLegacyRecords();
            loadPlayerRecords();
        } else {
            System.out.println("    ini file not found... new one created.");
        }

        // debug mode
        debugMode = false;

        System.out.println();
    }

    /**
     * Set debug mode for this player.
     */
    public void setDebugMode(final boolean d) {
        debugMode = d;
    }

    /**
     * Set max exit physics mode for this player.
     */
    public void setMaximumExitPhysics(final boolean e) {
        maximumExitPhysics = e;
    }
    
    private boolean backupPlayerProgress(Path ini) {
    	if (!Files.exists(ini)) return true;
	    Path backup = ini.resolveSibling(ini.getFileName() + ".bak");
	    try {
	        Files.copy(ini, backup, StandardCopyOption.REPLACE_EXISTING);
	        return true;
	    } catch (IOException e) {
	    	System.err.println("Failed to back up player INI: " + e.getMessage());
	        return false;
	    }
    }
    
    /**
     * Load legacy player records from INI.
     */
    private void loadLegacyPlayerRecords() {
        for (int idx = 0; true; idx++) {
            String[] s = props.getArray("group" + idx, null);
            if (s == null || s.length < 2 || s[0] == null) break;
            
            String s1 = s[s.length-1];
            String groupName = "";
            for (int a = 0; a < s.length-1; a++) {
                groupName += s[a];
                if (a < s.length-2 )
                    groupName +=", ";
            }
            BigInteger unlockedLevels = ToolBox.parseBigInteger(s1);
            Map<Integer, LevelRecord> levelRecords = new LinkedHashMap<>();
            int maxLevel = props.getHighestLevel(idx) + 1;
            maxLevel = Math.max(maxLevel, unlockedLevels.bitLength());
            for (int j = 0; j < maxLevel; j++) {
                String levelSetting = "group" + idx + "_level" + j;
                String completedKey = levelSetting + "_completed";
                // BOOKMARK TODO: check if we're on the last level, and there is no compKey...
                // props.containsKey(completedKey);
                boolean completed = props.getBoolean(completedKey, false);
                if (completed) {
                    int lemmingsSaved = props.getInt(levelSetting + "_lemmingsSaved", -1);
                    int skillsUsed = props.getInt(levelSetting + "_skillsUsed", -1);
                    int timeElapsed = props.getInt(levelSetting + "_timeElapsed", -1);
                    int score = props.getInt(levelSetting + "_score", -1);
                    levelRecords.put(j, new LevelRecord(completed, lemmingsSaved, skillsUsed, timeElapsed, score));
                } else {
                    levelRecords.put(j, LevelRecord.BLANK_LEVEL_RECORD);
                }
            }
            int legacyName = groupName.lastIndexOf('-');
            String pack = groupName.substring(0, legacyName).trim();
            String rating = groupName.substring(legacyName + 1).trim();
            playerProgress.put(LevelPack.getID(pack, rating), new PlayerProgress(levelRecords));
        }
    }
    
    private void migrateLegacyRecords() {
    	Path ini = getPlayerINIFilePath(name);
    	if (!backupPlayerProgress(ini))
    		return; // abort migration silently if backup fails; new format will be appended (or original INI will remain as-is)
    	
        boolean hasLegacyKeys = props.keySet().stream().anyMatch(k -> k.startsWith("group"));
        if (!hasLegacyKeys) return;

        System.out.println("      migrating legacy player records to new format...");
	
	//      try {
	          // remove legacy keys & save without reloading
	        props.eraseKeysStartingWith("group");
	        props.save(ini, false);
	
	          // immediately save new-format records
	        storePlayerRecords();
	
	//          // all successful - backup can be deleted
	//          Files.deleteIfExists(backup);
	//      } catch (IOException e) {
	//          return; // fail silently
	//      }
	      
	    System.out.println("      migration to PlayerRecords complete");
    }
    
    /**
     * Load player records from INI.
     */
    public void loadPlayerRecords() {
        for (String key : props.keySet()) {
            if (!key.startsWith("pack.")) continue;

            String[] parts = key.split("\\.");
            if (parts.length < 5) continue;

            String pack = parts[1];
            String rating = parts[2];
            int levelNum = Integer.parseInt(parts[3].replace("level", ""));
            String field = parts[4];

            String id = LevelPack.getID(pack, rating);
            PlayerProgress prog = playerProgress.computeIfAbsent(id, k -> new PlayerProgress());
            LevelRecord oldRecord = prog.records.getOrDefault(levelNum, LevelRecord.BLANK_LEVEL_RECORD);

            boolean completed = oldRecord.isCompleted();
            int lemmingsSaved = oldRecord.getLemmingsSaved();
            int skillsUsed = oldRecord.getSkillsUsed();
            int timeElapsed = oldRecord.getTimeElapsed();
            int score = oldRecord.getScore();

            if ("completed".equals(field))
                completed = props.getBoolean(key, false);
            else if ("lemmingsSaved".equals(field))
                lemmingsSaved = props.getInt(key, -1);
            else if ("skillsUsed".equals(field))
                skillsUsed = props.getInt(key, -1);
            else if ("timeElapsed".equals(field))
                timeElapsed = props.getInt(key, -1);
            else if ("score".equals(field))
                score = props.getInt(key, -1);

            prog.records.put(levelNum, new LevelRecord(completed, lemmingsSaved, skillsUsed, timeElapsed, score));
        }
    }

    /**
     * Store player's progress.
     */
    public void storePlayerRecords() {
        for (Map.Entry<String, PlayerProgress> entry : playerProgress.entrySet()) {
            String groupId = entry.getKey();
            PlayerProgress prog = entry.getValue();

            for (Map.Entry<Integer, LevelRecord> levelEntry : prog.records.entrySet()) {
                int lvlNum = levelEntry.getKey();
                LevelRecord lr = levelEntry.getValue();

                String prefix = "pack." + groupId + ".level" + lvlNum + ".";
                props.setBoolean(prefix + "completed", lr.isCompleted());
                if (lr.isCompleted()) {
                    props.setInt(prefix + "lemmingsSaved", lr.getLemmingsSaved());
                    props.setInt(prefix + "skillsUsed", lr.getSkillsUsed());
                    props.setInt(prefix + "timeElapsed", lr.getTimeElapsed());
                    props.setInt(prefix + "score", lr.getScore());
                }
            }
        }
        props.save(getPlayerINIFilePath(name), true);
    }
    
	/**
	 * Allow a level to be played.
	 */
    public void setAvailable(final String pack, final String rating, final int num) {
    	PlayerProgress prog = playerProgress.computeIfAbsent(LevelPack.getID(pack, rating), k -> new PlayerProgress(new LinkedHashMap<>()));
    }


    /**
     * Check if a player is allowed to play a level.
     */
    public boolean isAvailable(final String pack, final String rating, final int num) {
        if (LemGame.isOptionEnabled(LemGame.Option.UNLOCK_ALL_LEVELS) || isDebugMode()) {
            return true;
        }

        String id = LevelPack.getID(pack, rating);
        PlayerProgress prog = playerProgress.get(id);

        if (prog == null) {
            return num == 0;
        }

        // First level is always available
        if (num == 0) {
            return true;
        }

        // If this level is completed, it's always available
        LevelRecord thisRecord = prog.records.get(num);
        if (thisRecord != null && thisRecord.isCompleted()) {
            return true;
        }

        // Otherwise, previous level must be completed
        LevelRecord prevRecord = prog.records.get(num - 1);
        return prevRecord != null && prevRecord.isCompleted();
    }

	/**
	 * Store a completed level record.
	 */
    public void setLevelRecord(final String pack, final String rating, final int num, final LevelRecord record) {
        if (!record.isCompleted()) return; // only store completed levels
        PlayerProgress prog = playerProgress.computeIfAbsent(LevelPack.getID(pack, rating), k -> new PlayerProgress());

        // Merge with existing record if it exists
        LevelRecord oldRecord = prog.records.get(num);
        if (oldRecord != null) {
        	prog.records.put(num, new LevelRecord(
                true,
                Math.max(oldRecord.getLemmingsSaved(), record.getLemmingsSaved()),
                Math.min(oldRecord.getSkillsUsed(), record.getSkillsUsed()),
                Math.min(oldRecord.getTimeElapsed(), record.getTimeElapsed()),
                Math.max(oldRecord.getScore(), record.getScore())
            ));
        } else {
        	prog.records.put(num, record);
        }
    }
    
	/**
	 * Retrieve a completed level record.
	 */
    public LevelRecord getLevelRecord(final String pack, final String rating, final int num) {
    	String id = LevelPack.getID(pack, rating);
    	PlayerProgress prog = playerProgress.get(id);
        if (prog == null) {
            return LevelRecord.BLANK_LEVEL_RECORD;
        }
        if (!prog.records.containsKey(num)) {
            return LevelRecord.BLANK_LEVEL_RECORD;
        }
        return prog.records.get(num);
    }

    /**
     * Get player's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get debug mode state.
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Get maximum exit physics state.
     */
    public boolean isMaximumExitPhysics() {
        return maximumExitPhysics;
    }

    public static Path getPlayerINIFilePath(final String name) {
        Path retFile = Core.settingsTree.getPath("players/" + addEscapes(name) + ".ini");
        if (Files.notExists(retFile)) {
            for (Path file : Core.settingsTree.getAllPathsRegex("players/[^/]+\\.ini")) {
                String fileName = FilenameUtils.removeExtension(file.getFileName().toString());
                String convertedFileName = Player.convertEscapes(fileName);
                if (convertedFileName.equals(name)) {
                    retFile = file;
                    break;
                }
            }
        }

        return retFile;
    }

    public static void deletePlayerINIFile(final String name) {
        Core.resourceTree.getAllPathsRegex("players/[^/]+\\.ini").stream()
                .map(file -> FilenameUtils.removeExtension(file.getFileName().toString()))
                .forEach(fileName -> {
            String convertedFileName = Player.convertEscapes(fileName);
            if (convertedFileName.equals(name)) {
                try {
                    Core.resourceTree.delete("players/" + fileName + ".ini");
                } catch (IOException ex) {
                }
            }
        });
    }

    /**
     * Converts certain characters and names to escape sequences to ensure
     * compatibility with various file systems.
     */
    public static String addEscapes(final String s) {
        int length = s.length();
        StringBuilder sb = null;
        boolean convertAllChars;
        switch (s.toLowerCase(Locale.ROOT)) {
            // The file names below are illegal in Windows file systems, so
            // escape every character if the player name matches any of these.
            case "com0":
            case "com1":
            case "com2":
            case "com3":
            case "com4":
            case "com5":
            case "com6":
            case "com7":
            case "com8":
            case "com9":
            case "lpt0":
            case "lpt1":
            case "lpt2":
            case "lpt3":
            case "lpt4":
            case "lpt5":
            case "lpt6":
            case "lpt7":
            case "lpt8":
            case "lpt9":
            case "aux":
            case "con":
            case "nul":
            case "prn":
                convertAllChars = true;
                break;
            default:
                convertAllChars = false;
                break;
        }
        for (int c, i = 0; i < length; i += Character.charCount(c)) {
            c = s.codePointAt(i);
            boolean convertChar;
            if (convertAllChars) {
                convertChar = true;
            } else {
                switch (c) {
                    // Escape these characters since they're illegal in
                    // Windows file names.
                    case '"':
                    case '*':
                    case '/': // also illegal in UNIX file names
                    case ':': // also illegal in Mac file names
                    case '<':
                    case '>':
                    case '?':
                    case '\\':
                    case '_': // legal in file names but used as escape character
                    case '|':
                        convertChar = true;
                        break;
                    default:
                        // Escape any character outside the printable ASCII
                        // range because not all systems and file systems
                        // support Unicode. Also escape the first character
                        // if it's a period because UNIX file systems do not
                        // allow initial periods.
                        convertChar = c < ' ' || c > '~' || (i == 0 && c == '.');
                        break;
                }
            }
            if (convertChar) {
                if (sb == null) {
                    sb = new StringBuilder(length * 5);
                    sb.append(s.substring(0, i));
                }
                if (Character.isBmpCodePoint(c)) {
                    sb.append(String.format(Locale.ROOT, "_%04x", c));
                } else {
                    sb.append(String.format(Locale.ROOT, "__%06x", c));
                }
            } else {
                if (sb != null) {
                    sb.appendCodePoint(c);
                }
            }
        }
        return (sb == null) ? s : sb.toString();
    }

    /**
     * Converts every instance of _xxxx or __xxxxxx (where x is a hex digit) to
     * the corresponding Unicode code point.
     */
    public static String convertEscapes(final String s) {
        int length = s.length();
        StringBuilder sb = null;
        for (int i = 0; i < length; i++) {
            if (i < s.length() - 4 && s.charAt(i) == '_'
                    && ToolBox.isHexDigit(s.charAt(i + 1))
                    && ToolBox.isHexDigit(s.charAt(i + 2))
                    && ToolBox.isHexDigit(s.charAt(i + 3))
                    && ToolBox.isHexDigit(s.charAt(i + 4))) {
                if (sb == null) {
                    sb = new StringBuilder(length);
                    sb.append(s.substring(0, i));
                }
                sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                i += 4;
            } else if (i < s.length() - 7 && s.charAt(i) == '_' && s.charAt(i + 1) == '_'
                    && ToolBox.isHexDigit(s.charAt(i + 2))
                    && ToolBox.isHexDigit(s.charAt(i + 3))
                    && ToolBox.isHexDigit(s.charAt(i + 4))
                    && ToolBox.isHexDigit(s.charAt(i + 5))
                    && ToolBox.isHexDigit(s.charAt(i + 6))
                    && ToolBox.isHexDigit(s.charAt(i + 7))) {
                if (sb == null) {
                    sb = new StringBuilder(length);
                    sb.append(s.substring(0, i));
                }
                sb.appendCodePoint(Integer.parseInt(s.substring(i + 2, i + 8), 16));
                i += 7;
            } else {
                if (sb != null) {
                    sb.append(s.charAt(i));
                }
            }
        }
        return (sb == null) ? s : sb.toString();
    }

    private class PlayerProgress {
        private final Map<Integer, LevelRecord> records;

        // Constructor
        private PlayerProgress(Map<Integer, LevelRecord> records) {
            this.records = records;
        }

        // Convenience constructor for blank records
        private PlayerProgress() {
            this(new LinkedHashMap<>());
        }
    }
}