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

import lemmini.game.PlayerRecords.GroupRecord;
import lemmini.game.PlayerRecords.PackRecord;
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
    
    /** LEGACY: stores player progress */
    private Map<String, LevelGroup> lvlGroups;
    /** NEW: stores player progress */
    private PlayerRecords records = new PlayerRecords();
    
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
        lvlGroups = new LinkedHashMap<>();
        props = new Props();
        Path iniFilePath = getPlayerINIFilePath(name);
        
        System.out.println("    loading player level stats: " + iniFilePath);
        if (props.load(iniFilePath)) {
        	loadLegacyPlayerRecords(); // BOOKMARK TODO
            migrateRecords();          // These can eventually be removed altogether
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
            lvlGroups.put(groupName, new LevelGroup(levelRecords));
        }
    }
    
    /**
     * Migrate existing lvlGroups data into PlayerRecords.
     */
    private void migrateRecords() {
        if (lvlGroups == null || lvlGroups.isEmpty()) return;
        
        System.out.println("      migrating player records from lvlGroups to PlayerRecords...");
        for (Map.Entry<String, LevelGroup> groupEntry : lvlGroups.entrySet()) {
            String groupName = groupEntry.getKey();
            LevelGroup oldGroup = groupEntry.getValue();
            String pack, rating;
            
            int idx = groupName.indexOf('-');
            if (idx != -1) {
                pack = groupName.substring(0, idx).trim();
                rating = groupName.substring(idx + 1).trim();
            } else {
                pack = groupName;
                rating = "default";
            }
            
            PlayerRecords.PackRecord packRecord = records.getOrCreatePack(pack);
            PlayerRecords.GroupRecord groupRecord = packRecord.getOrCreateGroup(rating);

            for (Map.Entry<Integer, LevelRecord> levelEntry : oldGroup.levelRecords.entrySet()) {
                int levelNum = levelEntry.getKey();
                LevelRecord record = levelEntry.getValue();
                if (record.isCompleted()) {
                    groupRecord.setLevelRecord(levelNum, record);
                }
            }
        }
        // create backup
        Path ini = getPlayerINIFilePath(name);
        Path backup = ini.resolveSibling(ini.getFileName() + ".bak");
        try {
            Files.copy(ini, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return; // abort migration silently, new format will be appended (or original INI will remain as-is)
        }

//        try {
            // remove legacy keys & save without reloading
            props.eraseKeysStartingWith("group");
            props.save(ini, false);

            // immediately save new-format records
            storePlayerRecords();

//            // all successful - backup can be deleted
//            Files.deleteIfExists(backup);
//        } catch (IOException e) {
//            return; // fail silently
//        }
        
        System.out.println("      migration to PlayerRecords complete");
    }
    
    /**
     * Load player records from INI.
     */
    public void loadPlayerRecords() {
        if (!props.load(getPlayerINIFilePath(name))) return;

        for (String key : props.keySet()) {
            if (!key.startsWith("pack.")) continue;

            String[] parts = key.split("\\.");
            if (parts.length < 4) continue;

            String pack = parts[1].toLowerCase(Locale.ROOT);
            String rating = parts[2].toLowerCase(Locale.ROOT);
            String levelPart = parts[3];

            int levelNum;
            try {
                levelNum = Integer.parseInt(levelPart.substring(5));
            } catch (NumberFormatException e) {
                continue;
            }

            boolean completed = props.getBoolean(key, false);
            LevelRecord record = LevelRecord.BLANK_LEVEL_RECORD;
            if (completed) {
                int lemmingsSaved = props.getInt("pack." + pack + "." + rating + ".level" + levelNum + ".lemmingsSaved", 0);
                int skillsUsed = props.getInt("pack." + pack + "." + rating + ".level" + levelNum + ".skillsUsed", 0);
                int timeElapsed = props.getInt("pack." + pack + "." + rating + ".level" + levelNum + ".timeElapsed", 0);
                int score = props.getInt("pack." + pack + "." + rating + ".level" + levelNum + ".score", 0);
                record = new LevelRecord(true, lemmingsSaved, skillsUsed, timeElapsed, score);
            }

            records.getOrCreatePack(pack).getOrCreateGroup(rating).setLevelRecord(levelNum, record);
        }
    }

    /**
     * Store player's progress.
     */
    public void storePlayerRecords() {
        for (Map.Entry<String, PlayerRecords.PackRecord> packEntry : records.getPacks().entrySet()) {
            String pack = packEntry.getKey();
            PlayerRecords.PackRecord packRecord = packEntry.getValue();

            for (Map.Entry<String, PlayerRecords.GroupRecord> groupEntry : packRecord.getGroups().entrySet()) {
                String rating = groupEntry.getKey();
                PlayerRecords.GroupRecord groupRecord = groupEntry.getValue();

                String section = "pack." + pack + "." + rating;
                for (Map.Entry<Integer, LevelRecord> levelEntry : groupRecord.getLevels().entrySet()) {
                    int lvl = levelEntry.getKey();
                    LevelRecord rec = levelEntry.getValue();

                    String prefix = section + ".level" + lvl;
                    props.setBoolean(prefix + ".completed", rec.isCompleted());
                    if (rec.isCompleted()) {
                        props.setInt(prefix + ".lemmingsSaved", rec.getLemmingsSaved());
                        props.setInt(prefix + ".skillsUsed", rec.getSkillsUsed());
                        props.setInt(prefix + ".timeElapsed", rec.getTimeElapsed());
                        props.setInt(prefix + ".score", rec.getScore());
                    }
                }
            }
        }
        props.save(getPlayerINIFilePath(name), true);
    }

    /**
     * Allow a level to be played.
     */
    public void setAvailable(final String pack, final String rating, final int num) {
        PlayerRecords.PackRecord packRecord = records.getOrCreatePack(pack);
        PlayerRecords.GroupRecord groupRecord = packRecord.getOrCreateGroup(rating);

        if (!groupRecord.hasLevel(num)) {
            groupRecord.setLevelRecord(num, LevelRecord.BLANK_LEVEL_RECORD);
        }
    }

    /**
     * Check if a player is allowed to play a level.
     */
    public boolean isAvailable(final String pack, final String rating, final int num) {
        if (LemGame.isOptionEnabled(LemGame.Option.UNLOCK_ALL_LEVELS) || isDebugMode()) {
            return true;
        }
        PlayerRecords.PackRecord packRecord = records.getPack(pack.toLowerCase(Locale.ROOT));
        if (packRecord == null) {
            return num == 0;
        }
        PlayerRecords.GroupRecord groupRecord = packRecord.getGroup(rating.toLowerCase(Locale.ROOT));
        if (groupRecord == null) {
            return num == 0;
        }
        // first level is always available
        if (num == 0) return true;
        
        // If level is already completed, always consider it available
        LevelRecord thisRecord = groupRecord.getLevel(num);
        if (thisRecord != null && thisRecord.isCompleted()) return true;

        // previous level must be completed to unlock this one
        LevelRecord prevRecord = groupRecord.getLevel(num - 1);
        return prevRecord != null && prevRecord.isCompleted();
    }

    /**
     * Store a completed level record.
     */
    public void setLevelRecord(final String pack, final String rating, final int num, final LevelRecord record) {
        if (!record.isCompleted()) return;

        PlayerRecords.PackRecord packRecord = records.getOrCreatePack(pack);
        PlayerRecords.GroupRecord groupRecord = packRecord.getOrCreateGroup(rating);
        LevelRecord oldRecord = groupRecord.getLevel(num);

        if (oldRecord != null && oldRecord.isCompleted()) {
            groupRecord.setLevelRecord(num, new LevelRecord(
                true,
                Math.max(oldRecord.getLemmingsSaved(), record.getLemmingsSaved()),
                Math.min(oldRecord.getSkillsUsed(), record.getSkillsUsed()),
                Math.min(oldRecord.getTimeElapsed(), record.getTimeElapsed()),
                Math.max(oldRecord.getScore(), record.getScore())
            ));
        } else {
            groupRecord.setLevelRecord(num, record);
        }
    }
    
    public LevelRecord getLevelRecord(final String pack, final String rating, final int num) {  	
        PackRecord packRecord = records.getPack(pack.toLowerCase(Locale.ROOT));
        if (packRecord == null) {
            return LevelRecord.BLANK_LEVEL_RECORD;
        }
        GroupRecord groupRecord = packRecord.getGroup(rating.toLowerCase(Locale.ROOT));
        if (groupRecord == null) {
            return LevelRecord.BLANK_LEVEL_RECORD;
        }
        return groupRecord.getLevelOrDefault(num);
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

    private class LevelGroup {

        private final Map<Integer, LevelRecord> levelRecords;

        private LevelGroup(Map<Integer, LevelRecord> levelRecords) {
            this.levelRecords = levelRecords;
        }
    }
}