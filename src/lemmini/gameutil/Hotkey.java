/*
 * Copyright 2026 Will James.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemmini.gameutil;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * Represents a single hotkey mapping.
 * @author Will James
 */
public class Hotkey {

    private final RetroLemminiHotkeys.HotkeyAction action;  // Enum action
    private final String description;                       // user-friendly description
    private int keyCode;                                    // KeyEvent VK code
    private String keyDescription;                          // human-readable key (plus modifier if applicable)
    private String modifier;                                // optional: "Ctrl", "Shift", "Alt", "Meta"

    /** Constructor without modifier */
    /** Constructor without modifier or keyDescription (uses getKeyName automatically) */
    public Hotkey(RetroLemminiHotkeys.HotkeyAction action, int keyCode) {
        this.action = action;
        this.description = action.getDescription();
        this.keyCode = keyCode;
        this.keyDescription = RetroLemminiHotkeys.getKeyName(keyCode);
        this.modifier = null;
    }

    /** Constructor with modifier */
    public Hotkey(RetroLemminiHotkeys.HotkeyAction action, int keyCode, String modifier) {
        this(action, keyCode);
        this.modifier = modifier;
    }


    // Getters
    public RetroLemminiHotkeys.HotkeyAction getAction() { return action; }
    public String getDescription() { return description; }
    public int getKeyCode() { return keyCode; }

    /** Returns key string, including modifier if present */
    public String getKeyDescription() {
        if (modifier != null && !modifier.isEmpty()) {
            return modifier + "+" + keyDescription;
        }
        return keyDescription;
    }
    
	public void setKeyDescription(String keyDescription) {
		this.keyDescription = keyDescription;
	}

    public String getModifier() { return modifier; }

    // Setters
    public void setKey(int keyCode, String keyDescription) {
        this.keyCode = keyCode;
        this.setKeyDescription(keyDescription);
    }
    
    public void clearKey() {
        this.keyCode = KeyEvent.VK_UNDEFINED;
        this.modifier = null;
        this.keyDescription = RetroLemminiHotkeys.UNDEFINED;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /** Check if this hotkey matches a key code and modifier */
    public boolean matches(int keyCode, String modifier) {
        return this.keyCode == keyCode && Objects.equals(this.modifier, modifier);
    }

    @Override
    public String toString() {
        return getKeyDescription();
    }
    
    /** copy constructor */
    public Hotkey(Hotkey other) {
        this(other.getAction(), other.getKeyCode(), other.getModifier());
    }
}