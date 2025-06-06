package lemmini.extract;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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
 * Convert binary "Lemmings for Win95" level files into text format.
 */
public class ExtractLevel {

    /** Scale (to convert lowres levels into hires levels) */
    private static final double DEFAULT_SCALE = 2.0;
    private static final int DEFAULT_WIDTH = 1584;
    private static final int DEFAULT_HEIGHT = 160;
    private static final int MINIMUM_WIDTH = 320;
    private static final int MINIMUM_HEIGHT = 160;
    private static final int FAKE_OBJECT_CUTOFF = 16;
    private static final int MAX_ENTRANCES = 4;
    private static final int MAX_ENTRANCES_MULTI = 2;
    private static final int MAX_GREEN_FLAGS = 1;

    /** names for default styles */
    private static final Map<Integer, String> STYLES = new HashMap<>(32);
    /** names for default special styles */
    private static final Map<Integer, String> SPECIAL_STYLES = new HashMap<>(16);
    private static final Map<String, Set<Integer>> OBJECTS_TO_ALIGN = new HashMap<>(32);
    private static final Map<Integer, String> MUSIC_INDEX = new HashMap<>(64);
    private static final Map<String, String> MUSIC_STRING = new HashMap<>(64);

    private static void addStyle(int styleIndex, String styleName, Integer... styleObjectsToAlign) {
        STYLES.put(styleIndex, styleName);
        OBJECTS_TO_ALIGN.put(styleName, new HashSet<>(Arrays.asList(styleObjectsToAlign)));
    }

    static {
        addStyle(0, "dirt", 0, 3, 4, 5, 6, 7, 8, 10);
        addStyle(1, "fire", 0, 3, 4, 5, 6, 7, 8, 10);
        addStyle(2, "marble", 0, 3, 4, 5, 6, 8, 9);
        addStyle(3, "pillar", 0, 3, 4, 5, 6, 8, 9, 10, 15, 16, 17);
        addStyle(4, "crystal", 0, 4, 5, 6, 7, 8, 9, 10);
        addStyle(5, "brick", 0, 3, 4, 5, 6, 7, 9);
        addStyle(6, "rock", 0, 3, 4, 5, 6, 7, 8, 9, 10);
        addStyle(7, "snow", 0, 3, 4, 5, 6, 8, 9);
        addStyle(8, "bubble", 0, 3, 4, 5, 6, 8, 9, 10);
        addStyle(9, "xmas", 0, 3, 10, 11, 12);
        //addStyle(20, "dirt_md", 0, 3, 4, 5, 6, 7, 8, 10);
        //addStyle(21, "fire_md", 0, 3, 4, 5, 6, 7, 8, 10);
        //addStyle(22, "marble_md", 0, 3, 4, 5, 6, 8, 9);
        //addStyle(23, "pillar_md", 0, 3, 4, 5, 6, 8, 9, 10, 15, 16, 17);
        //addStyle(24, "crystal_md", 0, 4, 5, 6, 7, 8, 9, 10);

        SPECIAL_STYLES.put(0, "awesome");
        SPECIAL_STYLES.put(1, "menace");
        SPECIAL_STYLES.put(2, "beastii");
        SPECIAL_STYLES.put(3, "beasti");
        SPECIAL_STYLES.put(4, "covox");
        SPECIAL_STYLES.put(5, "prima");
        SPECIAL_STYLES.put(10, "awesome_md");
        SPECIAL_STYLES.put(11, "menace_md");
        SPECIAL_STYLES.put(12, "beasti_md");
        SPECIAL_STYLES.put(13, "beastii_md");
        SPECIAL_STYLES.put(14, "hebereke");
        SPECIAL_STYLES.put(15, "apple");
        SPECIAL_STYLES.put(101, "apple");

        MUSIC_INDEX.put(1, "cancan.mod");
        MUSIC_INDEX.put(2, "lemming1.mod");
        MUSIC_INDEX.put(3, "tim2.mod");
        MUSIC_INDEX.put(4, "lemming2.mod");
        MUSIC_INDEX.put(5, "tim8.mod");
        MUSIC_INDEX.put(6, "tim3.mod");
        MUSIC_INDEX.put(7, "tim5.mod");
        MUSIC_INDEX.put(8, "doggie.mod");
        MUSIC_INDEX.put(9, "tim6.mod");
        MUSIC_INDEX.put(10, "lemming3.mod");
        MUSIC_INDEX.put(11, "tim7.mod");
        MUSIC_INDEX.put(12, "tim9.mod");
        MUSIC_INDEX.put(13, "tim1.mod");
        MUSIC_INDEX.put(14, "tim10.mod");
        MUSIC_INDEX.put(15, "tim4.mod");
        MUSIC_INDEX.put(16, "tenlemms.mod");
        MUSIC_INDEX.put(17, "mountain.mod");
        MUSIC_INDEX.put(18, "tune1.mod");
        MUSIC_INDEX.put(19, "tune2.mod");
        MUSIC_INDEX.put(20, "tune3.mod");
        MUSIC_INDEX.put(21, "tune4.mod");
        MUSIC_INDEX.put(22, "tune5.mod");
        MUSIC_INDEX.put(23, "tune6.mod");

        MUSIC_STRING.put("awesome", "special/awesome.mod");
        MUSIC_STRING.put("beasti", "special/beasti.mod");
        MUSIC_STRING.put("beastii", "special/beastii.mod");
        MUSIC_STRING.put("menace", "special/menace.mod");
        MUSIC_STRING.put("ohno_01", "tune1.mod");
        MUSIC_STRING.put("ohno_02", "tune2.mod");
        MUSIC_STRING.put("ohno_03", "tune3.mod");
        MUSIC_STRING.put("ohno_04", "tune4.mod");
        MUSIC_STRING.put("ohno_05", "tune5.mod");
        MUSIC_STRING.put("ohno_06", "tune6.mod");
        MUSIC_STRING.put("orig_01", "cancan.mod");
        MUSIC_STRING.put("orig_02", "lemming1.mod");
        MUSIC_STRING.put("orig_03", "tim2.mod");
        MUSIC_STRING.put("orig_04", "lemming2.mod");
        MUSIC_STRING.put("orig_05", "tim8.mod");
        MUSIC_STRING.put("orig_06", "tim3.mod");
        MUSIC_STRING.put("orig_07", "tim5.mod");
        MUSIC_STRING.put("orig_08", "doggie.mod");
        MUSIC_STRING.put("orig_09", "tim6.mod");
        MUSIC_STRING.put("orig_10", "lemming3.mod");
        MUSIC_STRING.put("orig_11", "tim7.mod");
        MUSIC_STRING.put("orig_12", "tim9.mod");
        MUSIC_STRING.put("orig_13", "tim1.mod");
        MUSIC_STRING.put("orig_14", "tim10.mod");
        MUSIC_STRING.put("orig_15", "tim4.mod");
        MUSIC_STRING.put("orig_16", "tenlemms.mod");
        MUSIC_STRING.put("orig_17", "mountain.mod");
        MUSIC_STRING.put("sunsoftspecial", "special/hebereke.ogg");
        MUSIC_STRING.put("xmas_01", "xmas/jb.mod");
        MUSIC_STRING.put("xmas_02", "xmas/kw.mod");
        MUSIC_STRING.put("xmas_03", "xmas/rudi.mod");
    }

    private static final int GIMMICK_FLAG_SUPERLEMMING = 1;
    private static final int GIMMICK_FLAG_CHEAPO_FALL_DISTANCE = 1 << 30;

    private static final int SKILL_FLAG_CLIMBER = 1 << 14;
    private static final int SKILL_FLAG_FLOATER = 1 << 12;
    private static final int SKILL_FLAG_BOMBER = 1 << 9;
    private static final int SKILL_FLAG_BLOCKER = 1 << 7;
    private static final int SKILL_FLAG_BUILDER = 1 << 5;
    private static final int SKILL_FLAG_BASHER = 1 << 3;
    private static final int SKILL_FLAG_MINER = 1 << 2;
    private static final int SKILL_FLAG_DIGGER = 1 << 1;

    private static final int OPTION_FLAG_NEGATIVE_STEEL = 1;
    private static final int OPTION_FLAG_AUTOSTEEL = 1 << 1;
    private static final int OPTION_FLAG_IGNORE_STEEL = 1 << 2;
    private static final int OPTION_FLAG_SIMPLE_AUTOSTEEL = 1 << 3;
    private static final int OPTION_FLAG_CUSTOM_GIMMICKS = 1 << 5;
    private static final int OPTION_FLAG_CUSTOM_SKILL_SET = 1 << 6;
    private static final int OPTION_FLAG_INVERT_ONE_WAY = 1 << 7;
    private static final int OPTION_FLAG_LOCK_RELEASE_RATE = 1 << 8;

    /**
     * Convert one binary LVL file into text file
     * @param fnIn Name of binary LVL file
     * @param out Writer to target text file
     * @param multi Whether this is a multiplayer level
     * @param classic Whether to convert in classic mode
     * @throws Exception
     */
    public static void convertLevel(final Path fnIn, final Writer out, final boolean multi, final boolean classic) throws Exception {
        byte[] b;
        try {
            if (!Files.isRegularFile(fnIn)) {
                throw new Exception(String.format("File %s not found.", fnIn));
            }
            long fileSize = Files.size(fnIn);
            if (fileSize < 177L) {
                throw new Exception("Lemmings level files must be at least 177 bytes in size!");
            }
            b = Files.readAllBytes(fnIn);
        } catch (IOException e) {
            throw new Exception(String.format("I/O error while reading %s.", fnIn));
        }
        convertLevel(b, fnIn.getFileName().toString().toLowerCase(Locale.ROOT), out, multi, classic);
    }

    /**
     * Convert one binary LVL file into text file
     * @param in Byte array of binary LVL file
     * @param fName File name
     * @param out Writer to target text file
     * @param multi Whether this is a multiplayer level
     * @param classic Whether to convert in classic mode
     * @throws Exception
     */
    public static void convertLevel(final byte[] in, final String fName, final Writer out, final boolean multi, final boolean classic) throws Exception {
        int format = 0;
        /* release rate : 0 is slowest, 0x0FA (250) is fastest */
        int releaseRate = 0;
        /* number of Lemmings in this level (maximum 0x0072 in original LVL format) */
        int numLemmings = 0;
        /* number of Lemmings to rescue : should be less than or equal to number of Lemmings */
        int numToRescue = 0;
        /* time limit in seconds */
        int timeLimitSeconds = 0;
        /* time limit in minutes: max 0x00FF, 0x0001 to 0x0009 works best */
        int timeLimit = 0;
        int gimmickFlags = 0;
        /* number of climbers in this level : max 0xfa (250) */
        int numClimbers = 0;
        /* number of floaters in this level : max 0xfa (250) */
        int numFloaters = 0;
        /* number of bombers in this level : max 0xfa (250) */
        int numBombers = 0;
        /* number of blockers in this level : max 0xfa (250) */
        int numBlockers = 0;
        /* number of builders in this level : max 0xfa (250) */
        int numBuilders = 0;
        /* number of bashers in this level : max 0xfa (250) */
        int numBashers = 0;
        /* number of miners in this level : max 0xfa (250) */
        int numMiners = 0;
        /* number of diggers in this level : max 0xfa (250) */
        int numDiggers = 0;
        double scale = DEFAULT_SCALE;
        /* start screen x pos : 0 - 0x04f0 (1264) rounded to modulo 8 */
        long xPos = 0L;
        long yPos = StrictMath.round((DEFAULT_HEIGHT / 2) * DEFAULT_SCALE);
        int optionFlags = 0;
        /*
         * 0x0000 is dirt,  <br>0x0001 is fire,   <br>0x0002 is marble,  <br>
         * 0x0003 is pillar,<br>0x0004 is crystal,<br>0x0005 is brick,   <br>
         * 0x0006 is rock,  <br>0x0007 is snow,   <br>0x0008 is bubble,  <br>
         * 0x0009 is xmas
         */
        int style;
        String styleStr = null;
        String[] styleList = ArrayUtils.EMPTY_STRING_ARRAY;
        /* special style */
        int specialStyle = -1;
        String specialStyleStr = null;
        long specialStylePositionX = 0L;
        long specialStylePositionY = 0L;
        int music = 0;
        String musicStr = null;
        int extra1 = 0;
        int extra2 = 0;
        /* objects like doors */
        List<LvlObject> objects = new ArrayList<>(256);
        /* terrain the Lemmings walk on etc. */
        List<Terrain> terrain = new ArrayList<>(2048);
        /* steel areas which are indestructible */
        List<Steel> steel = new ArrayList<>(256);
        List<Integer> entranceOrder = new ArrayList<>(64);
        List<Integer> remappedEntranceOrder = new ArrayList<>(64);
        long width = StrictMath.round(DEFAULT_WIDTH * DEFAULT_SCALE);
        long height = StrictMath.round(DEFAULT_HEIGHT * DEFAULT_SCALE);
        String origLvlName = null;
        /* 32 byte level name - filled with whitespaces */
        String lvlName = StringUtils.EMPTY;
        String author = StringUtils.EMPTY;
        int entranceCount = 0;
        int activeEntranceCount = 0;
        int greenFlagCount = 0;

        if (in.length < 177) {
            throw new Exception("Lemmings level files must be at least 177 bytes in size!");
        }
        ByteBuffer b = ByteBuffer.wrap(in).asReadOnlyBuffer();
        // analyze buffer
        if (classic) {
            if (in.length != 2048) {
                throw new Exception("Format 0 level files must be 2,048 bytes in size!");
            }
            // read configuration in big endian word
            releaseRate = b.getShort();
            if (releaseRate >= 100) {
                releaseRate -= 65536;
            }
            numLemmings = Short.toUnsignedInt(b.getShort());
            numToRescue = Short.toUnsignedInt(b.getShort());
            timeLimit = Short.toUnsignedInt(b.getShort());
            numClimbers = Short.toUnsignedInt(b.getShort());
            numFloaters = Short.toUnsignedInt(b.getShort());
            numBombers = Short.toUnsignedInt(b.getShort());
            numBlockers = Short.toUnsignedInt(b.getShort());
            numBuilders = Short.toUnsignedInt(b.getShort());
            numBashers = Short.toUnsignedInt(b.getShort());
            numMiners = Short.toUnsignedInt(b.getShort());
            numDiggers = Short.toUnsignedInt(b.getShort());
            xPos = Short.toUnsignedLong(b.getShort());
            xPos += multi ? 72L : 160L;
            xPos = StrictMath.round(xPos * scale);
            yPos = StrictMath.round((DEFAULT_HEIGHT / 2) * scale);
            style = Short.toUnsignedInt(b.getShort());
            styleStr = STYLES.get(style);
            if (styleStr == null) {
                throw new Exception(String.format("%s uses an unsupported style: %d", fName, style));
            }
            specialStyle = Short.toUnsignedInt(b.getShort()) - 1;
            if (specialStyle > -1) {
                specialStyleStr = SPECIAL_STYLES.get(specialStyle);
                if (specialStyleStr == null) {
                    throw new Exception(String.format("%s uses an unsupported special style: %d", fName, specialStyle));
                }
            }
            extra1 = b.get();
            extra2 = b.get();
        } else {
            int skillFlags = 0;
            int[] skillCounts;
            format = b.get();
            switch (format) {
                case 0:
                    if (in.length != 2048) {
                        throw new Exception("Format 0 level files must be 2,048 bytes in size!");
                    }
                    releaseRate = b.get();
                    if (releaseRate >= 100) {
                        releaseRate -= 256;
                    }
                    numLemmings = Short.toUnsignedInt(b.getShort());
                    numToRescue = Short.toUnsignedInt(b.getShort());
                    timeLimitSeconds = Byte.toUnsignedInt(b.get());
                    timeLimit = Byte.toUnsignedInt(b.get());
                    skillCounts = new int[8];
                    gimmickFlags = Byte.toUnsignedInt(b.get()) << 24;
                    skillCounts[0] = Byte.toUnsignedInt(b.get());
                    gimmickFlags |= Byte.toUnsignedInt(b.get()) << 16;
                    skillCounts[1] = Byte.toUnsignedInt(b.get());
                    gimmickFlags |= Byte.toUnsignedInt(b.get()) << 8;
                    skillCounts[2] = Byte.toUnsignedInt(b.get());
                    gimmickFlags |= Byte.toUnsignedInt(b.get());
                    skillCounts[3] = Byte.toUnsignedInt(b.get());
                    b.get();
                    skillCounts[4] = Byte.toUnsignedInt(b.get());
                    b.get();
                    skillCounts[5] = Byte.toUnsignedInt(b.get());
                    skillFlags = Byte.toUnsignedInt(b.get()) << 8;
                    skillCounts[6] = Byte.toUnsignedInt(b.get());
                    skillFlags |= Byte.toUnsignedInt(b.get());
                    skillCounts[7] = Byte.toUnsignedInt(b.get());
                    for (int i = 0; i < skillCounts.length; i++) {
                        if (skillCounts[i] >= 100) {
                            skillCounts[i] = Integer.MAX_VALUE;
                        }
                    }
                    xPos = Short.toUnsignedLong(b.getShort());
                    xPos += multi ? 72L : 160L;
                    xPos = Math.round(xPos * scale);
                    yPos = Math.round((DEFAULT_HEIGHT / 2) * scale);
                    music = Byte.toUnsignedInt(b.get());
                    if (music > 0 && music < 253) {
                        musicStr = MUSIC_INDEX.get(music);
                        if (musicStr == null) {
                            throw new Exception(String.format("%s uses an unsupported music index: %d", fName, music));
                        }
                    }
                    style = Byte.toUnsignedInt(b.get());
                    styleStr = STYLES.get(style);
                    if (styleStr == null) {
                        throw new Exception(String.format("%s uses an unsupported style: %d", fName, style));
                    }
                    optionFlags = b.get();
                    specialStyle = Byte.toUnsignedInt(b.get()) - 1;
                    if (specialStyle > -1) {
                        specialStyleStr = SPECIAL_STYLES.get(specialStyle);
                        if (specialStyleStr == null) {
                            throw new Exception(String.format("%s uses an unsupported special style: %d", fName, specialStyle));
                        }
                    }
                    extra1 = b.get();
                    extra2 = b.get();
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    if (format <= 3 && in.length != 10240) {
                        throw new Exception("Format 1, 2, and 3 level files must be 10,240 bytes in size!");
                    }
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    if (format >= 2) {
                        music = Byte.toUnsignedInt(b.get());
                    } else {
                        b.get();
                    }
                    numLemmings = Short.toUnsignedInt(b.getShort());
                    numToRescue = Short.toUnsignedInt(b.getShort());
                    timeLimitSeconds = Short.toUnsignedInt(b.getShort());
                    releaseRate = b.get();
                    if (releaseRate >= 100) {
                        releaseRate -= 256;
                    }
                    optionFlags = b.get();
                    if (format >= 4) {
                        style = 255;
                        specialStyle = 255;
                        int scaleInt = Byte.toUnsignedInt(b.get());
                        if (scaleInt == 0) {
                            scale = DEFAULT_SCALE;
                        } else {
                            scale = 16.0 / scaleInt;
                        }
                        b.get();
                    } else {
                        style = Byte.toUnsignedInt(b.get());
                        specialStyle = Byte.toUnsignedInt(b.get()) - 1;
                    }
                    if (format >= 2) {
                        xPos = Short.toUnsignedLong(b.getShort());
                        xPos += multi ? 72L : 160L;
                        xPos = Math.round(xPos * scale);
                        yPos = Short.toUnsignedLong(b.getShort()) + 80L;
                        yPos = Math.round(yPos * scale);
                    } else {
                        music = Byte.toUnsignedInt(b.get());
                        b.get();
                        xPos = Short.toUnsignedLong(b.getShort());
                        xPos += multi ? 72L : 160L;
                        xPos = Math.round(xPos * scale);
                        yPos = Math.round((DEFAULT_HEIGHT / 2) * scale);
                    }
                    if (music > 0 && music < 253) {
                        musicStr = MUSIC_INDEX.get(music);
                        if (musicStr == null) {
                            throw new Exception(String.format("%s uses an unsupported music index: %d", fName, music));
                        }
                    }
                    skillCounts = new int[16];
                    for (int i = 0; i < skillCounts.length; i++) {
                        skillCounts[i] = Byte.toUnsignedInt(b.get());
                        if (skillCounts[i] >= 100) {
                            skillCounts[i] = Integer.MAX_VALUE;
                        }
                    }
                    gimmickFlags = b.getInt();
                    skillFlags = Short.toUnsignedInt(b.getShort());
                    b.get();
                    b.get();
                    if (format >= 4) {
                        width = Math.round(Integer.toUnsignedLong(b.getInt()) * scale);
                        height = Math.round(Integer.toUnsignedLong(b.getInt()) * scale);
                        specialStylePositionX = Math.round(b.getInt() * scale);
                        specialStylePositionY = Math.round(b.getInt() * scale);
                        b.getInt();
                        optionFlags |= Byte.toUnsignedInt(b.get()) << 8;
                        b.position(b.position() + 3);
                    } else {
                        width = Math.round(Math.max(DEFAULT_WIDTH + b.getShort(), MINIMUM_WIDTH) * scale);
                        height = Math.round(Math.max(DEFAULT_HEIGHT + b.getShort(), MINIMUM_HEIGHT) * scale);
                        specialStylePositionX = Math.round(b.getShort() * scale);
                        specialStylePositionY = Math.round(b.getShort() * scale);
                    }
                    byte[] bString = new byte[16];
                    b.get(bString);
                    author = new String(bString, StandardCharsets.US_ASCII).trim();
                    author = ToolBox.addBackslashes(author, false);
                    bString = new byte[32];
                    b.get(bString);
                    lvlName = new String(bString, StandardCharsets.US_ASCII);
                    if (format >= 3) {
                        bString = new byte[16];
                        b.get(bString);
                        String strTemp = new String(bString, StandardCharsets.US_ASCII).trim().toLowerCase(Locale.ROOT);
                        if (!strTemp.isEmpty()) {
                            styleStr = strTemp;
                        }
                        b.get(bString);
                        strTemp = new String(bString, StandardCharsets.US_ASCII).trim().toLowerCase(Locale.ROOT);
                        if (strTemp.equals("none")) {
                            specialStyle = -1;
                            specialStyleStr = null;
                        } else if (!strTemp.isEmpty()) {
                            specialStyleStr = strTemp;
                        }
                    } else {
                        b.position(b.position() + 32);
                    }
                    if (styleStr == null && style != 255) {
                        styleStr = STYLES.get(style);
                        if (styleStr == null) {
                            throw new Exception(String.format("%s uses an unsupported style: %d", fName, style));
                        }
                    }
                    if (specialStyleStr == null && specialStyle != 254 && specialStyle > -1) {
                        specialStyleStr = SPECIAL_STYLES.get(specialStyle);
                        if (specialStyleStr == null) {
                            throw new Exception(String.format("%s uses an unsupported special style: %d", fName, specialStyle));
                        }
                    }
                    if (format == 3) {
                        for (int i = 0; i < 32; i++) {
                            int entranceIndex = Byte.toUnsignedInt(b.get());
                            if (toBoolean(entranceIndex & 0x80)) {
                                entranceOrder.add(entranceIndex & 0x7f);
                            }
                        }
                    } else if (format <= 2) {
                        b.position(b.position() + 32);
                    }
                    b.position(b.position() + 32);
                    break;
                default:
                    throw new Exception(String.format("Unsupported level format: %d", format));
            }
            if (format >= 1 || toBoolean(optionFlags & OPTION_FLAG_CUSTOM_SKILL_SET)) {
                int skillIndex = 15;
                int numSkills = 0;
                int skillCountIndex;
                while (skillIndex >= 0 && numSkills < 8) {
                    switch (format) {
                        case 0:
                            skillCountIndex = numSkills;
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            skillCountIndex = skillCounts.length - 1 - skillIndex;
                            break;
                        default:
                            throw new Exception(String.format("Unsupported level format: %d", format));
                    }
                    switch (1 << skillIndex) {
                        case SKILL_FLAG_CLIMBER:
                            if (toBoolean(skillFlags & SKILL_FLAG_CLIMBER)) {
                                numClimbers = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numClimbers = 0;
                            }
                            break;
                        case SKILL_FLAG_FLOATER:
                            if (toBoolean(skillFlags & SKILL_FLAG_FLOATER)) {
                                numFloaters = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numFloaters = 0;
                            }
                            break;
                        case SKILL_FLAG_BOMBER:
                            if (toBoolean(skillFlags & SKILL_FLAG_BOMBER)) {
                                numBombers = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numBombers = 0;
                            }
                            break;
                        case SKILL_FLAG_BLOCKER:
                            if (toBoolean(skillFlags & SKILL_FLAG_BLOCKER)) {
                                numBlockers = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numBlockers = 0;
                            }
                            break;
                        case SKILL_FLAG_BUILDER:
                            if (toBoolean(skillFlags & SKILL_FLAG_BUILDER)) {
                                numBuilders = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numBuilders = 0;
                            }
                            break;
                        case SKILL_FLAG_BASHER:
                            if (toBoolean(skillFlags & SKILL_FLAG_BASHER)) {
                                numBashers = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numBashers = 0;
                            }
                            break;
                        case SKILL_FLAG_MINER:
                            if (toBoolean(skillFlags & SKILL_FLAG_MINER)) {
                                numMiners = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numMiners = 0;
                            }
                            break;
                        case SKILL_FLAG_DIGGER:
                            if (toBoolean(skillFlags & SKILL_FLAG_DIGGER)) {
                                numDiggers = skillCounts[skillCountIndex];
                                numSkills++;
                            } else {
                                numDiggers = 0;
                            }
                            break;
                        default:
                            if (toBoolean(skillFlags & (1 << skillIndex))) {
                                numSkills++;
                            }
                    }
                    skillIndex--;
                }
            } else {
                numClimbers = skillCounts[0];
                numFloaters = skillCounts[1];
                numBombers = skillCounts[2];
                numBlockers = skillCounts[3];
                numBuilders = skillCounts[4];
                numBashers = skillCounts[5];
                numMiners = skillCounts[6];
                numDiggers = skillCounts[7];
            }
        }
        // read level items
        if (format >= 4) {
            boolean exitLoop = false;
            do {
                switch (b.get()) {
                    case 0:
                    default:
                        // end-of-file marker or unsupported item reached; stop parsing here
                        exitLoop = true;
                        break;
                    case 1:
                        // read object
                        objects.add(LvlObject.getObject(b, scale, classic, format));
                        break;
                    case 2:
                        // read terrain
                        terrain.add(Terrain.getTerrain(b, scale, classic, format));
                        break;
                    case 3:
                        // read steel block
                        steel.add(Steel.getSteel(b, scale, classic, format));
                        break;
                    case 4:
                        // read entrance order
                        entranceOrder.clear();
                        int entranceIndex;
                        while ((entranceIndex = Short.toUnsignedInt(b.getShort())) != 0xffff) {
                            entranceOrder.add(entranceIndex);
                        }
                        break;
                    case 5:
                        // read subheader
                        xPos = Integer.toUnsignedLong(b.getInt());
                        xPos += multi ? 72L : 160L;
                        xPos = Math.round(xPos * scale);
                        yPos = Integer.toUnsignedLong(b.getInt()) + 80L;
                        yPos = Math.round(yPos * scale);
                        b.getInt();
                        b.getInt();
                        byte[] bString = new byte[16];
                        b.get(bString);
                        String strTemp = new String(bString, StandardCharsets.US_ASCII).trim().toLowerCase(Locale.ROOT);
                        switch(strTemp) {
                            case StringUtils.EMPTY:
                            case "*":
                            case "frenzy":
                            case "gimmick":
                                musicStr = null;
                                break;
                            default:
                                musicStr = MUSIC_STRING.getOrDefault(strTemp, strTemp);
                                break;
                        }
                        break;
                    case 6:
                        // style list
                        int listSize = Short.toUnsignedInt(b.getShort());
                        if (listSize > styleList.length) {
                            Arrays.copyOf(styleList, listSize);
                        }
                        for (int i = 0; i < listSize; i++) {
                            bString = new byte[16];
                            b.get(bString);
                            styleList[i] = new String(bString, StandardCharsets.US_ASCII).trim().toLowerCase(Locale.ROOT);
                        }
                        break;
                }
            } while (!exitLoop);
        } else {
            int objectCount, terrainCount, steelCount;
            switch (format) {
                case 0:
                    objectCount = 32;
                    terrainCount = 400;
                    steelCount = 32;
                    break;
                case 1:
                case 2:
                    objectCount = 64;
                    terrainCount = 1000;
                    steelCount = 128;
                    break;
                case 3:
                    objectCount = 128;
                    terrainCount = 1000;
                    steelCount = 128;
                    break;
                case 4:
                    // this should never be reached, but it's here just in case
                    objectCount = 0;
                    terrainCount = 0;
                    steelCount = 0;
                    break;
                default:
                    throw new Exception(String.format("Unsupported level format: %d", format));
            }
            // read objects
            for (int i = 0; i < objectCount; i++) {
                objects.add(LvlObject.getObject(b, scale, classic, format));
            }
            // read terrain
            for (int i = 0; i < terrainCount; i++) {
                terrain.add(Terrain.getTerrain(b, scale, classic, format));
            }
            // read steel blocks
            for (int i = 0; i < steelCount; i++) {
                steel.add(Steel.getSteel(b, scale, classic, format));
            }
        }
        // perform some modifications to level items
        int[] entranceLookup = new int[objects.size()];
        Arrays.fill(entranceLookup, -1);
        for (ListIterator<LvlObject> it = objects.listIterator(); it.hasNext(); ) {
            int i = it.nextIndex();
            LvlObject obj = it.next();
            if (obj.exists) {
                if (classic) {
                    if (obj.id == LvlObject.ENTRANCE_ID) {
                        if (++entranceCount > (multi ? MAX_ENTRANCES_MULTI : MAX_ENTRANCES)) {
                            obj.flags |= LvlObject.FLAG_FAKE;
                        }
                    } else if (obj.id == LvlObject.GREEN_FLAG_ID) {
                        if (++greenFlagCount > MAX_GREEN_FLAGS) {
                            obj.flags |= LvlObject.FLAG_FAKE;
                        }
                    } else if (i >= FAKE_OBJECT_CUTOFF) {
                        obj.flags |= LvlObject.FLAG_FAKE;
                    }
                }
                if (obj.id == LvlObject.ENTRANCE_ID && !toBoolean(obj.flags & LvlObject.FLAG_FAKE)) {
                    entranceLookup[i] = activeEntranceCount++;
                }
            }
        }
        for (Terrain ter : terrain) {
            if (toBoolean(optionFlags & OPTION_FLAG_INVERT_ONE_WAY)) {
                if (toBoolean(ter.modifier & Terrain.FLAG_NO_ONE_WAY)) {
                    ter.modifier &= ~Terrain.FLAG_NO_ONE_WAY;
                } else {
                    ter.modifier |= Terrain.FLAG_NO_ONE_WAY;
                }
            }
        }
        if (format == 0 && toBoolean(optionFlags & OPTION_FLAG_NEGATIVE_STEEL) && steel.size() > 16) {
            for (ListIterator<Steel> it = steel.listIterator(16); it.hasNext(); ) {
                it.next().negative = true;
            }
        }
        // remap the entrance order
        entranceOrder.stream()
                .filter(entranceIndex -> (entranceLookup[entranceIndex] >= 0))
                .forEachOrdered(entranceIndex -> remappedEntranceOrder.add(entranceLookup[entranceIndex]));
        // read name
        if (format == 0) {
            byte[] bName = new byte[32];
            b.get(bName);
            lvlName = new String(bName, classic ? StandardCharsets.ISO_8859_1 : StandardCharsets.US_ASCII);
        }
        lvlName = ToolBox.addBackslashes(lvlName, false);
        if (classic && lvlName.indexOf('`') != StringUtils.INDEX_NOT_FOUND) {
            origLvlName = lvlName;
            // replace wrong apostrophes
            lvlName = lvlName.replace('`', '\'');
        }

        // write the level
        // add only file name without the path in the first line
        out.write("# LVL extracted by SuperLemmini # " + fName + "\r\n");
        // write configuration
        out.write("releaseRate = " + releaseRate + "\r\n");
        if (toBoolean(optionFlags & OPTION_FLAG_LOCK_RELEASE_RATE)) {
            out.write("lockReleaseRate = true\r\n");
        }
        out.write("numLemmings = " + numLemmings + "\r\n");
        out.write("numToRescue = " + numToRescue + "\r\n");
        if (classic) {
            out.write("timeLimit = " + timeLimit + "\r\n");
            out.write("numClimbers = " + numClimbers + "\r\n");
            out.write("numFloaters = " + numFloaters + "\r\n");
            out.write("numBombers = " + numBombers + "\r\n");
            out.write("numBlockers = " + numBlockers + "\r\n");
            out.write("numBuilders = " + numBuilders + "\r\n");
            out.write("numBashers = " + numBashers + "\r\n");
            out.write("numMiners = " + numMiners + "\r\n");
            out.write("numDiggers = " + numDiggers + "\r\n");
        } else {
            out.write("timeLimitSeconds = " + (timeLimit * 60 + timeLimitSeconds) + "\r\n");
            out.write("numClimbers = " + ToolBox.intToString(numClimbers, false) + "\r\n");
            out.write("numFloaters = " + ToolBox.intToString(numFloaters, false) + "\r\n");
            out.write("numBombers = " + ToolBox.intToString(numBombers, false) + "\r\n");
            out.write("numBlockers = " + ToolBox.intToString(numBlockers, false) + "\r\n");
            out.write("numBuilders = " + ToolBox.intToString(numBuilders, false) + "\r\n");
            out.write("numBashers = " + ToolBox.intToString(numBashers, false) + "\r\n");
            out.write("numMiners = " + ToolBox.intToString(numMiners, false) + "\r\n");
            out.write("numDiggers = " + ToolBox.intToString(numDiggers, false) + "\r\n");
            if (!remappedEntranceOrder.isEmpty()) {
                out.write("entranceOrder = ");
                for (Iterator<Integer> it = remappedEntranceOrder.iterator(); it.hasNext(); ) {
                    out.write(it.next().toString());
                    if (it.hasNext()) {
                        out.write(", ");
                    }
                }
                out.write("\r\n");
            } else if (activeEntranceCount == 3) {
                out.write("entranceOrder = 0, 1, 2\r\n");
            }
        }
        out.write("xPosCenter = " + xPos + "\r\n");
        if (!classic) {
            out.write("yPosCenter = " + yPos + "\r\n");
        }
        out.write("style = " + ToolBox.addBackslashes(styleStr, false) + "\r\n");
        if (specialStyleStr != null) {
            out.write("specialStyle = " + ToolBox.addBackslashes(specialStyleStr, false) + "\r\n");
            if (format >= 3) {
                out.write("specialStylePositionX = " + specialStylePositionX + "\r\n");
                out.write("specialStylePositionY = " + specialStylePositionY + "\r\n");
            }
        }
        if (musicStr != null) {
            out.write("music = " + ToolBox.addBackslashes(musicStr, false) + "\r\n");
        }
        if (toBoolean(optionFlags & OPTION_FLAG_AUTOSTEEL)) {
            if (toBoolean(optionFlags & OPTION_FLAG_SIMPLE_AUTOSTEEL)) {
                out.write("autosteelMode = 1\r\n");
            } else {
                out.write("autosteelMode = 2\r\n");
            }
        }
        if (classic) {
            if (extra1 != 0) {
                out.write("superlemming = true\r\n");
            }
            if ((extra1 != 0 || extra2 != 0)
                    && (extra1 != -1 || extra2 != -1)) {
                out.write("#byte30Value = " + extra1 + "\r\n");
                out.write("#byte31Value = " + extra2 + "\r\n");
            }
            out.write("forceNormalTimerSpeed = true\r\n");
            out.write("classicSteel = true\r\n");
        } else {
            if (format >= 1 || toBoolean(optionFlags & OPTION_FLAG_CUSTOM_GIMMICKS)) {
                if (toBoolean(gimmickFlags & GIMMICK_FLAG_SUPERLEMMING)) {
                    out.write("superlemming = true\r\n");
                }
                if (toBoolean(gimmickFlags & GIMMICK_FLAG_CHEAPO_FALL_DISTANCE)) {
                    out.write("maxFallDistance = 152\r\n");
                }
            } else {
                switch (extra1) {
                    case -1:
                        out.write("superlemming = true\r\n");
                        break;
                    case 66:
                        switch (extra2) {
                            case 1:
                            case 9:
                            case 10:
                                out.write("superlemming = true\r\n");
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
            out.write("width = " + width + "\r\n");
            out.write("height = " + height + "\r\n");
        }
        // write objects
        out.write("\r\n# Objects\r\n");
        out.write("# ID, X position, Y position, paint mode, flags,\r\n");
        out.write("#         object-specific modifier (optional),\r\n");
        out.write("#         style (optional, requires object-specific modifier)\r\n");
        out.write("# Paint modes: 0 = full, 2 = invisible, 4 = don't overwrite,\r\n");
        out.write("#              8 = visible only on terrain (only one value possible)\r\n");
        out.write("# Flags: 1 = upside down, 2 = fake, 4 = upside-down mask,\r\n");
        out.write("#        8 = flip horizontally, 16 = rotate (combining allowed)\r\n");
        int maxObjectID = -1;
        for (ListIterator<LvlObject> it = objects.listIterator(objects.size()); it.hasPrevious(); ) {
            int i = it.previousIndex();
            LvlObject obj = it.previous();
            if (obj.exists) {
                maxObjectID = i;
                break;
            }
        }
        for (ListIterator<LvlObject> it = objects.listIterator(); it.nextIndex() <= maxObjectID && it.hasNext(); ) {
            int i = it.nextIndex();
            LvlObject obj = it.next();
            long newXPos;
            long newYPos;
            if (classic && OBJECTS_TO_ALIGN.getOrDefault(styleStr, Collections.emptySet()).contains(obj.id)) {
                newXPos = obj.xPos - obj.xPos % StrictMath.round(4 * scale);
                newYPos = obj.yPos - obj.yPos % StrictMath.round(4 * scale);
            } else {
                newXPos = obj.xPos;
                newYPos = obj.yPos;
            }
            if (obj.exists) {
                out.write("object_" + i + " = " + obj.id + ", " + newXPos + ", " + newYPos + ", " + obj.paintMode + ", " + obj.flags);
                if (!classic) {
                    if (obj.id == LvlObject.ENTRANCE_ID && obj.leftFacing) {
                        out.write(", 1");
                    } else {
                        out.write(", 0");
                    }
                    if (obj.styleIndex >= 0 && obj.styleIndex < styleList.length) {
                        out.write(", " + styleList[obj.styleIndex]);
                    }
                }
                out.write("\r\n");
                if (classic) {
                    if (newXPos != obj.xPos || newYPos != obj.yPos) {
                        out.write("#object_" + i + "_origPosition = " + obj.xPos + ", " + obj.yPos + "\r\n");
                    }
                    if (toBoolean(obj.byte4Value & 0x80)) {
                        out.write("#object_" + i + "_byte4Value = " + obj.byte4Value + "\r\n");
                    }
                    if ((obj.byte6Value & 0x3f) != 0) {
                        out.write("#object_" + i + "_byte6Value = " + obj.byte6Value + "\r\n");
                    }
                    if ((obj.byte7Value & 0x7f) != 0x0f) {
                        out.write("#object_" + i + "_byte7Value = " + obj.byte7Value + "\r\n");
                    }
                }
            } else {
                out.write("object_" + i + " = -1, 0, 0, 0, 0\r\n");
            }
        }
        // write terrain
        out.write("\r\n# Terrain\r\n");
        out.write("# ID, X position, Y position, modifier, style (optional)\r\n");
        out.write("# Modifier: 1 = invisible, 2 = remove, 4 = upside down, 8 = don't overwrite,\r\n");
        out.write("#           16 = fake, 32 = flip horizontally, 64 = no one-way arrows,\r\n");
        out.write("#           128 = rotate (combining allowed)\r\n");
        int maxTerrainID = -1;
        for (ListIterator<Terrain> it = terrain.listIterator(terrain.size()); it.hasPrevious(); ) {
            int i = it.previousIndex();
            Terrain ter = it.previous();
            if (ter.exists) {
                maxTerrainID = i;
                break;
            }
        }
        int maxValidTerrainID = -1;
        if (!classic) {
            maxValidTerrainID = maxTerrainID;
        } else if (specialStyle < 0) {
            for (ListIterator<Terrain> it = terrain.listIterator(); it.nextIndex() <= maxTerrainID && it.hasNext(); ) {
                int i = it.nextIndex();
                Terrain ter = it.next();
                if (ter.exists) {
                    maxValidTerrainID = i;
                } else {
                    break;
                }
            }
        }
        for (ListIterator<Terrain> it = terrain.listIterator(); it.nextIndex() <= maxTerrainID && it.hasNext(); ) {
            int i = it.nextIndex();
            if (i > maxValidTerrainID) {
                out.write("#");
            }
            Terrain ter = it.next();
            if (ter.exists) {
                out.write("terrain_" + i + " = " + ter.id + ", " + ter.xPos + ", " + ter.yPos + ", " + ter.modifier);
                if (ter.styleIndex >= 0 && ter.styleIndex < styleList.length) {
                    out.write(", " + styleList[ter.styleIndex]);
                }
                out.write("\r\n");
                if (classic && toBoolean(ter.byte3Value & 0x40)) {
                    out.write("#terrain_" + i + "_byte3Value = " + ter.byte3Value + "\r\n");
                }
            } else {
                out.write("terrain_" + i + " = -1, 0, 0, 0\r\n");
            }
        }
        // write steel blocks
        out.write("\r\n# Steel\r\n");
        out.write("# X position, Y position, width, height, flags (optional)\r\n");
        out.write("# Flags: 1 = remove existing steel\r\n");
        int maxSteelID = -1;
        if (!toBoolean(optionFlags & OPTION_FLAG_IGNORE_STEEL)) {
            for (ListIterator<Steel> it = steel.listIterator(steel.size()); it.hasPrevious(); ) {
                int i = it.previousIndex();
                Steel stl = it.previous();
                if (stl.exists) {
                    maxSteelID = i;
                    break;
                }
            }
        }
        for (ListIterator<Steel> it = steel.listIterator(); it.nextIndex() <= maxSteelID && it.hasNext(); ) {
            int i = it.nextIndex();
            Steel stl = it.next();
            if (stl.exists) {
                out.write("steel_" + i + " = " + stl.xPos + ", " + stl.yPos + ", " + stl.width + ", " + stl.height);
                if (!classic) {
                    out.write(", " + (stl.negative ? "1" : "0"));
                }
                out.write("\r\n");
                if (classic && stl.byte3Value != 0) {
                    out.write("#steel_" + i + "_byte3Value = " + stl.byte3Value + "\r\n");
                }
            } else {
                out.write("steel_" + i + " = 0, 0, 0, 0\r\n");
            }
        }
        // write name
        out.write("\r\n# Name and author\r\n");
        if (origLvlName != null) {
            out.write("#origName = " + origLvlName + "\r\n");
        }
        out.write("name = " + lvlName + "\r\n");
        if (!author.isEmpty()) {
            out.write("author = " + author + "\r\n");
        }
    }
}

/**
 * Storage class for level objects.
 * @author Volker Oth
 */
class LvlObject {

    /** paint mode: only visible on a terrain pixel */
    static final int MODE_VIS_ON_TERRAIN = 8;
    /** paint mode: don't overwrite terrain pixel in the original foreground image */
    static final int MODE_NO_OVERWRITE = 4;
    static final int MODE_INVISIBLE = 2;

    /** flag: paint object upside down */
    static final int FLAG_UPSIDE_DOWN = 1;
    static final int FLAG_FAKE = 2;
    static final int FLAG_UPSIDE_DOWN_MASK = 4;
    static final int FLAG_FLIP_HORIZONTALLY = 8;
    static final int FLAG_ROTATE = 16;

    static final int ENTRANCE_ID = 1;
    static final int GREEN_FLAG_ID = 2;

    /** x position in pixels */
    long xPos;
    /** y position in pixels */
    long yPos;
    /** identifier */
    int id;
    /** paint mode */
    int paintMode;
    int flags;
    boolean leftFacing;
    int styleIndex;
    boolean exists;
    byte byte4Value;
    byte byte6Value;
    byte byte7Value;

    /**
     * Constructor.
     * @param b buffer
     * @param classic
     * @param scale Scale
     */
    LvlObject(final byte[] b, final double scale, final boolean classic, final int format) throws Exception {
        switch (format) {
            case 0:
                styleIndex = 0;
                byte4Value = b[4];
                byte6Value = b[6];
                byte7Value = b[7];
                int sum = 0;
                for (byte b1 : b) {
                    sum += Byte.toUnsignedInt(b1);
                }
                exists = sum != 0;
                // x pos  : min 0xFFF8, max 0x0638.  0xFFF8 = -24, 0x0000 = -16, 0x0008 = -8
                // 0x0010 = 0, 0x0018 = 8, ... , 0x0638 = 1576    note: should be multiples of 8
                xPos = ((b[0] << 8L) | Byte.toUnsignedLong(b[1])) - 16L;
                xPos = StrictMath.round(xPos * scale);
                // y pos  : min 0xFFD7, max 0x009F.  0xFFD7 = -41, 0xFFF8 = -8, 0xFFFF = -1
                // 0x0000 = 0, ... , 0x009F = 159.  note: can be any value in the specified range
                yPos = (b[2] << 8L) | Byte.toUnsignedLong(b[3]);
                yPos = StrictMath.round(yPos * scale);
                // obj id : min 0x0000, max 0x000F.  the object id is different in each
                // graphics set, however 0x0000 is always an exit and 0x0001 is always a start.
                if (classic) {
                    id = ((b[4] & 0x7f) << 8) | Byte.toUnsignedInt(b[5]);
                } else {
                    id = Byte.toUnsignedInt(b[5]);
                }
                // modifier : first byte can be 80 (do not overwrite existing terrain) or 40
                // (must have terrain underneath to be visible). 00 specifies always draw full graphic.
                // second byte can be 8F (display graphic upside-down) or 0F (display graphic normally)
                paintMode = 0;
                flags = 0;
                if (toBoolean(b[6] & 0x80)) {
                    paintMode |= MODE_NO_OVERWRITE;
                }
                if (toBoolean(b[6] & 0x40)) {
                    paintMode |= MODE_VIS_ON_TERRAIN;
                }
                if (toBoolean(b[7] & 0x80) && (b[7] & 0x0f) == 0x0f) {
                    flags |= FLAG_UPSIDE_DOWN;
                    if (!classic) {
                        flags |= FLAG_UPSIDE_DOWN_MASK;
                    }
                }
                if (!classic && toBoolean(b[6] & 0x10)) {
                    flags |= FLAG_FAKE;
                }
                break;
            case 1:
            case 2:
            case 3:
                styleIndex = 0;
                byte4Value = 0;
                byte6Value = 0;
                byte7Value = 0;
                xPos = Byte.toUnsignedLong(b[0]) | (b[1] << 8L);
                xPos = Math.round(xPos * scale);
                yPos = Byte.toUnsignedLong(b[2]) | (b[3] << 8L);
                yPos = Math.round(yPos * scale);
                id = Byte.toUnsignedInt(b[4]);
                paintMode = 0;
                flags = 0;
                if (toBoolean(b[7] & 0x01)) {
                    paintMode |= MODE_NO_OVERWRITE;
                }
                if (toBoolean(b[7] & 0x02)) {
                    paintMode |= MODE_VIS_ON_TERRAIN;
                }
                if (toBoolean(b[7] & 0x04)) {
                    flags |= FLAG_UPSIDE_DOWN;
                    flags |= FLAG_UPSIDE_DOWN_MASK;
                }
                leftFacing = toBoolean(b[7] & 0x08);
                if (toBoolean(b[7] & 0x10)) {
                    flags |= FLAG_FAKE;
                }
                if (toBoolean(b[7] & 0x20)) {
                    paintMode |= MODE_INVISIBLE;
                }
                if (toBoolean(b[7] & 0x40)) {
                    flags |= FLAG_FLIP_HORIZONTALLY;
                }
                exists = toBoolean(b[7] & 0x80);
                break;
            case 4:
                byte4Value = 0;
                byte6Value = 0;
                byte7Value = 0;
                xPos = Byte.toUnsignedLong(b[0])
                        | (Byte.toUnsignedLong(b[1]) << 8L)
                        | (Byte.toUnsignedLong(b[2]) << 16L)
                        | (b[3] << 24L);
                xPos = Math.round(xPos * scale);
                yPos = Byte.toUnsignedLong(b[4])
                        | (Byte.toUnsignedLong(b[5]) << 8L)
                        | (Byte.toUnsignedLong(b[6]) << 16L)
                        | (b[7] << 24L);
                yPos = Math.round(yPos * scale);
                id = Byte.toUnsignedInt(b[8]) | (Byte.toUnsignedInt(b[9]) << 8);
                paintMode = 0;
                flags = 0;
                if (toBoolean(b[12] & 0x01)) {
                    paintMode |= MODE_NO_OVERWRITE;
                }
                if (toBoolean(b[12] & 0x02)) {
                    paintMode |= MODE_VIS_ON_TERRAIN;
                }
                if (toBoolean(b[12] & 0x04)) {
                    flags |= FLAG_UPSIDE_DOWN;
                    flags |= FLAG_UPSIDE_DOWN_MASK;
                }
                leftFacing = toBoolean(b[12] & 0x08);
                if (toBoolean(b[12] & 0x10)) {
                    flags |= FLAG_FAKE;
                }
                if (toBoolean(b[12] & 0x20)) {
                    paintMode |= MODE_INVISIBLE;
                }
                if (toBoolean(b[12] & 0x40)) {
                    flags |= FLAG_FLIP_HORIZONTALLY;
                }
                exists = toBoolean(b[12] & 0x80);
                if (toBoolean(b[13] & 0x01)) {
                    flags |= FLAG_ROTATE;
                }
                styleIndex = Byte.toUnsignedInt(b[14]);
                break;
            default:
                throw new Exception(String.format("Unsupported level format: %d", format));
        }
    }

    static LvlObject getObject(ByteBuffer b, double scale, boolean classic, int format) throws Exception {
        int byteCount;
        switch (format) {
            case 0:
            case 3:
                byteCount = 8;
                break;
            case 1:
            case 2:
                byteCount = 16;
                break;
            case 4:
                byteCount = 20;
                break;
            default:
                throw new Exception(String.format("Unsupported level format: %d", format));
        }
        byte[] bytes = new byte[byteCount];
        b.get(bytes);
        return new LvlObject(bytes, scale, classic, format);
    }
}

/**
 * Storage class for terrain tiles.
 * @author Volker Oth
 */
class Terrain {

    static final int FLAG_ROTATE = 128;
    static final int FLAG_NO_ONE_WAY = 64;
    static final int FLAG_FLIP_HORIZONTALLY = 32;
    static final int FLAG_NO_OVERWRITE = 8;
    static final int FLAG_UPSIDE_DOWN = 4;
    static final int FLAG_ERASE = 2;

    /** identifier */
    int id;
    /** x position in pixels */
    long xPos;
    /** y position in pixels */
    long yPos;
    /** modifier - must be one of the above MODEs */
    int modifier;
    int styleIndex;
    boolean exists;
    byte byte3Value;

    /**
     * Constructor.
     * @param b buffer
     * @param scale Scale
     */
    Terrain(final byte[] b, final double scale, final boolean classic, final int format) throws Exception {
        switch (format) {
            case 0:
                styleIndex = 0;
                byte3Value = b[3];
                int mask = 0xff;
                for (byte b1 : b) {
                    mask &= Byte.toUnsignedInt(b1);
                }
                exists = mask != 0xff;
                // xpos: 0x0000..0x063F.  0x0000 = -16, 0x0008 = -8, 0x0010 = 0, 0x063f = 1583.
                // note: the xpos also contains modifiers.  the first nibble can be
                // 8 (do no overwrite existing terrain), 4 (display upside-down), or
                // 2 (remove terrain instead of add it). you can add them together.
                // 0 indicates normal.
                // eg: 0xC011 means draw at xpos=1, do not overwrite, upside-down.
                modifier = (b[0] & 0xe0) >> 4;
                xPos = (((b[0] & (classic ? 0x1fL : 0x0fL)) << 8L) | Byte.toUnsignedLong(b[1])) - 16L;
                xPos = StrictMath.round(xPos * scale);
                // y pos : 9-bit value. min 0xEF0, max 0x518.  0xEF0 = -38, 0xEF8 = -37,
                // 0x020 = 0, 0x028 = 1, 0x030 = 2, 0x038 = 3, ... , 0x518 = 159
                // note: the ypos value bleeds into the next value since it is 9 bits.
                yPos = (Byte.toUnsignedLong(b[2]) << 1L) | ((b[3] & 0x80L) >> 7L);
                if (toBoolean((int) yPos & 0x100)) { // highest bit set -> negative
                    yPos -= 512L;
                }
                yPos -= 4L;
                yPos = StrictMath.round(yPos * scale);
                // terrain id: min 0x00, max 0x3F.  not all graphic sets have all 64 graphics.
                id = b[3] & 0x3f;
                if (!classic && toBoolean(b[0] & 0x10)) {
                    id += 64;
                }
                break;
            case 1:
            case 2:
            case 3:
                styleIndex = 0;
                byte3Value = 0;
                xPos = Byte.toUnsignedLong(b[0]) | (b[1] << 8L);
                xPos = Math.round(xPos * scale);
                yPos = Byte.toUnsignedLong(b[2]) | (b[3] << 8L);
                yPos = Math.round(yPos * scale);
                id = Byte.toUnsignedInt(b[4]);
                modifier = 0;
                if (toBoolean(b[5] & 0x01)) {
                    modifier |= FLAG_NO_OVERWRITE;
                }
                if (toBoolean(b[5] & 0x02)) {
                    modifier |= FLAG_ERASE;
                }
                if (toBoolean(b[5] & 0x04)) {
                    modifier |= FLAG_UPSIDE_DOWN;
                }
                if (toBoolean(b[5] & 0x08)) {
                    modifier |= FLAG_FLIP_HORIZONTALLY;
                }
                if (toBoolean(b[5] & 0x10)) {
                    modifier |= FLAG_NO_ONE_WAY;
                }
                exists = toBoolean(b[5] & 0x80);
                break;
            case 4:
                byte3Value = 0;
                xPos = Byte.toUnsignedLong(b[0])
                        | (Byte.toUnsignedLong(b[1]) << 8L)
                        | (Byte.toUnsignedLong(b[2]) << 16L)
                        | (b[3] << 24L);
                xPos = Math.round(xPos * scale);
                yPos = Byte.toUnsignedLong(b[4])
                        | (Byte.toUnsignedLong(b[5]) << 8L)
                        | (Byte.toUnsignedLong(b[6]) << 16L)
                        | (b[7] << 24L);
                yPos = Math.round(yPos * scale);
                id = Byte.toUnsignedInt(b[8]) | (Byte.toUnsignedInt(b[9]) << 8);
                modifier = 0;
                if (toBoolean(b[10] & 0x01)) {
                    modifier |= FLAG_NO_OVERWRITE;
                }
                if (toBoolean(b[10] & 0x02)) {
                    modifier |= FLAG_ERASE;
                }
                if (toBoolean(b[10] & 0x04)) {
                    modifier |= FLAG_UPSIDE_DOWN;
                }
                if (toBoolean(b[10] & 0x08)) {
                    modifier |= FLAG_FLIP_HORIZONTALLY;
                }
                if (toBoolean(b[10] & 0x10)) {
                    modifier |= FLAG_NO_ONE_WAY;
                }
                if (toBoolean(b[10] & 0x20)) {
                    modifier |= FLAG_ROTATE;
                }
                exists = toBoolean(b[10] & 0x80);
                styleIndex = Byte.toUnsignedInt(b[12]);
                break;
            default:
                throw new Exception(String.format("Unsupported level format: %d", format));
        }
    }

    static Terrain getTerrain(ByteBuffer b, double scale, boolean classic, int format) throws Exception {
        int byteCount;
        switch (format) {
            case 0:
                byteCount = 4;
                break;
            case 1:
            case 2:
            case 3:
                byteCount = 8;
                break;
            case 4:
                byteCount = 16;
                break;
            default:
                throw new Exception(String.format("Unsupported level format: %d", format));
        }
        byte[] bytes = new byte[byteCount];
        b.get(bytes);
        return new Terrain(bytes, scale, classic, format);
    }
}

/**
 *
 * Storage class for steel areas.
 * @author Volker Oth
 */
class Steel {

    /** x position in pixels */
    long xPos;
    /** y position in pixels */
    long yPos;
    /** width in pixels */
    long width;
    /** height in pixels */
    long height;
    boolean negative;
    boolean exists;
    byte byte3Value;

    /**
     * Constructor.
     * @param b buffer
     * @param scale Scale
     */
    Steel(final byte[] b, final double scale, final boolean classic, final int format) throws Exception {
        int steelType;
        switch (format) {
            case 0:
                byte3Value = b[3];
                int sum = 0;
                for (byte b1 : b) {
                    sum += Byte.toUnsignedInt(b1);
                }
                exists = sum != 0;
                // xpos: 9-bit value: 0x000-0x178).  0x000 = -16, 0x178 = 1580
                xPos = ((Byte.toUnsignedLong(b[0]) << 1L) | ((b[1] & 0x80L) >> 7L)) * 4L - 16L;
                if (!classic) {
                    xPos -= (b[3] & 0xc0L) >>> 6L;
                }
                xPos = StrictMath.round(xPos * scale);
                // ypos: 0x00-0x27. 0x00 = 0, 0x27 = 156 - each hex value represents 4 pixels
                yPos = (b[1] & 0x7fL) * 4L;
                if (!classic) {
                    yPos -= (b[3] & 0x30L) >>> 4L;
                }
                yPos = StrictMath.round(yPos * scale);
                // area: 0x00-0xFF.  first nibble is the x-size, from 0-F (represents 4 pixels)
                // second nibble is the y-size. 0x00 = (4,4), 0x11 = (8,8), 0x7F = (32,64)
                width = ((b[2] & 0xf0L) >> 4L) * 4L + 4L;
                if (!classic) {
                    width -= (b[3] & 0x0cL) >>> 2L;
                }
                width = StrictMath.round(width * scale);
                height = (b[2] & 0xfL) * 4L + 4L;
                if (!classic) {
                    height -= b[3] & 0x03L;
                }
                height = StrictMath.round(height * scale);
                negative = false;
                break;
            case 1:
            case 2:
            case 3:
                byte3Value = 0;
                xPos = Byte.toUnsignedLong(b[0]) | (b[1] << 8L);
                xPos = Math.round(xPos * scale);
                yPos = Byte.toUnsignedLong(b[2]) | (b[3] << 8L);
                yPos = Math.round(yPos * scale);
                width = Byte.toUnsignedLong(b[4]) + 1L;
                width = Math.round(width * scale);
                height = Byte.toUnsignedLong(b[5]) + 1L;
                height = Math.round(height * scale);
                steelType = b[6] & 0x7f;
                negative = steelType == 1;
                exists = toBoolean(b[6] & 0x80) && (steelType == 0 || steelType == 1);
                break;
            case 4:
                byte3Value = 0;
                xPos = Byte.toUnsignedLong(b[0])
                        | (Byte.toUnsignedLong(b[1]) << 8L)
                        | (Byte.toUnsignedLong(b[2]) << 16L)
                        | (b[3] << 24L);
                xPos = Math.round(xPos * scale);
                yPos = Byte.toUnsignedLong(b[4])
                        | (Byte.toUnsignedLong(b[5]) << 8L)
                        | (Byte.toUnsignedLong(b[6]) << 16L)
                        | (b[7] << 24L);
                yPos = Math.round(yPos * scale);
                width = (Byte.toUnsignedLong(b[8])
                        | (Byte.toUnsignedLong(b[9]) << 8L)
                        | (Byte.toUnsignedLong(b[10]) << 16L)
                        | (b[11] << 24L)) + 1L;
                width = Math.round(width * scale);
                height = (Byte.toUnsignedLong(b[12])
                        | (Byte.toUnsignedLong(b[13]) << 8L)
                        | (Byte.toUnsignedLong(b[14]) << 16L)
                        | (b[15] << 24L)) + 1L;
                height = Math.round(height * scale);
                steelType = b[16] & 0x7f;
                negative = steelType == 1;
                exists = toBoolean(b[16] & 0x80) && (steelType == 0 || steelType == 1);
                break;
            default:
                throw new Exception(String.format("Unsupported level format: %d", format));
        }
    }

    static Steel getSteel(ByteBuffer b, double scale, boolean classic, int format) throws Exception {
        int byteCount;
        switch (format) {
            case 0:
                byteCount = 4;
                break;
            case 1:
            case 2:
            case 3:
                byteCount = 8;
                break;
            case 4:
                byteCount = 20;
                break;
            default:
                throw new Exception(String.format("Unsupported level format: %d", format));
        }
        byte[] bytes = new byte[byteCount];
        b.get(bytes);
        return new Steel(bytes, scale, classic, format);
    }
}