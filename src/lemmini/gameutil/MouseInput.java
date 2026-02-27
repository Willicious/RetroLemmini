package lemmini.gameutil;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import lemmini.game.GameController;

import java.awt.event.MouseEvent;
import java.util.*;

public class MouseInput {

    public enum MouseAction {
        TOGGLEPAUSE,
        SELECTWALKER,
        DRAGVIEWAREA,
        FASTSCROLL
    }

    public final Map<Integer, List<MouseAction>> buttonMap =
            new HashMap<Integer, List<MouseAction>>();

    public MouseInput() {
        setDefaultMappings();
    }

    public void setDefaultMappings() {
        buttonMap.clear();

        addMapping(MouseEvent.BUTTON2, MouseAction.TOGGLEPAUSE);
        addMapping(MouseEvent.BUTTON3, MouseAction.DRAGVIEWAREA);
        addMapping(MouseEvent.BUTTON3, MouseAction.SELECTWALKER);
        addMapping(MouseEvent.BUTTON3, MouseAction.FASTSCROLL);
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
    
    public void loadFromProperties(Path path) {
        if (path == null || !Files.exists(path)) return;
        Properties props = new Properties();
        try {
            InputStream in = Files.newInputStream(path);
            props.load(in);
            in.close();
            clearMappings();
            
            for (MouseAction action : MouseAction.values()) {
                String key = "mouseInput." + action.name();
                String value = props.getProperty(key);
                if (value != null && !value.trim().isEmpty()) {
                    String[] parts = value.split(",");
                    for (String part : parts) {
                        try {
                            int button = Integer.parseInt(part.trim());
                            addMapping(button, action);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            // If nothing loaded, restore defaults
            if (buttonMap.isEmpty()) {
                setDefaultMappings();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveToProperties(Path path, boolean enableWheelSkillSelect, boolean enableWheelBrushSize) {
        if (path == null) return;
        Properties props = new Properties();
        try {
            // Preserve existing settings
            if (Files.exists(path)) {
                InputStream in = Files.newInputStream(path);
                props.load(in);
                in.close();
            }
            // Remove all existing mouseInput.* keys first
            for (Object keyObj : new java.util.HashSet<Object>(props.keySet())) {
                String key = keyObj.toString();
                if (key.startsWith("mouseInput.")) {
                    props.remove(key);
                }
            }
            // Write current mappings
            for (MouseAction action : MouseAction.values()) {
                StringBuilder csv = new StringBuilder();
                for (Integer button : buttonMap.keySet()) {
                    if (buttonMap.get(button).contains(action)) {
                        if (csv.length() > 0) csv.append(",");
                        csv.append(button);
                    }
                }
                if (csv.length() > 0) {
                    String key = "mouseInput." + action.name();
                    props.setProperty(key, csv.toString());
                }
            }
            
            // Save general options
            props.setProperty("enableWheelSkillSelect", Boolean.toString(enableWheelSkillSelect));
            props.setProperty("enableWheelBrushSize", Boolean.toString(enableWheelBrushSize));

            // Write back to file
            OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            props.store(out, "RetroLemmini Settings");
            out.close();

            // Apply changes immediately to GameController
            GameController.setOption(GameController.RetroLemminiOption.ENABLE_WHEEL_SKILL_SELECT, enableWheelSkillSelect);
            GameController.setOption(GameController.RetroLemminiOption.ENABLE_WHEEL_BRUSH_SIZE, enableWheelBrushSize);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}