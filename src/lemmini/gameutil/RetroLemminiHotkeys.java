package lemmini.gameutil;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default hotkeys and key factory for RetroLemmini.
 */
public class RetroLemminiHotkeys {

    /** Enum for all actions */
	public enum HotkeyAction {
	    HotkeyPause("Pause the game"),
	    HotkeyExample2("Hotkey example 5"),
	    HotkeyExample3("Hotkey example 5"),
	    HotkeyExample4("Hotkey example 5"),
	    HotkeyExample5("Hotkey example 5"),
	    HotkeyExample6("Hotkey example 6"),
	    HotkeyExample7("Hotkey example 7"),
	    HotkeyExample8("Hotkey example 8"),
	    HotkeyExample9("Hotkey example 9"),
	    HotkeyExample10("An example of a very long hotkey in the first column"),
	    HotkeyExample11("Hotkey example 11"),
	    HotkeyExample12("Hotkey example 12"),
	    HotkeyExample13("Hotkey example 13"),
	    HotkeyExample14("Hotkey example 14"),
	    HotkeyExample15("Hotkey example 15"),
	    HotkeyExample16("Hotkey example 16"),
	    HotkeyExample17("Hotkey example 17"),
	    HotkeyExample18("Hotkey example 18"),
	    HotkeyExample19("Hotkey example 19"),
	    HotkeyExample20("An example of a very long hotkey in the second column"),
	    HotkeyExample21("Hotkey example 21"),
	    HotkeyExample22("Hotkey example 22"),
	    HotkeyExample23("Hotkey example 23"),
	    HotkeyExample24("Hotkey example 24"),
	    HotkeyExample25("Hotkey example 25"),
	    HotkeyExample26("Hotkey example 26"),
	    HotkeyExample27("Hotkey example 27"),
	    HotkeyExample28("Hotkey example 28"),
	    HotkeyExample29("Hotkey example 29"),
	    HotkeyExample30("Blibbidy blobbidy bloop"),
	    HotkeyExample31("Hotkey example 31"),
	    HotkeyExample32("Hotkey example 32"),
	    HotkeyExample33("Hotkey example 33"),
	    HotkeyExample34("An example of an even longer hotkey in the third column."),
	    HotkeyExample35("Hotkey example 35"),
	    HotkeyExample36("Hotkey example 36"),
	    HotkeyExample37("Hotkey example 37"),
	    HotkeyExample38("Hotkey example 38"),
	    HotkeyExample39("Hotkey example 39"),
	    HotkeyExample40("Hotkey example 40");

	    private final String description;

	    HotkeyAction(String description) {
	        this.description = description;
	    }

	    public String getDescription() {
	        return description;
	    }
	}

    /**
     * Returns default hotkeys as a list of Hotkey objects.
     */
	public static List<Hotkey> getDefaultHotkeys() {
	    List<Hotkey> hotkeys = new ArrayList<>();
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyPause, KeyEvent.VK_SPACE));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample2, KeyEvent.VK_A));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample3, KeyEvent.VK_B));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample4, KeyEvent.VK_C));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample5, KeyEvent.VK_D));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample6, KeyEvent.VK_E));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample7, KeyEvent.VK_F));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample8, KeyEvent.VK_G));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample9, KeyEvent.VK_H));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample10, KeyEvent.VK_I));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample11, KeyEvent.VK_J));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample12, KeyEvent.VK_K));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample13, KeyEvent.VK_L));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample14, KeyEvent.VK_M));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample15, KeyEvent.VK_N));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample16, KeyEvent.VK_O));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample17, KeyEvent.VK_P));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample18, KeyEvent.VK_Q));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample19, KeyEvent.VK_R));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample20, KeyEvent.VK_S));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample21, KeyEvent.VK_T));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample22, KeyEvent.VK_U));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample23, KeyEvent.VK_V));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample24, KeyEvent.VK_W));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample25, KeyEvent.VK_X));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample26, KeyEvent.VK_Y));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample27, KeyEvent.VK_Z));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample28, KeyEvent.VK_1));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample29, KeyEvent.VK_2));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample30, KeyEvent.VK_3));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample31, KeyEvent.VK_F1));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample32, KeyEvent.VK_F2));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample33, KeyEvent.VK_F3));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample34, KeyEvent.VK_F4));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample35, KeyEvent.VK_F5));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample36, KeyEvent.VK_F6));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample37, KeyEvent.VK_F7));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample38, KeyEvent.VK_F8));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample39, KeyEvent.VK_F9));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyExample40, KeyEvent.VK_F10));

	    return hotkeys;
	}
	
    private static final Map<String, Integer> nameToCode = new HashMap<>();
    private static final Map<Integer, String> codeToName = new HashMap<>();

    static {
        // Letters
        for (char c = 'A'; c <= 'Z'; c++) addKey(String.valueOf(c), KeyEvent.getExtendedKeyCodeForChar(c));
        // Top-row numbers
        for (char c = '0'; c <= '9'; c++) addKey(String.valueOf(c), KeyEvent.getExtendedKeyCodeForChar(c));
        // Numpad 0-9
        for (int i = 0; i <= 9; i++) addKey("Num" + i, KeyEvent.VK_NUMPAD0 + i);
        // Function keys
        for (int i = 1; i <= 12; i++) addKey("F" + i, KeyEvent.VK_F1 + (i - 1));
        // Arrows
        addKey("Up", KeyEvent.VK_UP);
        addKey("Down", KeyEvent.VK_DOWN);
        addKey("Left", KeyEvent.VK_LEFT);
        addKey("Right", KeyEvent.VK_RIGHT);
        // Common keys
        addKey("Enter", KeyEvent.VK_ENTER);
        addKey("Escape", KeyEvent.VK_ESCAPE);
        addKey("Space", KeyEvent.VK_SPACE);
        addKey("Tab", KeyEvent.VK_TAB);
        addKey("Caps", KeyEvent.VK_CAPS_LOCK);
        addKey("Backspace", KeyEvent.VK_BACK_SPACE);
        addKey("Insert", KeyEvent.VK_INSERT);
        addKey("Delete", KeyEvent.VK_DELETE);
        addKey("Home", KeyEvent.VK_HOME);
        addKey("End", KeyEvent.VK_END);
        addKey("PgUp", KeyEvent.VK_PAGE_UP);
        addKey("PgDown", KeyEvent.VK_PAGE_DOWN);
    }

    private static void addKey(String name, int code) {
        nameToCode.put(name, code);
        codeToName.put(code, name);
    }

    /** Convert key name string (from INI) to KeyEvent code */
    public static int getKeyCode(String keyName) {
        if (keyName == null || keyName.isEmpty()) return KeyEvent.VK_UNDEFINED;
        keyName = keyName.trim();

        if (nameToCode.containsKey(keyName)) return nameToCode.get(keyName);

        // Single character fallback
        if (keyName.length() == 1) return KeyEvent.getExtendedKeyCodeForChar(keyName.charAt(0));

        return KeyEvent.VK_UNDEFINED;
    }

    /** Convert KeyEvent code to display string (for INI / UI) */
    public static String getKeyName(int keyCode) {
        if (codeToName.containsKey(keyCode)) return codeToName.get(keyCode);
        return KeyEvent.getKeyText(keyCode);
    }

    /** Modifier handling */
    public static boolean isModifierKey(int keyCode) {
        return keyCode == KeyEvent.VK_CONTROL
            || keyCode == KeyEvent.VK_SHIFT
            || keyCode == KeyEvent.VK_ALT
            || keyCode == KeyEvent.VK_META;
    }

    public static String modifierName(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_CONTROL: return "Ctrl";
            case KeyEvent.VK_SHIFT: return "Shift";
            case KeyEvent.VK_ALT: return "Alt";
            case KeyEvent.VK_META: return "Meta";
            default: return "";
        }
    }
    
    /**
     * Returns the HotkeyAction corresponding to the given KeyEvent, or null if none matches.
     * @param e the KeyEvent
     * @param config the HotkeyConfig holding all hotkeys
     * @return the matching HotkeyAction, or null
     */
    public static RetroLemminiHotkeys.HotkeyAction getHotkeyActionForEvent(KeyEvent e, List<Hotkey> hotkeys) {
        if (hotkeys == null) return null;

        int code = e.getKeyCode();
        String modifier = null;
        if (e.isControlDown()) modifier = "Ctrl";
        else if (e.isShiftDown()) modifier = "Shift";
        else if (e.isAltDown()) modifier = "Alt";
        else if (e.isMetaDown()) modifier = "Meta";

        for (Hotkey hk : hotkeys) {
            if (hk.matches(code, modifier)) {
                return hk.getAction();
            }
        }
        return null;
    }
}