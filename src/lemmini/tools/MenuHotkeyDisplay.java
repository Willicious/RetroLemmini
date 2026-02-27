package lemmini.tools;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lemmini.gameutil.Hotkey;
import lemmini.gameutil.RetroLemminiHotkeys;

public class MenuHotkeyDisplay {

    /** Apply hotkey to a JMenuItem using the activeHotkeys list */
    public static void applyHotkey(JMenuItem menuItem, RetroLemminiHotkeys.HotkeyAction action, List<Hotkey> activeHotkeys) {
        Hotkey hk = getHotkeyForAction(activeHotkeys, action);
        if (hk == null) return;

        String keyDescription = hk.getKeyDescription();
        if (!RetroLemminiHotkeys.UNDEFINED.equalsIgnoreCase(keyDescription)) {
            KeyStroke ks = getKeyStrokeFromHotkey(hk);
            if (ks != null) {
                menuItem.setAccelerator(ks); // puts hotkey string on the right
            }
        }
    }

    /** Find the Hotkey object for the given action */
    private static Hotkey getHotkeyForAction(List<Hotkey> hotkeys, RetroLemminiHotkeys.HotkeyAction action) {
        for (Hotkey hk : hotkeys) {
            if (hk.getAction() == action) return hk;
        }
        return null;
    }

    /** Convert Hotkey to KeyStroke for JMenuItem */
    private static KeyStroke getKeyStrokeFromHotkey(Hotkey hk) {
        if (hk.getKeyCode() == KeyEvent.VK_UNDEFINED) return null;

        int modifiers = 0;
        String mod = hk.getModifier();
        if ("Ctrl".equalsIgnoreCase(mod)) modifiers |= KeyEvent.CTRL_DOWN_MASK;
        if ("Shift".equalsIgnoreCase(mod)) modifiers |= KeyEvent.SHIFT_DOWN_MASK;
        if ("Alt".equalsIgnoreCase(mod)) modifiers |= KeyEvent.ALT_DOWN_MASK;
        if ("Meta".equalsIgnoreCase(mod)) modifiers |= KeyEvent.META_DOWN_MASK;

        return KeyStroke.getKeyStroke(hk.getKeyCode(), modifiers);
    }
}