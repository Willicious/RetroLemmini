package lemmini.gui;

import javax.swing.*;

import lemmini.game.Core;
import lemmini.game.GameController;
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
            
            checkOff.setEnabled(true);
            final MouseAction thisAction = action;
            checkOff.addActionListener(e -> checkOffCheckChanged(thisAction, checkOff));
            
            if (!isAllowed(action, MouseEvent.BUTTON1)) checkLeft.setEnabled(false);
            if (!isAllowed(action, MouseEvent.BUTTON2)) checkMiddle.setEnabled(false);
            if (!isAllowed(action, MouseEvent.BUTTON3)) checkRight.setEnabled(false);

            row.add(checkOff);
            row.add(checkLeft);
            row.add(checkMiddle);
            row.add(checkRight);

            // Store checkboxes
            Map<Integer, JCheckBox> map = new HashMap<Integer, JCheckBox>();
            map.put(MouseEvent.NOBUTTON, checkOff);
            map.put(MouseEvent.BUTTON1, checkLeft);
            map.put(MouseEvent.BUTTON2, checkMiddle);
            map.put(MouseEvent.BUTTON3, checkRight);

            actionCheckBoxes.put(action, map);

            // Pre-select based on current mapping
            List<MouseAction> offActions = mouseInput.getActionsForButton(MouseEvent.NOBUTTON);
            List<MouseAction> leftActions = mouseInput.getActionsForButton(MouseEvent.BUTTON1);
            List<MouseAction> midActions = mouseInput.getActionsForButton(MouseEvent.BUTTON2);
            List<MouseAction> rightActions = mouseInput.getActionsForButton(MouseEvent.BUTTON3);
            
            if (offActions != null && offActions.contains(action)) {
            	checkOff.setSelected(true);
            	
            	checkLeft.setSelected(false);
            	checkMiddle.setSelected(false);
            	checkRight.setSelected(false);
            	
            	checkLeft.setEnabled(false);
            	checkMiddle.setEnabled(false);
            	checkRight.setEnabled(false);
            } else {
	            if (leftActions != null && leftActions.contains(action)) checkLeft.setSelected(true);
	            if (midActions != null && midActions.contains(action)) checkMiddle.setSelected(true);
	            if (rightActions != null && rightActions.contains(action)) checkRight.setSelected(true);
            }
            
            mainPanel.add(row);
        }
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Click air to cancel replay
        JPanel cancelReplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkClickAirToCancelReplay = new JCheckBox("Click Air to Cancel Replay");
        checkClickAirToCancelReplay.setSelected(GameController.isOptionEnabled(GameController.RetroLemminiOption.CLICK_AIR_TO_CANCEL_REPLAY));
        cancelReplayPanel.add(checkClickAirToCancelReplay);
        optionsPanel.add(cancelReplayPanel);

        // Scroll wheel to select skills
        JPanel skillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkWheelSkillSelect = new JCheckBox("Use Scroll Wheel to Select Skills");
        checkWheelSkillSelect.setSelected(GameController.isOptionEnabled(GameController.RetroLemminiOption.ENABLE_WHEEL_SKILL_SELECT));
        skillPanel.add(checkWheelSkillSelect);
        optionsPanel.add(skillPanel);

        // Scroll wheel to change debug brush size
        JPanel brushPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkWheelBrushSize = new JCheckBox("Use Scroll Wheel to Change Debug Draw Paintbrush Size");
        checkWheelBrushSize.setSelected(GameController.isOptionEnabled(GameController.RetroLemminiOption.ENABLE_WHEEL_BRUSH_SIZE));
        brushPanel.add(checkWheelBrushSize);
        optionsPanel.add(brushPanel);

        // Add to main panel
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(optionsPanel);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(this::saveConfig);

        // Wrap in a panel to center it
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        savePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        savePanel.add(saveBtn);

        add(savePanel, BorderLayout.SOUTH);

        JScrollPane pane = new JScrollPane(mainPanel);
        pane.setBorder(BorderFactory.createEmptyBorder());

        add(pane, BorderLayout.CENTER);
        saveBtn.requestFocusInWindow();
    }
    
    private void checkOffCheckChanged(MouseAction action, JCheckBox checkOff) {       
        Map<Integer, JCheckBox> map = actionCheckBoxes.get(action);
        if (map == null) return;

        JCheckBox left = map.get(MouseEvent.BUTTON1);
        JCheckBox middle = map.get(MouseEvent.BUTTON2);
        JCheckBox right = map.get(MouseEvent.BUTTON3);

        if (checkOff.isSelected()) {
            left.setSelected(false);
            middle.setSelected(false);
            right.setSelected(false);

            left.setEnabled(false);
            middle.setEnabled(false);
            right.setEnabled(false);
        } else {
            left.setEnabled(isAllowed(action, MouseEvent.BUTTON1));
            middle.setEnabled(isAllowed(action, MouseEvent.BUTTON2));
            right.setEnabled(isAllowed(action, MouseEvent.BUTTON3));
        }
    }

	private String describeAction(MouseAction action) {
        switch (action) {
            case TOGGLEPAUSE: return "Toggle Pause";
            case SELECTWALKER: return "Select Walker";
            case DRAGVIEWAREA: return "Drag View Area";
            case FASTSCROLL: return "Fast Scroll";
            default: return action.name();
        }
    }

    private boolean isAllowed(MouseAction action, int button) {
    	switch (action) {
    		case TOGGLEPAUSE:
    		case SELECTWALKER:
    			return button != MouseEvent.BUTTON1;
    		default:
    			return true;
    	}
    }

    private void saveConfig(ActionEvent e) {
        mouseInput.clearMappings();
        for (MouseAction action : actionCheckBoxes.keySet()) {
            Map<Integer, JCheckBox> checkMap = actionCheckBoxes.get(action);
            for (Integer button : checkMap.keySet()) {
                JCheckBox box = checkMap.get(button);
                if (box.isSelected()) {
                    mouseInput.addMapping(button, action);
                }
            }
        }
        boolean clickAirToCancelReplay = checkClickAirToCancelReplay.isSelected();
        boolean enableWheelSkillSelect = checkWheelSkillSelect.isSelected();
        boolean enableWheelBrushSize = checkWheelBrushSize.isSelected();
        
        mouseInput.saveToProperties(Core.getProgramPropsFilePath(), clickAirToCancelReplay, enableWheelSkillSelect, enableWheelBrushSize);
        JOptionPane.showMessageDialog(this, "Mouse configuration saved!");
        dispose();
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