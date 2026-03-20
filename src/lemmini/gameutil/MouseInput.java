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

import lemmini.game.GameController;
import lemmini.tools.Props;

import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Handles user-configurable mouse-to-action bindings
 * @author Will James
 */
public class MouseInput {

    public enum MouseAction {
        TOGGLEPAUSE,
        SELECTWALKER,
        DRAGVIEWAREA,
        FASTSCROLL,
        RELEASERATEDOWN,
        RELEASERATEUP
    }
    
    public boolean clickAirToCancelReplay;
    public boolean enableWheelSkillSelect;
    public boolean enableWheelBrushSize;

    public final Map<Integer, List<MouseAction>> buttonMap =
            new HashMap<Integer, List<MouseAction>>();

    public MouseInput() {
        setDefaultMappings();
    }

    public void setDefaultMappings() {
    	clearMappings();

        addMapping(MouseEvent.BUTTON2, MouseAction.TOGGLEPAUSE);
        addMapping(MouseEvent.BUTTON3, MouseAction.DRAGVIEWAREA);
        addMapping(MouseEvent.BUTTON3, MouseAction.SELECTWALKER);
        addMapping(MouseEvent.BUTTON3, MouseAction.FASTSCROLL);
        addMapping(4, MouseAction.RELEASERATEDOWN);
        addMapping(5, MouseAction.RELEASERATEUP);
        
        clickAirToCancelReplay = true;
        enableWheelSkillSelect = false;
        enableWheelBrushSize = true;
    }

    public void clearMappings() {
        buttonMap.clear();
    }

    public void addMapping(int mouseButton, MouseAction action) {
        List<MouseAction> list = buttonMap.get(mouseButton);
        if (list == null) {
            list = new ArrayList<MouseAction>();
            buttonMap.put(mouseButton, list);
        }
        if (!list.contains(action)) {
            list.add(action);
        }
    }

    public void removeMapping(int mouseButton, MouseAction action) {
        List<MouseAction> list = buttonMap.get(mouseButton);
        if (list != null) {
            list.remove(action);
            if (list.isEmpty()) {
                buttonMap.remove(mouseButton);
            }
        }
    }

    public List<MouseAction> getActionsForButton(int mouseButton) {
        List<MouseAction> list = buttonMap.get(mouseButton);
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<MouseAction>(list);
    }

    public Map<Integer, List<MouseAction>> getButtonMap() {
        return buttonMap;
    }
    
    private void applyOptionChanges() {
        GameController.setOption(GameController.Option.CLICK_AIR_TO_CANCEL_REPLAY, clickAirToCancelReplay);
        GameController.setOption(GameController.Option.ENABLE_WHEEL_SKILL_SELECT, enableWheelSkillSelect);
        GameController.setOption(GameController.Option.ENABLE_WHEEL_BRUSH_SIZE, enableWheelBrushSize);
    }
    
    public void loadFromProgramProps(Props props) {
        clickAirToCancelReplay = props.getBoolean("clickAirToCancelReplay", true);
        enableWheelSkillSelect = props.getBoolean("enableWheelSkillSelect", false);
        enableWheelBrushSize = props.getBoolean("enableWheelBrushSize", true);
        applyOptionChanges();

        clearMappings();

        for (MouseAction action : MouseAction.values()) {
            String key = "mouseInput." + action.name();
            String value = props.get(key, null);
            if (value != null && !value.trim().isEmpty()) {
                for (String part : value.split(",")) {
                    try {
                        int button = Integer.parseInt(part.trim());
                        addMapping(button, action);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (buttonMap.isEmpty()) {
            setDefaultMappings();
        }
    }
    
    public void saveToProgramProps(Props props) {
        props.setBoolean("clickAirToCancelReplay", clickAirToCancelReplay);
        props.setBoolean("enableWheelSkillSelect", enableWheelSkillSelect);
        props.setBoolean("enableWheelBrushSize", enableWheelBrushSize);
        applyOptionChanges();
    	
        for (MouseAction action : MouseAction.values()) {
            props.remove("mouseInput." + action.name());
        }
        for (MouseAction action : MouseAction.values()) {
            StringBuilder csv = new StringBuilder();
            for (Integer b : buttonMap.keySet()) {
                if (buttonMap.get(b).contains(action)) {
                    if (csv.length() > 0) csv.append(",");
                    csv.append(b);
                }
            }
            if (csv.length() > 0) {
                props.set("mouseInput." + action.name(), csv.toString());
            }
        }
    }
}