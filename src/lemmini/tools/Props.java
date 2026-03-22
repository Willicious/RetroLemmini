package lemmini.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import lemmini.game.Core;
import lemmini.game.Resource;

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
 * Property class to ease use of INI files to save/load properties.
 *
 * @author Volker Oth
 * Modified by Ryan Sakowski and William James
 */
public class Props {

    /** extended hash to store properties */
    private final Properties hash;
    /** header string */
    private String header;

    /**
     * Constructor
     */
    public Props() {
        hash = new Properties();
        header = new String();
    }

    /**
     * Set the property file header
     */
    public void setHeader(final String h) {
        header = h;
    }

    /**
     * Clear all properties
     */
    public void clear() {
        hash.clear();
    }

    /**
     * Remove key
     */
    public void remove(final String key) {
        hash.remove(key);
    }

    /**
     * Set string property
     */
    public void set(final String key, final String value) {
        hash.setProperty(key, value);
    }

    /**
     * Set integer property
     */
    public void setInt(final String key, final int value) {
        hash.setProperty(key, Integer.toString(value));
    }

    /**
     * Set double property
     */
    public void setDouble(final String key, final double value) {
        hash.setProperty(key, Double.toString(value));
    }

    /**
     * Set boolean property
     */
    public void setBoolean(final String key, final boolean value) {
        hash.setProperty(key, Boolean.toString(value));
    }

    /**
     * Get string property
     */
    public String get(final String key, final String def) {
        return hash.getProperty(key, def);
    }

    /**
     * Get string property from the first Props object that contains it
     */
    public static String get(final Collection<? extends Props> pCollection, final String key, final String def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.get(key, def);
            }
        }
        return def;
    }

    /**
     * Get string array property (an array is a result split by commas)
     */
    public String[] getArray(final String key, final String[] def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        String[] members = s.split(",");
        // remove trailing and leading spaces
        for (int i = 0; i < members.length; i++) {
            members[i] = members[i].trim();
        }

        return members;
    }

    /**
     * Get string array property from the first Props object that contains it
     */
    public static String[] getArray(final Collection<? extends Props> pCollection, final String key, final String[] def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getArray(key, def);
            }
        }
        return def;
    }

    /**
     * Get integer property
     */
    public int getInt(final String key, final int def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        return ToolBox.parseInt(s.trim());
    }

    /**
     * Get integer property from the first Props object that contains it
     */
    public static int getInt(final Collection<? extends Props> pCollection, final String key, final int def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getInt(key, def);
            }
        }
        return def;
    }

    /**
     * Get integer array property
     */
    public int[] getIntArray(final String key, final int[] def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        String[] members = s.split(",");

        int[] ret;
        ret = new int[members.length];
        for (int i = 0; i < members.length; i++) {
            ret[i] = ToolBox.parseInt(members[i].trim());
        }

        return ret;
    }

    /**
     * Get integer array property from the first Props object that contains it
     */
    public static int[] getIntArray(final Collection<? extends Props> pCollection, final String key, final int[] def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getIntArray(key, def);
            }
        }
        return def;
    }

    /**
     * Get double property
     */
    public double getDouble(final String key, final double def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        s = s.trim();
        if (s.equalsIgnoreCase("Infinity") || s.equalsIgnoreCase("+Infinity")) {
            return Double.POSITIVE_INFINITY;
        } else if (s.equalsIgnoreCase("-Infinity")) {
            return Double.NEGATIVE_INFINITY;
        } else if (s.equalsIgnoreCase("NaN") || s.equalsIgnoreCase("+NaN") || s.equalsIgnoreCase("-NaN")) {
            return Double.NaN;
        } else {
            return Double.parseDouble(s);
        }
    }

    /**
     * Get double property from the first Props object that contains it
     */
    public static double getDouble(final Collection<? extends Props> pCollection, final String key, final double def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getDouble(key, def);
            }
        }
        return def;
    }

    /**
     * Get double array property
     */
    public double[] getDoubleArray(final String key, final double[] def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        String[] members = s.split(",");

        double[] ret;
        ret = new double[members.length];
        for (int i = 0; i < members.length; i++) {
            members[i] = members[i].trim();
            if (s.equalsIgnoreCase("Infinity") || s.equalsIgnoreCase("+Infinity")) {
                ret[i] = Double.POSITIVE_INFINITY;
            } else if (s.equalsIgnoreCase("-Infinity")) {
                ret[i] = Double.NEGATIVE_INFINITY;
            } else if (s.equalsIgnoreCase("NaN") || s.equalsIgnoreCase("+NaN") || s.equalsIgnoreCase("-NaN")) {
                ret[i] = Double.NaN;
            } else {
                ret[i] = Double.parseDouble(members[i]);
            }
        }

        return ret;
    }

    /**
     * Get double array property from the first Props object that contains it
     */
    public static double[] getDoubleArray(final Collection<? extends Props> pCollection, final String key, final double[] def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getDoubleArray(key, def);
            }
        }
        return def;
    }

    /**
     * Get boolean property
     */
    public boolean getBoolean(final String key, final boolean def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        return Boolean.parseBoolean(s.trim());
    }

    /**
     * Get boolean property from the first Props object that contains it
     */
    public static boolean getBoolean(final Collection<? extends Props> pCollection, final String key, final boolean def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getBoolean(key, def);
            }
        }
        return def;
    }

    /**
     * Get boolean array property
     */
    public boolean[] getBooleanArray(final String key, final boolean[] def) {
        String s = hash.getProperty(key);
        if (s == null) {
            return def;
        }
        String[] members = s.split(",");

        boolean[] ret;
        ret = new boolean[members.length];
        for (int i = 0; i < members.length; i++) {
            ret[i] = Boolean.parseBoolean(members[i].trim());
        }

        return ret;
    }

    /**
     * Get boolean array property from the first Props object that contains it
     */
    public static boolean[] getBooleanArray(final Collection<? extends Props> pCollection, final String key, final boolean[] def) {
        for (Props p : pCollection) {
            if (p.containsKey(key)) {
                return p.getBooleanArray(key, def);
            }
        }
        return def;
    }

    public boolean containsKey(final String key) {
        return hash.containsKey(key);
    }
    
    public Set<String> keySet() {
        Set<String> keys = new LinkedHashSet<>();
        for (Object k : hash.keySet()) {
            keys.add(k.toString());
        }
        return keys;
    }

    /**
     * Save property file
     */
    public boolean save(final Path fname, boolean reload) {
        try {
        	// Reload the latest version before saving
            if (Files.exists(fname) && reload) {
                load(fname);
            }
            // Proceed with saving
            try (Writer w = Files.newBufferedWriter(fname)) {
                return save(w);
            }
        } catch (FileNotFoundException e) {
        	return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Save property file
     */
    public boolean save(final String fname) {
        try (Writer w = Core.resourceTree.newBufferedWriter(fname)) {
            return save(w);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Save property file
     */
    public boolean save(final Writer w) {
        try {
            hash.store(w, header);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Load property file
     */
    public boolean load(final Path fname) {
        try (Reader r = ToolBox.getBufferedReader(fname)) {
            hash.load(r);
            return true;
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Load property file
     */
    public boolean load(final URL file) {
        try (Reader r = ToolBox.getBufferedReader(file)) {
            hash.load(r);
            return true;
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Load property file
     */
    public boolean load(final Resource resource) {
        try (Reader r = resource.getBufferedReader()) {
            hash.load(r);
            return true;
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Load property file
     */
    public boolean load(final Reader r) {
        try {
            hash.load(r);
            return true;
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Returns the highest level number present for the supplied Group, in the records.
     */
    public int getHighestLevel(int groupNum) {
        Set<Object> keys = hash.keySet();
        int max = -1;
        for(Object k:keys) {
            String key = k.toString();
            String match = "group" + groupNum + "_level";
            if(key.startsWith(match)) {
                int idx2 = key.indexOf("_", match.length()-1);
                String tmpNum = key.substring(match.length(), idx2);
                try {
                    int num = Integer.parseInt(tmpNum);
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException e) {
                    //just catching errors because we have no way of trusting the data we're inputting.
                    //but we don't need to actually do anything with the error... just to prevent it from bringing down the whole system.
                }
            }
        }
        return max;
    }

}
