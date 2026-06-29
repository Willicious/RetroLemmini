/*
 * Copyright 2026 Will James
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lemmini.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import lemmini.LemminiFrame;

/**
 * Dialog for managing replays.
 * @author Will James
 */
public class ReplayDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    public enum DialogResult {
        LOAD_REPLAY,
        BATCH_REPLAY_CHECK,
        CANCEL
    }

    private DialogResult dialogResult = DialogResult.CANCEL;

    public DialogResult getResult() {
        return dialogResult;
    }

    /**
     * Creates new form ReplayDialog
     */
    public ReplayDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
	    jButtonLoadReplay = new JButton();
	    jButtonBatchReplayCheck = new JButton();
	    jButtonCancel = new JButton();

	    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	    setTitle("Manage Replays");
	    setIconImage(Toolkit.getDefaultToolkit().getImage(
	        LemminiFrame.class.getClassLoader().getResource("icon_256.png")
	    ));

	    jButtonLoadReplay.setText("Load Replay");
	    jButtonBatchReplayCheck.setText("Batch Replay Check");
	    jButtonCancel.setText("Cancel");

	    jButtonLoadReplay.addActionListener(evt -> jButtonLoadReplayActionPerformed(evt));
	    jButtonBatchReplayCheck.addActionListener(evt -> jButtonBatchReplayCheckActionPerformed(evt));
	    jButtonCancel.addActionListener(evt -> jButtonCancelActionPerformed(evt));

	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	    Dimension btnSize = new Dimension(240, 40);

	    for (JButton b : new JButton[]{jButtonLoadReplay, jButtonBatchReplayCheck, jButtonCancel}) {
	        b.setAlignmentX(Component.CENTER_ALIGNMENT);
	        b.setMaximumSize(btnSize);
	        b.setPreferredSize(btnSize);
	    }
	    
	    panel.add(jButtonLoadReplay);
	    panel.add(Box.createVerticalStrut(12));

	    panel.add(jButtonBatchReplayCheck);
	    panel.add(Box.createVerticalStrut(36));

	    panel.add(jButtonCancel);

	    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
	    setContentPane(panel);
	}// </editor-fold>//GEN-END:initComponents
    
    private void jButtonLoadReplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadReplayActionPerformed
    	dialogResult = DialogResult.LOAD_REPLAY;
        dispose();
    }//GEN-LAST:event_jButtonLoadReplayActionPerformed
    
    private void jButtonBatchReplayCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadReplayActionPerformed
    	dialogResult = DialogResult.BATCH_REPLAY_CHECK;
        dispose();
    }//GEN-LAST:event_jButtonLoadReplayActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
    	dialogResult = DialogResult.CANCEL;
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonLoadReplay;
    private javax.swing.JButton jButtonBatchReplayCheck;
    // End of variables declaration//GEN-END:variables
}
