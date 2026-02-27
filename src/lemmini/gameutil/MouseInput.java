package lemmini.gameutil;

import lemmini.game.GameController;
import lemmini.tools.Props;

import java.awt.event.MouseEvent;
import java.util.*;

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
        GameController.setOption(GameController.RetroLemminiOption.CLICK_AIR_TO_CANCEL_REPLAY, clickAirToCancelReplay);
        GameController.setOption(GameController.RetroLemminiOption.ENABLE_WHEEL_SKILL_SELECT, enableWheelSkillSelect);
        GameController.setOption(GameController.RetroLemminiOption.ENABLE_WHEEL_BRUSH_SIZE, enableWheelBrushSize);
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