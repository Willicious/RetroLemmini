package lemmini.gui;

import javax.swing.*;

import lemmini.game.Core;
import lemmini.gameutil.MouseInput;
import lemmini.gameutil.MouseInput.MouseAction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.*;

public class MouseConfig extends JDialog {

    private static final long serialVersionUID = 1L;

    private final MouseInput mouseInput;

    // action -> (button -> checkbox)
    private final Map<MouseAction, Map<Integer, JCheckBox>> actionCheckBoxes =
            new EnumMap<MouseAction, Map<Integer, JCheckBox>>(MouseAction.class);
    
    JCheckBox checkWheelSkillSelect;
    JCheckBox checkWheelBrushSize;
    JCheckBox checkClickAirToCancelReplay;

    public MouseConfig(MouseInput mouseInput) {

        this.mouseInput = mouseInput;

        setTitle("Mouse Configuration");
        setModal(true);
        setResizable(false);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        initComponents();
        pack();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        for (MouseAction action : MouseAction.values()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel(describeAction(action));
            
            label.setPreferredSize(new Dimension(150, 25));
            row.add(label);

            final JCheckBox checkOff = new JCheckBox("Off");
            final JCheckBox checkLeft = new JCheckBox("Left");
            final JCheckBox checkMiddle = new JCheckBox("Middle");
            final JCheckBox checkRight = new JCheckBox("Right");
            final JCheckBox checkBackward = new JCheckBox("X1");
            final JCheckBox checkForward = new JCheckBox("X2");
            
            checkOff.setEnabled(true);
            final MouseAction thisAction = action;
            checkOff.addActionListener(e -> checkOffCheckChanged(thisAction, checkOff));
            
            if (!isAllowed(action, MouseEvent.BUTTON1)) checkLeft.setEnabled(false);
            if (!isAllowed(action, MouseEvent.BUTTON2)) checkMiddle.setEnabled(false);
            if (!isAllowed(action, MouseEvent.BUTTON3)) checkRight.setEnabled(false);
            if (!isAllowed(action, 4)) checkBackward.setEnabled(false);
            if (!isAllowed(action, 5)) checkForward.setEnabled(false);

            row.add(checkOff);
            row.add(checkLeft);
            row.add(checkMiddle);
            row.add(checkRight);
            row.add(checkBackward);
            row.add(checkForward);

            // Store checkboxes
            Map<Integer, JCheckBox> map = new HashMap<Integer, JCheckBox>();
            map.put(MouseEvent.NOBUTTON, checkOff);
            map.put(MouseEvent.BUTTON1, checkLeft);
            map.put(MouseEvent.BUTTON2, checkMiddle);
            map.put(MouseEvent.BUTTON3, checkRight);
            map.put(4, checkBackward);
            map.put(5, checkForward);

            actionCheckBoxes.put(action, map);
            
            mainPanel.add(row);
        }
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Click air to cancel replay
        JPanel cancelReplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkClickAirToCancelReplay = new JCheckBox("Click Air to Cancel Replay");
        cancelReplayPanel.add(checkClickAirToCancelReplay);
        optionsPanel.add(cancelReplayPanel);

        // Scroll wheel to select skills
        JPanel skillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkWheelSkillSelect = new JCheckBox("Use Scroll Wheel to Select Skills");
        skillPanel.add(checkWheelSkillSelect);
        optionsPanel.add(skillPanel);

        // Scroll wheel to change debug brush size
        JPanel brushPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkWheelBrushSize = new JCheckBox("Use Scroll Wheel to Change Debug Draw Paintbrush Size");
        brushPanel.add(checkWheelBrushSize);
        optionsPanel.add(brushPanel);

        // Add to main panel
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(optionsPanel);

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(this::reloadDefaults);
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(this::saveConfig);

        // Wrap in a panel to center it
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        buttonPanel.add(btnReset);
        buttonPanel.add(btnSave);

        add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane pane = new JScrollPane(mainPanel);
        pane.setBorder(BorderFactory.createEmptyBorder());

        add(pane, BorderLayout.CENTER);
        
        loadDefaults();
        loadUserConfig();
        
        btnSave.requestFocusInWindow();
    }
    
    private void applyMouseInputToUI(MouseInput source) {
        for (MouseAction action : MouseAction.values()) {
            Map<Integer, JCheckBox> map = actionCheckBoxes.get(action);
            if (map == null) continue;

            for (Map.Entry<Integer, JCheckBox> e : map.entrySet()) {
                int button = e.getKey();
                JCheckBox box = e.getValue();
                box.setSelected(source.getActionsForButton(button).contains(action));
                box.setEnabled(isAllowed(action, button));
            }
        }

        checkClickAirToCancelReplay.setSelected(source.clickAirToCancelReplay);
        checkWheelSkillSelect.setSelected(source.enableWheelSkillSelect);
        checkWheelBrushSize.setSelected(source.enableWheelBrushSize);
    }
    
    private void loadUserConfig() {
        mouseInput.loadFromProperties(Core.getProgramPropsFilePath());
        applyMouseInputToUI(mouseInput);
    }
    
    private void loadDefaults() {
        MouseInput defaults = new MouseInput();
        applyMouseInputToUI(defaults);
    }
    
    private Map<Integer, JCheckBox> getCheckMap(MouseAction action) {
        return actionCheckBoxes.getOrDefault(action, Collections.emptyMap());
    }
    
    private void updateOffSelection(MouseAction action, boolean offSelected) {
        Map<Integer, JCheckBox> map = getCheckMap(action);
        for (Map.Entry<Integer, JCheckBox> e : map.entrySet()) {
            int button = e.getKey();
            JCheckBox box = e.getValue();
            if (button == MouseEvent.NOBUTTON) continue;
            box.setEnabled(!offSelected && isAllowed(action, button));
            if (offSelected) box.setSelected(false);
        }
    }
    
    private void checkOffCheckChanged(MouseAction action, JCheckBox checkOff) {
        updateOffSelection(action, checkOff.isSelected());
    }

	private String describeAction(MouseAction action) {
        switch (action) {
            case TOGGLEPAUSE: return "Toggle Pause";
            case SELECTWALKER: return "Select Walker Only";
            case DRAGVIEWAREA: return "Drag-to-Scroll View Area";
            case FASTSCROLL: return "Fast Scroll";
            case RELEASERATEDOWN: return "Release Rate (-)";
            case RELEASERATEUP: return "Release Rate (+)";
            default: return action.name();
        }
    }

    private boolean isAllowed(MouseAction action, int button) {
    	switch (action) {
    		case TOGGLEPAUSE:
    		case SELECTWALKER:
    		case RELEASERATEDOWN:
    		case RELEASERATEUP:
    			return button != MouseEvent.BUTTON1;
    		default:
    			return true;
    	}
    }
    
    private void copyUIToMouseInput(MouseInput target) {
        target.clearMappings();
        for (MouseAction action : MouseAction.values()) {
            Map<Integer, JCheckBox> map = getCheckMap(action);
            for (Map.Entry<Integer, JCheckBox> e : map.entrySet()) {
                if (e.getValue().isSelected()) {
                    target.addMapping(e.getKey(), action);
                }
            }
        }
        target.clickAirToCancelReplay = checkClickAirToCancelReplay.isSelected();
        target.enableWheelSkillSelect = checkWheelSkillSelect.isSelected();
        target.enableWheelBrushSize = checkWheelBrushSize.isSelected();
    }

    private void saveConfig(ActionEvent e) {
        copyUIToMouseInput(mouseInput);
        mouseInput.saveToProperties(Core.getProgramPropsFilePath());
        JOptionPane.showMessageDialog(this, "Mouse configuration saved!");
        dispose();
    }
    
    private void reloadDefaults(ActionEvent e) {
    	loadDefaults();
    }

    public static void main(String[] args) {
        MouseInput mi = new MouseInput();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MouseConfig(mi).setVisible(true);
            }
        });
    }
}