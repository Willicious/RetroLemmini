package lemmini.gui;

import lemmini.game.Core;
import lemmini.gameutil.Hotkey;
import lemmini.gameutil.RetroLemminiHotkeys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.KeyboardFocusManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Hotkey configuration dialog for RetroLemmini.
 */
public class HotkeyConfig extends JDialog {

    private static final long serialVersionUID = 1L;

    private final List<Hotkey> hotkeys = new ArrayList<>();
    private final Map<RetroLemminiHotkeys.HotkeyAction, JButton> actionButtons = new HashMap<>();
    private final Map<RetroLemminiHotkeys.HotkeyAction, String> currentKeys = new HashMap<>();
    private final Path hotkeysIniPath = Core.settingsPath.resolve("retrolemmini_hotkeys.ini");

    private String pendingModifier = null;
    private KeyEventDispatcher activeDispatcher = null;

    public HotkeyConfig(List<Hotkey> defaultHotkeys) {
        this.hotkeys.addAll(defaultHotkeys);
        setTitle("Hotkey Configuration");
        setModal(true);
        setResizable(false);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadHotkeysFromIni();
        initComponents();

        pack();
        setLocationRelativeTo(null);
    }

    /** Load hotkeys from INI, fallback to defaults */
    private void loadHotkeysFromIni() {
        if (!Files.exists(hotkeysIniPath)) return;

        try (BufferedReader reader = Files.newBufferedReader(hotkeysIniPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;

                String actionName = parts[0].trim();
                String keyString = parts[1].trim();

                try {
                    RetroLemminiHotkeys.HotkeyAction action = RetroLemminiHotkeys.HotkeyAction.valueOf(actionName);
                    Hotkey hk = getHotkey(action);
                    if (hk != null) {
                    	String[] keyParts = keyString.split("\\+");
                    	if (keyParts.length == 2) {
                    	    hk.setModifier(keyParts[0]);
                    	    hk.setKey(RetroLemminiHotkeys.getKeyCode(keyParts[1]), keyParts[1]);
                    	} else {
                    	    hk.setModifier(null);
                    	    hk.setKey(RetroLemminiHotkeys.getKeyCode(keyString), keyString);
                    	}
                        currentKeys.put(action, hk.getKeyDescription());
                    }
                } catch (IllegalArgumentException ex) {
                    // Unknown action in INI, skip
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel hotkeysPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(hotkeysPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();

        int row = 0;
        int col = 0;
        final int maxRows = 17;
        final int columnPadding = 35;
        final int buttonWidth = 90;
        final int buttonHeight = 25;

        // Insets
        Insets buttonInsets       = new Insets(2, 8, 2, 6);   // button → label gap
        Insets labelInsets        = new Insets(2, 6, 2, 12);  // label → next column
        Insets columnButtonInsets = new Insets(2, columnPadding, 2, 6); // first button in column

        for (Hotkey hk : hotkeys) {

            boolean firstRowInColumn = (row == 0);

            // ---------- Button ----------
            JButton btn = new JButton(hk.getKeyDescription());
            btn.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
            btn.setMinimumSize(new Dimension(buttonWidth, buttonHeight));

            gbc.gridx = col * 2;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;

            if (col > 0) {
                gbc.insets = new Insets(2, columnPadding, 2, 6);
            } else {
                gbc.insets = buttonInsets;
            }

            hotkeysPanel.add(btn, gbc);

            // ---------- Label ----------
            JLabel lbl = new JLabel(hk.getDescription());

            gbc.gridx = col * 2 + 1;
            gbc.weightx = 1.0;               // label expands
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = labelInsets;

            hotkeysPanel.add(lbl, gbc);

            // Store references
            actionButtons.put(hk.getAction(), btn);
            currentKeys.put(hk.getAction(), hk.getKeyDescription());
            btn.addActionListener(e -> startListening(btn, hk));

            row++;
            if (row >= maxRows) {
                row = 0;
                col++;
            }
        }

        // Bottom panel with Save / Cancel
     // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel tipLabel = new JLabel("TIP: When assigning a modifier key (Ctrl, Shift, Alt), press the modifier first, then the main key");
        tipLabel.setFont(tipLabel.getFont().deriveFont(Font.PLAIN, 11f)); // smaller font
        tipLabel.setForeground(Color.DARK_GRAY);                           // subtle gray color
        tipPanel.add(tipLabel);
        bottomPanel.add(tipPanel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        saveBtn.addActionListener(e -> saveHotkeys());
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Dynamically calculate dialog width based on content
        hotkeysPanel.doLayout(); // ensure component sizes are calculated
        int columns = (int) Math.ceil(hotkeys.size() / (double) maxRows);
        int totalWidth = 0;
        for (int c = 0; c < columns; c++) {
            int maxLabel = 0;
            for (Component comp : hotkeysPanel.getComponents()) {
                GridBagLayout layout = (GridBagLayout) hotkeysPanel.getLayout();
                GridBagConstraints cst = layout.getConstraints(comp);
                if (cst.gridx / 2 == c && comp instanceof JLabel) {
                    maxLabel = Math.max(maxLabel, comp.getPreferredSize().width);
                }
            }
            totalWidth += maxLabel + buttonHeight + columnPadding;
        }
        totalWidth += 260; // extra margin
        int totalHeight = Math.min(maxRows * 30 + 100, 800);

        setPreferredSize(new Dimension(totalWidth, totalHeight));
        pack();
    }

    private void startListening(JButton btn, Hotkey hotkey) {
        if (activeDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .removeKeyEventDispatcher(activeDispatcher);
        }

        // Cancel if same button clicked
        if ("Press key...".equals(btn.getText())) {
            btn.setText(getKeyForAction(hotkey.getAction()));
            return;
        }

        restoreOtherListeningButtons(btn);
        btn.setText("Press key...");
        pendingModifier = null;

        activeDispatcher = e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;

            int code = e.getKeyCode();

            // Modifier
            if (isModifierKey(code)) {
                pendingModifier = modifierName(code);
                return true;
            }

            // Ignore if only modifier pressed
            if (isModifierKey(code)) return true;

            // Duplicate detection
            String modifier = pendingModifier;
            int newKeyCode = code;

            for (Hotkey other : hotkeys) {
                if (RetroLemminiHotkeys.conflicts(
                        hotkey,
                        other,
                        newKeyCode,
                        modifier)) {

                    other.clearKey();

                    JButton otherBtn = actionButtons.get(other.getAction());
                    if (otherBtn != null) {
                        otherBtn.setText("Unassigned");
                    }
                }
            }

            // Set the new key
            String keyName = RetroLemminiHotkeys.getKeyName(code);
            hotkey.setKey(code, keyName);
            hotkey.setModifier(pendingModifier);
            btn.setText(hotkey.getKeyDescription());
            currentKeys.put(hotkey.getAction(), hotkey.getKeyDescription());

            pendingModifier = null;
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .removeKeyEventDispatcher(activeDispatcher);
            activeDispatcher = null;

            return true;
        };

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(activeDispatcher);
    }
    
    private String getKeyForAction(RetroLemminiHotkeys.HotkeyAction action) {
        return currentKeys.getOrDefault(action, "Unassigned");
    }

    private void restoreOtherListeningButtons(JButton currentBtn) {
        for (Map.Entry<RetroLemminiHotkeys.HotkeyAction, JButton> entry : actionButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (btn != currentBtn && "Press key...".equals(btn.getText())) {
                btn.setText(getKeyForAction(entry.getKey()));
            }
        }
    }

    /** Save hotkeys to INI */
    private void saveHotkeys() {
        try (BufferedWriter writer = Files.newBufferedWriter(hotkeysIniPath)) {
            for (Hotkey hk : hotkeys) {
                writer.write(hk.getAction().name() + "=" + hk.getKeyDescription());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispose();
    }

    private boolean isModifierKey(int code) {
        return code == KeyEvent.VK_SHIFT
                || code == KeyEvent.VK_CONTROL
                || code == KeyEvent.VK_ALT
                || code == KeyEvent.VK_META;
    }

    private String modifierName(int code) {
        switch (code) {
            case KeyEvent.VK_SHIFT: return "Shift";
            case KeyEvent.VK_CONTROL: return "Ctrl";
            case KeyEvent.VK_ALT: return "Alt";
            case KeyEvent.VK_META: return "Meta";
            default: return "";
        }
    }

    /** Get hotkey by action */
    public Hotkey getHotkey(RetroLemminiHotkeys.HotkeyAction action) {
        for (Hotkey hk : hotkeys) {
            if (hk.getAction() == action) return hk;
        }
        return null;
    }

    /** Get all hotkeys */
    public List<Hotkey> getAllHotkeys() {
        return Collections.unmodifiableList(hotkeys);
    }
}