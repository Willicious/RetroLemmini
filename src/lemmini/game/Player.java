package lemmini.game;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

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
 * Stores player progress.
 * @author Volker Oth
 */
public class Player {

    /** property class to store player settings persistently */
    private Props props;
    /** used to store level progress */
    private Map<String, LevelGroup> lvlGroups;
    /** debug mode enabled? */
    private boolean debugMode;
    /** maximum exit physics enabled? */
    private boolean maximumExitPhysics;
    /** player's name */
    private String name;

    /**
     * Constructor.
     * @param n player's name
     */
    public Player(final String n) {
        System.out.println("    initalizing player: " + n);

        name = n;
        lvlGroups = new LinkedHashMap<>();
        // read main ini file
        props = new Props();
        Path iniFilePath = getPlayerINIFilePath(name);
        System.out.println("    loading player level stats: " + iniFilePath);

        if (props.load(iniFilePath)) { // might exist or not - if not, it's created
            // file exists, now parse entries
            for (int idx = 0; true; idx++) {
                System.out.print("    loading level group " + idx);
                // first string is the level group key identifier
                // second string is a BigInteger used as bitfield to store available levels
                String[] s = props.getArray("group" + idx, null);
                if (s == null || s.length < 2 || s[0] == null) {
                    System.out.println(": <no valid group data found. skipping...>");
                    break;
                }
                String s1 = s[s.length-1]; // get the last
                String groupName = "";
                // due to a bug in the ini property "Array" saving, commas are not escaped, and so they're treated as you would any column separator
                // because the getArray function above trims out any spaces, knowledge if there was or was not a space after a comma is lost
                // so let's just assume there's always a space after it, because that's better grammar.
                for (int a = 0; a < s.length-1; a++) {
                    groupName += s[a];
                    if (a < s.length-2 ) {
                        groupName +=", "; //we're assuming there's always a space after any comma
                    }
                }
                System.out.print(": �" + groupName + "�");

                // note: unlockedLevels are stored as bits. a bit 1 indicates the level is unlocked; bit 0 indicates the level is locked.
                // unlocked means it is either completed, or is after a group of uncompleted levels.
                BigInteger unlockedLevels = ToolBox.parseBigInteger(s1);
                System.out.println("  [" + unlockedLevels.bitLength() + ":" + unlockedLevels.toString(16) + "]");
                Map<Integer, LevelRecord> levelRecords = new LinkedHashMap<>();


                System.out.print("    building level stats map...");

                if (GameController.isOptionEnabled(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD))
                    System.out.println();

                int compCount = 0;

                int maxLevel = props.getHighestLevel(idx) + 1;
                maxLevel = Math.max(maxLevel, unlockedLevels.bitLength());
                for (int j = 0; j < maxLevel; j++) {
                    if (GameController.isOptionEnabled(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD))
                        System.out.print("     level " + j + ": ");

                    String levelSetting = "group" + idx + "_level" + j;
                    String completedKey = levelSetting + "_completed";
                    // BOOKMARK TODO: check if we're on the last level, and there is no compKey...
                    // props.containsKey(completedKey);
                    boolean completed = props.getBoolean(completedKey, false);
                    if (completed) {
                        compCount++;
                        int lemmingsSaved = props.getInt(levelSetting + "_lemmingsSaved", -1);
                        int skillsUsed = props.getInt(levelSetting + "_skillsUsed", -1);
                        int timeElapsed = props.getInt(levelSetting + "_timeElapsed", -1);
                        int score = props.getInt(levelSetting + "_score", -1);
                        levelRecords.put(j, new LevelRecord(completed, lemmingsSaved, skillsUsed, timeElapsed, score));
                        if (GameController.isOptionEnabled(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD)) {
                            // BOOKMARK TODO: ALso show if a level has been attempted?
                            System.out.print("[completed]");
                            System.out.print(" saved: " + lemmingsSaved);
                            System.out.print(" skills: " + skillsUsed);
                            System.out.print(" time: " + timeElapsed);
                            System.out.print(" score: " + score);
                        }
                    } else {
                        levelRecords.put(j, LevelRecord.BLANK_LEVEL_RECORD);
                        if (GameController.isOptionEnabled(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD))
                            System.out.print("[incomplete] ... creating blank records.");
                    }
                    if (GameController.isOptionEnabled(GameController.SLTooOption.DEBUG_VERBOSE_PLAYER_LOAD))
                        System.out.println();
                }
                lvlGroups.put(groupName, new LevelGroup(levelRecords));
                //and finally print out the summary of this level.
                //this gets printed regardless if verbose or not.
                System.out.println("     Highest recorded level: " + maxLevel + ", Total completed: " + compCount);
            }
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
     * Store player's progress.
     */
    public void store() {
        Set<String> groupKeys = lvlGroups.keySet();
        int idx = 0;
        for (String s : groupKeys) {
            LevelGroup lg = lvlGroups.get(s);
            String sout = s + ", " + lg.getBitField();
            props.set("group" + idx, sout);
            Set<Integer> availableLevels = lg.levelRecords.keySet();
            for (Integer lvlNum : availableLevels) {
                LevelRecord lr = lg.levelRecords.get(lvlNum);
                if (lr.isCompleted()) {
                    props.setBoolean("group" + idx + "_level" + lvlNum + "_completed", true);
                    props.setInt("group" + idx + "_level" + lvlNum + "_lemmingsSaved", lr.getLemmingsSaved());
                    props.setInt("group" + idx + "_level" + lvlNum + "_skillsUsed", lr.getSkillsUsed());
                    props.setInt("group" + idx + "_level" + lvlNum + "_timeElapsed", lr.getTimeElapsed());
                    props.setInt("group" + idx + "_level" + lvlNum + "_score", lr.getScore());
                }
            }
            idx++;
        }
        props.save(getPlayerINIFilePath(name), true);
    }

    /**
     * Allow a level to be played.
     * @param pack level pack
     * @param rating rating
     * @param num level number
     */
    public void setAvailable(final String pack, final String rating, final int num) {
        String id = LevelPack.getID(pack, rating);
        LevelGroup lg = lvlGroups.get(id);
        if (lg == null) {
            // first level is always available
            Map<Integer, LevelRecord> records = new LinkedHashMap<>();
            records.put(0, LevelRecord.BLANK_LEVEL_RECORD);
            lg = new LevelGroup(records);
            lvlGroups.put(id, lg);
        }
        if (!lg.levelRecords.containsKey(num)) {
            // add level record to level group
            lg.levelRecords.put(num, LevelRecord.BLANK_LEVEL_RECORD);
        }
    }

    /**
     * Check if player is allowed to play a level.
     * @param pack level pack
     * @param rating rating
     * @param num level number
     * @return true if allowed, false if not
     */
    public boolean isAvailable(final String pack, final String rating, final int num) {
        if (GameController.isOptionEnabled(GameController.SLTooOption.UNLOCK_ALL_LEVELS) || isDebugMode()) {
            return true;
        }
        String id = LevelPack.getID(pack, rating);
        LevelGroup lg = lvlGroups.get(id);
        if (lg == null) {
            return num == 0; // first level is always available
        }
        return (lg.levelRecords.containsKey(num));
    }

    public void setLevelRecord(final String pack, final String rating, final int num, final LevelRecord record) {
        String id = LevelPack.getID(pack, rating);
        LevelGroup lg = lvlGroups.get(id);
        if (lg == null) {
            // first level is always available
            Map<Integer, LevelRecord> records = new LinkedHashMap<>();
            records.put(0, LevelRecord.BLANK_LEVEL_RECORD);
            lg = new LevelGroup(records);
            lvlGroups.put(id, lg);
        }
        // Only store the record if the level is completed
        if (record.isCompleted()) {
            LevelRecord oldRecord = lg.levelRecords.get(num);
            // If there's an old record, merge the new record keeping the best result for each value
            if (oldRecord != null) {
                lg.levelRecords.put(num, new LevelRecord(
                        true,
                        Math.max(oldRecord.getLemmingsSaved(), record.getLemmingsSaved()),
                        Math.min(oldRecord.getSkillsUsed(), record.getSkillsUsed()),
                        Math.min(oldRecord.getTimeElapsed(), record.getTimeElapsed()),
                        Math.max(oldRecord.getScore(), record.getScore())));
            } else {
                // If no old record exists, store the new one
                lg.levelRecords.put(num, record);
            }
        }
    }

    public LevelRecord getLevelRecord(final String pack, final String rating, final int num) {
        String id = LevelPack.getID(pack, rating);
        LevelGroup lg = lvlGroups.get(id);
        if (lg == null || !lg.levelRecords.containsKey(num)) {
            return LevelRecord.BLANK_LEVEL_RECORD;
        } else {
            return lg.levelRecords.get(num);
        }
    }

    /**
     * Get player's name.
     * @return player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get debug mode state.
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Get maximum exit physics state.
     * @return true if maximum exit physics is enabled
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
     * @param s
     * @return
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
     * @param s
     * @return
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

        private BigInteger getBitField() {
            Set<Integer> availableLevels = levelRecords.keySet();
            BigInteger bf = BigInteger.ZERO;
            for (Integer lvlNum : availableLevels) {
                bf = bf.setBit(lvlNum);
            }
            return bf;
        }
    }
}