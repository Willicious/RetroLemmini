package lemmini.game;

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
 * Storage class for replay level info.
 * @author Volker Oth
 */
public class ReplayLevelInfo {
    /** name of level pack */
    private String levelPack;
    /** rating number */
    private int ratingNumber;
    /** rating name */
    private String ratingName;
    /** level number */
    private int lvlNumber;
    /** level name */
    private String lvlName;

    /**
     * Set name of level pack.
     */
    public void setLevelPack(final String levelPack) {
        this.levelPack = levelPack;
    }
    /**
     * Get name of level pack.
     */
    public String getLevelPack() {
        return levelPack;
    }

    /**
     * Set rating number.
     */
    public void setRatingNumber(final int ratingNumber) {
        this.ratingNumber = ratingNumber;
    }

    /**
     * Get rating number.
     */
    public int getRatingNumber() {
        return ratingNumber;
    }

    /**
     * Set rating name.
     */
    public void setRatingName(final String ratingName) {
        this.ratingName = ratingName;
    }

    /**
     * Get rating name.
     */
    public String getRatingName() {
        return ratingName;
    }

    /**
     * Set level number.
     */
    public void setLvlNumber(final int lvlNumber) {
        this.lvlNumber = lvlNumber;
    }

    /**
     * Get level number.
     */
    public int getLvlNumber() {
        return lvlNumber;
    }

    /**
     * Set level name.
     */
    public void setLvlName(final String lvlName) {
        this.lvlName = lvlName;
    }

    /**
     * Get level name.
     */
    public String getLvlName() {
        return lvlName;
    }
}
