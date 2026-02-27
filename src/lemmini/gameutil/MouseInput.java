package lemmini.gameutil;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MouseInput {

    public enum MouseAction {
        TOGGLE_PAUSE,
        SELECT_WALKER,
        DRAG_VIEW_AREA,
        FAST_SCROLL
    }

    public final static Map<Integer, List<MouseAction>> buttonMap = new HashMap<>();

    public MouseInput() {
        // initialize defaults
        addMapping(MouseEvent.BUTTON2, MouseAction.TOGGLE_PAUSE);
        addMapping(MouseEvent.BUTTON2, MouseAction.DRAG_VIEW_AREA);
        addMapping(MouseEvent.BUTTON3, MouseAction.SELECT_WALKER);
        addMapping(MouseEvent.BUTTON3, MouseAction.FAST_SCROLL);
    }

    /** Add an action to a button (multiple actions per button allowed) */
    public void addMapping(int mouseButton, MouseAction action) {
        buttonMap.computeIfAbsent(mouseButton, k -> new ArrayList<>()).add(action);
    }

    /** Remove an action from a button */
    public void removeMapping(int mouseButton, MouseAction action) {
        List<MouseAction> actions = buttonMap.get(mouseButton);
        if (actions != null) {
            actions.remove(action);
            if (actions.isEmpty()) {
                buttonMap.remove(mouseButton);
            }
        }
    }

    /** Call this whenever you want to trigger all actions for a button */
    public static void handleButton(int mouseButton, Consumer<MouseAction> handler) {
        List<MouseAction> actions = buttonMap.get(mouseButton);
        if (actions != null) {
            actions.forEach(handler);
        }
    }
}