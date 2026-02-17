package lemmini.gui;

import lemmini.game.Core;
import lemmini.gameutil.Hotkey;
import lemmini.gameutil.RetroLemminiHotkeys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final Path hotkeysIniPath = Core.getProgramHotkeysFilePath();

    private String pendingModifier = null;
    private KeyEventDispatcher activeDispatcher = null;
    
    private static final Color TRANSPARENT  = new Color(0, 0, 0, 0);
    
    private String defaultTip = "TIP: When assigning a modifier key (Ctrl, Shift, Alt), press the modifier first, then the main key";
    JLabel tipLabel = new JLabel(defaultTip);

    public HotkeyConfig() {
        hotkeys.clear();
        List<Hotkey> defaultHotkeys = RetroLemminiHotkeys.getHotkeys(RetroLemminiHotkeys.HotkeyProfile.DEFAULT);
        for (Hotkey hk : defaultHotkeys) hotkeys.add(new Hotkey(hk));

        setTitle("Hotkey Configuration");
        setModal(true);
        setResizable(false);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadHotkeysFromIni();
        initComponents();
        updateButtonColors();

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
                        if (RetroLemminiHotkeys.UNDEFINED.equalsIgnoreCase(keyString)) {
                            hk.clearKey(); // sets keyCode = VK_UNDEFINED, modifier = null
                        } else {
                            String[] keyParts = keyString.split("\\+");
                            if (keyParts.length == 2) {
                                hk.setModifier(keyParts[0]);
                                hk.setKey(RetroLemminiHotkeys.getKeyCode(keyParts[1]), keyParts[1]);
                            } else {
                                hk.setModifier(null);
                                hk.setKey(RetroLemminiHotkeys.getKeyCode(keyString), keyString);
                            }
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
        final int maxRows = 18;
        final int columnPadding = 35;
        final int buttonWidth = 90;
        final int buttonHeight = 25;

        // Insets
        Insets buttonInsets       = new Insets(2, 8, 2, 6);   // button → label gap
        Insets labelInsets        = new Insets(2, 6, 2, 12);  // label → next column

        for (Hotkey hk : hotkeys) {

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

        // Bottom paneL (tip panel and buttons)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        tipLabel.setFont(tipLabel.getFont().deriveFont(Font.PLAIN, 11f));
        tipLabel.setForeground(Color.GRAY);
        tipPanel.add(tipLabel);
        bottomPanel.add(tipPanel);
        
        JPanel buttonRow = new JPanel(new BorderLayout());
        JButton btnClearAll = new JButton("Clear All");
        JButton btnLoadClassic = new JButton("Load Classic Keys");
        JButton btnLoadDefaults = new JButton("Load RetroLemmini Keys");
        JButton btnReload = new JButton("Reload");
        JButton btnSave = new JButton("Save Current Configuration");
        JButton btnCancel = new JButton("Cancel");
        btnClearAll.addActionListener(e -> { clearAllHotkeys(); updateButtonColors(); });
        btnLoadClassic.addActionListener(e -> { loadHotkeys(RetroLemminiHotkeys.HotkeyProfile.CLASSIC); updateButtonColors(); });
        btnLoadDefaults.addActionListener(e -> { loadHotkeys(RetroLemminiHotkeys.HotkeyProfile.DEFAULT); updateButtonColors(); });
        btnReload.addActionListener(e -> { loadHotkeysFromIni(); initComponents(); updateButtonColors(); });
        btnSave.addActionListener(e -> saveHotkeys());
        btnCancel.addActionListener(e -> dispose());
        setButtonTip(btnClearAll, "Clear all keys to build a hotkey config from scratch");
        setButtonTip(btnLoadClassic, "Load the hotkeys from previous Lemmini versions");
        setButtonTip(btnLoadDefaults, "Load the RetroLemmini default hotkeys");
        setButtonTip(btnReload, "Reload your previously saved hotkeys");
        setButtonTip(btnSave, "Save your chosen keys and close the dialog");
        setButtonTip(btnCancel, "Close without saving");
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        leftButtons.add(btnClearAll);
        leftButtons.add(btnLoadClassic);
        leftButtons.add(btnLoadDefaults);
        buttonRow.add(leftButtons, BorderLayout.WEST);
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightButtons.add(btnReload);
        rightButtons.add(btnSave);
        rightButtons.add(btnCancel);
        buttonRow.add(rightButtons, BorderLayout.EAST);        
        bottomPanel.add(buttonRow);

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
    
    private void setButtonTip(JButton button, String tip) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
            	tipLabel.setForeground(Color.BLUE);
                tipLabel.setText(tip);
            }
            @Override
            public void mouseExited(MouseEvent e) {
            	tipLabel.setForeground(Color.GRAY);
                tipLabel.setText(defaultTip);
            }
        });
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
                        otherBtn.setText(RetroLemminiHotkeys.UNDEFINED);
                    }
                }
            }

            // Set the new key
            String keyName = RetroLemminiHotkeys.getKeyName(code);
            hotkey.setKey(code, keyName);
            hotkey.setModifier(pendingModifier);
            btn.setText(hotkey.getKeyDescription());
            updateButtonColors();
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
        return currentKeys.getOrDefault(action, RetroLemminiHotkeys.UNDEFINED);
    }

    private void restoreOtherListeningButtons(JButton currentBtn) {
        for (Map.Entry<RetroLemminiHotkeys.HotkeyAction, JButton> entry : actionButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (btn != currentBtn && "Press key...".equals(btn.getText())) {
                btn.setText(getKeyForAction(entry.getKey()));
            }
        }
    }
    
    private void updateButtonColors() {
    	for (JButton btn : actionButtons.values()) {
    		if ((btn.getText().startsWith(RetroLemminiHotkeys.UNDEFINED)) || (btn.getText().startsWith(RetroLemminiHotkeys.UNKNOWN)))
    			btn.setForeground(TRANSPARENT);
    		else
    			btn.setForeground(Color.BLACK);
    	}
    }
    
    /** Clear all hotkeys */
    private void clearAllHotkeys() {
        currentKeys.clear();
        for (Hotkey hk : hotkeys) {
            hk.clearKey();
            JButton btn = actionButtons.get(hk.getAction());
            if (btn != null)
                btn.setText(RetroLemminiHotkeys.UNDEFINED);
        }
    }
    
    /** Load the requested hotkey profile */
    private void loadHotkeys(RetroLemminiHotkeys.HotkeyProfile profile) {
        List<Hotkey> profileHotkeys = RetroLemminiHotkeys.getHotkeys(profile);

        currentKeys.clear();

        for (Hotkey source : profileHotkeys) {
            Hotkey hk = getHotkey(source.getAction());
            if (hk != null) {
                hk.setKey(source.getKeyCode(), RetroLemminiHotkeys.getKeyName(source.getKeyCode()));
                hk.setModifier(source.getModifier());

                String keyText = hk.getKeyDescription();
                currentKeys.put(hk.getAction(), keyText);

                JButton btn = actionButtons.get(hk.getAction());
                if (btn != null) {
                    btn.setText(keyText);
                }
            }
        }
    }


    /** Save hotkeys to INI */
    private void saveHotkeys() {
        try (BufferedWriter writer = Files.newBufferedWriter(hotkeysIniPath)) {
            for (Hotkey hk : hotkeys) {
                String keyString = hk.getKeyCode() == KeyEvent.VK_UNDEFINED
                                   ? RetroLemminiHotkeys.UNDEFINED
                                   : hk.getKeyDescription();
                writer.write(hk.getAction().name() + "=" + keyString);
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