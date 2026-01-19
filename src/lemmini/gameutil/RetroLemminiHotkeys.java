package lemmini.gameutil;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default hotkeys and key factory for RetroLemmini.
 */
public class RetroLemminiHotkeys {
	
	/** Hotkey-specific scope */
	public enum HotkeyScope {
	    ANYWHERE,
	    IN_GAME,
	    PREVIEW,
	    POSTVIEW,
	    DEBUG
	}

    /** Enum for all actions */
	public enum HotkeyAction {
		HotkeyToggleMusic("(In-Game) Mute/unmute music", HotkeyScope.IN_GAME),
		HotkeyToggleSound("(In-Game) Mute/unmute sound", HotkeyScope.IN_GAME),
	    HotkeyPause("(In-Game) Pause the game", HotkeyScope.IN_GAME),
	    HotkeyRestart("(In-Game) Restart the game", HotkeyScope.IN_GAME),
	    HotkeyNuke("(In-Game) Nuke the level", HotkeyScope.IN_GAME),
	    HotkeyDecreaseRR("(In-Game) Decrease release rate", HotkeyScope.IN_GAME),
	    HotkeyIncreaseRR("(In-Game) Increase release rate", HotkeyScope.IN_GAME),
	    HotkeySelectClimber("(In-Game) Select Climber skill", HotkeyScope.IN_GAME),
	    HotkeySelectFloater("(In-Game) Select Floater skill", HotkeyScope.IN_GAME),
	    HotkeySelectBomber("(In-Game) Select Bomber skill", HotkeyScope.IN_GAME),
	    HotkeySelectBlocker("(In-Game) Select Blocker skill", HotkeyScope.IN_GAME),
	    HotkeySelectBuilder("(In-Game) Select Builder skill", HotkeyScope.IN_GAME),
	    HotkeySelectBasher("(In-Game) Select Basher skill", HotkeyScope.IN_GAME),
	    HotkeySelectMiner("(In-Game) Select Miner skill", HotkeyScope.IN_GAME),
	    HotkeySelectDigger("(In-Game) Select Digger skill", HotkeyScope.IN_GAME),
	    HotkeyToggleVerticalLock("(In-Game) Toggle Vertical Lock", HotkeyScope.IN_GAME),
	    HotkeyFastForward("(In-Game) Fast-Forward", HotkeyScope.IN_GAME),
	    HotkeyTurboForward("(In-Game) Turbo Fast-Forward", HotkeyScope.IN_GAME),
	    HotkeySaveAsImage("(In-Game + Preview) Save level as image", HotkeyScope.IN_GAME, HotkeyScope.PREVIEW),
	    HotkeySelectLeft("(In-Game) Select left-facing lemming", HotkeyScope.IN_GAME),
	    HotkeySelectRight("(In-Game) Select right-facing lemming", HotkeyScope.IN_GAME),
	    HotkeySelectWalker("(In-Game) Select walking lemming", HotkeyScope.IN_GAME),
	    HotkeyNudgeViewLeft("(In-Game) Nudge viewport left", HotkeyScope.IN_GAME),
	    HotkeyNudgeViewRight("(In-Game) Nudge viewport right", HotkeyScope.IN_GAME),
	    HotkeyNudgeViewUp("(In-Game) Nudge viewport up", HotkeyScope.IN_GAME),
	    HotkeyNudgeViewDown("(In-Game) Nudge viewport down", HotkeyScope.IN_GAME),
	    HotkeyEndLevel("(In-Game) End gameplay", HotkeyScope.IN_GAME),
	    HotkeySaveReplay("(In-Game + Postview) Save replay", HotkeyScope.IN_GAME, HotkeyScope.POSTVIEW),
	    HotkeyLoadReplay("(Anywhere) Load replay", HotkeyScope.ANYWHERE),
	    HotkeyCancelReplay("(In-Game) Cancel replay", HotkeyScope.IN_GAME),
	    HotkeyNextLevel("(Preview) Next level", HotkeyScope.PREVIEW),
	    HotkeyPreviousLevel("(Preview) Previous level", HotkeyScope.PREVIEW),
	    HotkeyNextGroup("(Preview) Next group", HotkeyScope.PREVIEW),
	    HotkeyPreviousGroup("(Preview) Previous group", HotkeyScope.PREVIEW),
	    HotkeyDebugSaveAll("(Debug) Set saved lems to maximum", HotkeyScope.DEBUG),
	    HotkeyDebugInvertTimer("(Debug) Invert timer direction", HotkeyScope.DEBUG),
	    HotkeyDebugToggleSuperLemming("(Debug) Toggle superlemming mode", HotkeyScope.DEBUG),
	    HotkeyDebugToggleDrawMode("(Debug) Toggle draw mode", HotkeyScope.DEBUG),
	    HotkeyDebugToggleDebug("(Debug) Toggle debug mode", HotkeyScope.DEBUG),
	    HotkeyDebugPrintLevelName("(Debug) Print level name to console", HotkeyScope.DEBUG),
	    HotkeyDebugAddLemAtCursor("(Debug) Add lemming at cursor", HotkeyScope.DEBUG),
	    HotkeyToggleMenuBar("(Anywhere) Toggle menu bar", HotkeyScope.ANYWHERE),
		HotkeyManagePlayers("(Anywhere) Manage players", HotkeyScope.ANYWHERE),
		HotkeyLevelSelect("(Anywhere) Select level", HotkeyScope.ANYWHERE),
		HotkeyEnterCode("(Anywhere) Enter code", HotkeyScope.ANYWHERE),
		HotkeyOpenSettings("(Anywhere) Options", HotkeyScope.ANYWHERE),
		HotkeyManageHotkeys("(Anywhere) Hotkeys", HotkeyScope.ANYWHERE),
		HotkeyAbout("(Anywhere) About RetroLemmini", HotkeyScope.ANYWHERE),
		HotkeyCloseApp("(Anywhere) Close RetroLemmini", HotkeyScope.ANYWHERE);

	    private final String description;
	    private final EnumSet<HotkeyScope> scopes;

	    HotkeyAction(String description, HotkeyScope... scopes) {
	        this.description = description;
	        this.scopes = EnumSet.copyOf(Arrays.asList(scopes));
	    }

	    public String getDescription() {
	        return description;
	    }

	    public EnumSet<HotkeyScope> getScopes() {
	        return scopes;
	    }

	    public boolean isAnywhere() {
	        return scopes.contains(HotkeyScope.ANYWHERE);
	    }
	}

    /**
     * Returns default hotkeys as a list of Hotkey objects.
     */
	public static List<Hotkey> getDefaultHotkeys() {
	    List<Hotkey> hotkeys = new ArrayList<>();
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyToggleMusic, KeyEvent.VK_M));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyToggleSound, KeyEvent.VK_Z));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyPause, KeyEvent.VK_P));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyPause, KeyEvent.VK_SPACE));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyRestart, KeyEvent.VK_R, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNuke, KeyEvent.VK_N));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDecreaseRR, KeyEvent.VK_MINUS));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyIncreaseRR, KeyEvent.VK_EQUALS));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectClimber, KeyEvent.VK_1));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectFloater, KeyEvent.VK_2));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectBomber, KeyEvent.VK_3));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectBlocker, KeyEvent.VK_4));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectBuilder, KeyEvent.VK_5));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectBasher, KeyEvent.VK_6));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectMiner, KeyEvent.VK_7));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectDigger, KeyEvent.VK_8));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyToggleVerticalLock, KeyEvent.VK_V));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyFastForward, KeyEvent.VK_F));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyTurboForward, KeyEvent.VK_T));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectLeft, KeyEvent.VK_LEFT));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectRight, KeyEvent.VK_RIGHT));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectWalker, KeyEvent.VK_W));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySelectWalker, KeyEvent.VK_UP));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNudgeViewLeft, KeyEvent.VK_LEFT, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNudgeViewRight, KeyEvent.VK_RIGHT, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNudgeViewUp, KeyEvent.VK_UP, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNudgeViewDown, KeyEvent.VK_DOWN, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyEndLevel, KeyEvent.VK_ESCAPE));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySaveReplay, KeyEvent.VK_S, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyLoadReplay, KeyEvent.VK_L, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyCancelReplay, KeyEvent.VK_C));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeySaveAsImage, KeyEvent.VK_I));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNextLevel, KeyEvent.VK_RIGHT));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyPreviousLevel, KeyEvent.VK_LEFT));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyNextGroup, KeyEvent.VK_UP));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyPreviousGroup, KeyEvent.VK_DOWN));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugSaveAll, KeyEvent.VK_NUMPAD1));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugInvertTimer, KeyEvent.VK_NUMPAD2));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugToggleSuperLemming, KeyEvent.VK_NUMPAD3));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugToggleDrawMode, KeyEvent.VK_D));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugToggleDebug, KeyEvent.VK_D, "Ctrl"));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugPrintLevelName, KeyEvent.VK_NUMPAD4));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyDebugAddLemAtCursor, KeyEvent.VK_L));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyToggleMenuBar, KeyEvent.VK_F1));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyManagePlayers, KeyEvent.VK_F2));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyLevelSelect, KeyEvent.VK_F3));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyEnterCode, KeyEvent.VK_F4));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyOpenSettings, KeyEvent.VK_F5));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyManageHotkeys, KeyEvent.VK_F6));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyAbout, KeyEvent.VK_F7));
	    hotkeys.add(new Hotkey(HotkeyAction.HotkeyCloseApp, KeyEvent.VK_F8, "Alt"));

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
        addKey("Minus", KeyEvent.VK_MINUS);
        addKey("Equals", KeyEvent.VK_EQUALS);
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
    
    /** Detect scope conflicts to be resolved */
    public static boolean conflicts(
            Hotkey target,
            Hotkey other,
            int proposedKeyCode,
            String proposedModifier) {

        if (target == other) return false;

        // Unassigned keys never conflict
        if (proposedKeyCode == KeyEvent.VK_UNDEFINED
         || other.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            return false;
        }

        // Different key or modifier → no conflict
        if (proposedKeyCode != other.getKeyCode()) return false;
        if (!Objects.equals(proposedModifier, other.getModifier())) return false;

        EnumSet<HotkeyScope> scopesA = target.getAction().getScopes();
        EnumSet<HotkeyScope> scopesB = other.getAction().getScopes();

        // ANYWHERE must be globally unique
        if (scopesA.contains(HotkeyScope.ANYWHERE)
         || scopesB.contains(HotkeyScope.ANYWHERE)) {
            return true;
        }

        // Same scope = conflict
        for (HotkeyScope scope : scopesA) {
            if (scopesB.contains(scope)) {
                return true;
            }
        }

        return false;
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